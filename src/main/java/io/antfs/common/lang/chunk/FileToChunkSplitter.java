package io.antfs.common.lang.chunk;

import io.antfs.common.lang.object.DistributedAntMetaObject;
import io.antfs.protocol.Packet;
import io.antfs.protocol.QueenPacketSender;
import io.antfs.protocol.carriers.Carrier;
import io.antfs.protocol.carriers.ChunkStoreCarrier;
import io.antfs.protocol.carriers.ChunkStoreReplyCarrier;
import io.antfs.protocol.reverter.CarrierRevertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public class FileToChunkSplitter implements Callable<DistributedAntMetaObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileToChunkSplitter.class);

    private ChannelHandlerContext ctx;
    private QueenPacketSender sender;
    private long fileLength;
    private String fid;
    private int bufferSize;
    private RandomAccessFile randomAccessFile;
    private ExecutorService executorService;
    private Set<ReadPointer> readPointers;
    private CountDownLatch countDownLatch;
    private DistributedAntMetaObject distributedAntMetaObject;

    public FileToChunkSplitter(ChannelHandlerContext ctx, File file, String fid, int bufferSize){
        this.ctx = ctx;
        this.sender = new QueenPacketSender(ctx.channel());
        this.fileLength = file.length();
        this.fid = fid;
        this.bufferSize = bufferSize;
        int threadSize = (int)Math.ceil((double)this.fileLength/this.bufferSize);
        try {
            this.randomAccessFile = new RandomAccessFile(file,"r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.executorService = new ThreadPoolExecutor(threadSize,100,0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1024),
                new DefaultThreadFactory("chunk-pool-thread"),
                new ThreadPoolExecutor.AbortPolicy());
        this.readPointers = new HashSet<>();
        this.countDownLatch = new CountDownLatch(threadSize);
        this.distributedAntMetaObject = new DistributedAntMetaObject(fid,file.getName());
    }

    @Override
    public DistributedAntMetaObject call() {
        // prepare the read pointer
        calculateReadPointer(0, this.bufferSize);
        LOGGER.info("readPointers={}",this.readPointers);

        final long startTime = System.currentTimeMillis();
        for(ReadPointer readPointer: this.readPointers){
            this.executorService.execute(new Splitter(readPointer));
        }
        try {
            // wait for all Splitter finish their job
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // all Splitter has finished
        LOGGER.info("total cost time=({}ms)",(System.currentTimeMillis()-startTime));
        shutdown();
        LOGGER.info("distributedAntMetaObject={}",distributedAntMetaObject);
        return this.distributedAntMetaObject;
    }


    /**
     * calculate the readPointer
     * @param start the start index
     * @param size the size to read
     */
    private void calculateReadPointer(long start, long size){
        if(start > this.fileLength-1){
            return;
        }
        ReadPointer pointer = new ReadPointer();
        pointer.start = start;
        long end = start+size-1;
        if(end >= this.fileLength-1){
            pointer.end = this.fileLength-1;
            this.readPointers.add(pointer);
            return;
        }
        pointer.end = end;
        this.readPointers.add(pointer);
        calculateReadPointer(end+1, size);
    }

    /**
     * shutdown
     */
    private void shutdown(){
        LOGGER.info("all tasks have finished,will shutdown");
        try {
            this.randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.executorService.shutdown();
    }

    /* ============== the ReadPointer =============== */

    /**
     * the read pointer
     */
    private class ReadPointer {

        private long start;
        private long end;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (end ^ (end >>> 32));
            result = prime * result + (int) (start ^ (start >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            }
            if (obj == null){
                return false;
            }
            if (getClass() != obj.getClass()){
                return false;
            }
            ReadPointer that = (ReadPointer) obj;
            return this.start==that.start && this.end==that.end;
        }

        @Override
        public String toString() {
            return "{start:"+this.start+",end:"+this.end+"}";
        }
    }


    private class Splitter implements Runnable{

        private ReadPointer readPointer;
        private byte[] readBuff;

        /**
         * Splitter
         * @param readPointer the ReadPointer
         */
        Splitter(ReadPointer readPointer) {
            this.readPointer = readPointer;
            this.readBuff = new byte[bufferSize];
        }

        @Override
        public void run() {
            byte[] content;
            try {
                long start = this.readPointer.start;
                long end = this.readPointer.end;
                long size = end-start+1;
                MappedByteBuffer mapBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY,start,size);
                mapBuffer.get(this.readBuff, 0, (int)size);
                if(size==bufferSize){
                    content = this.readBuff;
                }else{
                    content = new byte[(int)size];
                    System.arraycopy(this.readBuff, 0, content, 0, (int)size);
                }
                ChunkStoreCarrier chunkStoreCarrier = new ChunkStoreCarrier(fid,start,end,bufferSize,content);
                LOGGER.info("file read finished with start={},size={}",start,size);
                Packet packet = sender.sendPacket(chunkStoreCarrier.getPacket()).get();
                Carrier carrier = CarrierRevertUtil.revert(packet);
                if(carrier instanceof ChunkStoreReplyCarrier){
                    ChunkStoreReplyCarrier replyCarrier = (ChunkStoreReplyCarrier)carrier;
                    LOGGER.info("get ChunkStoreReplyCarrier={},will add it to distributedAntMetaObject",replyCarrier);
                    distributedAntMetaObject.addDistributedOid(replyCarrier.getOid(),replyCarrier.getHost(),replyCarrier.getPort());
                }

            }catch (Exception e) {
                LOGGER.error("Splitter error,cause:",e);
            }finally {
                countDownLatch.countDown();
            }
        }
    }

}

package com.antfs.core.file;

import com.antfs.core.object.AntMetaObject;
import com.antfs.core.object.AntObject;
import com.antfs.core.object.ObjectHandler;
import com.antfs.core.util.LogUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FileStorer
 * @author gris.wang
 * @since 2017/12/27
 **/
public class FileStorer {

	private long fileLength;

	private String fid;

	private FileStorerListener listener;

	private ObjectHandler objectHandler;

	private int bufferSize;

	private int threadSize;

	private RandomAccessFile randomAccessFile;

	private ExecutorService  executorService;

	private Set<ReadPointer> readPointers;

	private CyclicBarrier cyclicBarrier;

	private AtomicLong counter = new AtomicLong(0);

	private AntMetaObject antMetaObject;
	
	private FileStorer(File file, String fid, ObjectHandler objectHandler, int bufferSize){
		this.fileLength = file.length();
		this.fid = fid;
		this.objectHandler = objectHandler;
		this.bufferSize = bufferSize;
		this.threadSize = (int)Math.ceil((double)this.fileLength/this.bufferSize);
		try {
			this.randomAccessFile = new RandomAccessFile(file,"r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ThreadFactory threadFactory = new DefaultThreadFactory("store-pool-thread");
		this.executorService = new ThreadPoolExecutor(this.threadSize,100,0L,TimeUnit.MILLISECONDS,
										new LinkedBlockingDeque<>(1024),
										threadFactory,
										new ThreadPoolExecutor.AbortPolicy());
		this.readPointers = new HashSet<ReadPointer>();
		this.antMetaObject = new AntMetaObject(this.fid,file.getName());
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
		int part = (int)(start/size)+1;
		// generate the oid of antObject
		pointer.oid = this.objectHandler.oid(this.fid,part);
		this.antMetaObject.addOid(pointer.oid);
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
	 * listener antMetaObject
	 * @param antMetaObject the antMetaObject to be handled
	 */
	private void handleMeta(AntMetaObject antMetaObject){
		if(this.listener!=null) {
			LogUtil.info("handle antMetaObject={}",antMetaObject);
			this.listener.onMetaObjectReady(antMetaObject);
		}else{
			LogUtil.error("FileStorerListener is null");
		}
	}

	/**
	 * listener antObject
	 * @param antObject the antObject to be handled
	 */
	private void handleObject(AntObject antObject){
		if(this.listener!=null) {
			LogUtil.info("handle antObject={}", antObject);
			this.listener.onAntObjectReady(antObject);
		}else{
			LogUtil.error("FileStorerListener is null");
		}
		counter.incrementAndGet();
	}

	/**
	 * addListener
	 * @param listener the listener
	 */
	public void addListener(FileStorerListener listener){
		this.listener = listener;
	}

	/**
	 * start the service
	 */
	public void start(){
		// prepare the read pointer
		calculateReadPointer(0, this.bufferSize);
		LogUtil.info("readPointers={}",this.readPointers);
		// listener AntMetaObject
		handleMeta(this.antMetaObject);

		final long startTime = System.currentTimeMillis();
		cyclicBarrier = new CyclicBarrier(this.threadSize,new Runnable() {
			@Override
			public void run() {
				// all FileReader has finished
				LogUtil.info("split file into ({}) antObjects",counter.get());
				LogUtil.info("total cost time=({}ms)",(System.currentTimeMillis()-startTime));
				shutdown();
			}
		});
		for(ReadPointer readPointer: this.readPointers){
			this.executorService.execute(new FileReader(readPointer));
		}
	}

	/**
	 * shutdown
	 */
	private void shutdown(){
		LogUtil.info("all readPointers have finished,will shutdown");
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

		private String oid;

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
			return this.start==that.start && this.end==that.end && this.oid.equals(that.oid);
		}

		@Override
		public String toString() {
			return "{start:"+this.start+",end:"+this.end+"}";
		}
	}

	/* ============== the FileReader =============== */

	private class FileReader implements Runnable{

		private ReadPointer readPointer;

		private byte[] readBuff;

		/**
		 * FileReader
		 * @param readPointer the ReadPointer
		 */
		FileReader(ReadPointer readPointer) {
			this.readPointer = readPointer;
			this.readBuff = new byte[bufferSize];
		}

		@Override
		public void run() {
			ByteArrayOutputStream bos = null;
			try {
				long start = this.readPointer.start;
				long end = this.readPointer.end;
				long size = end-start+1;
				MappedByteBuffer mapBuffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY,start,size);
				bos = new ByteArrayOutputStream();
				mapBuffer.get(this.readBuff, 0, (int)size);
				for(int i=0;i<size;i++){
					byte b = this.readBuff[i];
					bos.write(b);
				}
				LogUtil.info("file read finished with start={},size={},oid={}",start,size,this.readPointer.oid);
				handleObject(new AntObject(fid,this.readPointer.oid,start,end,bos.toByteArray()));
				cyclicBarrier.await();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(bos!=null){
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	/* ============== the FileStorer Builder =============== */

	public static class Builder{

		private File file;

		private String fid;

		private ObjectHandler objectHandler;

		private int bufferSize = 0x400000;

		public Builder(File file,String fid,ObjectHandler objectHandler){
			this.file = file;
			if(!this.file.exists() || this.file.isDirectory()) {
				throw new IllegalArgumentException("file does not exist or is not a file!");
			}
			this.fid = fid;
			this.objectHandler = objectHandler;
		}

		public Builder bufferSize(int bufferSize){
			this.bufferSize = bufferSize;
			return this;
		}

		public FileStorer build(){
			return new FileStorer(this.file,this.fid,this.objectHandler,this.bufferSize);
		}
	}
	
	
}
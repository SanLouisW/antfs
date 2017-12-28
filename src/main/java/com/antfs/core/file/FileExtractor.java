package com.antfs.core.file;

import com.antfs.core.object.AntMetaObject;
import com.antfs.core.object.AntObject;
import com.antfs.core.object.ObjectHandler;
import com.antfs.core.util.LogUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FileExtractor
 * @author gris.wang
 * @since 2017/12/27
 **/
public class FileExtractor {

	private long fileLength;

	private String fid;

	private FileExtractorHandle handle;

	private ObjectHandler objectHandler;

	private int bufferSize;

	private RandomAccessFile randomAccessFile;

	private ExecutorService  executorService;

	private Set<ReadPointer> readPointers;

	private CyclicBarrier cyclicBarrier;

	private AtomicLong counter = new AtomicLong(0);

	private AntMetaObject antMetaObject;
	
	private FileExtractor(File file, String fid, FileExtractorHandle handle, ObjectHandler objectHandler, int bufferSize){
		this.fileLength = file.length();
		this.fid = fid;
		this.handle = handle;
		this.objectHandler = objectHandler;
		this.bufferSize = bufferSize;
		int threadSize = (int)Math.ceil((double)this.fileLength/this.bufferSize);
		try {
			this.randomAccessFile = new RandomAccessFile(file,"r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ThreadFactory threadFactory = new DefaultThreadFactory("pool-thread-%d");
		this.executorService = new ThreadPoolExecutor(threadSize,100,0L,TimeUnit.MILLISECONDS,
										new LinkedBlockingDeque<>(1024),
										threadFactory,
										new ThreadPoolExecutor.AbortPolicy());
		this.readPointers = new HashSet<ReadPointer>();
		this.antMetaObject = new AntMetaObject(this.fid,file.getName());
	}

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
	 * start the service
	 */
	public void start(){
		// prepare the read pointer
		calculateReadPointer(0, this.bufferSize);
		LogUtil.info("readPointers=%s",readPointers);
		// handle AntMetaObject
		handleMeta(this.antMetaObject);

		final long startTime = System.currentTimeMillis();
		cyclicBarrier = new CyclicBarrier(readPointers.size(),new Runnable() {
			@Override
			public void run() {
				// all Reader has finished
				LogUtil.info("file split into antObjects number=(%d)",counter.get());
				LogUtil.info("total cost time=(%dms)",(System.currentTimeMillis()-startTime));
				shutdown();
			}
		});
		for(ReadPointer readPointer: readPointers){
			this.executorService.execute(new Reader(readPointer));
		}
	}

	/**
	 * shutdown
	 */
	public void shutdown(){
		LogUtil.info("all Reader has finished,will shutdown");
		try {
			this.randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.executorService.shutdown();
	}


	private void handleMeta(AntMetaObject antMetaObject){
		LogUtil.info("handle antMetaObject=%s",antMetaObject);
		this.handle.storeMeta(antMetaObject);
	}

	private void handleObject(AntObject antObject){
		LogUtil.info("handle antObject=%s",antObject);
		this.handle.store(antObject);
		counter.incrementAndGet();
	}


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

	private class Reader implements Runnable{

		private long start;

		private long size;

		private String oid;

		private byte[] readBuff;

		/**
		 * Reader
		 * @param readPointer the ReadPointer
		 */
		Reader(ReadPointer readPointer) {
			this.start = readPointer.start;
			this.size = readPointer.end-readPointer.start+1;
			this.oid = readPointer.oid;
			this.readBuff = new byte[bufferSize];
		}

		@Override
		public void run() {
			ByteArrayOutputStream bos = null;
			try {
				MappedByteBuffer mapBuffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY,this.start,this.size);
				bos = new ByteArrayOutputStream();
				mapBuffer.get(this.readBuff, 0, (int)this.size);
				for(int i=0;i<this.size;i++){
					byte b = this.readBuff[i];
					bos.write(b);
				}
				LogUtil.info("read finished with start=%d,size=%d,oid=%s",start,size,oid);
				handleObject(new AntObject(fid,this.oid,bos.toByteArray()));
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

		/*@Override
		public void run() {
			ByteArrayOutputStream bos = null;
			try {
				MappedByteBuffer mapBuffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY,start,this.size);
				bos = new ByteArrayOutputStream();
				for(int offset=0;offset<size;offset+=bufferSize){
					int readLength;
					if(offset+bufferSize<=size){
						readLength = bufferSize;
					}else{
						readLength = (int) (size-offset);
					}
					mapBuffer.get(readBuff, 0, readLength);
					for(int i=0;i<readLength;i++){
						byte tmp = readBuff[i];
						bos.write(tmp);
					}
					handle(bos.toByteArray());
					bos.reset();
				}
				if(bos.size()>0){
					handle(bos.toByteArray());
				}
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
		}*/


	}
	
	public static class Builder{

		private File file;

		private String fid;

		private FileExtractorHandle handle;

		private ObjectHandler objectHandler;

		private int bufferSize=4*1024*1024;

		public Builder(File file,String fid,FileExtractorHandle handle){
			this.file = file;
			if(!this.file.exists() || this.file.isDirectory()) {
				throw new IllegalArgumentException("file does not exist or is not a file!");
			}
			this.fid = fid;
			this.handle = handle;
		}

		public Builder objectHandler(ObjectHandler objectHandler){
			this.objectHandler = objectHandler;
			return this;
		}

		public Builder bufferSize(int bufferSize){
			this.bufferSize = bufferSize;
			return this;
		}

		public FileExtractor build(){
			return new FileExtractor(this.file,this.fid,this.handle,this.objectHandler,this.bufferSize);
		}
	}
	
	
}
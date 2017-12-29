package com.antfs.core.file;

import com.antfs.core.common.Constants;
import com.antfs.core.object.AntMetaObject;
import com.antfs.core.object.AntObject;
import com.antfs.core.object.ObjectReader;
import com.antfs.core.util.LogUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.*;

/**
 * FileRestorer
 * @author gris.wang
 * @since 2017/12/27
 **/
public class FileRestorer {

	private String fid;

	private ObjectReader objectReader;

	private CountDownLatch latch;

	private ExecutorService  executorService;

	private AntMetaObject antMetaObject;

	private FileRestorer(String fid, ObjectReader objectReader){
		this.fid = fid;
		this.objectReader = objectReader;
		this.antMetaObject = this.objectReader.readMeta(fid);
		if(this.antMetaObject==null){
			LogUtil.error("no antMetaObject found with fid=%s",fid);
			throw new IllegalArgumentException("fid is invalid");
		}
		int threadSize = this.antMetaObject.getOids().size();
		this.latch = new CountDownLatch(threadSize);
		ThreadFactory threadFactory = new DefaultThreadFactory("restore-pool-thread-%d");
		this.executorService = new ThreadPoolExecutor(threadSize,100,0L,TimeUnit.MILLISECONDS,
										new LinkedBlockingDeque<>(1024),
										threadFactory,
										new ThreadPoolExecutor.AbortPolicy());
	}



	/**
	 * start to restore the file
	 */
	public File restore(){
		File file;
		RandomAccessFile accessFile = null;
		try {
			File restoreDir = new File(Constants.FILE_RESTORE_PATH);
			if (!restoreDir.exists()) {
				restoreDir.mkdirs();
			}
			file = new File(Constants.FILE_RESTORE_PATH+File.separator+this.antMetaObject.getFileName());
			if(file.exists()){
				file.delete();
			}
			accessFile = new RandomAccessFile(file,"rw");

			long startTime = System.currentTimeMillis();
			for(String oid : antMetaObject.getOids()){
				this.executorService.execute(new AntObjectReader(accessFile,oid));
			}
			try {
				// wait for all AntObjectReader finish their job
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// all AntObjectReader has finished
			LogUtil.info("total cost time=(%dms)",(System.currentTimeMillis()-startTime));
			shutdown();
			return file;
		}catch(IOException e){
			LogUtil.error("restore error,cause:%s",e);
		}finally {
			if(accessFile!=null){
				try {
					accessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
     * shutdown
	 */
	private void shutdown(){
		LogUtil.info("all AntObjectReaders have finished,will shutdown");
		this.executorService.shutdown();
	}





	/* ============== the AntObjectReader =============== */

	private class AntObjectReader implements Runnable{

		private RandomAccessFile accessFile;

		private String oid;

		/**
		 * AntObjectReader
		 * @param accessFile the BufferedOutputStream
		 * @param oid the object id
		 */
		AntObjectReader(RandomAccessFile accessFile, String oid) {
			this.accessFile = accessFile;
			this.oid = oid;
		}

		@Override
		public void run() {
			try {
				AntObject antObject = objectReader.read(fid,this.oid);
				if(antObject!=null){
					long offset = antObject.getBitStart();
					accessFile.seek(offset);
					accessFile.write(antObject.getContent());
					LogUtil.info("antObject read finished with antObject=%s",antObject);
				}else{
					LogUtil.error("antObject read from disk is null,with fid=(%s),oid=(%s)",fid,this.oid);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				latch.countDown();
			}
		}
	}


	/* ============== the FileRestorer Builder =============== */

	public static class Builder{

		private String fid;

		private ObjectReader objectReader;

		public Builder(String fid,ObjectReader objectReader){
			this.fid = fid;
			this.objectReader = objectReader;
		}

		public FileRestorer build(){
			return new FileRestorer(this.fid,this.objectReader);
		}
	}
	
	
}
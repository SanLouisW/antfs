package io.antfs.lang.file;

import io.antfs.common.Constants;
import io.antfs.lang.object.AntMetaObject;
import io.antfs.lang.object.AntObject;
import io.antfs.lang.object.ObjectReader;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.*;

/**
 * FileRestorer
 * @author gris.wang
 * @since 2017/12/27
 **/
public class FileRestorer {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileRestorer.class);
	
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
			LOGGER.error("no antMetaObject found with fid={}",fid);
			throw new IllegalArgumentException("fid is invalid");
		}
		int threadSize = this.antMetaObject.getOids().size();
		this.latch = new CountDownLatch(threadSize);
		this.executorService = new ThreadPoolExecutor(threadSize,100,0L,TimeUnit.MILLISECONDS,
										new LinkedBlockingDeque<>(1024),
										new DefaultThreadFactory("restore-pool-thread"),
										new ThreadPoolExecutor.AbortPolicy());
	}



	/**
	 * start to restore the file
	 */
	public File restore(){
		File restoreDir = new File(Constants.FILE_RESTORE_PATH);
		if (!restoreDir.exists() && restoreDir.mkdirs()) {
			LOGGER.info("create restore directory=({})",restoreDir.getAbsolutePath());
		}
		File file = new File(Constants.FILE_RESTORE_PATH+File.separator+this.antMetaObject.getFileName());
		if(file.exists() && file.delete()){
			LOGGER.info("deleted exists file=({})",file.getAbsolutePath());
		}
		long startTime = System.currentTimeMillis();
		for(String oid : antMetaObject.getOids()){
			this.executorService.execute(new AntObjectReader(file,oid));
		}
		try {
			// wait for all AntObjectReader finish their job
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// all AntObjectReader has finished
		LOGGER.info("total cost time=({}ms)",(System.currentTimeMillis()-startTime));
		shutdown();
		return file;
	}

	/**
     * shutdown
	 */
	private void shutdown(){
		LOGGER.info("all AntObjectReaders have finished,will shutdown");
		this.executorService.shutdown();
	}





	/* ============== the AntObjectReader =============== */

	private class AntObjectReader implements Runnable{

		private RandomAccessFile randomAccessFile;

		private String oid;

		/**
		 * AntObjectReader
		 * @param file the file
		 * @param oid the object id
		 */
		AntObjectReader(File file, String oid) {
			try {
				this.randomAccessFile = new RandomAccessFile(file,"rw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			this.oid = oid;
		}

		@Override
		public void run() {
			try {
				AntObject antObject = objectReader.read(fid,this.oid);
				if(antObject!=null){
					long start = antObject.getByteStart();
					this.randomAccessFile.seek(start);
					this.randomAccessFile.write(antObject.getContent());
					LOGGER.info("antObject read finished with antObject={}",antObject);
				}else{
					LOGGER.error("antObject read from disk is null,with fid=({}),oid=({})",fid,this.oid);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(this.randomAccessFile!=null){
					try {
						this.randomAccessFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
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
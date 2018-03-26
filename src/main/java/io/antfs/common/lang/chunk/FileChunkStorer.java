package io.antfs.common.lang.chunk;

import io.antfs.common.lang.object.AntObject;
import io.antfs.common.lang.object.DefaultObjectWriter;
import io.antfs.common.lang.object.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Chunk Storer
 * @author gris.wang
 * @since 2017/12/27
 **/
public class FileChunkStorer {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileChunkStorer.class);

	private static ObjectWriter objectWriter;
	static{
		objectWriter = new DefaultObjectWriter();
	}

	private String fid;
	private long start;
	private long end;
	private long chunkSize;
	private byte[] content;


	private FileChunkStorer(String fid, long start, long end, long chunkSize, byte[] content){
		this.fid = fid;
		this.start = start;
		this.end = end;
		this.chunkSize = chunkSize;
		this.content = content;
	}


	public void store(){
		int part = (int)(start/chunkSize)+1;
		// generate the oid of antObject
		String oid = objectWriter.oid(fid,part);
		AntObject antObject = new AntObject(fid,oid,start,end,content);
		LOGGER.info("store antObject={}", antObject);
		objectWriter.write(antObject);
	}

}
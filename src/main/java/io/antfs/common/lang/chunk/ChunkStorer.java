package io.antfs.common.lang.chunk;

import io.antfs.common.lang.object.AntObject;
import io.antfs.common.lang.object.DefaultObjectWriter;
import io.antfs.common.lang.object.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chunk Storer
 * @author gris.wang
 * @since 2017/12/27
 **/
public class ChunkStorer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChunkStorer.class);

	private static ObjectWriter objectWriter;
	static{
		objectWriter = new DefaultObjectWriter();
	}

	private String fid;
	private String oid;
	private long start;
	private long end;
	private long chunkSize;
	private byte[] content;

	public ChunkStorer(){

	}

	public ChunkStorer(String fid, long start, long end, long chunkSize, byte[] content){
		this.fid = fid;
		this.start = start;
		this.end = end;
		this.chunkSize = chunkSize;
		this.content = content;
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public void store(){
		int part = (int)(start/chunkSize)+1;
		// generate the oid of antObject
		oid = objectWriter.oid(fid,part);
		AntObject antObject = new AntObject(fid,oid,start,end,content);
		LOGGER.info("store antObject={}", antObject);
		objectWriter.write(antObject);
	}

	public String getOid(){
		return oid;
	}

}
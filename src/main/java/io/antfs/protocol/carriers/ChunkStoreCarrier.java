package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.Serializable;

/**
 * chunk store carrier
 * from queen to worker
 * @author gris.wang
 * @since 2018/3/28
 **/
public class ChunkStoreCarrier extends AbstractCarrier implements Serializable {

    private String fid;
    private long start;
    private long end;
    private long chunkSize;
    private byte[] content;

    public ChunkStoreCarrier(String fid, long start, long end, long chunkSize, byte[] content){
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

    @Override
    public PacketType getPacketType() {
        return PacketType.CHUNK_STORE;
    }

    @Override
    public byte[] getBody() {
        return ObjectUtil.serialize(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
            .append("fid:").append(fid)
            .append(",start:").append(start)
            .append(",end:").append(end)
            .append(",chunkSize:").append(chunkSize)
            .append("}");
        return sb.toString();
    }
}

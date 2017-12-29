package com.antfs.core.object;

import java.io.Serializable;

/**
 * A file will be split to one antMetaObject and several antObjects
 * the antMetaObject store the sorted oids which reference the antObject
 * each antObject store a part of the file's content
 * except the last antObject each antObject has the same content length
 * @author gris.wang
 * @since 2017/12/25
 **/
public class AntObject implements Serializable{

    /**
     * file id
     */
    private String fid;

    /**
     * object id
     */
    private String oid;

    /**
     * start bit of file
     */
    private long bitStart;

    /**
     * end bit of file
     */
    private long bitEnd;

    /**
     * the file bytes stored by AntObject
     */
    private byte[] content;

    public AntObject(String fid,String oid,long bitStart,long bitEnd,byte[] content){
        this.fid = fid;
        this.oid = oid;
        this.bitStart = bitStart;
        this.bitEnd = bitEnd;
        this.content = content;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public long getBitStart() {
        return bitStart;
    }

    public void setBitStart(long bitStart) {
        this.bitStart = bitStart;
    }

    public long getBitEnd() {
        return bitEnd;
    }

    public void setBitEnd(long bitEnd) {
        this.bitEnd = bitEnd;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{fid:").append(fid)
                                  .append(",oid:").append(oid)
                                  .append(",start:").append(bitStart)
                                  .append(",end:").append(bitEnd).append("}").toString();
    }
}

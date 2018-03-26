package io.antfs.common.lang.object;

import java.io.Serializable;

/**
 * A file will be split to one antMetaObject and several antObjects
 * the antMetaObject start the sorted oids which reference the antObject
 * each antObject start a part of the file's content
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
     * start byte of file
     */
    private long byteStart;

    /**
     * end byte of file
     */
    private long byteEnd;

    /**
     * the file bytes stored by AntObject
     */
    private byte[] content;

    public AntObject(String fid, String oid, long byteStart, long byteEnd, byte[] content){
        this.fid = fid;
        this.oid = oid;
        this.byteStart = byteStart;
        this.byteEnd = byteEnd;
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

    public long getByteStart() {
        return byteStart;
    }

    public void setByteStart(long byteStart) {
        this.byteStart = byteStart;
    }

    public long getByteEnd() {
        return byteEnd;
    }

    public void setByteEnd(long byteEnd) {
        this.byteEnd = byteEnd;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{")
                                  .append("fid:").append(fid)
                                  .append(",oid:").append(oid)
                                  .append(",start:").append(byteStart)
                                  .append(",end:").append(byteEnd)
                                  .append("}").toString();
    }
}

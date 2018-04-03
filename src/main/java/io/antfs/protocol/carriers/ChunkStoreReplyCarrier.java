package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.Serializable;

/**
 * chunk store reply carrier
 * from worker to queen
 * @author gris.wang
 * @since 2018/3/28
 **/
public class ChunkStoreReplyCarrier extends AbstractCarrier implements Serializable {

    private String fid;
    private String oid;
    private String host;
    private int port;

    public ChunkStoreReplyCarrier(){

    }

    public ChunkStoreReplyCarrier(String fid,String oid,String host,int port){
        this.fid = fid;
        this.oid = oid;
        this.host = host;
        this.port = port;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.CHUNK_STORE_REPLY;
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
            .append(",oid:").append(oid)
            .append(",host:").append(host)
            .append(",port:").append(port)
            .append("}");
        return sb.toString();
    }
}

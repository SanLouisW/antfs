package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.Serializable;

/**
 * chunk replica reply carrier
 * from worker to worker
 * @author gris.wang
 * @since 2018/3/28
 **/
public class ChunkReplicaReplyCarrier extends AbstractCarrier implements Serializable {

    private String fid;
    private String oid;
    private String host;
    private int port;

    public ChunkReplicaReplyCarrier(String fid){
        this.fid = fid;
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

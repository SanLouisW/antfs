package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.Serializable;

/**
 * file restore carrier
 * from client to queen
 * @author gris.wang
 * @since 2018/3/28
 **/
public class FileReStoreCarrier extends AbstractCarrier implements Serializable {

    private String fid;

    public FileReStoreCarrier(String fid){
        this.fid = fid;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_RESTORE;
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
            .append("}");
        return sb.toString();
    }

}

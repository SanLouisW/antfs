package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.Serializable;

/**
 * file store carrier
 * from client to queen
 * @author gris.wang
 * @since 2018/3/28
 **/
public class FileStoreCarrier extends AbstractCarrier implements Serializable {

    private String filePath;

    public FileStoreCarrier(String filePath){
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_STORE;
    }

    @Override
    public byte[] getBody() {
        return ObjectUtil.serialize(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
            .append("filePath:").append(filePath)
            .append("}");
        return sb.toString();
    }

}

package io.antfs.protocol.carriers;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;

import java.io.File;
import java.io.Serializable;

/**
 * file restore reply carrier
 * from queen to client
 * @author gris.wang
 * @since 2018/3/28
 **/
public class FileReStoreReplyCarrier extends AbstractCarrier implements Serializable {

    private File file;

    public FileReStoreReplyCarrier(File file){
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.FILE_RESTORE_REPLY;
    }

    @Override
    public byte[] getBody() {
        return ObjectUtil.serialize(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
            .append("file Path:").append(file.getAbsolutePath())
            .append("}");
        return sb.toString();
    }
}

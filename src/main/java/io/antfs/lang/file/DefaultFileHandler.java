package io.antfs.lang.file;

import io.antfs.common.Constants;
import io.antfs.lang.object.AntMetaObject;
import io.antfs.lang.object.AntObject;
import io.antfs.lang.object.ObjectReader;
import io.antfs.lang.object.ObjectWriter;
import io.antfs.common.util.FileUtil;
import io.netty.util.internal.StringUtil;

import java.io.File;

/**
 * DefaultFileHandler
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultFileHandler implements FileHandler {

    private final ObjectWriter objectWriter;

    private final ObjectReader objectReader;

    public DefaultFileHandler(ObjectWriter objectWriter,ObjectReader objectReader){
        this.objectWriter = objectWriter;
        this.objectReader = objectReader;
    }

    @Override
    public String fid(File file) {
        return FileUtil.getCRC32(file);
    }

    @Override
    public String store(File file) {
        String fid = fid(file);
        if(StringUtil.isNullOrEmpty(fid)){
            return null;
        }
        FileStorer.Builder builder = new FileStorer.Builder(file,fid,this.objectWriter);
        builder.bufferSize(Constants.ANT_OBJECT_BUFFER_SIZE);

        FileStorer fileStorer = builder.build();
        fileStorer.addListener(new FileStorerListener() {
            @Override
            public void onMetaObjectReady(AntMetaObject antMetaObject) {
                objectWriter.writeMeta(antMetaObject);
            }

            @Override
            public void onAntObjectReady(AntObject antObject) {
                objectWriter.write(antObject);
            }
        });

        fileStorer.start();

        return fid;
    }

    @Override
    public File restore(String fid) {
        FileRestorer.Builder builder = new FileRestorer.Builder(fid,this.objectReader);
        FileRestorer fileRestorer = builder.build();
        return fileRestorer.restore();
    }

}

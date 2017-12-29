package com.antfs.core.file;

import com.antfs.core.common.Constants;
import com.antfs.core.object.AntMetaObject;
import com.antfs.core.object.AntObject;
import com.antfs.core.object.ObjectReader;
import com.antfs.core.object.ObjectWriter;
import com.antfs.core.util.FileUtil;
import com.antfs.core.util.LogUtil;
import io.netty.util.internal.StringUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        FileExtractor.Builder builder = new FileExtractor.Builder(file,fid,this.objectWriter);
        builder.bufferSize(Constants.ANT_OBJECT_BUFFER_SIZE);

        FileExtractor fileExtractor = builder.build();
        fileExtractor.addListener(new FileExtractorListener() {
            @Override
            public void onMetaObjectReady(AntMetaObject antMetaObject) {
                objectWriter.writeMeta(antMetaObject);
            }

            @Override
            public void onAntObjectReady(AntObject antObject) {
                objectWriter.write(antObject);
            }
        });

        fileExtractor.start();

        return fid;
    }

    @Override
    public File restore(String fid) {
        File file;
        AntMetaObject antMetaObject = objectReader.readMeta(fid);
        if(antMetaObject==null){
            LogUtil.error("no antMetaObject found with fid=%s",fid);
            return null;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            file = new File(Constants.FILE_RESTORE_PATH+File.separator+antMetaObject.getFileName());
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            for(String oid : antMetaObject.getOids()){
                AntObject antObject = objectReader.read(fid,oid);
                if(antObject!=null){
                    bos.write(antObject.getContent());
                }
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}

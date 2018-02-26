package io.antfs.common.lang.object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * DefaultObjectWriter
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultObjectWriter extends DefaultObjectHandler implements ObjectWriter {

    /**
     * store the object into disk
     * @param storePath the path where the object should be stored
     * @param storeFileName the fileName which the object should be stored as
     * @param object the object to be stored
     */
    private void store(String storePath,String storeFileName,Object object){
        ObjectOutputStream os = null;
        try {
            File storeDir = new File(storePath);
            if(!storeDir.exists()){
                storeDir.mkdirs();
            }
            os = new ObjectOutputStream(new FileOutputStream(storePath+File.separator+storeFileName));
            os.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeMeta(AntMetaObject antMetaObject) {
        String fid = antMetaObject.getFid();
        String metaStorePath = genMetaObjectPath(fid);
        String meteObjectName = genMetaObjectName(fid);
        store(metaStorePath,meteObjectName,antMetaObject);
    }

    @Override
    public void write(AntObject antObject) {
        String fid = antObject.getFid();
        String oid = antObject.getOid();
        String objectStorePath = genObjectPath(fid,oid);
        String objectName = genObjectName(fid,oid);
        store(objectStorePath,objectName,antObject);
    }


}

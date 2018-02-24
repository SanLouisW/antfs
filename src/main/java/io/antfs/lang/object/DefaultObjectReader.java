package io.antfs.lang.object;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * DefaultObjectReader
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultObjectReader extends DefaultObjectHandler implements ObjectReader {

    /**
     * restore the object stored in disk
     * @param storePath the object stored path
     * @param storeFileName the object stored fileName
     * @return the object
     */
    private Object restore(String storePath,String storeFileName){
        Object object = null;
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(storePath+File.separator+storeFileName));
            object = is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally{
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    @Override
    public AntMetaObject readMeta(String fid) {
        String metaStorePath = genMetaObjectPath(fid);
        String meteObjectName = genMetaObjectName(fid);
        Object object = restore(metaStorePath,meteObjectName);
        return object!=null?(object instanceof AntMetaObject?(AntMetaObject)object:null):null;
    }

    @Override
    public AntObject read(String fid,String oid) {
        String objectStorePath = genObjectPath(fid,oid);
        String objectName = genObjectName(fid,oid);
        Object object = restore(objectStorePath,objectName);
        return object!=null?(object instanceof AntObject?(AntObject)object:null):null;
    }

}

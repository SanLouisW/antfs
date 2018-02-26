package io.antfs.common.lang.object;

/**
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface ObjectReader extends ObjectHandler {

    /**
     * extract the antMetaObject from disk
     * @param fid the file id
     * @return the antMetaObject
     */
    AntMetaObject readMeta(String fid);

    /**
     * extract the antObject from disk
     * @param fid the file id
     * @param oid the object id
     * @return the antObject
     */
    AntObject read(String fid,String oid);

}

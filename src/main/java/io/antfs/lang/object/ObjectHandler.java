package io.antfs.lang.object;

/**
 * ObjectHandler
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface ObjectHandler {

    /**
     * generate the object id due to the file id ant part number
     * @param fid the file id
     * @param part the part number of the AntObject
     * @return the object id of the AntObject
     */
    String oid(String fid,int part);

    /**
     * generate the path where the metaObject should be stored
     * @param fid the file id
     * @return the metaObject store path
     */
    String genMetaObjectPath(String fid);

    /**
     * generate the metaObject name
     * @param fid the file id
     * @return the metaObject name
     */
    String genMetaObjectName(String fid);

    /**
     * generate the path where the antObject should be stored
     * @param fid the file id
     * @param oid the object id
     * @return the antObject store path
     */
    String genObjectPath(String fid,String oid);

    /**
     * generate the antObject name
     * @param fid the file id
     * @param oid the object id
     * @return the antObject name
     */
    String genObjectName(String fid,String oid);

}

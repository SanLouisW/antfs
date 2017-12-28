package com.antfs.core.object;

/**
 * DefaultObjectHandler
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultObjectHandler implements ObjectHandler {

    @Override
    public String oid(String fid, int part) {
        return fid+"##"+part;
    }

    @Override
    public String genMetaObjectPath(String fid) {
        return null;
    }

    @Override
    public String genMetaObjectName(String fid) {
        return null;
    }

    @Override
    public String genObjectPath(String fid, String oid) {
        return null;
    }

    @Override
    public String genObjectName(String fid, String oid) {
        return null;
    }

}

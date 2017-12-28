package com.antfs.core.object;

import com.antfs.core.common.Constants;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;

/**
 * DefaultObjectHandler
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultObjectHandler implements ObjectHandler {

    @Override
    public String oid(String fid, int part) {
        String remixFid = DigestUtils.md5Hex(fid).toLowerCase();
        remixFid = remixFid.substring(0,remixFid.length()/2);
        return remixFid+"##"+part;
    }

    @Override
    public String genMetaObjectPath(String fid) {
        // TODO the path to store ant meta object
        return Constants.FILE_STORE_PATH+File.separator+"meta";
    }

    @Override
    public String genMetaObjectName(String fid) {
        return DigestUtils.md5Hex(fid+fid.length()).toLowerCase();
    }

    @Override
    public String genObjectPath(String fid, String oid) {
        // TODO the path to store ant object
        return Constants.FILE_STORE_PATH+File.separator+"ant";
    }

    @Override
    public String genObjectName(String fid, String oid) {
        return DigestUtils.md5Hex(fid+oid).toLowerCase();
    }

}

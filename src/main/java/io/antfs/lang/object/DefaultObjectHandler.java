package io.antfs.lang.object;

import io.antfs.common.Constants;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;

/**
 * DefaultObjectHandler
 * @author gris.wang
 * @since 2017/12/27
 **/
public class DefaultObjectHandler implements ObjectHandler {

    private String remix(String str,int subLen){
        if(str==null || str.trim().length()==0){
            return "";
        }
        String remix = DigestUtils.md5Hex(str).toLowerCase();
        remix = remix.substring(0,subLen>remix.length()-1?remix.length()-1:subLen);
        return remix;
    }

    @Override
    public String oid(String fid, int part) {
        return remix(fid,16)+"##"+part;
    }

    @Override
    public String genMetaObjectPath(String fid) {
        // TODO the path to store ant meta object
        return Constants.ANT_OBJECT_STORE_PATH +File.separator+"meta";
    }

    @Override
    public String genMetaObjectName(String fid) {
        return DigestUtils.md5Hex(fid+fid.length()).toLowerCase();
    }

    @Override
    public String genObjectPath(String fid, String oid) {
        // TODO the path to store ant object
        return Constants.ANT_OBJECT_STORE_PATH +File.separator+"ant";
    }

    @Override
    public String genObjectName(String fid, String oid) {
        return DigestUtils.md5Hex(fid+oid).toLowerCase();
    }

}

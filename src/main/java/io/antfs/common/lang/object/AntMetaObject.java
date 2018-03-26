package io.antfs.common.lang.object;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * each file has a AntMetaObject
 * @author gris.wang
 * @since 2017/12/27
 **/
public class AntMetaObject implements Serializable{

    /**
     * file id
     */
    private String fid;

    /**
     * original file name
     */
    private String fileName;

    /**
     * the sorted oids
     */
    private Set<String> oids;

    public AntMetaObject(String fid,String fileName){
        this.fid = fid;
        this.fileName = fileName;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Set<String> getOids() {
        return oids;
    }

    private void setOids(Set<String> oids) {
        this.oids = oids;
    }

    public void addOid(String oid){
        if(oids==null || oids.isEmpty()){
            oids = new TreeSet<>();
        }
        oids.add(oid);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{")
                                  .append("fid:").append(fid)
                                  .append(",fileName:").append(fileName)
                                  .append(",oids:").append(String.join(",",oids))
                                  .append("}")
                                  .toString();
    }
}

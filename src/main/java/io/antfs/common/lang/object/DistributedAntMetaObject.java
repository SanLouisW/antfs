package io.antfs.common.lang.object;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public class DistributedAntMetaObject {

    /**
     * file id
     */
    private String fid;
    /**
     * original file name
     */
    private String fileName;
    /**
     * distributedOidList
     */
    private List<DistributedOid> distributedOidList;

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

    public List<DistributedOid> getDistributedOidList() {
        return distributedOidList;
    }

    public void addDistributedOid(String oid,String host,int port){
        if(this.distributedOidList==null || this.distributedOidList.isEmpty()){
            this.distributedOidList = new CopyOnWriteArrayList<>();
        }
        boolean match = false;
        for(DistributedOid distributedOid : this.distributedOidList){
            if(distributedOid.getOid().equals(oid)){
                match = true;
                distributedOid.addHost(new DistributedHost(host,port));
                break;
            }
        }
        if(!match){
            DistributedOid distributedOid = new DistributedOid();
            distributedOid.setOid(oid);
            distributedOid.addHost(new DistributedHost(host,port));
            this.distributedOidList.add(distributedOid);
        }
    }

    public DistributedAntMetaObject(){

    }

    public DistributedAntMetaObject(String fid,String fileName){
        this.fid = fid;
        this.fileName = fileName;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
            .append("fid:").append(fid)
            .append(",fileName:").append(fileName)
            .append(",distributedOidList:").append(Arrays.toString(distributedOidList.toArray()))
            .append("}");
        return sb.toString();
    }



    public static class DistributedOid {
        private String oid;
        private Set<DistributedHost> hosts;
        public String getOid() {
            return oid;
        }
        public void setOid(String oid) {
            this.oid = oid;
        }
        public Set<DistributedHost> getHosts() {
            return hosts;
        }
        void addHost(DistributedHost host){
            if(this.hosts==null || this.hosts.isEmpty()){
                this.hosts = new CopyOnWriteArraySet<>();
            }
            this.hosts.add(host);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{")
                .append("oid:").append(oid)
                .append(",hosts:").append(Arrays.toString(hosts.toArray()))
                .append("}");
            return sb.toString();
        }
    }

    public static class DistributedHost {
        private String host;
        private int port;
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public int getPort() {
            return port;
        }
        public void setPort(int port) {
            this.port = port;
        }
        DistributedHost(String host, int port){
            this.host = host;
            this.port = port;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long hostCode = host.hashCode();
            long portCode = port;
            result = prime * result + (int) (hostCode ^ (hostCode >>> 32));
            result = prime * result + (int) (portCode ^ (portCode >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            }
            if (obj == null){
                return false;
            }
            if (getClass() != obj.getClass()){
                return false;
            }
            DistributedHost that = (DistributedHost) obj;
            return this.host.equals(that.host) && this.port==that.port;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{")
                .append("host:").append(host)
                .append(",port:").append(port)
                .append("}");
            return sb.toString();
        }
    }
}

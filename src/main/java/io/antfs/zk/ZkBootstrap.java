package io.antfs.zk;

import com.alibaba.fastjson.JSON;
import io.antfs.common.util.LogUtil;
import com.xiaoleilu.hutool.io.FileUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;

import java.io.File;
import java.util.List;


/**
 * Zk bootstrap
 * @author gris.wang
 * @since 2017/11/21
 **/
public class ZkBootstrap {

    private static final String DEFAULT_ZK_CFG = ZkBootstrap.class.getResource("/zk.cfg").getPath();

    private static final int SYSTEM_PORT_UP_RANGE = 1024;

    public static void main(String[] args) {
        String zkCfg = DEFAULT_ZK_CFG;
        // read out cfg
        if(args.length>0 && StrUtil.isNotBlank(args[0])){
            zkCfg = args[0];
        }
        try {
            ZkConfig zkConfig = JSON.parseObject(FileUtil.readUtf8String(new File(zkCfg)),ZkConfig.class);
            start(zkConfig);
        }catch (Exception e){
            LogUtil.error("ZkBootstrap start failed,cause:{}",e);
            System.exit(1);
        }
    }


    /**
     * start zk
     * @param zkConfig the zkConfig
     * @throws RuntimeException
     */
    public static void start(ZkConfig zkConfig) throws RuntimeException{
        if(zkConfig==null || CollectionUtil.isEmpty(zkConfig.getConfigs())){
            throw new RuntimeException("zkConfig is null or configs node is empty,please check zk.cfg");
        }
        String model = zkConfig.getModel();
        // 将zk服务的地址写入文件
        zkConfig.writeZkAddressToFile();
        // 单机模式
        if(ZkConfig.STANDALONE_MODEL.equalsIgnoreCase(model)){
            startStandalone(zkConfig.getConfigs().get(0));
        // 伪集群模式
        }else if(ZkConfig.CLUSTER_MODEL.equalsIgnoreCase(model)){
            startCluster(zkConfig.getConfigs());
        }
    }


    /**
     * check and generate zk properties file before start zk
     * @param model the mode
     * @param configSize config size
     * @param config the config
     * @param propertiesFile the properties file
     */
    private static void checkAndGenerateProperties(String model,int configSize,ZkConfig.Config config,File propertiesFile){
        if(!propertiesFile.exists()){
            FileUtil.touch(propertiesFile);
        }
        StringBuffer sb = new StringBuffer();
        int clientPort = config.getClientPort();
        if(clientPort<=SYSTEM_PORT_UP_RANGE){
            throw new RuntimeException("clientPort should not be less than "+SYSTEM_PORT_UP_RANGE);
        }
        sb.append("clientPort=").append(clientPort).append(StrUtil.CRLF);
        sb.append("tickTime=").append(config.getTickTime()).append(StrUtil.CRLF);
        sb.append("initLimit=").append(config.getInitLimit()).append(StrUtil.CRLF);
        sb.append("syncLimit=").append(config.getSyncLimit()).append(StrUtil.CRLF);
        String dataDir = config.getDataDir();
        if(StrUtil.isBlank(dataDir)){
            throw new RuntimeException("dataDir node should not be blank");
        }
        sb.append("dataDir=").append(dataDir).append(StrUtil.CRLF);
        File dataDirFile = new File(dataDir);
        if(!dataDirFile.exists()) {
            FileUtil.mkdir(dataDirFile);
        }
        String dataLogDir = config.getDataLogDir();
        if(StrUtil.isBlank(dataLogDir)){
            throw new RuntimeException("dataLogDir node should not be blank");
        }
        sb.append("dataLogDir=").append(dataLogDir).append(StrUtil.CRLF);
        File dataLogDirFile = new File(dataLogDir);
        if(!dataLogDirFile.exists()) {
            FileUtil.mkdir(dataLogDirFile);
        }
        sb.append("maxClientCnxns=").append(config.getMaxClientCnxns()).append(StrUtil.CRLF);
        if(ZkConfig.CLUSTER_MODEL.equalsIgnoreCase(model)){
            String myidStr = config.getMyid();
            if(StrUtil.isBlank(myidStr)){
                throw new RuntimeException("myid node should not be blank");
            }
            String[] strs = myidStr.split(":");
            if(strs.length!=2) {
                throw new RuntimeException("myid node format is invalid please check the zk.cfg");
            }
            File myidFile = new File(strs[0]);
            String myidContent = strs[1];
            if (!(myidFile.exists())) {
                FileUtil.touch(myidFile);
                FileUtil.writeUtf8String(myidContent,myidFile);
            }
            List<String> servers = config.getServers();
            if(CollectionUtil.isEmpty(servers) || configSize!=servers.size()){
                throw new RuntimeException("servers node should not be empty or does not match configs size");
            }
            for(int i=0,s=servers.size();i<s;i++){
                sb.append("server.").append(i+1).append("=").append(servers.get(i)).append(StrUtil.CRLF);
            }
        }
        // write properties to file
        FileUtil.writeUtf8String(sb.toString(),propertiesFile);
    }


    /**
     * start zk in standalone mode
     * @param config the config
     */
    private static void startStandalone(ZkConfig.Config config) throws RuntimeException{
        try{
            // properties的真实路径
            String realPropertiesPath = ZkServer.ZOOKEEPER_STANDALONE_PROPERTIES_FILE;
            checkAndGenerateProperties(ZkConfig.STANDALONE_MODEL,0,config,new File(realPropertiesPath));

            ZkServer zkServer = new ZkServer();
            zkServer.startStandalone(realPropertiesPath);
        }catch (Exception e) {
            LogUtil.error("startStandalone error,cause:",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * start zk in cluster mode
     * @param configs the configs
     */
    private static void startCluster(List<ZkConfig.Config> configs) throws RuntimeException{
        try{
            for(int i=0,size=configs.size();i<size;i++){
                ZkConfig.Config config = configs.get(i);
                // properties的真实路径
                String realPropertiesPath = String.format(ZkServer.ZOOKEEPER_CLUSTER_PROPERTIES_FILE,i+1);
                checkAndGenerateProperties(ZkConfig.CLUSTER_MODEL,size,config,new File(realPropertiesPath));

                ZkServer zkServer = new ZkServer();
                zkServer.startCluster(realPropertiesPath);
            }
        }catch (Exception e) {
            LogUtil.error("startCluster error,cause:",e);
            throw new RuntimeException(e.getMessage());
        }
    }


}

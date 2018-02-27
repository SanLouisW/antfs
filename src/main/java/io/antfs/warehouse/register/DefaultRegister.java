package io.antfs.warehouse.register;

import com.xiaoleilu.hutool.util.StrUtil;
import io.antfs.colony.node.Node;
import io.antfs.zk.ZkClient;
import io.antfs.zk.ZkNode;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default register
 * @author gris.wang
 * @since 2017/11/21
 **/
public class DefaultRegister implements Register {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegister.class);

    private static Register register;

    private CuratorFramework client;

    private DefaultRegister(String zkServerAddress){
        client = ZkClient.getClient(zkServerAddress);
    }

    /**
     * create the Register
     * @param zkServerAddress zk server address
     */
    public static Register create(String zkServerAddress){
        if(register==null){
            synchronized(DefaultRegister.class) {
                register = new DefaultRegister(zkServerAddress);
            }
        }
        return register;
    }

    /**
     * register the node
     * @param node current node
     */
    @Override
    public void register(Node node) {
        register(node,ZkNode.WORKER_PATH,CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * register the node
     * @param node current node
     * @param path the path where node registered to
     * @param createMode the createMode
     */
    @Override
    public void register(Node node, String path, CreateMode createMode){
        if(client==null || node ==null || StrUtil.isBlank(path)){
            throw new IllegalArgumentException(String.format("param illegal with client={%s},node={%s},path={%s}",client==null?null:client.toString(), node ==null?null: node.toString(),path));
        }
        try {
            if(client.checkExists().forPath(path)==null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(createMode)
                        .forPath(path, StrUtil.utf8Bytes(node.toString()));
            }
        } catch (Exception e) {
            LOGGER.error("register node error with node={},cause:", node,e);
        }
    }

}

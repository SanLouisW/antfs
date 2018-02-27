package io.antfs.warehouse.discovery;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.antfs.colony.node.Node;
import io.antfs.warehouse.NodeWareHouse;
import io.antfs.zk.ZkClient;
import io.antfs.zk.ZkNode;
import io.netty.util.CharsetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * default discovery
 * @author gris.wang
 * @since 2017/11/20
 **/
public class DefaultDiscovery implements Discovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDiscovery.class);

    private static Discovery discovery;

    private CuratorFramework client;

    private final Object lock;

    private int nodeIndex = 0;

    private DefaultDiscovery(String zkServerAddress){
        client = ZkClient.getClient(zkServerAddress);
        lock = new Object();
    }

    /**
     * init the Discovery
     * @param zkServerAddress zk server address
     */
    public static Discovery create(String zkServerAddress){
        if(discovery==null) {
            synchronized (DefaultDiscovery.class) {
                discovery = new DefaultDiscovery(zkServerAddress);
            }
        }
        return discovery;
    }

    private void initNodeMap(){
        try {
            if(client.checkExists().forPath(ZkNode.ROOT_PATH)!=null){
                List<String> children = client.getChildren().forPath(ZkNode.ROOT_PATH);
                for(String child : children){
                    String childPath = ZkNode.ROOT_PATH +"/"+child;
                    byte[] data = client.getData().forPath(childPath);
                    Node node = Node.parse(JSON.parseObject(data,JSONObject.class));
                    if(node !=null){
                        LOGGER.info("add node={} to nodeMap on init", node);
                        NodeWareHouse.addNode(node.getId(),node);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("initNodeMap error cause:",e);
        }
    }


    /**
     * watch the nodes
     */
    @Override
    public void watchNodes() {
        if(client==null){
            throw new IllegalArgumentException("param illegal with client={null}");
        }
        try {
            initNodeMap();
            PathChildrenCache watcher = new PathChildrenCache(
                    client,
                    ZkNode.ROOT_PATH,
                    true
            );
            watcher.getListenable().addListener(new NodeWatcher());
            watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        }catch(Exception e){
            LOGGER.error("watchNodes error cause:",e);
        }
    }


    /**
     * get an available worker node
     * @return Node
     */
    @Override
    public Node discoveryWorker() {
        Map<String,Node> nodeMap = NodeWareHouse.getNodeMap();
        if (nodeMap.size() == 0) {
            LOGGER.error("No available Node!");
            return null;
        }
        synchronized (lock){
            Node[] nodes = new Node[]{};
            nodes = nodeMap.values().toArray(nodes);
            if (nodeIndex >=nodes.length) {
                nodeIndex = 0;
            }
            return nodes[nodeIndex++];
        }
    }

    /**
     * get the queen node
     * @return Node
     */
    @Override
    public Node discoveryQueen(){
        if(client==null){
            throw new IllegalArgumentException("param illegal with client={null}");
        }
        try {
            byte[] data = client.getData().forPath(ZkNode.QUEEN_PATH);
            if(data!=null){
                return Node.parse(JSON.parseObject(data,JSONObject.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class NodeWatcher implements PathChildrenCacheListener{
        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            ChildData data = event.getData();
            if(data==null || data.getData()==null){
                return;
            }
            Node node = Node.parse(JSON.parseObject(data.getData(),JSONObject.class));
            if(node==null){
                LOGGER.error("get a null node with eventType={},path={},data={}",event.getType(),data.getPath(),data.getData());
            }else {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        NodeWareHouse.addNode(node.getId(),node);
                        LOGGER.info("CHILD_ADDED with path={},data={},current node size={}", data.getPath(), new String(data.getData(),CharsetUtil.UTF_8), NodeWareHouse.getNodeMap().size());
                        break;
                    case CHILD_REMOVED:
                        NodeWareHouse.removeNode(node.getId());
                        LOGGER.info("CHILD_REMOVED with path={},data={},current node size={}", data.getPath(), new String(data.getData(),CharsetUtil.UTF_8), NodeWareHouse.getNodeMap().size());
                        break;
                    case CHILD_UPDATED:
                        NodeWareHouse.updateNode(node.getId(),node);
                        LOGGER.info("CHILD_UPDATED with path={},data={},current node size={}", data.getPath(), new String(data.getData(),CharsetUtil.UTF_8), NodeWareHouse.getNodeMap().size());
                        break;
                    default:
                        break;
                }
            }
        }
    }

}

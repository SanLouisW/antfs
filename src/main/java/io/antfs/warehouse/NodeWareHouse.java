package io.antfs.warehouse;

import io.antfs.colony.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Node warehouse which hold the worker nodes
 * @author gris.wang
 * @since 2018/2/26
 **/
public class NodeWareHouse {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeWareHouse.class);

    private static Map<String,Node> nodeMap;
    static{
        nodeMap = new ConcurrentHashMap<>();
    }

    public static Map<String, Node> getNodeMap() {
        return nodeMap;
    }

    /**
     * whether the node exists or not
     * @param id the node id
     * @return true if exists
     */
    public static boolean nodeExists(String id){
        return nodeMap.containsKey(id);
    }

    /**
     * add node
     * @param id the id
     * @param node the node
     */
    public static void addNode(String id,Node node){
        if(id==null || id.trim().length()==0 || node==null){
            return;
        }
        nodeMap.putIfAbsent(id,node);
        showCurrentNodes();
    }

    /**
     * remove node
     * @param id the id
     */
    public static void removeNode(String id){
        if(id==null || id.trim().length()==0){
            return;
        }
        nodeMap.remove(id);
        showCurrentNodes();
    }

    /**
     * update node
     * @param id the id
     * @param node the node
     */
    public static void updateNode(String id,Node node){
        if(id==null || id.trim().length()==0 || node==null){
            return;
        }
        nodeMap.replace(id,node);
        showCurrentNodes();
    }

    /**
     * print current nodes
     */
    private static void showCurrentNodes(){
        LOGGER.debug("Current Nodes={},size={}",nodeMap,nodeMap.size());
    }
}

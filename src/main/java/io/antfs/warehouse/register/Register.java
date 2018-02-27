package io.antfs.warehouse.register;

import io.antfs.colony.node.Node;
import org.apache.zookeeper.CreateMode;

/**
 * Register the node
 * @author gris.wang
 * @since 2017/11/21
 **/
public interface Register {

    /**
     * register the node
     * @param node current node
     */
    void register(Node node);

    /**
     * register the node
     * @param node current node
     * @param path the path where node registered to
     * @param createMode the createMode
     */
    void register(Node node, String path, CreateMode createMode);
}

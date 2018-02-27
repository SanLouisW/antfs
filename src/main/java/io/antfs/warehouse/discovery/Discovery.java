package io.antfs.warehouse.discovery;

import io.antfs.colony.node.Node;

/**
 * Discovery the node
 * @author gris.wang
 * @since 2017/11/20
 **/
public interface Discovery {

    /**
     * watch the nodes
     */
    void watchNodes();

    /**
     * get an available worker node
     * @return Node
     */
    Node discoveryWorker();

    /**
     * get the queen node
     * @return Node
     */
    Node discoveryQueen();

}

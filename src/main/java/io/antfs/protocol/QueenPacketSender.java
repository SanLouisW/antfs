package io.antfs.protocol;

import io.antfs.colony.node.Node;
import io.antfs.warehouse.discovery.DefaultDiscovery;
import io.antfs.warehouse.discovery.Discovery;
import io.antfs.zk.ZkServer;
import io.netty.channel.Channel;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public class QueenPacketSender extends AbstractPacketSender {

    private static Discovery discovery;
    static{
        discovery = DefaultDiscovery.create(ZkServer.getZkAddress());
    }

    public QueenPacketSender(Channel channel){
        super(channel);
    }

    @Override
    public Node getNode() {
        return discovery.discoveryWorker();
    }


}

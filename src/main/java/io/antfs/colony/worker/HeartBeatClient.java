package io.antfs.colony.worker;

import io.antfs.colony.node.Node;
import io.antfs.common.Constants;
import io.antfs.protocol.Packet;
import io.antfs.protocol.PacketDecoder;
import io.antfs.protocol.PacketEncoder;
import io.antfs.warehouse.discovery.DefaultDiscovery;
import io.antfs.warehouse.discovery.Discovery;
import io.antfs.zk.ZkServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HeartBeatClient
 * @author gris.wang
 * @since 2017/11/20
 **/
public class HeartBeatClient extends ChannelInboundHandlerAdapter implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatClient.class);

    private static Discovery discovery;
    private static Node queenNode;
    static{
        discovery = DefaultDiscovery.create(ZkServer.getZkAddress());
        queenNode = discovery.discoveryQueen();
    }

    private static final int MAX_RETRY = 3;
    private static Channel channel;

    public HeartBeatClient(){
        if(discovery==null){
            LOGGER.warn("discovery is null,can't get queenNode");
            return;
        }
        connect();
    }

    @Override
    public void run() {
        heartBeat();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Has connected to Queen Server");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Queen Server is inactive,will reconnect after 10s");
        Thread.sleep(10 * 1000);
        connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("HeartBeatClient caught exception,cause:{}", cause);
        ctx.close();
    }

    private void connect(){
        int retry = 0;
        while(queenNode==null && retry<MAX_RETRY){
            queenNode = discovery.discoveryQueen();
            retry++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(queenNode==null){
            LOGGER.warn("queenNode is null,can't connect to queen");
            return;
        }
        if(channel==null){
            doConnect();
        }
    }

    private void doConnect(){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new PacketEncoder());
                    pipeline.addLast(new PacketDecoder(Constants.MAX_FRAME_LENGTH,Constants.LENGTH_FIELD_OFFSET,Constants.LENGTH_FIELD_LENGTH,Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                    pipeline.addLast(HeartBeatClient.this);
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // doConnect to queen server
            ChannelFuture future = bootstrap.connect(queenNode.getHost(), queenNode.getPort()).sync();
            channel = future.channel();
        }catch(InterruptedException e){
            LOGGER.error("HeartBeatClient doConnect to queen error,cause:",e);
        }
    }

    /**
     * send heart beat packet to queen server
     */
    private void heartBeat() {
        if(channel==null){
            LOGGER.warn("channel is null,can't send a heart beat packet to queen");
            return;
        }
        Packet packet = Packet.HEART_BEAT_PACKET;
        LOGGER.debug("worker send heart beat packet to queen with packet={},remoteAddress={}",packet,channel.remoteAddress());
        channel.writeAndFlush(packet);
    }

}

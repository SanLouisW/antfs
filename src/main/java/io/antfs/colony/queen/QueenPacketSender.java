package io.antfs.colony.queen;

import io.antfs.colony.node.Node;
import io.antfs.common.Constants;
import io.antfs.protocol.Packet;
import io.antfs.protocol.PacketDecoder;
import io.antfs.protocol.PacketEncoder;
import io.antfs.warehouse.discovery.DefaultDiscovery;
import io.antfs.warehouse.discovery.Discovery;
import io.antfs.zk.ZkServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Queen node will send the packet to Worker node
 * use QueenPacketSender
 * @author gris.wang
 * @since 2017/11/20
 */
public class QueenPacketSender {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueenPacketSender.class);

    private static Discovery discovery;
    static{
        discovery = DefaultDiscovery.create(ZkServer.getZkAddress());
    }

    private Node node;
    private Channel inboundChannel;
    private ChannelFuture connectFuture;
    private Channel outboundChannel;
    private Packet packet;

    public QueenPacketSender(ChannelHandlerContext ctx, Packet packet){
        this.node = discovery.discoveryWorker();
        this.inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                // use proxy inboundChannel to write back the response get from remote server
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new PacketEncoder());
                        pipeline.addLast(new PacketDecoder(Constants.MAX_FRAME_LENGTH,Constants.LENGTH_FIELD_OFFSET,Constants.LENGTH_FIELD_LENGTH,Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                        pipeline.addLast(new QueenPacketReceiver(inboundChannel));
                    }
                });
        bootstrap.option(ChannelOption.AUTO_READ, false);
        this.connectFuture = bootstrap.connect(node.getHost(), node.getPort());
        // get outboundChannel to remote server
        this.outboundChannel = connectFuture.channel();
        this.packet = packet;
    }

    public void sendPacket() {
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    if(outboundChannel.isActive()) {
                        outboundChannel.writeAndFlush(packet).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) {
                                if (future.isSuccess()) {
                                    // was able to flush out data, start to read the next chunk
                                    inboundChannel.read();
                                } else {
                                    LOGGER.error("write to backend {}:{} error,cause:{}", node.getHost(), node.getPort(),future.cause());
                                    future.channel().close();
                                }
                            }
                        });
                    }
                } else {
                    LOGGER.error("connect to backend {}:{} error,cause:{}", node.getHost(), node.getPort(),future.cause());
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }


    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}

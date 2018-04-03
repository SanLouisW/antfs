package io.antfs.protocol;

import io.antfs.colony.node.Node;
import io.antfs.common.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * The [Queen|Worker] node will send the packet to [Worker|Queen] node
 * use AbstractPacketSender
 * @author gris.wang
 * @since 2017/11/20
 */
public abstract class AbstractPacketSender extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractPacketSender.class);

    private Channel inboundChannel;
    private Bootstrap bootstrap;
    private Channel outboundChannel;
    private AttributeKey<PacketReceiverFuture<Packet>> receiverFuture;

    AbstractPacketSender(Channel channel){
        inboundChannel = channel;
        bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(inboundChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new PacketEncoder());
                        pipeline.addLast(new PacketDecoder(Constants.MAX_FRAME_LENGTH,Constants.LENGTH_FIELD_OFFSET,Constants.LENGTH_FIELD_LENGTH,Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                        pipeline.addLast(AbstractPacketSender.this);
                    }
                });
        bootstrap.option(ChannelOption.AUTO_READ, false);
        receiverFuture = AttributeKey.newInstance("receiverFuture");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof Packet) {
            outboundChannel.attr(receiverFuture).get().complete((Packet)msg);
        }
    }

    public PacketReceiverFuture<Packet> sendPacket(Packet packet) {
        Node node = getNode();
        if(node==null){
            throw new IllegalArgumentException("node is null");
        }
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(node.getHost(), node.getPort()));
        // get outboundChannel to remote server
        outboundChannel = connectFuture.channel();
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    outboundChannel.attr(receiverFuture);
                    outboundChannel.writeAndFlush(packet);
                } else {
                    LOGGER.error("connect to remote server {}:{} error,cause:{}", node.getHost(), node.getPort(),future.cause());
                }
            }
        });
        return outboundChannel.attr(receiverFuture).get();
    }

    /**
     * getNode
     * @return the node
     */
    public abstract Node getNode();

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }


}

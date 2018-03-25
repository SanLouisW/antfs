package io.antfs.colony.worker;

import io.antfs.protocol.Packet;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkerServerHandler
 * @author gris.wang
 * @since 2017/11/22
 */
public class WorkerServerHandler extends SimpleChannelInboundHandler<Packet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServerHandler.class);
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        Object response = handlePacket(msg);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        afterReadComplete();
    }


    private Object handlePacket(Packet packet){
        Object response = new Object();

        return response;
    }

    public void afterReadComplete(){

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("WorkerServerHandler ctx close,cause:",cause);
    }


}

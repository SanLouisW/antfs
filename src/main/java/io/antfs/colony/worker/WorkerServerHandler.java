package io.antfs.colony.worker;

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
public class WorkerServerHandler extends SimpleChannelInboundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServerHandler.class);
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Object response = handleResponse(msg);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        afterReadComplete();
    }


    public Object handleResponse(Object msg){
        return msg;
    }

    public void afterReadComplete(){

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("WorkerServerHandler ctx close,cause:",cause);
    }


}

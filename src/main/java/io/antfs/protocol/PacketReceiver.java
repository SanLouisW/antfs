package io.antfs.protocol;

import io.netty.channel.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author gris.wang
 * @since 2018/1/18
 **/
public class PacketReceiver extends ChannelInboundHandlerAdapter {

    private BlockingQueue<Object> responseQueen = new ArrayBlockingQueue<>(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.responseQueen.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        AbstractPacketSender.closeOnFlush(ctx.channel());
    }

    public Object getResponse() {
        try {
            return responseQueen.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}

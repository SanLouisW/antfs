package io.antfs.colony.queen;

import io.netty.channel.*;

/**
 * @author gris.wang
 * @since 2018/1/18
 **/
public class QueenPacketReceiver extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    public QueenPacketReceiver(Channel inboundChannel){
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        writeBackToInbound(ctx, msg);
    }

    private void writeBackToInbound(final ChannelHandlerContext ctx, Object msg){
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        QueenPacketSender.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        QueenPacketSender.closeOnFlush(ctx.channel());
    }


}

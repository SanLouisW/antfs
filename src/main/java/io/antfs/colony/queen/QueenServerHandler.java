package io.antfs.colony.queen;

import io.antfs.common.Constants;
import io.antfs.protocol.MsgType;
import io.antfs.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * QueenServerHandler
 * @author gris.wang
 * @since 2017/11/20
 */
public class QueenServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueenServerHandler.class);
    
    private ChannelHandlerContext ctx;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof HttpRequest){
            handleHttpRequest(ctx,(HttpRequest)msg);
        }else if(msg instanceof Packet){
            Packet packet = (Packet)msg;
            // handle heart beat
            if(packet.getHeader().getMsgType()== MsgType.HEARTBEAT.getType()){
                ctx.fireChannelRead(msg);
            // handle common packet
            }else {
                handlePacket(ctx,packet);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("QueenServerHandler ctx close,cause:{}",cause);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx,HttpRequest request){
        if(request.uri().equals(Constants.FAVICON_ICO)){
            return;
        }
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        this.ctx = ctx;

        ByteBuf byteBuf = Unpooled.copiedBuffer("welcome to antfs",CharsetUtil.UTF_8);
        Object response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        writeResponse(response);
    }

    private void handlePacket(ChannelHandlerContext ctx,Packet packet){
        // TODO handle common packet
        QueenPacketSender packetSender = new QueenPacketSender(ctx, packet);
        packetSender.sendPacket();
    }

    /**
     * write response to the channel
     */
    private void writeResponse(Object response){
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}

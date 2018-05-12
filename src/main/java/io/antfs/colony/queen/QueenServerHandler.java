package io.antfs.colony.queen;

import com.alibaba.fastjson.JSONObject;
import io.antfs.common.util.HttpRenderUtil;
import io.antfs.protocol.Packet;
import io.antfs.protocol.PacketType;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.ctx = ctx;
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            // handle common packet
            if (packet.getHeader().getPacketType() != PacketType.HEART_BEAT.getType()) {
                handlePacket(ctx, packet);
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


    private void handlePacket(ChannelHandlerContext ctx,Packet packet){
        // TODO handle common packet
//        AbstractPacketSender packetSender = new AbstractPacketSender(ctx, packet);
//        packetSender.sendPacket();
        PacketType type = PacketType.getByType(packet.getHeader().getPacketType());
        if(type==null){
            responseError("");
        }else{
            switch (type){
                case FILE_STORE:{

                }break;
                default:break;
            }
        }
    }

    private void responseError(String error){
        JSONObject object = new JSONObject();
        object.put("error",error);
        responseJSON(object.toJSONString());
    }

    private void responseJSON(String jsonStr){
        FullHttpResponse response = HttpRenderUtil.renderJSON(jsonStr);
        writeResponse(response);
    }

    private void writeResponse(Object response){
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}

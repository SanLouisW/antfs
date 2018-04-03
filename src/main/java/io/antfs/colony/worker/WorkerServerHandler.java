package io.antfs.colony.worker;

import io.antfs.colony.node.Node;
import io.antfs.common.lang.chunk.ChunkStorer;
import io.antfs.protocol.Packet;
import io.antfs.protocol.carriers.Carrier;
import io.antfs.protocol.carriers.ChunkStoreCarrier;
import io.antfs.protocol.carriers.ChunkStoreReplyCarrier;
import io.antfs.protocol.reverter.CarrierRevertUtil;
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
    private static final Node CURRENT_NODE = Node.CurrentNode.get();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        Packet response = handlePacket(msg);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    private Packet handlePacket(Packet packet){
        Carrier carrier = CarrierRevertUtil.revert(packet);
        Packet responsePacket = null;
        if(carrier instanceof ChunkStoreCarrier){
            responsePacket = handleChunkStore((ChunkStoreCarrier)carrier);
        }
        return responsePacket;
    }

    private Packet handleChunkStore(ChunkStoreCarrier chunkStoreCarrier){
        LOGGER.info("get chunkStoreCarrier={},will store the chunk",chunkStoreCarrier);
        // do chunk store
        ChunkStorer chunkStorer = new ChunkStorer(chunkStoreCarrier.getFid(),chunkStoreCarrier.getStart(),chunkStoreCarrier.getEnd(),chunkStoreCarrier.getChunkSize(),chunkStoreCarrier.getContent());
        chunkStorer.store();
        // TODO chunk replica

        ChunkStoreReplyCarrier replyCarrier = new ChunkStoreReplyCarrier(chunkStorer.getFid(),chunkStorer.getOid(), CURRENT_NODE.getHost(),CURRENT_NODE.getPort());
        return replyCarrier.getPacket();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("WorkerServerHandler ctx close,cause:",cause);
    }


}

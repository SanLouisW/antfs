package io.antfs.colony.queen;

import io.antfs.colony.node.Node;
import io.antfs.common.Constants;
import io.antfs.protocol.PacketType;
import io.antfs.protocol.Packet;
import io.antfs.warehouse.NodeWareHouse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * The heartbeat handler
 * @author gris.wang
 * @since 2017/11/20
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatServerHandler.class);

	private int idleCounter;

	private Node parseNode(ChannelHandlerContext ctx){
		SocketAddress remoteAddress = ctx.channel().remoteAddress();
		if(remoteAddress instanceof InetSocketAddress){
			InetSocketAddress inetSocketAddress = (InetSocketAddress)remoteAddress;
			String host = inetSocketAddress.getHostString();
			int port = inetSocketAddress.getPort();
			return new Node(host,port);
		}
		return null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof Packet){
			Packet packet = (Packet)msg;
			// handle heart beat
			if(packet.getHeader().getPacketType()==PacketType.HEART_BEAT.getType()){
				handleHeartBeat(ctx,packet);
				ReferenceCountUtil.release(msg);
			}else{
				ctx.fireChannelRead(msg);
			}
		}else{
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			idleCounter++;
			IdleStateEvent event = (IdleStateEvent) evt;
			String idleType = "";
			switch (event.state()){
				case READER_IDLE:{
					idleType = "read idle";
				}break;
				case WRITER_IDLE:{
					idleType = "write idle";
				}break;
				case ALL_IDLE:{
					idleType = "all idle";
				}break;
				default:break;
			}
			LOGGER.info("The Worker={} idle state={}",ctx.channel().remoteAddress(),idleType);
			if(idleCounter >= Constants.MAX_IDLE_COUNT_THRESHOLD) {
				Node workerNode = parseNode(ctx);
				if(workerNode==null) {
					LOGGER.error("workerNode parse from ctx is null");
					return;
				}
				LOGGER.warn("the Worker={} has achieve the idle threshold will close the channel",workerNode);
				ctx.close();
				NodeWareHouse.removeNode(workerNode.getId());
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("HeartBeatServerHandler caught exception,cause:{}", cause);
		ctx.close();
	}

	private void handleHeartBeat(ChannelHandlerContext ctx,Packet packet){
		LOGGER.info("Worker={} send Queen a heart beat packet",ctx.channel().remoteAddress());
		Node workerNode = parseNode(ctx);
		if(workerNode==null) {
			LOGGER.error("workerNode parse from ctx is null");
			return;
		}
		String id = workerNode.getId();
		if(!NodeWareHouse.nodeExists(id)){
			LOGGER.info("A new worker={} has connected to queen,will add it to NodeWareHouse",workerNode);
			NodeWareHouse.addNode(id, workerNode);
		}
		idleCounter = 0;
	}

}

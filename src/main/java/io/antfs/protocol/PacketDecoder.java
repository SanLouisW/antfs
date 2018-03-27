package io.antfs.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the packet decoder
 * @author gris.wang
 * @since 2018/2/26
 **/
public class PacketDecoder extends LengthFieldBasedFrameDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecoder.class);

	public PacketDecoder(int maxFrameLength, int lengthFieldOffset,int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength,lengthAdjustment, initialBytesToStrip);
 	}

	public PacketDecoder(int maxFrameLength, int lengthFieldOffset,int lengthFieldLength, int lengthAdjustment,int initialBytesToStrip, boolean failFast) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength,lengthAdjustment, initialBytesToStrip, failFast);
	}

 
	@Override
	protected Packet decode(ChannelHandlerContext ctx, ByteBuf bufIn) throws Exception {
		ByteBuf in = (ByteBuf) super.decode(ctx, bufIn);
		if (in == null) {
			LOGGER.info("ByteBuf is null");
			return null;
		}
		// response header is 30 bytes
		if (in.readableBytes() < Packet.HEADER_SIZE) {
			LOGGER.info("ByteBuf length is less than packet header size");
			return null;
		}

		byte magic = in.readByte();
		byte msgType = in.readByte();
		int len = in.readInt();
		// until we have the entire payload return
		if (in.readableBytes() < len) {
			LOGGER.info("ByteBuf length is less than packet length");
			return null;
		}

		ByteBuf bodyBuf = in.readBytes(len);
		byte[] body = new byte[bodyBuf.readableBytes()];
		bodyBuf.readBytes(body);

		Packet packet = new Packet();
		Packet.Header header = new Packet.Header(magic,msgType,len);
		packet.setHeader(header);
		packet.setBody(body);
		return packet;
	}

}

package io.antfs.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the packet encoder
 * @author gris.wang
 * @since 2018/2/26
 **/
public class PacketEncoder extends MessageToByteEncoder<Packet> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PacketEncoder.class);

	public PacketEncoder() {

	}

	public PacketEncoder(Class<? extends Packet> outboundMessageType) {
		super(outboundMessageType);
	}

	public PacketEncoder(boolean preferDirect) {
		super(preferDirect);
	}

	public PacketEncoder(Class<? extends Packet> outboundMessageType,boolean preferDirect) {
		super(outboundMessageType, preferDirect);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
		if (msg == null || msg.getHeader() == null) {
			throw new Exception("the encode message is null");
		}
		Packet.Header header = msg.getHeader();
		out.writeByte(header.getMagic());
		out.writeByte(header.getMsgType());
		out.writeLong(header.getChunkSize());
		out.writeLong(header.getChunkStart());
		out.writeLong(header.getChunkEnd());
		int len = header.getLen();
		out.writeInt(len);

		if(len>0) {
			byte[] body = msg.getBody();
			out.writeBytes(body);
		}
	}

}

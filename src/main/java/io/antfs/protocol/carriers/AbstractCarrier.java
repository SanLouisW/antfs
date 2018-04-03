package io.antfs.protocol.carriers;

import io.antfs.protocol.PacketType;
import io.antfs.protocol.Packet;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public abstract class AbstractCarrier extends Carrier {

    /**
     * getPacket
     * @return the Packet
     */
    public Packet getPacket(){
        byte[] body = getBody();
        Packet.Header header = new Packet.Header(Packet.MAGIC,getPacketType().getType(),body==null?0:body.length);
        Packet packet = new Packet();
        packet.setHeader(header);
        packet.setBody(body);
        return packet;
    }

    /**
     * getPacketType
     * @return the PacketType
     */
    public abstract PacketType getPacketType();

    /**
     * getBody
     * @return the bytes
     */
    public abstract byte[] getBody();


}

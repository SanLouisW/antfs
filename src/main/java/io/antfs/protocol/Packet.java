package io.antfs.protocol;

/**
 * The Packet which travels in the internet
 * @author gris.wang
 * @since 2018/2/26
 **/
public class Packet {

    /** the default magic number */
    public static final byte MAGIC = 0x13;
    /** the heart beat header */
    public static final Header HEART_BEAT_HEADER = new Header(Packet.MAGIC, PacketType.HEART_BEAT.getType());
    /** the heart beat packet */
    public static final Packet HEART_BEAT_PACKET = new Packet(Packet.HEART_BEAT_HEADER,null);
    /** the header size */
    public static final int HEADER_SIZE = 6;

    private Header header;
    private byte[] body;

    public Packet(){

    }

    private Packet(Header header,byte[] body){
        this.header = header;
        this.body = body;
    }

    public Header getHeader() {
        return header;
    }
    public void setHeader(Header header) {
        this.header = header;
    }
    public byte[] getBody() {
        return body;
    }
    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean validPacket(){
        return this.header != null
              && this.header.getMagic() == Packet.MAGIC
              && PacketType.validPacketType(this.header.getPacketType());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("magic=").append(header.getMagic())
          .append(",packetType=").append(header.getPacketType())
          .append(",len=").append(header.getLen())
          .append("}");
        return sb.toString();
    }

    public static class Header{
        /** magic number */
        private byte magic;
        /** the packet type */
        private byte packetType;
        /** packet body length */
        private int len;

        public byte getMagic() {
            return magic;
        }
        public void setMagic(byte magic) {
            this.magic = magic;
        }
        public byte getPacketType() {
            return packetType;
        }
        public void setPacketType(PacketType packetType) {
            this.packetType = packetType.getType();
        }
        public int getLen() {
            return len;
        }
        public void setLen(int len) {
            this.len = len;
        }

        public Header(byte magic,byte packetType){
            this(magic,packetType,0);
        }

        public Header(byte magic,byte packetType,int len){
            this.magic = magic;
            this.packetType = packetType;
            this.len = len;
        }
    }

}

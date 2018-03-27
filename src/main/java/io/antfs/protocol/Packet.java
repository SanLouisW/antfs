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
    public static final Header HEART_BEAT_HEADER = new Header(MAGIC,MsgType.HEARTBEAT.getType());
    /** the heart beat packet */
    public static final Packet HEART_BEAT_PACKET = new Packet(HEART_BEAT_HEADER,null);
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
        return this.header.getMagic() == Packet.MAGIC
              && MsgType.getByType(this.header.getMsgType())!=null;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{")
                .append("magic=").append(header.getMagic())
                .append(",msgType=").append(header.getMsgType())
                .append(",len=").append(header.getLen())
                .append("}")
                .toString();
    }

    public static class Header{
        /** magic number */
        private byte magic;
        /** the message type */
        private byte msgType;
        /** packet body length */
        private int len;

        public byte getMagic() {
            return magic;
        }
        public void setMagic(byte magic) {
            this.magic = magic;
        }
        public byte getMsgType() {
            return msgType;
        }
        public void setMsgType(MsgType msgType) {
            this.msgType = msgType.getType();
        }
        public int getLen() {
            return len;
        }
        public void setLen(int len) {
            this.len = len;
        }

        public Header(byte magic,byte msgType){
            this(magic,msgType,0);
        }

        public Header(byte magic,byte msgType,int len){
            this.magic = magic;
            this.msgType = msgType;
            this.len = len;
        }
    }

}

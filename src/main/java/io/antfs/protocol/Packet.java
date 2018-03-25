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
    public static final int HEADER_SIZE = 30;

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
                .append(",chunkSize=").append(header.getChunkSize())
                .append(",chunkStart=").append(header.getChunkStart())
                .append(",chunkEnd=").append(header.getChunkEnd())
                .append(",len=").append(header.getLen())
                .append("}")
                .toString();
    }

    public static class Header{
        /** magic number */
        private byte magic;
        /** the message type */
        private byte msgType;
        /** the file chunk size */
        private long chunkSize;
        /** the file chunk start */
        private long chunkStart;
        /** the file chunk end */
        private long chunkEnd;
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
        public long getChunkSize() {
            return chunkSize;
        }
        public void setChunkSize(long chunkSize) {
            this.chunkSize = chunkSize;
        }
        public long getChunkStart() {
            return chunkStart;
        }
        public void setChunkStart(long chunkStart) {
            this.chunkStart = chunkStart;
        }
        public long getChunkEnd() {
            return chunkEnd;
        }
        public void setChunkEnd(long chunkEnd) {
            this.chunkEnd = chunkEnd;
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
            this(magic,msgType,0,0,0,len);
        }

        public Header(byte magic,byte msgType,long chunkSize,long chunkStart,long chunkEnd,int len){
            this.magic = magic;
            this.msgType = msgType;
            this.chunkSize = chunkSize;
            this.chunkStart = chunkStart;
            this.chunkEnd = chunkEnd;
            this.len = len;
        }
    }

}

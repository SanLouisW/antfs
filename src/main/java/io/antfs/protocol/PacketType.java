package io.antfs.protocol;

/**
 * the type of a Packet
 * @author gris.wang
 * @since 2018/2/26
 **/
public enum PacketType {

    /**
     * heart beat
     * from worker to queen
     */
    HEART_BEAT((byte)0x00),

    /**
     * file store
     * from client to queen
     */
    FILE_STORE((byte)0x01),

    /**
     * file store reply
     * from queen to client
     */
    FILE_STORE_REPLY((byte)0x02),

    /**
     * chunk store
     * from client to queen
     * from queen to worker
     */
    CHUNK_STORE((byte)0x03),

    /**
     * chunk store reply
     * from worker to queen
     * from queen to client
     */
    CHUNK_STORE_REPLY((byte)0x04),

    /**
     * file restore
     * from client to queen
     */
    FILE_RESTORE((byte)0x05),

    /**
     * file restore reply
     * from queen to client
     */
    FILE_RESTORE_REPLY((byte)0x06),

    /**
     * chunk restore
     * from client to queen
     * from queen to worker
     */
    CHUNK_RESTORE((byte)0x07),

    /**
     * chunk restore reply
     * from worker to queen
     * from queen to client
     */
    CHUNK_RESTORE_REPLY((byte)0x08),

    /**
     * chunk replication
     * from worker to worker
     */
    CHUNK_REPLICA((byte)0x09),

    /**
     * chunk replication reply
     * from worker to worker
     */
    CHUNK_REPLICA_REPLY((byte)0x10),

    /**
     * file meta sync
     * from queen to worker
     */
    META_SYNC((byte)0x11),

    /**
     * file meta sync reply
     * from worker to queen
     */
    META_SYNC_REPLY((byte)0x12);

    private byte type;

    PacketType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public static PacketType getByType(byte type){
        for(PacketType packetType : values()){
            if(packetType.getType()==type){
                return packetType;
            }
        }
        return null;
    }

    public static boolean validPacketType(byte type){
        for(PacketType packetType : values()){
            if(packetType.getType()==type){
                return true;
            }
        }
        return false;
    }

}

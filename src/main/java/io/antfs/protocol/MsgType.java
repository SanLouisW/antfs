package io.antfs.protocol;

/**
 * the message type of a packet
 * @author gris.wang
 * @since 2018/2/26
 **/
public enum MsgType {

    /**
     * heart beat
     * from worker to queen
     */
    HEARTBEAT((byte)0x00),

    /**
     * file store
     * from client to queen
     * from queen to worker
     */
    STORE((byte)0x02),

    /**
     * file store reply
     * from worker to queen
     */
    STORE_REPLY((byte)0x03),

    /**
     * file restore
     * from client to queen
     * from queen to worker
     */
    RESTORE((byte)0x05),

    /**
     * file restore reply
     * from worker to queen
     */
    RESTORE_REPLY((byte)0x06),

    /**
     * file chunk replication
     * from worker to worker
     */
    REPLICA((byte)0x08),

    /**
     * file chunk replication reply
     * from worker to worker
     */
    REPLICA_REPLY((byte)0x09),

    /**
     * file meta sync
     * from queen to worker
     */
    SYNC((byte)0x11),

    /**
     * file meta sync reply
     * from worker to queen
     */
    SYNC_REPLY((byte)0x12);

    private byte type;

    MsgType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public static MsgType getByType(byte type){
        for(MsgType msgType : values()){
            if(msgType.getType()==type){
                return msgType;
            }
        }
        return null;
    }

}

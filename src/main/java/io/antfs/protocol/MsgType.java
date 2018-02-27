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
    STORE((byte)0x03),

    /**
     * file restore
     * from client to queen
     * from queen to worker
     */
    RESTORE((byte)0x04),

    /**
     * file chunk replication
     * from worker to worker
     */
    REPLICA((byte)0x10),

    /**
     * file meta sync
     * from queen to worker
     */
    SYNC((byte)0x11);

    private byte val;

    MsgType(byte val){
        this.val = val;
    }

    public byte getVal() {
        return val;
    }


}

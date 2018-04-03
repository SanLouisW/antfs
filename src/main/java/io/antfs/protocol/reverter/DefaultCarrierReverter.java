package io.antfs.protocol.reverter;

import com.xiaoleilu.hutool.util.ObjectUtil;
import io.antfs.protocol.PacketType;
import io.antfs.protocol.Packet;
import io.antfs.protocol.carriers.*;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public class DefaultCarrierReverter implements CarrierReverter {

    @Override
    public Carrier revert(Packet packet) {
        PacketType packetType;
        if(packet==null || !packet.validPacket() || (packetType = PacketType.getByType(packet.getHeader().getPacketType()))==null) {
            return null;
        }
        Carrier carrier = new Carrier();
        byte[] body = packet.getBody();
        Object obj = ObjectUtil.unserialize(body);
        switch (packetType){
            case FILE_STORE:{
                if(obj instanceof FileStoreCarrier) {
                    carrier = (FileStoreCarrier) obj;
                }
            }break;
            case FILE_STORE_REPLY:{
                if(obj instanceof FileStoreReplyCarrier){
                    carrier = (FileStoreReplyCarrier) obj;
                }
            }break;
            case FILE_RESTORE:{
                if(obj instanceof FileReStoreCarrier){
                    carrier = (FileReStoreCarrier) obj;
                }
            }break;
            case FILE_RESTORE_REPLY:{
                if(obj instanceof FileReStoreReplyCarrier){
                    carrier = (FileReStoreReplyCarrier) obj;
                }
            }break;
            case CHUNK_STORE:{
                if(obj instanceof ChunkStoreCarrier){
                    carrier = (ChunkStoreCarrier) obj;
                }
            }break;
            case CHUNK_STORE_REPLY:{
                if(obj instanceof ChunkStoreReplyCarrier){
                    carrier = (ChunkStoreReplyCarrier) obj;
                }
            }break;
            case CHUNK_RESTORE:{
                if(obj instanceof ChunkReStoreCarrier){
                    carrier = (ChunkReStoreCarrier) obj;
                }
            }break;
            case CHUNK_RESTORE_REPLY:{
                if(obj instanceof ChunkReStoreReplyCarrier){
                    carrier = (ChunkReStoreReplyCarrier) obj;
                }
            }break;
            case CHUNK_REPLICA:{
                if(obj instanceof ChunkReplicaCarrier){
                    carrier = (ChunkReplicaCarrier) obj;
                }
            }break;
            case CHUNK_REPLICA_REPLY:{
                if(obj instanceof ChunkReplicaReplyCarrier){
                    carrier = (ChunkReplicaReplyCarrier) obj;
                }
            }break;
            default:break;
        }
        return carrier;
    }

    private DefaultCarrierReverter(){

    }

    private static final class ReverterHolder{
        private static final DefaultCarrierReverter INSTANCE = new DefaultCarrierReverter();
    }

    public static DefaultCarrierReverter getInstance(){
        return ReverterHolder.INSTANCE;
    }

}

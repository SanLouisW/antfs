# antfs


> antfs是一个分布式的大文件存储系统，可扩展、高可用、高吞吐、去中心化是系统设计的原则。


系统的架构如下图所示：

![antfs.png](http://upload-images.jianshu.io/upload_images/5417792-dc7d458adeaed6ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

整个系统主要有三个部分组成：

- **QueenAnt**：蚁后。负责管理WorkerAnt和对外接收文件存储、恢复的命令，并向WorkerAnt下发相关指令
- **WorkerAnt**：工蚁。负责具体的文件块的存储和恢复，以及和其他Worker之间的数据同步
- **ZooKeeper**：注册中心。Queen和Worker注册到ZK，Worker自动发现Queen并进行heartbeat，Worker自动发现其他Worker并进行数据同步

系统的执行流程如下：

1.启动一个Zk集群，或使用已存在的Zk

2.启动Queen，将自己注册到zk上去，路径为/queen，节点类型为临时节点

3.启动Worker，将字节注册到zk上去，路径为/root/worker，节点类型为临时顺序节点

4.Worker到/queen下获取Queen节点，通过netty与Queen进行heartbeat

5.Client调用Queen，进行文件的store或者restore操作

6.Queen将指令转发到具体的Worker

7.Worker进行相关的任务执行，并按条件进行数据的同步

其中，各组件之间通过自定义协议进行通讯，定义一个数据包Packet，如下：
``` java
public class Packet {
    /** the default magic number */
    public static final byte MAGIC = 0x13;
    /** the heart beat header */
    public static final Header HEART_BEAT_HEADER = new Header(MAGIC,MsgType.HEARTBEAT.getVal(),0);
    /** the heart beat packet */
    public static final Packet HEART_BEAT_PACKET = new Packet(HEART_BEAT_HEADER,null);
    /** the header size */
    public static final int HEADER_SIZE = 6;
    private Header header;
    private String body;
    // 省略get、set
    public static class Header{
        /** magic number */
        private byte magic;
        /** the packet type */
        private byte packetType;
        /** packet body length */
        private int len;
        // 省略get、set
    }
}
```

PacketType定义如下：
``` java
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
}
```

Worker与Queen进行heartbeat是通过Worker启动时，在EventLoop中调度一个定时任务实现，具体代码如下：
``` java
ChannelFuture future = bootstrap.bind(node.getPort()).sync();
LOGGER.info("WorkerServer Startup at port:{}", node.getPort());

Channel channel = future.channel();
// schedule a heartbeat runnable
channel.eventLoop().scheduleAtFixedRate(new HeartbeatClient(),0, Constants.HEART_BEAT_PERIOD,TimeUnit.SECONDS);
LOGGER.info("HeartBeatClient has scheduled");
```
HeartBeatClient实际是一个Runnable，具体的工作就是连接上Queen之后，向Queen发送HeartBeat的Packet，具体的核心代码如下：
``` java
public HeartBeatClient(){
    if(discovery==null){
        LOGGER.warn("discovery is null,can't get queenNode");
        return;
    }
    // connect to Queen
    connect();
}

@Override
public void run() {
    heartBeat();
}

private void heartBeat() {
    if(channel==null){
        LOGGER.warn("channel is null,can't send a heart beat packet to queen");
        return;
    }
    Packet packet = Packet.HEART_BEAT_PACKET;
    LOGGER.debug("worker send heart beat packet to queen with packet={},remoteAddress={}",packet,channel.remoteAddress());
    channel.writeAndFlush(packet);
}
```


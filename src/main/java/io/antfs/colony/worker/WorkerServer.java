package io.antfs.colony.worker;

import io.antfs.colony.node.Node;
import io.antfs.common.Constants;
import io.antfs.protocol.PacketDecoder;
import io.antfs.protocol.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * WorkerServer
 * @author gris.wang
 * @since 2017/11/20
 */
public final class WorkerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServer.class);
    
    public void start(Node node) {
        if(node==null){
            throw new IllegalArgumentException("node is null");
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.BOSS_GROUP_SIZE, new DefaultThreadFactory("boss", true));
        EventLoopGroup workerGroup = new NioEventLoopGroup(Constants.WORKER_GROUP_SIZE, new DefaultThreadFactory("worker", true));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new WorkerServerInitializer());

            ChannelFuture future = bootstrap.bind(node.getPort()).sync();
            LOGGER.info("WorkerServer Startup at port:{}", node.getPort());

            Channel channel = future.channel();
            // schedule a heartbeat runnable
            channel.eventLoop().scheduleAtFixedRate(new HeartbeatClient(),0, Constants.HEART_BEAT_PERIOD,TimeUnit.SECONDS);
            LOGGER.info("HeartbeatClient has scheduled");
            // wait channel close
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException:",e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class WorkerServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new PacketDecoder(Constants.MAX_FRAME_LENGTH,Constants.LENGTH_FIELD_OFFSET,Constants.LENGTH_FIELD_LENGTH,Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
            pipeline.addLast(new PacketEncoder());
            pipeline.addLast(new WorkerServerHandler());
        }
    }

}
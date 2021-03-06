package io.antfs.colony.queen;

import io.antfs.common.Constants;
import io.antfs.protocol.PacketDecoder;
import io.antfs.protocol.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * QueenServer
 * @author gris.wang
 * @since 2017/11/20
 */
public final class QueenServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueenServer.class);
    
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.BOSS_GROUP_SIZE, new DefaultThreadFactory("boss", true));
        EventLoopGroup workerGroup = new NioEventLoopGroup(Constants.WORKER_GROUP_SIZE, new DefaultThreadFactory("worker", true));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new QueenServerInitializer());

            ChannelFuture future = bootstrap.bind(Constants.QUEEN_PORT).sync();
            LOGGER.info("QueenServer Startup at port:{}",Constants.QUEEN_PORT);

            // wait server channel close
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException:{}",e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class QueenServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new PacketDecoder(Constants.MAX_FRAME_LENGTH,Constants.LENGTH_FIELD_OFFSET,Constants.LENGTH_FIELD_LENGTH,Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
            pipeline.addLast(new PacketEncoder());
            pipeline.addLast(new IdleStateHandler(Constants.READ_IDLE_TIME_OUT, Constants.WRITE_IDLE_TIME_OUT, Constants.ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
            pipeline.addLast(new HeartBeatServerHandler());
            pipeline.addLast(new QueenServerHandler());
        }
    }

}
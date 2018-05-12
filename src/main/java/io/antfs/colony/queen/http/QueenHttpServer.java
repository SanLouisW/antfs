package io.antfs.colony.queen.http;

import io.antfs.common.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * QueenHttpServer
 * @author gris.wang
 * @since 2017/11/20
 */
public final class QueenHttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueenHttpServer.class);
    
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

            ChannelFuture future = bootstrap.bind(Constants.QUEEN_HTTP_PORT).sync();
            LOGGER.info("QueenServer Startup at port:{}",Constants.QUEEN_HTTP_PORT);

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

            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(Constants.MAX_CONTENT_LENGTH));
            pipeline.addLast(new QueenHttpServerHandler());
        }
    }

}
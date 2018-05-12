package io.antfs.colony.queen.http;

import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import io.antfs.common.Constants;
import io.antfs.common.lang.chunk.FileToChunkSplitter;
import io.antfs.common.lang.object.DistributedAntMetaObject;
import io.antfs.common.util.FileUtil;
import io.antfs.common.util.HttpRenderUtil;
import io.antfs.common.util.HttpRequestUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * QueenHttpServerHandler
 * @author gris.wang
 * @since 2017/11/20
 */
public class QueenHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueenHttpServerHandler.class);
    private static final String TIP = Constants.QUEEN_POST_URI_TIP.toJSONString();
    private ChannelHandlerContext ctx;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.ctx = ctx;
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            handleHttpRequest(ctx, request);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("QueenServerHandler ctx close,cause:{}",cause);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx,HttpRequest request){
        if(request.uri().equals(Constants.FAVICON_ICO)){
            return;
        }
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        HttpMethod method = request.method();
        if(HttpMethod.POST.equals(method)){
            String uri = request.uri();
            Map<String, List<String>> paramMap = HttpRequestUtil.getParameterMap(request);
            LOGGER.info("uri={},paramMap={}",uri,paramMap);
            if(uri.contains(Constants.FILE_STORE_URI)){
                List<String> filePaths = paramMap.get(Constants.FILE_STORE_PARAM);
                String filePath = CollectionUtil.isNotEmpty(filePaths)?filePaths.get(0):"";
                if(StrUtil.isNotBlank(filePath)){
                    File file = new File(filePath);
                    if(file.exists() && file.isFile()){
                        String fid = FileUtil.getCRC32(file);
                        FileToChunkSplitter splitter = new FileToChunkSplitter(ctx,file,fid,Constants.ANT_OBJECT_BUFFER_SIZE);
                        ScheduledFuture<DistributedAntMetaObject> scheduledFuture = ctx.channel().eventLoop().schedule(splitter,0, TimeUnit.SECONDS);
                        if(scheduledFuture.isDone()){
                            try {
                                DistributedAntMetaObject distributedAntMetaObject = scheduledFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }

                        JSONObject object = new JSONObject();
                        object.put("fid",fid);
                        responseJSON(object.toJSONString());
                        return;
                    }else{
                        responseError("file=["+filePath+"] does not exists!");
                        return;
                    }
                }

            }else if(uri.contains(Constants.FILE_RESTORE_URI)){
                List<String> fidList = paramMap.get(Constants.FILE_RESTORE_PARAM);
                String fid = CollectionUtil.isNotEmpty(fidList)?fidList.get(0):"";
                if(StrUtil.isNotBlank(fid)){

                }
            }
        }
        responseJSON(TIP);
    }


    private void responseError(String error){
        JSONObject object = new JSONObject();
        object.put("error",error);
        responseJSON(object.toJSONString());
    }

    private void responseJSON(String jsonStr){
        FullHttpResponse response = HttpRenderUtil.renderJSON(jsonStr);
        writeResponse(response);
    }

    private void writeResponse(Object response){
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}

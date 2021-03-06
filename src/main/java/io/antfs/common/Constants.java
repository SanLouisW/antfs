package io.antfs.common;

import com.alibaba.fastjson.JSONObject;
import io.antfs.common.util.PropertiesUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author gris.wang
 * @since 2017/12/25
 **/
public class Constants {


    private static final String ANT_FS_PROPERTIES_PATH = "/antfs.properties";
    private static final PropertiesUtil ANT_FS_PROPERTIES = PropertiesUtil.getInstance(ANT_FS_PROPERTIES_PATH);

    /** Queen server port */
    public static final int QUEEN_PORT = ANT_FS_PROPERTIES.getInt("queen.server.port",8800);
    /** Queen http server port */
    public static final int QUEEN_HTTP_PORT = ANT_FS_PROPERTIES.getInt("queen.http.server.port",8900);
    /** BossGroup Size */
    public static final int BOSS_GROUP_SIZE = null!=Integer.getInteger("server.bossGroup.size")?Integer.getInteger("server.bossGroup.size"):ANT_FS_PROPERTIES.getInt("server.bossGroup.size",1);
    /** WorkerGroup Size */
    public static final int WORKER_GROUP_SIZE = null!=Integer.getInteger("server.workerGroup.size")?Integer.getInteger("server.workerGroup.size"):ANT_FS_PROPERTIES.getInt("server.workerGroup.size",4);
    /** the maxContentLength which set to HttpObjectAggregator */
    public static final int MAX_CONTENT_LENGTH = ANT_FS_PROPERTIES.getInt("netty.max.content.length",10485760);

    /** file store uri */
    public static final String FILE_STORE_URI = ANT_FS_PROPERTIES.getString("file.store.uri");
    public static final String FILE_STORE_PARAM = "filePath";
    /** file restore uri */
    public static final String FILE_RESTORE_URI = ANT_FS_PROPERTIES.getString("file.restore.uri");
    public static final String FILE_RESTORE_PARAM = "fid";
    public static final JSONObject QUEEN_POST_URI_TIP = new JSONObject();

    /** the default AntObject size 4MB */
    public static final int ANT_OBJECT_BUFFER_SIZE = ANT_FS_PROPERTIES.getInt("ant.object.buffer.size",0x400000);
    /** the path where antObject stored in */
    public static final String ANT_OBJECT_STORE_PATH = ANT_FS_PROPERTIES.getString("ant.object.store.path");
    /** the path where antObject restored to file */
    public static final String FILE_RESTORE_PATH = ANT_FS_PROPERTIES.getString("file.restore.path");

    /** read timeout unit:seconds */
    public static final int READ_IDLE_TIME_OUT = 15;
    /** write timeout unit:seconds */
    public static final int WRITE_IDLE_TIME_OUT = 0;
    /** all timeout unit:seconds */
    public static final int ALL_IDLE_TIME_OUT = 0;

    /** heart beat period unit:seconds */
    public static final int HEART_BEAT_PERIOD = 10;

    public static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
    /** the bytes offset to len */
    public static final int LENGTH_FIELD_OFFSET = 2;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int LENGTH_ADJUSTMENT = 0;
    public static final int INITIAL_BYTES_TO_STRIP = 0;

    /** the max idle count that queen will close the connection */
    public static final int MAX_IDLE_COUNT_THRESHOLD = 3;


    public static final String FAVICON_ICO = "/favicon.ico";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    public static final String CONNECTION_CLOSE = "close";


    static{
        JSONObject store = new JSONObject();
        store.put("uri",FILE_STORE_URI);
        store.put("param",FILE_STORE_PARAM);
        JSONObject restore = new JSONObject();
        restore.put("uri",FILE_RESTORE_URI);
        restore.put("param",FILE_RESTORE_PARAM);
        QUEEN_POST_URI_TIP.put("method", HttpMethod.POST.toString());
        QUEEN_POST_URI_TIP.put("store",store);
        QUEEN_POST_URI_TIP.put("restore",restore);
    }

}

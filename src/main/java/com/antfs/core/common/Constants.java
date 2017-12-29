package com.antfs.core.common;

import com.antfs.core.util.PropertiesUtil;

/**
 * @author gris.wang
 * @since 2017/12/25
 **/
public class Constants {


    private static final String ANT_FS_PROPERTIES_PATH = "/antfs.properties";

    private static final PropertiesUtil ANT_FS_PROPERTIES = PropertiesUtil.getInstance(ANT_FS_PROPERTIES_PATH);

    /**
     * Server port
     */
    public static final int SERVER_PORT = ANT_FS_PROPERTIES.getInt("netty.server.port",8889);

    /**
     * the default AntObject size
     * 4MB
     */
    public static final int ANT_OBJECT_BUFFER_SIZE = ANT_FS_PROPERTIES.getInt("ant.object.buffer.size",0x400000);

    /**
     * the path where antObject stored in
     */
    public static final String ANT_OBJECT_STORE_PATH = ANT_FS_PROPERTIES.getString("ant.object.store.path");

    /**
     * the path where antObject restored to file
     */
    public static final String FILE_RESTORE_PATH = ANT_FS_PROPERTIES.getString("file.restore.path");

}

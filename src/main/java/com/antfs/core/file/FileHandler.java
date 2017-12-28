package com.antfs.core.file;

import java.io.File;

/**
 * A FileHandler is a handler to handle the file
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface FileHandler {

    /**
     * generate the file id due to the file itself
     * the fid is a unique symbol that identifies the file
     * @param file the file to generated
     * @return the file id
     */
    String fid(File file);

    /**
     * store the file into disk
     * @param file the file to be stored
     * @return the file id
     */
    String store(File file);

    /**
     * restore the file by file id
     * @param fid the file id
     * @return the file stored in AntObject
     */
    File restore(String fid);


}

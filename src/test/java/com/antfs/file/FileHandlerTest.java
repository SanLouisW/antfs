package com.antfs.file;

import com.antfs.core.file.DefaultFileHandler;
import com.antfs.core.file.FileHandler;
import com.antfs.core.object.DefaultObjectReader;
import com.antfs.core.object.DefaultObjectWriter;
import com.antfs.core.util.LogUtil;
import org.junit.After;
import org.junit.Before;

import java.io.File;

/**
 * @author gris.wang
 * @since 2017/12/28
 **/
public class FileHandlerTest {

    @Before
    public void beforeTest(){

    }

    @After
    public void afterTest(){

    }


    private static void testStore(){
        String filePath = "/Users/wanghui/Downloads/2017alitech_01.pdf";
        File file = new File(filePath);
        FileHandler fileHandler = new DefaultFileHandler(new DefaultObjectWriter(),new DefaultObjectReader());
        String fid = fileHandler.store(file);
        LogUtil.info("fid=%s",fid);
    }

    private static void testRestore(){
        String fid = "2463290722f20318316771f94badbf9b";
        FileHandler fileHandler = new DefaultFileHandler(new DefaultObjectWriter(),new DefaultObjectReader());
        File file = fileHandler.restore(fid);
        LogUtil.info("file path=%s",file.getAbsolutePath());
    }

    public static void main(String[] args){
//        testStore();
        testRestore();
    }

}

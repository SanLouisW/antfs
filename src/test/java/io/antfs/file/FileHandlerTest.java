package io.antfs.file;

import io.antfs.lang.file.DefaultFileHandler;
import io.antfs.lang.file.FileHandler;
import io.antfs.lang.object.DefaultObjectReader;
import io.antfs.lang.object.DefaultObjectWriter;
import io.antfs.common.util.LogUtil;
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
        String filePath = "/Users/wanghui/Documents/2017alitech_01.pdf";
        File file = new File(filePath);
        FileHandler fileHandler = new DefaultFileHandler(new DefaultObjectWriter(),new DefaultObjectReader());
        String fid = fileHandler.store(file);
        LogUtil.info("fid={}",fid);
    }

    private static void testRestore(){
        String fid = "2463290722f20318316771f94badbf9b";
        FileHandler fileHandler = new DefaultFileHandler(new DefaultObjectWriter(),new DefaultObjectReader());
        File file = fileHandler.restore(fid);
        LogUtil.info("file path={}",file.getAbsolutePath());
    }

    public static void main(String[] args){
//        testStore();
        testRestore();
    }

}

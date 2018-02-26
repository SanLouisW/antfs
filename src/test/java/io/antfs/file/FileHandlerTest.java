package io.antfs.file;

import io.antfs.common.lang.file.DefaultFileHandler;
import io.antfs.common.lang.file.FileHandler;
import io.antfs.common.lang.object.DefaultObjectReader;
import io.antfs.common.lang.object.DefaultObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author gris.wang
 * @since 2017/12/28
 **/
public class FileHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerTest.class);
    
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
        LOGGER.info("fid={}",fid);
    }

    private static void testRestore(){
        String fid = "2463290722f20318316771f94badbf9b";
        FileHandler fileHandler = new DefaultFileHandler(new DefaultObjectWriter(),new DefaultObjectReader());
        File file = fileHandler.restore(fid);
        LOGGER.info("file path={}",file.getAbsolutePath());
    }

    public static void main(String[] args){
//        testStore();
        testRestore();
    }

}

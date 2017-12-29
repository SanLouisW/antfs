package com.antfs.core.util;

import com.antfs.core.common.Constants;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * FileUtil
 * @author gris.wang
 * @since 2017/12/26
 **/
public class FileUtil {

    public enum Capacity{
        B,
        KB,
        MB,
        GB,
        TB,
        PB
    }

    private static final String ALGORITHM_MD5 = "MD5";

    private static final String ALGORITHM_SHA1 = "SHA-1";

    /**
     * byte transfer to other
     * @param capacity the unit
     * @param byteLen byte length
     * @return the transferred value
     */
    public static BigDecimal byteTransfer(Capacity capacity, long byteLen){
        BigDecimal len;
        BigDecimal transferTo = new BigDecimal(1024);
        switch (capacity){
            case B:
                len = new BigDecimal(byteLen).setScale(2,BigDecimal.ROUND_HALF_UP);
                break;
            case KB:
                len = new BigDecimal(byteLen).divide(transferTo,2,BigDecimal.ROUND_HALF_UP);
                break;
            case MB:
                len = byteTransfer(Capacity.KB,byteLen).divide(transferTo,2,BigDecimal.ROUND_HALF_UP);
                break;
            case GB:
                len = byteTransfer(Capacity.MB,byteLen).divide(transferTo,2,BigDecimal.ROUND_HALF_UP);
                break;
            case TB:
                len = byteTransfer(Capacity.GB,byteLen).divide(transferTo,2,BigDecimal.ROUND_HALF_UP);
                break;
            case PB:
                len = byteTransfer(Capacity.TB,byteLen).divide(transferTo,2,BigDecimal.ROUND_HALF_UP);
                break;
            default:
                len = new BigDecimal(byteLen);
                break;
        }
        return len;
    }

    /**
     * getFileLengthByChannel
     * @param file the file
     * @return file length
     */
    public static long getFileLengthByChannel(File file){
        FileChannel fc = null;
        FileInputStream fis = null;
        long length = 0;
        try {
            if (file.exists() && file.isFile()){
                fis = new FileInputStream(file);
                fc = fis.getChannel();
                length = fc.size();
            }else{
                LogUtil.info("file doesn't exist or is not a file");
            }
        } catch (IOException e) {
            LogUtil.error(e);
        } finally {
            if (null!=fc){
                try{
                    fc.close();
                }catch(IOException e){
                    LogUtil.error(e);
                }
            }
            if (null!=fis){
                try{
                    fis.close();
                }catch(IOException e){
                    LogUtil.error(e);
                }
            }
        }
        return length;
    }

    /**
     * getFileLength
     * @param file the file
     * @return file length
     */
    public static long getFileLength(File file){
        long length = 0;
        if (file.exists() && file.isFile()){
            length = file.length();
        }else{
            LogUtil.info("file doesn't exist or is not a file");
        }
        return length;
    }

    /**
     * getAllFileBytes
     * @param filePath the file path
     * @return the all file bytes
     */
    public static byte[] getAllFileBytes(String filePath){
        LogUtil.info("Getting bytes from file...");
        Path inputPath = Paths.get(filePath);
        byte[] bytes = {};

        try {
            bytes = Files.readAllBytes(inputPath);
        } catch (IOException e) {
            LogUtil.error(e);
        }
        return bytes;
    }


    /**
     * getDigest
     * @param file the file
     * @param algorithm the algorithm
     * @return the digest result
     */
    private static String getDigest(File file,String algorithm){
        if(file==null || !file.exists() || file.isDirectory()){
            throw new IllegalArgumentException("file is null or file does not exists or file is a directory");
        }
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        MessageDigest digest = null;
        try{
            randomAccessFile = new RandomAccessFile(file,"rw");
            fileChannel = randomAccessFile.getChannel();
            digest = MessageDigest.getInstance(algorithm);
            MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            digest.update(byteBuffer);
            return DigestUtils.md5Hex(digest.digest()).toLowerCase();
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }finally {
            if(randomAccessFile!=null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileChannel!=null){
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * getMD5
     * @param file the file to calculate md5
     * @return the md5 result
     */
    public static String getMD5(File file){
        return getDigest(file,ALGORITHM_MD5);
    }

    /**
     * getSha1
     * @param file the file to calculate sha1
     * @return the sha1 result
     */
    public static String getSha1(File file){
        return getDigest(file,ALGORITHM_SHA1);
    }

    /**
     * getCRC32
     * @param file the file to calculate crc32
     * @return the file crc32 result
     */
    public static String getCRC32(File file){
        if(file==null || !file.exists() || file.isDirectory()){
            throw new IllegalArgumentException("file is null or file does not exists or file is a directory");
        }
        CRC32 crc32 = new CRC32();
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        try {
            randomAccessFile = new RandomAccessFile(file,"rw");
            fileChannel = randomAccessFile.getChannel();
            MappedByteBuffer mapBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,0,file.length());
            crc32.update(mapBuffer);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(randomAccessFile!=null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileChannel!=null){
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return DigestUtils.md5Hex(Long.toHexString(crc32.getValue()+getFileLength(file))).toLowerCase();
    }


    public static void main(String[] args) {
        String filePath = "/Users/wanghui/Downloads/2017alitech_01.pdf";
        int bufferSize = Constants.ANT_OBJECT_BUFFER_SIZE;
        File file = new File(filePath);
        long len = getFileLength(file);
        LogUtil.info("len =(%d)",len);
        int quotient = (int)(len/bufferSize);
        int mod = (int)(len%bufferSize);
        int size = (int)Math.ceil((double)len/bufferSize);
        LogUtil.info("quot=(%d)",quotient);
        LogUtil.info("mod =(%d)",mod);
        LogUtil.info("size=(%d)",size);

        String crc = FileUtil.getCRC32(file);
        String md5 = FileUtil.getMD5(file);
        String sha1 = FileUtil.getSha1(file);
        LogUtil.info("crc =(%s)",crc);
        LogUtil.info("md5 =(%s)",md5);
        LogUtil.info("sha1=(%s)",sha1);
    }

}

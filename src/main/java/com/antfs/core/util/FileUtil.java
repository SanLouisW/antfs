package com.antfs.core.util;

import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
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
     * @param capacity 容量单位
     * @param byteLen 字节大小
     * @return 转换后的大小
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
        FileInputStream is = null;
        FileChannel channel = null;
        MessageDigest digest = null;
        try{
            is = new FileInputStream(file);
            channel = is.getChannel();
            digest = MessageDigest.getInstance(algorithm);
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            digest.update(byteBuffer);
            return DigestUtils.md5Hex(digest.digest()).toUpperCase();
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(channel!=null){
                try {
                    channel.close();
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
        CRC32 crc32 = new CRC32();
        InputStream is = null;
        byte[] bytes = new byte[1024];
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            int cnt;
            while ((cnt = is.read(bytes)) != -1) {
                crc32.update(bytes, 0, cnt);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return DigestUtils.md5Hex(Long.toHexString(crc32.getValue()+getFileLength(file))).toUpperCase();
    }


    public static void main(String[] args) {
        String filePath = "/Users/wanghui/Downloads/2017alitech_01.pdf";
        File file = new File(filePath);
        long len = getFileLength(file);
        Capacity capacity = Capacity.MB;
        LogUtil.info("len=%f(%s)",byteTransfer(capacity,len).floatValue(),capacity);

        String crc = FileUtil.getCRC32(file);
        String md5 = FileUtil.getMD5(file);
        String sha1 = FileUtil.getSha1(file);
        LogUtil.info("crc =(%s)",crc);
        LogUtil.info("md5 =(%s)",md5);
        LogUtil.info("sha1=(%s)",sha1);
    }

}

package com.antfs.core.util;

/**
 * @author gris.wang
 * @since 2017/12/27
 **/
public class HashUtil {

    /**
     * Use FNV1_32_HASH method to get the hash of the str
     * @param str the string to get hash
     * @return the hash value
     */
    public static int getFnv32Hash(String str) {
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++){
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // if the hash is negative get the absolute value
        if (hash < 0){
            hash = Math.abs(hash);
        }
        return hash;
    }



}

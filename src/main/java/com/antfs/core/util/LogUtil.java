package com.antfs.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Log util
 * @author gris.wang
 * @since 2017/12/4
 **/
public class LogUtil{

    private LogUtil(){

    }

    private static Map<Class,Logger> loggerMap;
    private static Lock lock;

    static{
        loggerMap = new ConcurrentHashMap<>();
        lock = new ReentrantLock();
    }

    private static Logger getLogger(){
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        /*
         * the StackTrace are always like blow:
         * java.lang.Thread.getStackTrace(Thread.java:1559)------------- 0
         * com.antfs.core.util.LogUtil.getLogger(LogUtil.java:32)------- 1
         * com.antfs.core.util.LogUtil.info(LogUtil.java:71)------------ 2
         * the.class.who.call.LogUtil.info(Caller.java:254)------------- 3
         */
        StackTraceElement caller = traceElements[3];
        Class clazz = null;
        try{
            clazz = Class.forName(caller.getClassName());
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        if(clazz == null){
            return null;
        }
        Logger logger = loggerMap.get(clazz);
        if(logger==null){
            lock.lock();
            try {
                if(!loggerMap.containsKey(clazz)) {
                    logger = LoggerFactory.getLogger(clazz);
                    loggerMap.putIfAbsent(clazz,logger);
                }else{
                    logger = loggerMap.get(clazz);
                }
            }finally {
                lock.unlock();
            }
        }
        return logger;
    }


    /**
     * use info log level to print
     * @param log the log info
     * @param params log params
     */
    public static void info(String log,Object... params){
        Logger logger = getLogger();
        if(logger!=null && logger.isInfoEnabled()){
            // TODO the line number is not correct
            logger.info(log, params);
        }
    }

    /**
     * use warn log level to print
     * @param log the log info
     * @param params log params
     */
    public static void warn(String log,Object... params){
        Logger logger = getLogger();
        if(logger!=null && logger.isWarnEnabled()){
            logger.warn(log, params);
        }
    }

    /**
     * use error log level to print
     * @param log the log info
     * @param params log params
     */
    public static void error(String log,Object... params){
        Logger logger = getLogger();
        if(logger!=null && logger.isErrorEnabled()){
            logger.error(log, params);
        }
    }


}

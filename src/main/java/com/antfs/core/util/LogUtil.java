package com.antfs.core.util;

/**
 * 日志工具
 * @author gris.wang
 * @since 2017/12/4
 **/
public class LogUtil {

    private LogUtil(){

    }

    /**
     * 获取log信息
     * @param log 日志信息
     * @param params 日志信息中的参数
     * @return
     */
    public static String getLogInfo(String log,Object... params){
        String logInfo = log;
        if(log!=null && log.trim().length()>0){
            logInfo = String.format(log,params);
        }
        return logInfo;
    }

    /**
     * 以info方式打印
     * @param log 日志信息
     * @param params 日志信息中的参数
     */
    public static void info(String log,Object... params){
        if(log!=null && log.trim().length()>0){
            System.out.println(getLogInfo(log, params));
        }
    }

    /**
     * 以info方式打印
     * @param throwable 异常信息
     */
    public static void info(Throwable throwable){
        if(throwable!=null){
            System.out.println(throwable.getMessage());
            for(StackTraceElement traceElement : throwable.getStackTrace()) {
                System.out.println(traceElement.getClassName()+":"+traceElement.getLineNumber()+"-"+traceElement.getMethodName());
            }
        }
    }

    /**
     * 以error方式打印
     * @param log 日志信息
     * @param params 日志信息中的参数
     */
    public static void error(String log,Object... params){
        if(log!=null && log.trim().length()>0){
            System.err.println(getLogInfo(log, params));
        }
    }

    /**
     * 以error方式打印
     * @param throwable 异常信息
     */
    public static void error(Throwable throwable){
        if(throwable!=null){
            System.err.println(throwable.getMessage());
            for(StackTraceElement traceElement : throwable.getStackTrace()) {
                System.err.println(traceElement.getClassName()+":"+traceElement.getLineNumber()+"-"+traceElement.getMethodName());
            }
        }
    }

}

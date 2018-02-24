package io.antfs.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesUtil
 * @author gris.wang
 * @since 2017/12/25
 **/
public class PropertiesUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
	
    private static Map<String,PropertiesUtil> propertiesUtilsHolder = null;
    
    private static Map<PropertiesUtil,Properties> propertiesMap = null;

    private volatile boolean propertiesLoaded;

    private PropertiesUtil(){
    	
    }

    static{
		if(null==propertiesUtilsHolder){
			propertiesUtilsHolder = new HashMap<>();
		}
		if(null==propertiesMap){
			propertiesMap = new HashMap<>();
		}
	}

	/**
	 * whether the properties is loaded
	 * @return true:loaded false:not loaded
	 */
	private boolean propertiesLoaded(){
		int retryTime = 0;
		int retryTimeout = 1000;
		int sleep = 500;
		while(!propertiesLoaded && retryTime<retryTimeout){
			try {
				Thread.sleep(sleep);
				retryTime+=sleep;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return propertiesLoaded;
	}


	/**
	 * getPropertiesByResource
	 * @param propertiesPath the properties path which should be in the classpath
	 * @return the Properties
	 */
	public static Properties getPropertiesByResource(String propertiesPath){
		InputStream inputStream = null;
		Properties properties = null;
		try{
			inputStream = PropertiesUtil.class.getResourceAsStream(propertiesPath);
			if(inputStream!=null){
				properties = new Properties();
				properties.load(inputStream);
			}
		} catch (Exception e) {
			LOGGER.error("getPropertiesByResource error,cause:",e);
		} finally{
			try {
				if(inputStream!=null){
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

    /**
     * get singleton instance
     * @param propertiesPath the properties path
     * @return the propertiesUtil
     */
    public static synchronized PropertiesUtil getInstance(String propertiesPath){
    	PropertiesUtil propertiesUtil = propertiesUtilsHolder.get(propertiesPath);
    	if(null==propertiesUtil){
    		LOGGER.info("PropertiesUtil instance is null with propertiesPath={},will new a instance directly",propertiesPath);
			InputStream inputStream = null;
			try{
				propertiesUtil = new PropertiesUtil();
				Properties properties = new Properties();
				inputStream = PropertiesUtil.class.getResourceAsStream(propertiesPath);
				if(inputStream!=null){
					properties.load(inputStream);
					propertiesUtilsHolder.put(propertiesPath, propertiesUtil);
					propertiesMap.put(propertiesUtil, properties);

					LOGGER.info("PropertiesUtil instance init success");
					propertiesUtil.propertiesLoaded = true;
				}
			} catch (Exception e) {
				LOGGER.error("getInstance occur error,cause:",e);
			} finally{
				try {
					if(inputStream!=null){
						inputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	}
    	return propertiesUtil;
    }
    
    /**
     * getString
     * @param key the key
     * @return the value
     */
    public String getString(String key){
    	if(propertiesLoaded()){
			Properties properties = propertiesMap.get(this);
			return null != properties ? properties.getProperty(key) : null;
		}
		return null;
    }
    
    /**
     * getBoolean
     * @param key the key
     * @return the value
     */
    public boolean getBoolean(String key){
    	String value = getString(key);
    	return "true".equalsIgnoreCase(value);
    }
    
    /**
     * getInt
     * @param key the key
     * @param defaultValue the default value
     * @return the value
     */
    public int getInt(String key,int defaultValue){
    	String value = getString(key);
    	int intValue;
    	try{
    		intValue = Integer.parseInt(value);
    	}catch(Exception e){
    		intValue = defaultValue;
    	}
    	return intValue;
    }
    
    /**
     * getLong
     * @param key the key
     * @param defaultValue the default value
     * @return the value
     */
    public long getLong(String key,long defaultValue){
    	String value = getString(key);
    	long longValue;
    	try{
    		longValue = Long.parseLong(value);
    	}catch(Exception e){
    		longValue = defaultValue;
    	}
    	return longValue;
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}

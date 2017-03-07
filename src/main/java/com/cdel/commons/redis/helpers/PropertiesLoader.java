package com.cdel.commons.redis.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 加载redis配置文件
 * @author dell
 *
 */
public class PropertiesLoader {
	//引入redis配置文件
	private static String REDIS_CONFIGURATION_FILE = "/redis.properties";
    
	private static Properties propertie = null;
	
	static {
		InputStream inputStream = Object.class.getResourceAsStream(REDIS_CONFIGURATION_FILE);
		
		try {
			if(inputStream != null){
				propertie = new Properties();
				propertie.load(inputStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Properties getProperties(){
		return propertie;
	}
}

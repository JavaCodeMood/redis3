package com.cdel.commons.redis.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.cdel.commons.redis.helpers.PropertiesLoader;

import redis.clients.jedis.ShardedJedis;

/**
 * Redis工具类
 * @author dell
 *
 */
public class RedisUtilBak {
	private static RedisPool redisPool = null;
	
	/**
	 * Redis过期时间，以秒为单位
	 */
	public final static int EXRP_HOUR = 60 * 60;  //一小时
	public final static int EXRP_DAY = 60 * 60 * 24;  //一天
	public final static int EXRP_MONTH = 60 * 60 * 24 * 30; // 一个月
	
	/**
	 * 初始化redis连接池
	 */
	private static void initialPool(){
		Properties propertie = PropertiesLoader.getProperties();
        if (propertie == null){
            throw new RuntimeException("load redis properties failed!");
        }
        String servers = propertie.getProperty("redis.servers");
        String masters = propertie.getProperty("redis.masters");
        String password = propertie.getProperty("redis.password");
        int database = Integer.parseInt(propertie.getProperty("redis.database", "0"));
        int maxTotal = Integer.parseInt(propertie.getProperty("redis.maxTotal", "512"));
        int maxIdle = Integer.parseInt(propertie.getProperty("redis.maxIdle", "512"));
        int minIdle = Integer.parseInt(propertie.getProperty("redis.minIdle", "8"));
        int timeout = Integer.parseInt(propertie.getProperty("redis.timeout", "2000"));
	
        Set<String> sentinels = new HashSet<String>();
        if (servers != null){
            sentinels.addAll(Arrays.asList(servers.split(" ")));
        }
        List<String> masterList = new ArrayList<String>();
        if (masters != null){
            masterList.addAll(Arrays.asList(masters.split(" ")));
        }
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        redisPool = new RedisPool(masterList, sentinels, poolConfig, timeout, password, database); 
	}
	
	/**
	 * 在多线程环境同步初始化
	 */
	private static synchronized void poolInit(){
		if(redisPool == null){
			initialPool();
		}
	}
	
	private static ShardedJedis getResource(){
		if(redisPool == null){
			poolInit();
		}
		return redisPool.getResource();
	}
	
	public static void set(String key, String value){
		ShardedJedis shard = null;
		try {
			shard = getResource();
			shard.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(shard != null){
				shard.close();
			}
		}
	}
	
	public static String get(String key){
		ShardedJedis shard = null;
        try {
            shard = getResource();
            return shard.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (shard != null)
                shard.close();
        }
        return null;
	}

}

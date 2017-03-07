package com.cdel.commons.redis.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cdel.commons.redis.helpers.PropertiesLoader;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;

public class RedisPoolTest {
    private static RedisPool redisPool;
    private ShardedJedis sharding;

    @BeforeClass
    public static void setup() {
        Properties propertie = PropertiesLoader.getProperties();
        if (propertie == null)
            throw new RuntimeException("load redis properties failed!");
        String servers = propertie.getProperty("redis.servers");
        String masters = propertie.getProperty("redis.masters");
        String password = propertie.getProperty("redis.password");
        int database = Integer.parseInt(propertie.getProperty("redis.database", "0"));
        int maxTotal = Integer.parseInt(propertie.getProperty("redis.maxTotal", "512"));
        int maxIdle = Integer.parseInt(propertie.getProperty("redis.maxIdle", "512"));
        int minIdle = Integer.parseInt(propertie.getProperty("redis.minIdle", "8"));
        int timeout = Integer.parseInt(propertie.getProperty("redis.timeout", "2000"));

        Set<String> sentinels = new HashSet<String>();
        if (servers != null)
            sentinels.addAll(Arrays.asList(servers.split(" ")));
        List<String> masterList = new ArrayList<String>();
        if (masters != null)
            masterList.addAll(Arrays.asList(masters.split(" ")));
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        redisPool = new RedisPool(masterList, sentinels, poolConfig, timeout, password,
                database);
    }

    @Before
    public void before() {
        sharding = redisPool.getResource();
    }

    @After
    public void after() {
        if (sharding != null) {
            sharding.close();
        }
    }

    @AfterClass
    public static void destroy() {
        if (redisPool != null) {
            redisPool.close();
        }
    }


    @Test
    public void testShardNormal() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            sharding.set("shard" + i, "shard" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println("Simple@Sharing SET: " + ((end - start) / 1000.0) + " seconds");
    }

    @Test
    public void testShardpipelined() {
        ShardedJedisPipeline pipeline = sharding.pipelined();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pipeline.set("shardSenP" + i, "shardP" + i);
        }
        pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        System.out.println("Pipelined@Sharing SET: " + ((end - start) / 1000.0) + " seconds");
        // Pipelined@Sharing SET: 0.57 seconds

    }

}

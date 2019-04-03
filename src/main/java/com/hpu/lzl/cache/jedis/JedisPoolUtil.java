package com.hpu.lzl.cache.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
*   
* @author:awo  
* @time:2019/3/26  下午3:49 
* @Description: info
**/  
public class JedisPoolUtil {
    private static JedisPool jedisPool;

    private ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<>();

    private static JedisPoolUtil poolUtil = new JedisPoolUtil();

    private JedisPoolUtil(){
        jedisPool = new JedisPool(new GenericObjectPoolConfig(),
                "localhost",6379,5000,false);
    }

    public static JedisPoolUtil getInstance(){
        return poolUtil;
    }

    public Jedis getJedis(){
        Jedis resource = jedisThreadLocal.get();
        if (resource != null){
            return resource;
        }
        resource = jedisPool.getResource();
        jedisThreadLocal.set(resource);
        return resource;
    }

    public void close(){
        Jedis resource = jedisThreadLocal.get();
        if (resource != null){
            resource.close();
            jedisThreadLocal.remove();
        }
    }
}

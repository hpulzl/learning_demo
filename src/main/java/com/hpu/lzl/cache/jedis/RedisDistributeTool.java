package com.hpu.lzl.cache.jedis;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
*   
* @author:awo  
* @time:2019/3/25  下午2:59 
* @Description: info
**/  
public class RedisDistributeTool {

    private Jedis jedis;
    // key的过期时间:s
    private Long expireTime = 5L;
    // 获取锁的超时时间:ms
    private Long timeOut = 15L * 1000;

    private boolean lock = false;

    private String OK = "ok";

    // 分布式锁的key值
    private String lockKey;
    // 分布式锁的value值
    private String lockValue;

    public static final String UNLOCK_LUA;

    private JedisPoolUtil jedisPoolUtil;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    public RedisDistributeTool(String keyLock){
        this.lockKey = keyLock;
        jedisPoolUtil = JedisPoolUtil.getInstance();
    }

    public RedisDistributeTool(Long expireTime,Long timeOut,String keyLock){
        this.expireTime = expireTime;
        this.timeOut = timeOut;
        this.lockKey = keyLock;
        jedisPoolUtil = JedisPoolUtil.getInstance();
    }

    /**
     * 使用setnx，保证set数据设置expire时间的原子性操作
     * 阻塞式,设置获取锁的超时时间
     * @return
     */
    public boolean tryLock(){
        Long currentTime = System.currentTimeMillis();
        lockValue = UUID.randomUUID().toString();
        while (System.currentTimeMillis() - currentTime < timeOut){
            jedis = jedisPoolUtil.getJedis();
            try {
                if (OK.equalsIgnoreCase(jedis.set(lockKey,lockValue,"nx","ex",expireTime))){
                    lock = true;
                    return lock;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                jedisPoolUtil.close();
            }
        }
        System.out.println(Thread.currentThread().getName() + " 获取锁超时");
        return false;
    }

    /**
     * 只尝试获取一次
     * @return
     */
    public boolean tryGetLock(){
        try {
            lockValue = UUID.randomUUID().toString();
            jedis = jedisPoolUtil.getJedis();
            if (OK.equalsIgnoreCase(jedis.set(lockKey,lockValue,"nx","ex",expireTime))){
                lock = true;
                return lock;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            jedisPoolUtil.close();
        }
        return false;
    }

    /**
     * 使用lua脚本删除key：
     * 1. 校验value值的一致性
     * 2. 如果value一致，则删除key
     */
    public boolean unLock(){
        try {
            if (lock){
                List<String> keys = new ArrayList<>();
                keys.add(lockKey);
                List<String> values = new ArrayList<>();
                values.add(lockValue);
                jedis = jedisPoolUtil.getJedis();
                Long eval = (Long) jedis.eval(UNLOCK_LUA, keys, values);
                // eval == 0 表示锁没有释放
                lock = eval == 0;
                return eval != 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedisPoolUtil.close();
        }
        return false;
    }
}

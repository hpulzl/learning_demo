package com.hpu.lzl.cache.jedis;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
*   
* @author:awo  
* @time:2019/3/26  下午2:39 
* @Description: info
**/  
public class RedissonRedLockDemo {
    public static void main(String[] args) {
        RedissonClient redissonClient = Redisson.create();
        RLock rLock1 = redissonClient.getLock("redisson:lock:user1");
        RLock rLock2 = redissonClient.getLock("redisson:lock:user2");
        RLock rLock3 = redissonClient.getLock("redisson:lock:user3");
        RedissonRedLock redissonRedLock = new RedissonRedLock(rLock1,rLock2,rLock3);
        try {
            if (redissonRedLock.tryLock()){
                // do something
                System.out.println("do something");
            }
        } finally {
            redissonRedLock.unlock();
        }

        try {
            if (redissonRedLock.tryLock(5,10, TimeUnit.SECONDS)){
                System.out.println("do something");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            redissonRedLock.unlock();
        }
    }
}

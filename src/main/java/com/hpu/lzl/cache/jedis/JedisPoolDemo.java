package com.hpu.lzl.cache.jedis;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
*   
* @author:awo  
* @time:2019/3/25  下午6:19 
* @Description: info
**/  
public class JedisPoolDemo {
    /**
     * 多线程操作单个jedis实例会出现socket closed的错误。是因为jedis是线程不安全的。
     * 可以使用jedisPool来获取多个jedis实例
     * https://my.oschina.net/u/2474629/blog/916684
     */
    public static void main(String[] args) throws InterruptedException {
        RedissonClient redissonClient = Redisson.create();
        RLock rLock = redissonClient.getLock("redisson:lock:user");
        CountDownLatch countDownLatch = new CountDownLatch(5);
        TryLockRunnable runnable = new TryLockRunnable(countDownLatch);
//        RedLockRunnable runnable = new RedLockRunnable(countDownLatch,rLock);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,5,30000, TimeUnit.SECONDS,new ArrayBlockingQueue<>(5));
        for (int i=0;i<5;i++){
            executor.execute(runnable);
        }
        countDownLatch.await();
        executor.shutdown();
    }

    static class TryGetLockRunnable implements Runnable {

        @Override
        public void run() {
            RedisDistributeTool distributeTool = new RedisDistributeTool("lock:test:user");
            try {
                if (distributeTool.tryGetLock()){
                    System.out.println(Thread.currentThread().getName() + "==>获取锁");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("do something...");
                }else {
                    System.out.println(Thread.currentThread().getName() + "==>未获取锁");
                }
            } finally {
                distributeTool.unLock();
                System.out.println(Thread.currentThread().getName() + "==>释放锁");
            }
        }
    }

    static class RedLockRunnable implements Runnable {
        private CountDownLatch countDownLatch;
        private RLock[] rLocks;

        public RedLockRunnable(CountDownLatch countDownLatch,RLock... rLocks){
            this.countDownLatch = countDownLatch;
            this.rLocks = rLocks;
        }

        @Override
        public void run() {
            RedissonRedLock redissonRedLock = new RedissonRedLock(rLocks);
            try {
                // 尝试获取锁，没有手动解锁的话，30s后超时
                if (redissonRedLock.tryLock(30,TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread().getName() + "==>获取锁");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("do something...");
                }else {
                    System.out.println(Thread.currentThread().getName() + "==>未获取锁");
                }
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                redissonRedLock.unlock();
                System.out.println(Thread.currentThread().getName() + "==>释放锁 ");
            }
        }
    }

    static class TryLockRunnable implements Runnable {
        private CountDownLatch countDownLatch;

        public TryLockRunnable(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            RedisDistributeTool distributeTool = new RedisDistributeTool("redis:test:user");
            try {
                if (distributeTool.tryLock()){
                    System.out.println(Thread.currentThread().getName() + "==>获取锁");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("do something...");
                }else {
                    System.out.println(Thread.currentThread().getName() + "==>未获取锁");
                }
            } finally {
                boolean b = distributeTool.unLock();
                System.out.println(Thread.currentThread().getName() + "==>释放锁 " + b);
                countDownLatch.countDown();
                System.out.println(" count " + countDownLatch.getCount());
            }
        }
    }
}

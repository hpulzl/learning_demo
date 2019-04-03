package com.hpu.lzl.cache.jedis;

import com.alibaba.fastjson.JSON;
import com.hpu.lzl.model.User;
import redis.clients.jedis.*;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
*   
* @author:awo  
* @time:2019/3/25  上午10:19 
* @Description: info
**/  
public class JedisDemo {
    private static Jedis jedis;
    private static String key_prefix =  "redis:test:";

    public static void main(String[] args) {
//        workOne();
//        workTwo();
//        redisQueue();
//        hyperLogLogDemo();
//        userView();
//        batchSet();
//        scan();
        for (int i=0;i<10;i++){
            new Thread(() -> transactionDemo()).start();
        }
    }

    public static void transactionDemo(){
        String key = "account:user";
        Jedis jedis = JedisPoolUtil.getInstance().getJedis();
        while (true){
            // 事务开启前，关注某个key的变化。防止并发修改
            jedis.watch(key);
            Transaction tx = jedis.multi();
            tx.incr(key);
            List<Object> exec = tx.exec();
            if (exec != null){
                break;
            }
            System.out.println("key " + key + " 并发修改...");
        }
        jedis.close();
    }
    public static void workTwo(){
        try {
            getJedis();
            String lockResult = jedis.set("lock:user:a", "true", "nx", "ex", 30L);
            System.out.println("lock set nx " +lockResult);
            Long ttl = jedis.ttl("lock:user:a");
            System.out.println("lock ttl " + ttl);
        } finally {
            close();
        }

    }

    public static void workOne(){
        User user = new User();
        user.setUid("101310");
        user.setPassword("aaabbb");
        user.setUserName("lzl");
        try {
            getJedis();
            String result = jedis.set(key_prefix + user.getUid(), JSON.toJSONString(user));
            System.out.println("redis set result " + result);
            String getResult = jedis.get(user.getUid());
            System.out.println("redis get result " + getResult);
        } finally {
            close();
        }
    }

    public static void redisQueue() {
        getJedis();
        String notifyQueue = "notify:queue";
        jedis.lpush(notifyQueue,"a");
        jedis.lpush(notifyQueue,"b");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,5,3000, TimeUnit.SECONDS,new ArrayBlockingQueue<>(5));

        for (int i=0;i<5;i++){
            executor.execute(new QueueRunnable());
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

    }

    /**
     * 提供不精确的去重计数功能
     * 场景：统计UV
     * pfadd(存数据) key elements[...]
     * pfcount(统计个数) key
     * pfmerge(合并) destKey key1 key2...
     */
    public static void hyperLogLogDemo(){
        getJedis();
        String key = "uv:20190327:1";
        String str[] = {"user_1","user_3","user_2","user_4","user_5"};
        Long addResult = jedis.pfadd(key, str);
        System.out.println("pfadd result " + addResult);
        long pfcountResult = jedis.pfcount(key);
        System.out.println("pfcount result " + pfcountResult);
        jedis.close();
    }

    /**
     * 匹配满足要求的key
     */
    public static void scan(){
        getJedis();
        ScanParams params = new ScanParams();
        params.match("key:9*");
        int cursor = 0;
        do{
            ScanResult<String> scan = jedis.scan(cursor + "", params);
            cursor = Integer.parseInt(scan.getStringCursor());
            System.out.println("游标:" + cursor + " 获取的数据:" + scan.getResult());
        }while (cursor != 0);
    }

    public static void batchSet(){
        getJedis();
        for (int i=0;i<10000;i++){
            jedis.set("key:" + i,i+"");
        }
        jedis.close();
    }

    public static void userView(){
        getJedis();
        for (int i=0;i<10;i++){
            boolean actionAllowed = isActionAllowed(jedis, "hist:userA", 60, 5);
            System.out.println("是否可以访问:" + actionAllowed);
        }
        jedis.close();
    }

    /**
     * 使用zadd和zremrangebyscore来做一个接口的简单限流工具
     * 控制接口actionKey在time秒内最大访问数maxCount
     */
    public static boolean isActionAllowed(Jedis jedis,String userActionKey,int time,int maxCount){
        long currentTime = System.currentTimeMillis();
        // 接口访问添加数据
        jedis.zadd(userActionKey,currentTime,""+currentTime);
        // 清除掉time分钟前的记录数
        jedis.zremrangeByScore(userActionKey, 0, currentTime - time * 1000);
        Long viewCount = jedis.zcard(userActionKey);
        return viewCount < maxCount;
    }

    static class QueueRunnable implements Runnable{

        @Override
        public void run() {
            getJedis();
            String lpop = jedis.lpop("notify:queue");
            System.out.println(Thread.currentThread().getName() + " pop value " + lpop);
        }
    }

    public static void getJedis(){
        jedis = new Jedis("127.0.0.1",6379,0);

    }

    public static void close(){
        if (jedis != null){
            jedis.close();
        }
    }
}

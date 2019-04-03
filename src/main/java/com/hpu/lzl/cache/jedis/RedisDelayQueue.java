package com.hpu.lzl.cache.jedis;

import com.alibaba.fastjson.JSON;
import com.hpu.lzl.model.TaskItem;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
*   
* @author:awo  
* @time:2019/3/26  下午3:48 
* @Description: 基于zadd的redis延时队列
**/  
public class RedisDelayQueue {

    private JedisPoolUtil jedisPoolUtil;

    private String delayKey;

    private long DEFAULT_DELAY_TIME = 5000;

    public static void main(String[] args) {
        RedisDelayQueue delayQueue = new RedisDelayQueue("delay:queue");

        Thread customer = new Thread(() -> {
            for (int i=0;i<10;i++){
                TaskItem taskItem = new TaskItem();
                taskItem.setTaskId(UUID.randomUUID().toString());
                taskItem.setContent("content " + i);
                taskItem.setExtra("extra " + i);
                delayQueue.pushDelayTask(taskItem);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread producer = new Thread(() -> delayQueue.popTaskLoop());

        Thread producer2 = new Thread(() -> delayQueue.popTaskLoopLua());

        customer.start();
        producer.start();
        producer2.start();
    }

    public RedisDelayQueue(String delayKey) {
        jedisPoolUtil = JedisPoolUtil.getInstance();
        this.delayKey = delayKey;
    }

    public boolean pushDelayTask(TaskItem taskItem,long delayTime){
        Jedis jedis;
        try {
            jedis = jedisPoolUtil.getJedis();
            String strJson = JSON.toJSONString(taskItem);
            // 设置延时队列延时的时间
            Long timeToDelay = System.currentTimeMillis() + delayTime;
            return jedis.zadd(delayKey,timeToDelay,strJson) == 1;
        } finally {
            jedisPoolUtil.close();
        }
    }

    public boolean pushDelayTask(TaskItem taskItem){
        return pushDelayTask(taskItem,DEFAULT_DELAY_TIME);
    }

    public void popTaskLoop(){
        Jedis jedis;
        try {
            jedis = jedisPoolUtil.getJedis();
            while (true){
                Set<String> strings = jedis.zrangeByScore(delayKey, 0, System.currentTimeMillis(),0,1);
                if (strings.isEmpty()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                String s = strings.iterator().next();
                // 它的返回值决定了当前实例有没有抢到任务，因为 loop 方法可能会被多个线程、多个进程调用，
                // 同一个任务可能会被多个进程线程抢到，通过 zrem 来决定唯一的属主
                if (jedis.zrem(delayKey,s) > 0){
                    System.out.println(Thread.currentThread().getName() + " 从延迟队列中取出数据:" +s);
                }
            }
        } finally {
            jedisPoolUtil.close();
        }
    }

    /**
     * lua操作保证 zrangebyscore和zrem是原子性操作。
     * 避免多线程情况下zrem获取失败导致资源浪费。
     */
    public void popTaskLoopLua(){
        String delayPopScript = " local resultDelayMsg = {}; " +
                                " local arr = redis.call('zrangebyscore', KEYS[1], '0', ARGV[1]) ; " +
                                " if next(arr) == nil then return resultDelayMsg  end ;" +
                                " if redis.call('zrem', KEYS[1], arr[1]) > 0 then table.insert(resultDelayMsg, arr[1]) " +
                                "return resultDelayMsg end ; " +
                                " return resultDelayMsg ; ";
        Jedis jedis;
        try {
            jedis = jedisPoolUtil.getJedis();
            while (true){
                List<String> result = (List<String>) jedis.eval(delayPopScript,
                        Collections.singletonList(delayKey),
                        Collections.singletonList("" + System.currentTimeMillis()));
                if (result == null || result.isEmpty()){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                System.out.println("lua 脚本实现:" + Thread.currentThread().getName() + " 从队列中取出数据:" + result.get(0));
            }
        } finally {
            jedisPoolUtil.close();
        }
    }
}

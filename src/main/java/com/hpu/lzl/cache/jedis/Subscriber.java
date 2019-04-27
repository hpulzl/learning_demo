package com.hpu.lzl.cache.jedis;

import redis.clients.jedis.JedisPubSub;

/**
*   
* @author:awo  
* @time:2019/4/20  下午12:29 
* @Description: info
**/  
public class Subscriber extends JedisPubSub{

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("channel " + channel + " message " + message);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("pattern " + pattern + " subscribedChannels " + subscribedChannels);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("pattern " + pattern + " subscribedChannels " + " channel " + channel);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("pattern " + pattern + " subscribedChannels " + subscribedChannels);
    }
}

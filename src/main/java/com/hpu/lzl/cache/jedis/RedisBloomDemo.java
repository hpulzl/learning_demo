package com.hpu.lzl.cache.jedis;

import com.alibaba.fastjson.JSON;
import io.rebloom.client.Client;

/**
*   
* @author:awo  
* @time:2019/4/27  下午5:32 
* @Description: info
**/  
public class RedisBloomDemo {
    public static void main(String[] args) {
        String userIdBloomKey = "userid1";
        // 创建客户端，jedis实例
        Client client = new Client("localhost", 6379);
        client.delete(userIdBloomKey);
        // 创建一个有初始值和出错率的过滤器
        client.createFilter(userIdBloomKey,100000,0.01);
        // 新增一个<key,value>
        boolean userid1 = client.add(userIdBloomKey,"101310222");
        System.out.println("userid1 add " + userid1);

        // 批量新增values
        boolean[] booleans = client.addMulti(userIdBloomKey, "101310111", "101310222", "101310222");
        System.out.println("add multi result " + JSON.toJSONString(booleans));

        // 某个value是否存在
        boolean exists = client.exists(userIdBloomKey, "101310111");
        System.out.println("101310111 是否存在" + exists);

        //某批value是否存在
        boolean existsBoolean[] = client.existsMulti(userIdBloomKey, "101310111","101310222", "101310222","11111111");
        System.out.println("某批value是否存在 " + JSON.toJSONString(existsBoolean));

    }
}

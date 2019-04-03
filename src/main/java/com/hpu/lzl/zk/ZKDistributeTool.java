package com.hpu.lzl.zk;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
*   
* @author:awo  
* @time:2019/3/30  下午5:18 
* @Description: info
**/  
public class ZKDistributeTool {

    private ZkClient zkClient;
    private String lockName;
    private long timeOut = 3000;


    public ZKDistributeTool(String lockName,long timOut){
        zkClient = ZookeeperClient.createClientByZKClient();
        this.lockName = lockName;
        this.timeOut = timOut;
    }

    public boolean tryLock(){
        // 创建临时顺序节点
        if (!zkClient.exists(lockName)){
            if (zkClient.create(lockName,lockName, CreateMode.EPHEMERAL_SEQUENTIAL) != null){
                List<String> children = zkClient.getChildren("/demo");
                System.out.println(children);
                return true;
            }
        }else {
            List<String> children = zkClient.getChildren(lockName);
            System.out.println(children);
        }
        return false;
    }

    public void unLock(){
        zkClient.delete(lockName);
        zkClient.close();
    }


    public static void main(String[] args) {
        ZKDistributeTool lock = new ZKDistributeTool("/lock", 3000);
        lock.tryLock();
    }
}

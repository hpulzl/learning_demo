package com.hpu.lzl.zk;

import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
*   
* @author:awo  
* @time:2018/9/10  下午4:10 
* @Description: info
**/  
public class ZookeeperClient {
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final static String ROOT_PATH ="/demo";
    private static final String ZK_URL="127.0.0.1:2181";
    public static ZooKeeper createClient(){
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(ZK_URL,
                    5000, new WatchedEvent());
            countDownLatch.await();
            System.out.println("=====建立连接 end=====");
            Stat stat = zooKeeper.exists(ROOT_PATH,false);
            if (stat == null){
                zooKeeper.create(ROOT_PATH,"".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (KeeperException e) {
            e.printStackTrace();
        }

        return zooKeeper;
    }

    public static ZkClient createClientByZKClient(){
        ZkClient zkClient = new ZkClient(ZK_URL,5000);
        if (!zkClient.exists(ROOT_PATH)){
            zkClient.create(ROOT_PATH,null,CreateMode.PERSISTENT);
        }

        return zkClient;
    }

    public static CuratorFramework createCuratorClient(){

          CuratorFramework framework = CuratorFrameworkFactory.builder().connectString(ZK_URL)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                  .connectionTimeoutMs(1000).sessionTimeoutMs(1000).build();

          framework.start();

          return framework;
    }

    static class WatchedEvent implements Watcher{

        @Override
        public void process(org.apache.zookeeper.WatchedEvent watchedEvent) {
            if (watchedEvent.getState().equals(Watcher.Event.KeeperState.SyncConnected)){
                countDownLatch.countDown();
            }
        }
    }
}

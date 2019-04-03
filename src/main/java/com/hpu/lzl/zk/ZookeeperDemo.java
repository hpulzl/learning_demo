package com.hpu.lzl.zk;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.*;

/**
*   
* @author:awo  
* @time:2018/9/10  下午4:05 
* @Description: info
**/  
public class ZookeeperDemo {

    public static void main(String[] args) {
//        try {
//            zkCreateNodes();
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        zkClientCreateNodes();
    }

    /**
     * PERSISTENT 持久节点
     * PERSISTENT_SEQUENTIAL 持久的顺序节点
     * EPHEMERAL 临时节点
     * EPHEMERAL_SEQUENTIAL 临时的顺序节点
     *
     * ZooDefs.Ids.OPEN_ACL_UNSAFE 完全开放
     * ZooDefs.Ids.CREATOR_ALL_ACL 给创建znode连接所有权限
     * ZooDefs.Ids.READ_ACL_UNSAFE 所有客户都可读
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static void zkCreateNodes() throws KeeperException, InterruptedException {
        ZooKeeper zooKeeper = ZookeeperClient.createClient();
        //同步创建zk临时节点
        String nodeName = zooKeeper.create("/EPHEMERAL","".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("节点名称:" + nodeName);
        //异步创建zk临时节点
        zooKeeper.create("/SYNC_EPHEMERAL", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                (rc, path, ctx, name) -> {
                    System.out.println("状态:"+ rc +"异步创建临时节点:"+ ctx.toString()+ "路径:" + path + " 名称:" + name);
                },"传递内容");
        //睡一段时间，等待异步操作完成
        Thread.sleep(2000);
    }

    public static void zkClientCreateNodes(){
        ZkClient zkClient = ZookeeperClient.createClientByZKClient();
        String node = zkClient.create("/zkClient","hello world",CreateMode.EPHEMERAL);

        String data = zkClient.readData("/zkClient");

        System.out.println("node " + node + " data " + data);
    }
}

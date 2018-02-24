package io.antfs.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * generate ZkClient
 * @author gris.wang
 * @since 2017/11/21
 **/
public class ZkClient {

    /**
     * default session timeout
     * curator will trigger CHILD_REMOVED event only when the node is timeout or down
     */
    private static final int DEFAULT_SESSION_TIMEOUT_MS = 5000;

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 15000;

    /**
     * the clients
     */
    private static Map<String,CuratorFramework> clients;

    private static Lock lock;

    static{
        clients = new ConcurrentHashMap<>();
        lock = new ReentrantLock();
    }

    /**
     * get ZkClient
     * @param zkServerAddress the zk server address
     * @return the ZkClient
     */
    public static CuratorFramework getClient(String zkServerAddress){
        if(zkServerAddress == null || zkServerAddress.trim().length() == 0){
            return null;
        }
        CuratorFramework client = clients.get(zkServerAddress);
        if(client==null){
            lock.lock();
            try {
                if(!clients.containsKey(zkServerAddress)) {
                    client = CuratorFrameworkFactory.newClient(
                            zkServerAddress,
                            DEFAULT_SESSION_TIMEOUT_MS,
                            DEFAULT_CONNECTION_TIMEOUT_MS,
                            new RetryNTimes(10, 5000)
                    );
                    client.start();
                    clients.putIfAbsent(zkServerAddress,client);
                }else{
                    client = clients.get(zkServerAddress);
                }
            }finally {
                lock.unlock();
            }
        }
        return client;

    }

}

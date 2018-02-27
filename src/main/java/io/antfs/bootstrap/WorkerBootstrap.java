package io.antfs.bootstrap;

import com.xiaoleilu.hutool.util.NumberUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import io.antfs.colony.node.Node;
import io.antfs.colony.worker.WorkerServer;
import io.antfs.warehouse.register.DefaultRegister;
import io.antfs.zk.ZkServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkerBootstrap
 * @author gris.wang
 * @since 2017/11/20
 **/
public class WorkerBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerBootstrap.class);
    
    public static void main(String[] args) {
        if(args.length>0 && StrUtil.isNotBlank(args[0])){
            ZkServer.setZkAddressFromArgs(args[0]);
        }
        String zkServerAddress = ZkServer.getZkAddress();
        if(StrUtil.isBlank(zkServerAddress)){
            LOGGER.error("zkServerAddress is blank please check file={%s}",ZkServer.ZOOKEEPER_ADDRESS_CFG);
            System.exit(1);
        }

        Node node = Node.DEFAULT_PORT_NODE;
        if(args.length>1 && NumberUtil.isInteger(args[1])){
            node = new Node(Integer.parseInt(args[1]));
        }

        // register current worker node to zk
        DefaultRegister.create(zkServerAddress).register(node);

        // start the worker server
        WorkerServer workerServer = new WorkerServer();
        workerServer.start(node);
    }

}

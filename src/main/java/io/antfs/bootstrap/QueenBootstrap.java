package io.antfs.bootstrap;

import com.xiaoleilu.hutool.util.StrUtil;
import io.antfs.colony.node.Node;
import io.antfs.colony.queen.QueenServer;
import io.antfs.common.Constants;
import io.antfs.warehouse.register.DefaultRegister;
import io.antfs.zk.ZkNode;
import io.antfs.zk.ZkServer;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueenBootstrap
 * @author gris.wang
 * @since 2017/11/20
 **/
public class QueenBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueenBootstrap.class);
    
    public static void main(String[] args) {
        if(args.length>0 && StrUtil.isNotBlank(args[0])){
            ZkServer.setZkAddressFromArgs(args[0]);
        }
        String zkServerAddress = ZkServer.getZkAddress();
        if(StrUtil.isBlank(zkServerAddress)){
            LOGGER.error("zkServerAddress is blank please check file={}",ZkServer.ZOOKEEPER_ADDRESS_CFG);
            System.exit(1);
        }

        // register queen node to zk
//        DefaultRegister.create(zkServerAddress).register(new Node(Constants.QUEEN_PORT), ZkNode.QUEEN_PATH, CreateMode.EPHEMERAL);

        // start the queen server
        QueenServer queenServer = new QueenServer();
        queenServer.start();
    }

}

package io.antfs.zk;

import com.xiaoleilu.hutool.io.FileUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import org.apache.zookeeper.server.*;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * ZooKeeper Server
 * @author gris.wang
 * @since 2017/11/21
 *
 */
public class ZkServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZkServer.class);
	
	/**
	 * zk base directory
	 * used to store properties generated in runtime
	 */
	public static final String BASE_ZOOKEEPER_DIR = "/zookeeper/";

	/**
	 * store zk server address into this file
	 */
	public static final String ZOOKEEPER_ADDRESS_CFG = BASE_ZOOKEEPER_DIR+"zk_address.cfg";

	public static final String ZOOKEEPER_STANDALONE_PROPERTIES_FILE = BASE_ZOOKEEPER_DIR+"zk_standalone.properties";

	public static final String ZOOKEEPER_CLUSTER_PROPERTIES_FILE = BASE_ZOOKEEPER_DIR+"zk_cluster_server_%d.properties";

	/**
	 * zk server address
	 */
	private static String ZK_SERVER_ADDRESS;


	public static final String getZkServerAddress(){
		if(StrUtil.isBlank(ZK_SERVER_ADDRESS)){
			ZK_SERVER_ADDRESS = FileUtil.readUtf8String(new File(ZOOKEEPER_ADDRESS_CFG).getPath());
		}
		return ZK_SERVER_ADDRESS;
	}

	/**
	 * start zk in standalone mode
	 * @param zkPropertiesPath zk properties path
	 * @throws IOException
	 * @throws ConfigException
	 * @throws AdminServer.AdminServerException
	 */
	public void startStandalone(String zkPropertiesPath) throws IOException, ConfigException,AdminServer.AdminServerException {
		ServerConfig config = new ServerConfig();
		config.parse(zkPropertiesPath);
		startStandalone(config);
	}

	/**
	 * start zk in standalone mode
	 * @param config the ServerConfig
	 * @throws IOException
	 * @throws AdminServer.AdminServerException
	 */
	public void startStandalone(ServerConfig config) throws IOException,AdminServer.AdminServerException {
		ZooKeeperServerMain main = new ZooKeeperServerMain();
		LOGGER.info("ZkServer start with clientPortAddress={}", config.getClientPortAddress());
		main.runFromConfig(config);
	}

	/**
	 * start zk in cluster mode
	 * @param zkPropertiesPath zk properties path
	 * @throws IOException
	 * @throws ConfigException
	 */
	public void startCluster(String zkPropertiesPath) throws IOException, ConfigException{
		QuorumPeerConfig config = new QuorumPeerConfig();
		config.parse(zkPropertiesPath);
		startFakeCluster(config);
	}

	/**
	 * start zk in a fake cluster mode
	 * @param config the QuorumPeerConfig
	 * @throws IOException
	 */
	public void startFakeCluster(QuorumPeerConfig config) throws IOException{

		ServerCnxnFactory cnxnFactory = new NIOServerCnxnFactory();
		cnxnFactory.configure(config.getClientPortAddress(), config.getMaxClientCnxns());

		QuorumPeer quorumPeer = new QuorumPeer(config.getServers(), config.getDataDir(), config.getDataLogDir(), config.getElectionAlg(), config.getServerId(), config.getTickTime(), config.getInitLimit(), config.getSyncLimit(), config.getQuorumListenOnAllIPs(), cnxnFactory, config.getQuorumVerifier());
		quorumPeer.setClientAddress(config.getClientPortAddress());
		quorumPeer.setTxnFactory(new FileTxnSnapLog(config.getDataLogDir(), config.getDataDir()));
		quorumPeer.setElectionType(config.getElectionAlg());
		quorumPeer.setMyid(config.getServerId());
		quorumPeer.setTickTime(config.getTickTime());
		quorumPeer.setMinSessionTimeout(config.getMinSessionTimeout());
		quorumPeer.setMaxSessionTimeout(config.getMaxSessionTimeout());
		quorumPeer.setInitLimit(config.getInitLimit());
		quorumPeer.setSyncLimit(config.getSyncLimit());
		quorumPeer.setQuorumVerifier(config.getQuorumVerifier(), true);
		quorumPeer.setCnxnFactory(cnxnFactory);
		quorumPeer.setZKDatabase(new ZKDatabase(quorumPeer.getTxnFactory()));
		quorumPeer.setLearnerType(config.getPeerType());
		quorumPeer.setSyncEnabled(config.getSyncEnabled());
		quorumPeer.setQuorumListenOnAllIPs(config.getQuorumListenOnAllIPs());

		LOGGER.info("ZkServerCluster start with clientPortAddress={}", config.getClientPortAddress());
		quorumPeer.start();
	}

	/**
	 * start zk in a true cluster mode
	 * will execute quorumPeer.join();
	 * need to start in different servers
	 * @param config the QuorumPeerConfig
	 * @throws IOException
	 * @throws AdminServer.AdminServerException
	 */
	public void startCluster(QuorumPeerConfig config) throws IOException, AdminServer.AdminServerException {
		QuorumPeerMain main = new QuorumPeerMain();
		main.runFromConfig(config);
	}


}
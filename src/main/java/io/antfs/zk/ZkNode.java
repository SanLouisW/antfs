package io.antfs.zk;

/**
 * @author gris.wang
 * @since 2017/11/21
 **/
public class ZkNode {


    /** root path where worker nodes used to register */
    public static final String ROOT_PATH = "/root";

    /** the path where queen node registered to */
    public static final String QUEEN_PATH = "/queen";

    /** the path where worker node registered to */
    public static final String WORKER_PATH = ROOT_PATH + "/worker";

}

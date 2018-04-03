package io.antfs.colony.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.crypto.SecureUtil;
import com.xiaoleilu.hutool.util.NetUtil;

/**
 * A node represents a server
 * a queen ant is a node as well as a worker ant
 * @author gris.wang
 * @since 2017/11/20
 **/
public class Node {

    private static final String DEFAULT_HOST = NetUtil.getLocalhostStr();

    private static final int DEFAULT_PORT = 8088;

    public static final Node DEFAULT_PORT_NODE = new Node(DEFAULT_HOST,DEFAULT_PORT);

    private String id;

    private String host;

    private int port;

    public Node(int port){
        this(DEFAULT_HOST,port);
    }

    public Node(String host, int port){
        this(SecureUtil.md5(host+"&"+port),host,port);
    }

    public Node(String id, String host, int port){
        this.id = id;
        this.host = host;
        this.port = port;
    }

    /**
     * parse Node from JSONObject
     * @param object the jsonObject
     * @return the Node parsed from jsonObject
     */
    public static Node parse(JSONObject object){
        if(object==null){
            return null;
        }
        String host = object.getString("host");
        int port = object.getIntValue("port");
        String id = object.getString("id");
        return new Node(id,host,port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId(){
        return id;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static class CurrentNode {
        private static volatile Node current;

        public static void set(Node node){
            Node tmpNode = current;
            if(tmpNode==null){
                synchronized (CurrentNode.class){
                    tmpNode = current;
                    if(tmpNode==null){
                        tmpNode = current = node;
                    }
                }
            }
        }

        public static Node get(){
            return current;
        }
    }


}

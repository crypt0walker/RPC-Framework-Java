package com.async.rpc.server.server;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */
public interface RpcServer {
    //开启监听
    void start(int port);
    void stop();
}

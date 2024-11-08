package com.async.rpc.server;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.common.server.impl.UserServiceImpl;
import com.async.rpc.server.provider.ServiceProvider;
import com.async.rpc.server.server.impl.NettyRpcServerImpl;
import com.async.rpc.server.server.RpcServer;

/**
 * @program: simple_RPC
 *
 * @description: netty测试类
 **/
public class TestNettyServer {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer = new NettyRpcServerImpl(serviceProvider);
        rpcServer.start(9999);
    }
}
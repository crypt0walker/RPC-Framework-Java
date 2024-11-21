package com.async.rpc.server;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import com.async.rpc.common.server.UserService;
import com.async.rpc.common.server.impl.UserServiceImpl;
import com.async.rpc.server.provider.ServiceProvider;
import com.async.rpc.server.server.RpcServer;
import com.async.rpc.server.server.impl.NettyRpcServerImpl;

/**
 * @program: simple_RPC
 *
 * @description: 使用ZK注册中心的测试类
 **/
public class TestZKServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider("127.0.0.1",9999);
        serviceProvider.provideServiceInterface(userService,true);

        RpcServer rpcServer=new NettyRpcServerImpl(serviceProvider);
        rpcServer.start(9999);
    }
}

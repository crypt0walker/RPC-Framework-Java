package com.async.rpc.server;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/4
 */

import com.async.rpc.common.server.UserService;
import com.async.rpc.common.server.impl.UserServiceImpl;
import com.async.rpc.server.impl.SimpleRpcServerImpl;
import com.async.rpc.server.provider.ServiceProvider;

/**
 * @program: simple_RPC
 *
 * @description: 服务端功能测试
 **/
public class TestServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new SimpleRpcServerImpl(serviceProvider);
        rpcServer.start(9999);
    }
}

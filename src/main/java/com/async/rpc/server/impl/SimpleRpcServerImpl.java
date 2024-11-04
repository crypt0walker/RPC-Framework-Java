package com.async.rpc.server.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import com.async.rpc.server.RpcServer;
import com.async.rpc.server.provider.ServiceProvider;
import com.async.rpc.server.work.WorkThread;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: simple_RPC
 *
 * @description: 简单的RPC Server接口实现
 **/
@AllArgsConstructor
public class SimpleRpcServerImpl implements RpcServer {
    private ServiceProvider serviceProvide;
    @Override
    public void start(int port) {
        try {
            ServerSocket serverSocket=new ServerSocket(port);
            System.out.println("服务器启动了");
            while (true) {
                //如果没有连接，会堵塞在这里
                Socket socket = serverSocket.accept();
                //有连接，创建一个新的线程执行处理
                new Thread(new WorkThread(socket,serviceProvide)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }
}

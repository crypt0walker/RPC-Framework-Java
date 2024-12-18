package com.async.rpc.client.rpcClient.impl;

import com.async.rpc.client.rpcClient.RpcClient;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */
/**
 * @program: simple_RPC
 *
 * @description: 使用Socket的发送方法实现类，替代原IOClient功能
 **/
public class SimpleSocketRpcCilent implements RpcClient {
    private String host;
    private int port;
    public SimpleSocketRpcCilent(String host, int port){
        this.host=host;
        this.port=port;
    }
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            // 创建与服务端的 Socket 连接
            Socket socket = new Socket(host, port);
            // 创建输出流，用于发送请求对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // 创建输入流，用于接收响应对象
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 发送 RpcRequest 对象（写入缓冲区）
            oos.writeObject(request);
            oos.flush(); // 使缓冲区中数据发送
            // 从服务端接收 RpcResponse 对象，并进行类型转换
            RpcResponse response = (RpcResponse) ois.readObject();
            return response; // 返回响应
        } catch (IOException | ClassNotFoundException e) {
            // 捕获异常，打印堆栈跟踪信息
            e.printStackTrace();
            return null; // 返回 null 表示出错
        }
    }
}
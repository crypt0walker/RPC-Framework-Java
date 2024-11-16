package com.async.rpc.client.proxy;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import com.async.rpc.client.IOClient;
import com.async.rpc.client.rpcClient.RpcClient;
import com.async.rpc.client.rpcClient.impl.NettyRpcClient;
import com.async.rpc.client.rpcClient.impl.SimpleSocketRpcCilent;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @program: simple_RPC
 *
 * @description: 客户端动态代理
 **/
@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
//    // 服务器主机地址
//    private String host;
//    // 服务器端口
//    private int port;
    /* 非netty版本：直接用java原生
    public ClientProxy(String host,int port){
        rpcClient=new NettyRpcClient(host,port);
    }
     */
    /* Netty版本（非zookeeper）:采用写死的端口号和ip
    private RpcClient rpcClient;
    public ClientProxy(String host,int port,int choose){
        switch (choose){
            case 0:
                rpcClient=new NettyRpcClient(host,port);
                break;
            case 1:
                rpcClient=new SimpleSocketRpcCilent(host,port);
        }
    }
     */
    private RpcClient rpcClient;
    public ClientProxy(){
        rpcClient=new NettyRpcClient();
    }


    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        //数据传输
        RpcResponse response= rpcClient.sendRequest(request);
        return response.getData();
    }
    // 创建代理实例的方法
    public <T> T getProxy(Class<T> clazz) {
        // 使用Proxy.newProxyInstance创建一个代理实例
        // clazz.getClassLoader()：获取传入接口的类加载器
        // new Class[]{clazz}：指定代理的接口类型
        // this：当前ClientProxy实例作为InvocationHandler
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);

        // 将Object类型的代理实例强制转换为泛型T并返回
        return (T)o; // 返回代理对象
    }
    /* 简单版本的PRC调用（非Netty）：Netty版本用了Rpcclent封装，可以选择用哪种——Netty还是Java原生
//    // JDK动态代理，每一次代理对象调用方法时，都会经过此方法进行增强
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        // 构建RpcRequest对象，封装请求信息
//        RpcRequest request = RpcRequest.builder()
//                .interfaceName(method.getDeclaringClass().getName()) // 获取接口的类名
//                .methodName(method.getName()) // 获取调用的方法名
//                .params(args) // 获取方法参数
//                .paramsType(method.getParameterTypes()) // 获取参数类型
//                .build();
//
//        // 调用IOClient发送请求到服务端，并接收响应
//        RpcResponse response = IOClient.sendRequest(host, port, request);
//        // 返回响应中的数据
//        return response.getData();
//    }
     */
}

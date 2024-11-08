package com.async.rpc.client.rpcClient.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.client.netty.nettyInitializer.NettyClientInitializer;
import com.async.rpc.client.rpcClient.RpcClient;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

/**
 * @program: simple_RPC
 *
 * @description: 使用netty的发送方法实现类
 **/
public class NettyRpcClient implements RpcClient {
    private String host;
    private int port;
    // Netty 的客户端启动引导类，用于配置客户端
    private static final Bootstrap bootstrap;
    // 事件循环组，用于处理网络事件
    //EventLoopGroup是Netty的channel包组件，用于处理所有IO操作的线程池组，提供包括NIO、epoll、BIO等不同模型。
    private static final EventLoopGroup eventLoopGroup;
    public NettyRpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }
    //netty客户端初始化
    static {
        // 创建一个 NIO 事件循环组
        eventLoopGroup = new NioEventLoopGroup();
        // 创建 Bootstrap 实例
        bootstrap = new Bootstrap();
        //将eventLoopGroup绑定到bootstrap，并且设定channel类型和netty客户端的处理器
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                //NettyClientInitializer这里 配置netty对消息的处理机制
                //即使用nettyClientInitializer来设置处理器链
                .handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            //创建一个channelFuture对象用于监控操作执行情况，代表这一个操作事件，sync方法表示阻塞直到connect完成
            ChannelFuture channelFuture  = bootstrap.connect(host, port).sync();
            //channel表示一个连接的单位，类似socket
            Channel channel = channelFuture.channel();
            // 通过channel发送数据
            channel.writeAndFlush(request);
            // sync() 等待通道关闭，确保数据完全发送
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在handlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            // 使用 AttributeKey 从通道的上下文中获取响应数据
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            // 获取存储在通道属性中的响应对象
            RpcResponse response = channel.attr(key).get();
            System.out.println(response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.async.rpc.server.server.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.server.netty.nettyInitializer.NettyServerInitializer;
import com.async.rpc.server.provider.ServiceProvider;
import com.async.rpc.server.server.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * @program: simple_RPC
 *
 * @description: netty类型的实现RpcServer接口的类
 **/
@AllArgsConstructor
public class NettyRpcServerImpl implements RpcServer {
    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("netty服务端启动了");
        try {
            //启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //初始化
            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    //NettyClientInitializer这里 配置netty对消息的处理机制
                    .childHandler(new NettyServerInitializer(serviceProvider));
            //同步堵塞
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();
            //死循环监听
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
}

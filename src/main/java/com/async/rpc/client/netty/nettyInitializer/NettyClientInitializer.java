package com.async.rpc.client.netty.nettyInitializer;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.client.netty.handler.NettyClientHandler;
import com.async.rpc.common.serializer.myCoder.MyDecoder;
import com.async.rpc.common.serializer.myCoder.MyEncoder;
import com.async.rpc.common.serializer.mySerializer.JsonSerializer;
import com.async.rpc.server.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.AllArgsConstructor;

/**
 * @program: simple_RPC
 *
 * @description: 配置netty对消息的处理机制，如编码器、解码器、消息格式等（设置handler）
 *
 **/

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;  // 服务提供者，用于注册和管理本地服务实例

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();  // 获取当前通道的处理器链（管道）

        // 添加解码器：将字节流解码为 RpcRequest 或 RpcResponse 对象
        pipeline.addLast(new MyDecoder());

        // 添加编码器：将 RpcRequest 或 RpcResponse 对象编码为字节流，使用 JsonSerializer 序列化
        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        // 添加客户端业务逻辑处理器：处理从服务端接收的响应
        pipeline.addLast(new NettyClientHandler());
    }
}

/* 原有使用Java原生序列化器，添加一个长度字段解码器，解决 TCP 粘包/拆包问题
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    // 初始化通道并配置处理器链
    protected void initChannel(SocketChannel ch) throws Exception {
        // 获取通道的管道，用于依次添加处理器
        ChannelPipeline pipeline = ch.pipeline();
        // 添加一个长度字段解码器，解决 TCP 粘包/拆包问题
        pipeline.addLast(
                //基于消息长度字段的解码器。它在每条消息的头部增加一个长度字段，接收方可以通过读取长度字段，知道需要读取的字节数。
                new LengthFieldBasedFrameDecoder(
                        Integer.MAX_VALUE, // 数据帧的最大长度，防止因数据过大导致内存溢出
                        0,                 // 长度字段的起始偏移量，0 表示从数据帧的开头开始
                        4,                 // 长度字段的长度，表示长度字段占用4个字节
                        0,                 // 长度调整量，不调整数据帧的长度
                        4                  // 跳过前4个字节，即长度字段的长度，不包含在数据帧内容中
                )
        );
        //计算当前待发送消息的长度，写入到前4个字节中
        // 添加一个长度字段前置器，在消息头部插入消息长度字段
        // 用于在发送时自动在消息头部添加4个字节的长度字段
        pipeline.addLast(new LengthFieldPrepender(4));

        // 使用 Java 序列化方式的编码器，将 Java 对象编码成字节流以便传输，netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                // 使用反射机制根据类名加载类
                return Class.forName(className);
            }
        }));
        // 添加客户端业务逻辑处理器，处理接收到的响应
        pipeline.addLast(new NettyClientHandler());
    }
}

 */
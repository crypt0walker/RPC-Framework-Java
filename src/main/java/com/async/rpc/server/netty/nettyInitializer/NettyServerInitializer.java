package com.async.rpc.server.netty.nettyInitializer;


import com.async.rpc.client.netty.handler.NettyClientHandler;
import com.async.rpc.common.serializer.myCoder.MyDecoder;
import com.async.rpc.common.serializer.myCoder.MyEncoder;
import com.async.rpc.common.serializer.mySerializer.JsonSerializer;
import com.async.rpc.server.netty.handler.NettyRPCServerHandler;
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
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;  // 服务提供者，用于注册和管理本地服务实例

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();  // 获取当前通道的处理器链

        // 添加编码器：将服务端生成的 RpcResponse 对象编码为字节流
        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        // 添加解码器：将接收到的字节流解码为 RpcRequest 对象
        pipeline.addLast(new MyDecoder());

        // 添加服务端业务逻辑处理器：处理客户端请求，调用本地服务并返回结果
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}

/* 原有利用Java原生序列化器，配合加入长度字段解决沾包问题
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //消息格式 【长度】【消息体】，解决沾包问题
        pipeline.addLast(
                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        //计算当前待发送消息的长度，写入到前4个字节中
        pipeline.addLast(new LengthFieldPrepender(4));

        //使用Java序列化方式，netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));

        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
 */

package com.async.rpc.common.serializer.myCoder;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import com.async.rpc.common.message.MessageType;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;
import com.async.rpc.common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * @program: simple_RPC
 *
 * @description: 自定义编码器
 **/
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    //MessageToByteEncoder--Netty提供的编码器基类，用于将消息对象编码为字节流。
    //其子类需要实现 encode() 方法完成具体编码逻辑。
    private Serializer serializer;
    @Override
    //Netty 在发送消息时会自动调用encode方法。
    //ctx：当前通道的上下文，用于管理通道相关的资源。
    //msg：要编码的消息对象（如 RpcRequest 或 RpcResponse）。
    //out：Netty 提供的 ByteBuf，用于存储编码后的字节数据。
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        //1.写入消息类型，用于区分是请求（RpcRequest）还是响应（RpcResponse）。
        // MessageType 是一个枚举，包含请求和响应的类型码。
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        else if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        //2.写入序列化方式，目的是写入序列化方式的类型码，用于客户端和服务端解析消息时知道使用何种序列化方式
        // out.writeShort：将序列化方式（2 字节）写入 ByteBuf。
        // serializer.getType()：返回当前序列化器的类型（例如：JSON、Protobuf）。
        out.writeShort(serializer.getType());
        // 得到序列化数组
        // serializer.serialize(msg)：将消息对象序列化为字节数组。
        byte[] serializeBytes = serializer.serialize(msg);
        //3.写入数据长度，用于接收端知道需要读取多少字节。
        // out.writeInt(): 将数据长度（4 字节）写入 ByteBuf。
        out.writeInt(serializeBytes.length);
        //4.写入实际的序列化数据，作为消息的主体内容。
        //out.writeBytes():将字节数组的内容逐字节写入 ByteBuf。
        out.writeBytes(serializeBytes);
    }
}
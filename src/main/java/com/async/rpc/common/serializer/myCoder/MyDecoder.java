package com.async.rpc.common.serializer.myCoder;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.async.rpc.common.message.MessageType;
import java.util.List;
import com.async.rpc.common.serializer.mySerializer.Serializer;
/**
 * @program: simple_RPC
 *
 * @description: 自定义解码器,按照自定义的消息格式解码数据
 **/
//ByteToMessageDecoder:Netty 提供的解码器基类，用于将字节流解析为 Java 对象。
public class MyDecoder extends ByteToMessageDecoder {
    //decode() 方法完成具体解码逻辑。
    @Override
    //channelHandlerContext：通道上下文，管理通道的生命周期和资源。
    //in：Netty 提供的 ByteBuf，表示接收到的数据缓冲区。
    //out：存储解码后的对象，解码完成后会传递给后续的处理器。
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //1.读取消息类型例如：REQUEST还是RESPONSE
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if(messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂不支持此种数据");
            return;
        }
        //2.读取序列化的方式&类型
        // 从缓冲区中读取消息类型（2 字节），判断是请求（RpcRequest）还是响应（RpcResponse）。
        short serializerType = in.readShort();
        // 通过消息类型，获取对应的序列化器（如 JSON、Protobuf）。
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null)
            throw new RuntimeException("不存在对应的序列化器");
        //3.读取序列化数组长度（4 字节），用于确定需要读取的字节数。
        // 如果长度信息不完整，ByteToMessageDecoder 会自动等待更多数据到达。
        int length = in.readInt();
        //4.读取序列化数组
        byte[] bytes=new byte[length];
        // 从缓冲区中读取指定长度的字节数据，作为序列化后的数据内容。
        // 将 length 个字节读取到 bytes 数组中。
        in.readBytes(bytes);
        // 使用对应的序列化器将字节数组解析为 Java 对象（如 RpcRequest 或 RpcResponse）。
        Object deserialize= serializer.deserialize(bytes, messageType);
        // 将解码后的对象添加到 out 列表中，供后续处理器使用。
        out.add(deserialize);
    }
}

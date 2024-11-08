package com.async.rpc.client.netty.handler;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * @program: simple_RPC
 *
 * @description: 指定netty对接收消息的处理方式
 **/
// 客户端业务逻辑处理器，用于接收和处理来自服务器的响应
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    // 当通道读取到数据时调用
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 使用 AttributeKey 将响应对象存储在通道的属性中，供后续获取
        // 接收到response, 给channel设计别名，让sendRequest里读取response
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        // 将服务器返回的响应对象存储在通道属性中
        ctx.channel().attr(key).set(response);
        // 关闭通道，表示处理完成
        ctx.channel().close();
    }
//// 捕获并处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常处理
        cause.printStackTrace();
        // 关闭通道，释放资源
        ctx.close();
    }
}
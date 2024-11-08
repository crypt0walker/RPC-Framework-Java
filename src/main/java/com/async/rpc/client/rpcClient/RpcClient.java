package com.async.rpc.client.rpcClient;

import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */
//定义使用Rpc发送消息的接口
public interface RpcClient {
    //发送消息的底层方法
    RpcResponse sendRequest(RpcRequest request);
}

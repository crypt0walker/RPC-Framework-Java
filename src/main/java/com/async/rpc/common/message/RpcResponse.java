package com.async.rpc.common.message;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: simple_RPC
 *
 * @description: Rpc的响应消息格式
 **/
//定义返回信息格式RpcResponse（类似http格式）
@Data
@Builder
public class RpcResponse implements Serializable {
    //状态码
    private int code;
    //状态信息
    private String message;
    //具体数据
    private Object data;
    //构造成功信息
    public static RpcResponse sussess(Object data){
        return RpcResponse.builder().code(200).data(data).build();
    }
    //构造失败信息
    public static RpcResponse fail(){
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}

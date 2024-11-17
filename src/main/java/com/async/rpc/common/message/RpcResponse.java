package com.async.rpc.common.message;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: simple_RPC
 *
 * @description: Rpc的响应消息格式
 **/
//定义返回信息格式RpcResponse（类似http格式）
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcResponse implements Serializable {
    //状态码
    private int code;
    //状态信息
    private String message;
    //具体数据
    private Object data;
    //更新：加入传输数据的类型，以便在自定义序列化器中解析
    private Class<?> dataType;
    //构造成功信息
    public static RpcResponse sussess(Object data) {
        RpcResponse response = RpcResponse.builder()
                .code(200)
                .data(data)
                //增加了数据类型后更新
                .dataType(data != null ? data.getClass() : null) // 设置返回数据类型
                .build();
        return response;
    }
    //构造失败信息
    public static RpcResponse fail(){
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}

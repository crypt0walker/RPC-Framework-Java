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
 * @description: Rpc的请求消息格式
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {
    //服务类名，客户端只知道接口
    private String interfaceName;
    //调用的方法名
    private String methodName;
    //参数列表
    private Object[] params;
    //参数类型
    private Class<?>[] paramsType;
}

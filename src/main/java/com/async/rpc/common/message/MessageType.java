package com.async.rpc.common.message;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import lombok.AllArgsConstructor;

/**
 * @program: simple_RPC
 *
 * @description: 指定消息的数据类型
 **/
@AllArgsConstructor
public enum MessageType {
    REQUEST(0),RESPONSE(1);
    private int code;
    public int getCode(){
        return code;
    }
}
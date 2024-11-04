package com.async.rpc.common.pojo;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/4
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: simple_RPC
 *
 * @description: User类
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    // 客户端和服务端共有的User对象
    private Integer id;
    private String userName;
    private Boolean gender;
}

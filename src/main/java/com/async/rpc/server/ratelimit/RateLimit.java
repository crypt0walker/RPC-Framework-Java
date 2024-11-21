package com.async.rpc.server.ratelimit;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 */
public interface RateLimit {
    //获取访问许可
    boolean getToken();
}

package com.async.rpc.server.serviceRegister;

import java.net.InetSocketAddress;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */
public interface ServiceRegister {
    //  注册：保存服务与地址。
    void register(String serviceName, InetSocketAddress serviceAddress);
}

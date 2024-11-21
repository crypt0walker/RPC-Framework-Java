package com.async.rpc.client.serviceCenter;

import java.net.InetSocketAddress;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */
//服务中心接口
public interface ServiceCenter {
    //  查询：根据服务名查找地址
    InetSocketAddress serviceDiscovery(String serviceName);
    //判断是否可重试
    boolean checkRetry(String serviceName) ;
}

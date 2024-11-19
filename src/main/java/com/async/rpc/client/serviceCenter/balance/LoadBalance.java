package com.async.rpc.client.serviceCenter.balance;

import java.util.List;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/19
 * 配置服务地址列表，根据负载均衡策略选择相应节点
 */
public interface LoadBalance {
    String balance(List<String> address);
    void addNode(String node);
    void removeNode(String node);
}

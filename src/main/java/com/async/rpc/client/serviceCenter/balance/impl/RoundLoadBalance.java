package com.async.rpc.client.serviceCenter.balance.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/19
 */

import com.async.rpc.client.serviceCenter.balance.LoadBalance;

import java.util.List;

/**
 * @program: simple_RPC
 *
 * @description: 轮询负载均衡算法
 **/
public class RoundLoadBalance implements LoadBalance {
    private int choose = -1; // 用于记录上一次选择的服务器索引

    @Override
    public String balance(List<String> addressList) {
        choose++;
        choose = choose % addressList.size(); // 循环选择
        System.out.println("负载均衡选择了" + choose + "号服务器");
        return addressList.get(choose);
    }

    // 这些方法在简单轮询算法中不需要特殊实现
    public void addNode(String node) {}
    public void removeNode(String node) {}
}

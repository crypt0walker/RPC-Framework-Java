package com.async.rpc.client.serviceCenter.balance.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/19
 */

import com.async.rpc.client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;

/**
 * @program: simple_RPC
 *
 * @description: 随机访问负载均衡算法
 **/
public class RandomLoadBalance implements LoadBalance {
    @Override
    public String balance(List<String> addressList) {
        //构建随机数生成器
        Random random = new Random();
        //随机选择列表内的一个节点
        int choose = random.nextInt(addressList.size());
        System.out.println("负载均衡选择了" + choose + "号服务器");
        return addressList.get(choose); // 注意：这里应该返回选中的地址，而不是null
    }

    // 这些方法在随机算法中不需要特殊实现，因为每次选择都是独立的
    public void addNode(String node) {}

    @Override
    public void removeNode(String node) {

    }
}

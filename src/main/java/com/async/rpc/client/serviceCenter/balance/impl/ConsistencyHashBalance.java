package com.async.rpc.client.serviceCenter.balance.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/19
 */

import com.async.rpc.client.serviceCenter.balance.LoadBalance;

import java.util.*;

/**
 * @program: simple_RPC
 *
 * @description: 一致性哈希负载均衡算法
 **/
public class ConsistencyHashBalance implements LoadBalance {
    // 虚拟节点是一致性哈希负载均衡算法中用于控制负载均衡的工具，通过将一个实际服务器配置为多个虚拟节点，
    // 可以使得哈希更加均衡，并且可以更具虚拟节点的分配个数调整实际服务器的负载权重。
    // 虚拟节点的个数，增加虚拟节点可以使得负载分配更均匀
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
    private static SortedMap<Integer, String> shards = new TreeMap<>();

    // 真实节点列表
    private static List<String> realNodes = new LinkedList<>();

    // 初始化方法，将真实服务器添加到哈希环上
    private  void init(List<String> serviceList) {
        // 遍历添加真实节点对应的虚拟节点值shards
        for (String server :serviceList) {
            realNodes.add(server);
            System.out.println("真实节点[" + server + "] 被添加");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                // 命名空间
                String virtualNode = server + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被添加");
            }
        }
    }

    // 根据输入的key选择服务器
    public String getServer(String node, List<String> serviceList) {
        init(serviceList);
        int hash = getHash(node);
        // 如果hash值大于哈希环上的最大值，则返回哈希环上的第一个节点
        if (!shards.containsKey(hash)) {
            SortedMap<Integer, String> tailMap = shards.tailMap(hash);
            hash = tailMap.isEmpty() ? shards.firstKey() : tailMap.firstKey();
        }
        return shards.get(hash).split("&&")[0]; // 返回真实节点名称
    }

    // 添加真实节点对应的虚拟节点
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            System.out.println("真实节点[" + node + "] 上线添加");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                // 向虚拟节点map中添加VIRTUAL_NUM个虚拟节点
                shards.put(hash, virtualNode);
                System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被添加");
            }
        }
    }

    // 将一个真实节点所对应的虚拟节点删除
    public void removeNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            System.out.println("真实节点[" + node + "] 下线移除");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被移除");
            }
        }
    }

    // 使用FNV1_32_HASH算法计算哈希值
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }
    // 提供接口给serviceCenter调用，其提供服务地址列表，此处利用uuid随机选个一返回（配合负载均衡）；
    @Override
    public String balance(List<String> addressList) {
        // 使用UUID作为随机key，确保负载均衡
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }
}

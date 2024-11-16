package com.async.rpc.client.serviceCenter;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @program: simple_RPC
 *
 * @description: zookeeper注册中心
 **/
public class ZKServiceCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端,是用来与 Zookeeper 服务器通信的核心对象。
    private CuratorFramework client;
    //定义 Zookeeper 中的根节点路径，所有的服务都注册在这个路径下。
    private static final String ROOT_PATH = "MyRPC";

    //构造函数：负责zookeeper客户端的初始化，并与zookeeper服务端进行连接
    public ZKServiceCenter(){
        // 创建重试策略：指数退避重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 初始化 Zookeeper 客户端
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181") // Zookeeper 服务器的地址,可以是单个或多个 IP+端口
                .sessionTimeoutMs(40000)        // 会话超时时间，40秒,心跳监听状态超时未响应将失效
                .retryPolicy(policy)            // 设置重试策略
                .namespace(ROOT_PATH)           // 设置根路径（命名空间），操作时所有路径都以这个为前缀
                .build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }
    //服务发现方法
    //向zk注册中心发起查询，根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 获取 Zookeeper 中指定服务名称的子节点列表
            //客户端通过服务名 serviceName 向 Zookeeper 查询注册的服务实例列表。
            //返回结果是一个服务实例的地址（如 192.168.1.100:8080）。
            List<String> strings = client.getChildren().forPath("/" + serviceName);

            // 检查列表是否为空——如果不检查若列表为空，后续get(0)则会报异常
            if (strings == null || strings.isEmpty()) {
                System.err.println("No available instances for service: " + serviceName);
                return null; // 或者你可以抛出一个自定义的异常来告知调用者
            }

            // 选择一个服务实例，默认用第一个节点，后续可以加入负载均衡机制
            String string = strings.get(0);

            // 解析并返回 InetSocketAddress
            // 将字符串形式的地址（如 192.168.1.100:8080）转换为 InetSocketAddress，便于后续网络连接。
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return null; // 或者根据需求返回一个默认的 InetSocketAddress
        }
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    //将 InetSocketAddress 对象转换为字符串形式 IP:Port，便于存储到 Zookeeper。
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }

    // 字符串解析为地址
    //将 IP:Port 字符串解析为 InetSocketAddress，便于客户端与服务端建立网络连接。
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":"); // 按 `:` 分割 IP 和端口
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
package com.async.rpc.client;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import com.async.rpc.client.proxy.ClientProxy;
import com.async.rpc.common.pojo.User;
import com.async.rpc.common.server.UserService;

/**
 * @program: simple_RPC
 *
 * @description: 测试客户端
 **/
public class Testclient {
    public static void main(String[] args) {
        /* 非zookeeper版本：写死了ip与端口
        //创建ClientProxy对象
        //1表示使用socket连接模式而不是RPC
        ClientProxy clientProxy=new ClientProxy("127.0.0.1",9999,1);
        //通过ClientProxy对象获取代理对象
        UserService proxy=clientProxy.getProxy(UserService.class);
        //调用代理对象的方法
        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());
         */
    }
}

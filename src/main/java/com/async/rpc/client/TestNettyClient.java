package com.async.rpc.client;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

import com.async.rpc.client.proxy.ClientProxy;
import com.async.rpc.common.pojo.User;
import com.async.rpc.common.server.UserService;

/**
 * @program: simple_RPC
 *
 * @description: Netty测试类
 **/
public class TestNettyClient {
    public static void main(String[] args) {
        ClientProxy clientProxy=new ClientProxy("127.0.0.1",9999,0);
        UserService proxy=clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u=User.builder().id(100).userName("wxx").gender(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);
    }
}
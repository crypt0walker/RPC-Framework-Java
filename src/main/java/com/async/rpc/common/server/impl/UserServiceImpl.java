package com.async.rpc.common.server.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/4
 */

import com.async.rpc.common.pojo.User;
import com.async.rpc.common.server.UserService;

import java.util.Random;
import java.util.UUID;

/**
 * @program: simple_RPC
 *
 * @description: UserService实现类
 **/
public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("客户端查询了"+id+"的用户");
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .gender(random.nextBoolean()).build();
        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println("插入数据成功"+user.getUserName());
        return user.getId();
    }
}

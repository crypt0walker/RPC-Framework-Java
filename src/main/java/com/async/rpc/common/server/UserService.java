package com.async.rpc.common.server;

import com.async.rpc.common.pojo.User;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/4
 */
public interface UserService {
    // 客户端通过这个接口调用服务端的实现类
    User getUserByUserId(Integer id);
    //新增一个功能
    Integer insertUserId(User user);
}

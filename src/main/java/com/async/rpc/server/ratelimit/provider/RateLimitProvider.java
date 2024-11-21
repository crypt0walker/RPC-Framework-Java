package com.async.rpc.server.ratelimit.provider;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 */

import com.async.rpc.server.ratelimit.RateLimit;
import com.async.rpc.server.ratelimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: simple_RPC
 *
 * @description: 为各个服务提供对应限流器
 **/
public class RateLimitProvider {
    //利用map存储各服务限流器
    //key：服务接口名称，value：实现RateLimit的具体限流器
    private Map<String, RateLimit> rateLimitMap=new HashMap<>();
    //凭借服务接口名获取限流器
    public RateLimit getRateLimit(String interfaceName){
        //若无给该服务限流器
        if(!rateLimitMap.containsKey(interfaceName)){
            //创建100ms一个令牌速率，令牌桶大小为10的令牌桶限流器
            RateLimit rateLimit=new TokenBucketRateLimitImpl(100,10);
            //加入map
            rateLimitMap.put(interfaceName,rateLimit);
            //返回限流器
            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
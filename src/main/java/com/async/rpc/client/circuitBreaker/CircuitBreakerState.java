package com.async.rpc.client.circuitBreaker;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 */

/**
 * @program: simple_RPC
 *
 * @description: 定义熔断器的枚举类
 **/
enum CircuitBreakerState {
    //关闭，开启，半开启
    CLOSED, OPEN, HALF_OPEN
}
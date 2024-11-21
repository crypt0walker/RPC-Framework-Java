package com.async.rpc.client.circuitBreaker;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 */

import java.util.HashMap;
import java.util.Map;
import com.async.rpc.client.circuitBreaker.CircuitBreakerState;

/**
 * @program: simple_RPC
 *
 * @description: 提供各服务的相应熔断器
 **/


/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 *
 * @description: 提供各服务的熔断器，通过服务名称管理独立的熔断器实例。
 */
public class CircuitBreakerProvider {
    // 使用 Map 存储服务名称和对应的熔断器实例，key 是服务名，value 是对应的熔断器
    private Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    /**
     * 获取指定服务的熔断器
     *
     * @param serviceName 服务名称，用于标识熔断器
     * @return 对应服务的熔断器实例
     */
    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker;

        // 检查是否已经存在该服务对应的熔断器
        if (circuitBreakerMap.containsKey(serviceName)) {
            // 如果存在，直接从 Map 中获取对应的熔断器实例
            circuitBreaker = circuitBreakerMap.get(serviceName);
        } else {
            // 如果不存在，创建一个新的熔断器实例
            System.out.println("serviceName=" + serviceName + " 创建一个新的熔断器");

            // 使用默认参数初始化熔断器
            // 参数说明：
            // failureThreshold = 1：最大失败次数阈值，1 次失败触发熔断
            // halfOpenSuccessRate = 0.5：半开状态下成功率达到 50% 切换回关闭状态
            // retryTimePeriod = 10000：熔断状态等待 10 秒后进入半开状态
            circuitBreaker = new CircuitBreaker(1, 0.5, 10000);

            // 将新创建的熔断器存入 Map，绑定到对应的服务名称
            circuitBreakerMap.put(serviceName, circuitBreaker);
        }

        // 返回找到或新创建的熔断器
        return circuitBreaker;
    }
}

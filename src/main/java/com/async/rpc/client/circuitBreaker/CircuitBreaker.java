package com.async.rpc.client.circuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/21
 *
 * @description: 定义熔断器实现，用于保护系统在高失败率或依赖服务异常时，
 *               通过三种状态 (CLOSED, OPEN, HALF_OPEN) 来控制服务请求的通过与拒绝。
 */
public class CircuitBreaker {
    // 当前熔断器状态，初始化为 CLOSED（关闭状态），表示正常工作状态
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    // 失败计数器，记录当前失败次数，用于判断是否触发熔断
    private AtomicInteger failureCount = new AtomicInteger(0);
    // 成功计数器，用于 HALF_OPEN 状态下判断是否恢复到 CLOSED 状态
    private AtomicInteger successCount = new AtomicInteger(0);
    // 请求计数器，用于统计 HALF_OPEN 状态下的总请求数
    private AtomicInteger requestCount = new AtomicInteger(0);

    // 配置参数
    private final int failureThreshold; // 最大失败次数阈值，超过此值触发熔断
    private final double halfOpenSuccessRate; // 半开状态下的成功率阈值，用于判断是否恢复到 CLOSED
    private final long retryTimePeriod; // 从 OPEN 状态进入 HALF_OPEN 状态的等待时间（毫秒）

    // 上一次失败的时间戳，用于判断是否可以从 OPEN 状态切换到 HALF_OPEN
    private long lastFailureTime = 0;

    /**
     * 构造函数，初始化熔断器
     *
     * @param failureThreshold     最大失败次数阈值
     * @param halfOpenSuccessRate  半开状态的成功率阈值
     * @param retryTimePeriod      熔断状态的恢复时间间隔
     */
    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    /**
     * 判断当前熔断器是否允许请求通过
     *
     * @return true 表示允许请求通过，false 表示请求被拒绝
     */
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis(); // 获取当前时间
        System.out.println("熔断器状态检查: failureCount=" + failureCount);

        // 根据当前状态进行判断
        switch (state) {
            case OPEN: // 当前是熔断状态
                if (currentTime - lastFailureTime > retryTimePeriod) { // 如果超过了恢复时间
                    state = CircuitBreakerState.HALF_OPEN; // 切换到 HALF_OPEN 状态，开始允许部分请求
                    resetCounts(); // 重置计数器
                    return true; // 测试请求是否可以成功
                }
                System.out.println("熔断器处于 OPEN 状态，请求被拒绝！");
                return false; // 未超过恢复时间，拒绝请求

            case HALF_OPEN: // 当前是半开状态
                // incrementAndGet()：线程安全的自增
                requestCount.incrementAndGet(); // 记录通过的请求数量
                return true; // 半开状态允许部分请求通过

            case CLOSED: // 当前是关闭状态（正常状态）
            default:
                return true; // 正常状态允许所有请求通过
        }
    }

    /**
     * 记录一次成功的请求
     */
    // 被ProxyClient调用
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) { // 如果当前处于半开状态
            successCount.incrementAndGet(); // 增加成功请求的计数
            // 如果成功率达到阈值，切换回 CLOSED 状态
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                state = CircuitBreakerState.CLOSED; // 恢复到 CLOSED 状态
                resetCounts(); // 重置计数器
            }
        } else {
            resetCounts(); // 其他状态直接重置计数器
        }
    }

    /**
     * 记录一次失败的请求
     */
    public synchronized void recordFailure() {
        failureCount.incrementAndGet(); // 增加失败计数
        System.out.println("记录失败次数: failureCount=" + failureCount);
        lastFailureTime = System.currentTimeMillis(); // 更新失败时间戳

        if (state == CircuitBreakerState.HALF_OPEN) { // 在半开状态下，任何失败都立即切换到 OPEN 状态
            state = CircuitBreakerState.OPEN;
            lastFailureTime = System.currentTimeMillis();
        } else if (failureCount.get() >= failureThreshold) { // 在 CLOSED 状态下，达到失败阈值则切换到 OPEN 状态
            state = CircuitBreakerState.OPEN;
        }
    }

    /**
     * 重置所有计数器
     */
    private void resetCounts() {
        failureCount.set(0); // 重置失败计数
        successCount.set(0); // 重置成功计数
        requestCount.set(0); // 重置请求计数
    }

    /**
     * 获取当前熔断器的状态
     *
     * @return 当前熔断器状态
     */
    public CircuitBreakerState getState() {
        return state;
    }
}

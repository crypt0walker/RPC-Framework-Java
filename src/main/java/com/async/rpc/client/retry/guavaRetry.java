package com.async.rpc.client.retry;

import com.async.rpc.client.rpcClient.RpcClient;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;
import com.github.rholder.retry.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Guava Retry 实现的 RPC 重试机制
 * 主要功能：
 *  - 针对网络异常或错误响应结果自动重试
 *  - 限制最大重试次数，避免资源浪费
 *  - 添加重试等待策略和监听器
 */
public class guavaRetry {
    // RpcClient 是客户端的通信组件，负责发送请求
    private RpcClient rpcClient;

    /**
     * 执行带重试机制的 RPC 请求
     * @param request   RPC 请求对象
     * @param rpcClient 客户端通信组件
     * @return          RPC 响应对象
     */
    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        // 初始化 RpcClient
        this.rpcClient = rpcClient;

        // 构建重试器 Retryer
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 1. 异常重试策略：对所有异常进行重试
                .retryIfException()

                // 2. 错误结果重试策略：如果响应状态码为 500，则进行重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))

                // 3. 等待策略：每次重试之间等待固定时间 2 秒
//                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 指数退避
                .withWaitStrategy(WaitStrategies.exponentialWait(500, 10, TimeUnit.SECONDS))
                // 随机等待
//                .withWaitStrategy(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 5, TimeUnit.SECONDS))


                // 4. 停止策略：重试达到最多 3 次后停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))

                // 5. 重试监听器：每次重试时执行额外操作（如记录日志）
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        // 打印重试日志信息
                        System.out.println("RetryListener: 第 " + attempt.getAttemptNumber() + " 次调用");
                        if (attempt.hasException()) {
                            System.out.println("发生异常: " + attempt.getExceptionCause());
                        } else if (attempt.hasResult()) {
                            System.out.println("返回结果: " + attempt.getResult());
                        }
                    }
                })

                // 构建完成
                .build();

        try {
            // 执行重试逻辑
            return retryer.call(() -> {
                // 调用实际的 RPC 请求
                return rpcClient.sendRequest(request);
            });
        } catch (Exception e) {
            // 捕获重试失败的异常，打印堆栈信息
            e.printStackTrace();
        }

        // 如果重试失败，返回一个默认的失败响应
        return RpcResponse.fail();
    }
}

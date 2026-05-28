package com.jeez.common.mq;

/**
 * RabbitMQ 异步发送回调接口
 *
 * @author Jeez.Fitness
 * @since 2025-01-01
 */
public interface RabbitMQCallback {

    /**
     * 发送成功回调
     */
    void onSuccess();

    /**
     * 发送失败回调
     *
     * @param throwable 异常信息
     */
    void onFailure(Throwable throwable);
}

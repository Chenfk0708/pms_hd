package com.jeez.common.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送工具类
 * 提供同步、异步、延迟消息发送能力
 *
 * @author Jeez.Fitness
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 同步发送消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param <T>        消息类型
     */
    public <T> void syncSend(String exchange, String routingKey, T message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("同步消息发送成功, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("同步消息发送失败, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步发送消息（带发布确认）
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param callback   回调处理
     * @param <T>        消息类型
     */
    public <T> void asyncSend(String exchange, String routingKey, T message, RabbitMQCallback callback) {
        try {
            CorrelationData correlationData = new CorrelationData();

            // 设置发布确认回调
            rabbitTemplate.setConfirmCallback((correlation, ack, cause) -> {
                if (ack) {
                    log.info("异步消息发送成功, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    log.error("异步消息发送失败, exchange: {}, routingKey: {}, message: {}, cause: {}",
                            exchange, routingKey, message, cause);
                    if (callback != null) {
                        callback.onFailure(new RuntimeException("消息发送失败: " + cause));
                    }
                }
            });

            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        } catch (Exception e) {
            log.error("异步消息发送异常, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * 发送延迟消息
     * 需要安装 RabbitMQ 延迟消息插件 (rabbitmq_delayed_message_exchange)
     *
     * @param exchange   延迟交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayMs    延迟时间（毫秒）
     * @param <T>        消息类型
     */
    public <T> void syncSendDelay(String exchange, String routingKey, T message, long delayMs) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setDelay((int) delayMs);
                return msg;
            });
            log.info("延迟消息发送成功, exchange: {}, routingKey: {}, delayMs: {}, message: {}",
                    exchange, routingKey, delayMs, message);
        } catch (Exception e) {
            log.error("延迟消息发送失败, exchange: {}, routingKey: {}, delayMs: {}, message: {}",
                    exchange, routingKey, delayMs, message, e);
            throw new RuntimeException("延迟消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送延迟消息（使用 TTL + 死信队列方式）
     * 不需要安装插件，但创建更多队列
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param ttlMs      存活时间（毫秒）
     * @param <T>        消息类型
     */
    public <T> void syncSendDelayWithTTL(String exchange, String routingKey, T message, long ttlMs) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setExpiration(String.valueOf(ttlMs));
                return msg;
            });
            log.info("延迟消息(TTL)发送成功, exchange: {}, routingKey: {}, ttlMs: {}, message: {}",
                    exchange, routingKey, ttlMs, message);
        } catch (Exception e) {
            log.error("延迟消息(TTL)发送失败, exchange: {}, routingKey: {}, ttlMs: {}, message: {}",
                    exchange, routingKey, ttlMs, message, e);
            throw new RuntimeException("延迟消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 单向发送消息（不关心发送结果）
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param <T>        消息类型
     */
    public <T> void sendOneWay(String exchange, String routingKey, T message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("单向消息发送, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("单向消息发送异常, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
        }
    }

    /**
     * 发送带优先级的消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param priority   优先级 (0-255)
     * @param <T>        消息类型
     */
    public <T> void syncSendWithPriority(String exchange, String routingKey, T message, int priority) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setPriority(priority);
                return msg;
            });
            log.info("优先级消息发送成功, exchange: {}, routingKey: {}, priority: {}, message: {}",
                    exchange, routingKey, priority, message);
        } catch (Exception e) {
            log.error("优先级消息发送失败, exchange: {}, routingKey: {}, priority: {}, message: {}",
                    exchange, routingKey, priority, message, e);
            throw new RuntimeException("优先级消息发送失败: " + e.getMessage(), e);
        }
    }
}

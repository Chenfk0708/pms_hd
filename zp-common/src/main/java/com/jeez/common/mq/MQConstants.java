package com.jeez.common.mq;

/**
 * RabbitMQ Exchange、Queue、RoutingKey 常量定义
 * 所有模块使用的消息队列相关常量统一在此定义
 *
 * @author Jeez.Fitness
 * @since 2025-01-01
 */
public class MQConstants {

    private MQConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== Exchange 定义 ====================

    /**
     * 订单交换机（直连交换机）
     */
    public static final String EXCHANGE_ORDER = "jeez.order.exchange";

    /**
     * 订单延迟交换机（用于延迟消息，需要安装 rabbitmq_delayed_message_exchange 插件）
     */
    public static final String EXCHANGE_ORDER_DELAY = "jeez.order.delay.exchange";

    /**
     * 订单死信交换机（用于 TTL + 死信队列实现延迟）
     */
    public static final String EXCHANGE_ORDER_DLX = "jeez.order.dlx.exchange";

    /**
     * 支付交换机
     */
    public static final String EXCHANGE_PAYMENT = "jeez.payment.exchange";

    /**
     * 会员交换机
     */
    public static final String EXCHANGE_MEMBER = "jeez.member.exchange";

    /**
     * 通知交换机
     */
    public static final String EXCHANGE_NOTIFICATION = "jeez.notification.exchange";

    /**
     * 课程交换机
     */
    public static final String EXCHANGE_COURSE = "jeez.course.exchange";

    /**
     * 预约交换机
     */
    public static final String EXCHANGE_BOOKING = "jeez.booking.exchange";

    // ==================== Queue 定义 ====================

    /**
     * 订单队列
     */
    public static class OrderQueue {
        public static final String CREATE = "jeez.order.create.queue";
        public static final String PAY = "jeez.order.pay.queue";
        public static final String CANCEL = "jeez.order.cancel.queue";
        public static final String COMPLETE = "jeez.order.complete.queue";
        public static final String REFUND = "jeez.order.refund.queue";
        public static final String TIMEOUT = "jeez.order.timeout.queue";
        /**
         * 订单超时延迟队列（TTL队列，消息过期后转发到死信队列）
         */
        public static final String TIMEOUT_DELAY = "jeez.order.timeout.delay.queue";
    }

    /**
     * 支付队列
     */
    public static class PaymentQueue {
        public static final String SUCCESS = "jeez.payment.success.queue";
        public static final String FAIL = "jeez.payment.fail.queue";
        public static final String REFUND = "jeez.payment.refund.queue";
    }

    /**
     * 会员队列
     */
    public static class MemberQueue {
        public static final String REGISTER = "jeez.member.register.queue";
        public static final String UPDATE = "jeez.member.update.queue";
        public static final String LEVEL_CHANGE = "jeez.member.level.change.queue";
        public static final String POINTS_CHANGE = "jeez.member.points.change.queue";
    }

    /**
     * 通知队列
     */
    public static class NotificationQueue {
        public static final String SMS = "jeez.notification.sms.queue";
        public static final String PUSH = "jeez.notification.push.queue";
        public static final String EMAIL = "jeez.notification.email.queue";
        public static final String WECHAT = "jeez.notification.wechat.queue";
    }

    /**
     * 课程队列
     */
    public static class CourseQueue {
        public static final String CREATE = "jeez.course.create.queue";
        public static final String UPDATE = "jeez.course.update.queue";
        public static final String CANCEL = "jeez.course.cancel.queue";
        public static final String START = "jeez.course.start.queue";
        public static final String END = "jeez.course.end.queue";
    }

    /**
     * 预约队列
     */
    public static class BookingQueue {
        public static final String CREATE = "jeez.booking.create.queue";
        public static final String CONFIRM = "jeez.booking.confirm.queue";
        public static final String CANCEL = "jeez.booking.cancel.queue";
        public static final String REMIND = "jeez.booking.remind.queue";
    }

    // ==================== RoutingKey 定义 ====================

    /**
     * 订单路由键
     */
    public static class OrderRoutingKey {
        public static final String CREATE = "order.create";
        public static final String PAY = "order.pay";
        public static final String CANCEL = "order.cancel";
        public static final String COMPLETE = "order.complete";
        public static final String REFUND = "order.refund";
        public static final String TIMEOUT = "order.timeout";
        public static final String TIMEOUT_DELAY = "order.timeout.delay";
    }

    /**
     * 支付路由键
     */
    public static class PaymentRoutingKey {
        public static final String SUCCESS = "payment.success";
        public static final String FAIL = "payment.fail";
        public static final String REFUND = "payment.refund";
    }

    /**
     * 会员路由键
     */
    public static class MemberRoutingKey {
        public static final String REGISTER = "member.register";
        public static final String UPDATE = "member.update";
        public static final String LEVEL_CHANGE = "member.level.change";
        public static final String POINTS_CHANGE = "member.points.change";
    }

    /**
     * 通知路由键
     */
    public static class NotificationRoutingKey {
        public static final String SMS = "notification.sms";
        public static final String PUSH = "notification.push";
        public static final String EMAIL = "notification.email";
        public static final String WECHAT = "notification.wechat";
    }

    /**
     * 课程路由键
     */
    public static class CourseRoutingKey {
        public static final String CREATE = "course.create";
        public static final String UPDATE = "course.update";
        public static final String CANCEL = "course.cancel";
        public static final String START = "course.start";
        public static final String END = "course.end";
    }

    /**
     * 预约路由键
     */
    public static class BookingRoutingKey {
        public static final String CREATE = "booking.create";
        public static final String CONFIRM = "booking.confirm";
        public static final String CANCEL = "booking.cancel";
        public static final String REMIND = "booking.remind";
    }

    // ==================== 延迟时间常量（毫秒）====================

    /**
     * 延迟时间
     */
    public static class DelayTime {
        public static final long SECOND_1 = 1000L;
        public static final long SECOND_5 = 5000L;
        public static final long SECOND_10 = 10000L;
        public static final long SECOND_30 = 30000L;
        public static final long MINUTE_1 = 60000L;
        public static final long MINUTE_2 = 120000L;
        public static final long MINUTE_3 = 180000L;
        public static final long MINUTE_5 = 300000L;
        public static final long MINUTE_10 = 600000L;
        public static final long MINUTE_15 = 900000L;
        public static final long MINUTE_30 = 1800000L;
        public static final long HOUR_1 = 3600000L;
        public static final long HOUR_2 = 7200000L;
    }
}

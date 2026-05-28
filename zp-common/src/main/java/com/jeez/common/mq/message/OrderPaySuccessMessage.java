package com.jeez.common.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付成功消息
 * 用于通知其他服务订单已支付成功
 *
 * @author Jeez.Fitness
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaySuccessMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单类型 1-会员卡订单 2-课程订单 3-私教订单 4-商品订单
     */
    private Integer orderType;

    /**
     * 商品ID（会员卡ID、课程ID等）
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付方式 1-微信支付 2-支付宝 3-现金
     */
    private Integer paymentMethod;

    /**
     * 支付流水号
     */
    private String paymentTransactionId;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 门店ID（可选）
     */
    private Long storeId;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
}

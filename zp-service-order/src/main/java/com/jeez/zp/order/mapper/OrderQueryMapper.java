package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.OrderQueryRowVO;
import com.jeez.zp.order.vo.OrderDetailRowVO;
import com.jeez.zp.order.vo.OrderGuestVO;
import com.jeez.zp.order.vo.OrderPaymentRecordRowVO;
import com.jeez.zp.order.vo.WorkspaceOrderListRowVO;
import com.jeez.zp.order.vo.StrongReminderRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderQueryMapper {

    List<OrderQueryRowVO> selectHouseOrders(@Param("campId") Long campId, @Param("keyword") String keyword);

    List<WorkspaceOrderListRowVO> selectWorkspaceOrders(@Param("campId") Long campId, @Param("keyword") String keyword);

    OrderDetailRowVO selectOrderDetail(@Param("campId") Long campId, @Param("orderId") Long orderId);

    List<OrderGuestVO> selectOrderGuests(@Param("orderId") Long orderId);

    List<OrderPaymentRecordRowVO> selectOrderPaymentRecords(@Param("campId") Long campId, @Param("orderId") Long orderId);

    List<StrongReminderRowVO> selectStrongReminderRows(@Param("campId") Long campId, @Param("keyword") String keyword);
}

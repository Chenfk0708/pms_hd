package com.jeez.zp.order.service.impl;

import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderCancelRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderDetailRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderModifyRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderPageRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderStatusSyncRequest;
import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.ChannelOrderMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.ChannelCallbackOrderService;
import com.jeez.zp.order.service.OrderActionService;
import com.jeez.zp.order.service.OrderQueryService;
import com.jeez.zp.order.vo.ChannelCallbackOrderOperationResponseVO;
import com.jeez.zp.order.vo.ChannelOrderAccountVO;
import com.jeez.zp.order.vo.OrderActionResponseVO;
import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChannelCallbackOrderServiceImpl implements ChannelCallbackOrderService {

    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_CHECKED_IN = "checked_in";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_NO_SHOW = "no_show";

    private final ChannelOrderMapper channelOrderMapper;
    private final UserCampMapper userCampMapper;
    private final OrderQueryService orderQueryService;
    private final OrderActionService orderActionService;

    @Override
    public OrderPageResponseVO getPage(String channelCode, ChannelCallbackOrderPageRequest request, Long operatorUserId) {
        ChannelOrderAccountVO account = requireAccount(channelCode, request == null ? null : request.getAccountId(), operatorUserId);
        int pageNum = normalizePageNum(firstNonNull(request == null ? null : request.getPageNum(), request == null ? null : request.getCurrent()));
        int pageSize = normalizePageSize(request == null ? null : request.getPageSize());
        String keyword = firstNonBlank(
                request == null ? null : request.getKeyword(),
                request == null ? null : request.getSearchContent(),
                request == null ? null : request.getOutOrderNo()
        );
        String status = firstNonBlank(request == null ? null : request.getOrderStatus(), request == null ? null : request.getChannelStatus());
        Long campId = resolveCampId(account, request == null ? null : request.getCampId(), operatorUserId);
        long total = channelOrderMapper.countCallbackOrders(campId, account.getAccountId(), keyword, status);
        int pages = total == 0 ? 0 : (int) Math.ceil(total / (double) pageSize);
        OrderPageResponseVO response = new OrderPageResponseVO();
        response.setTotal(total);
        response.setSize(pageSize);
        response.setCurrent(pageNum);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setPages(pages);
        response.setHasNextPage(pageNum < pages);
        response.setList(channelOrderMapper.selectCallbackOrders(
                campId,
                account.getAccountId(),
                keyword,
                status,
                (long) (pageNum - 1) * pageSize,
                pageSize
        ));
        return response;
    }

    @Override
    public OrderDetailAggregateVO getDetail(String channelCode, ChannelCallbackOrderDetailRequest request, Long operatorUserId) {
        ChannelOrderAccountVO account = requireAccount(channelCode, request == null ? null : request.getAccountId(), operatorUserId);
        Long orderId = resolveOrderId(account, request == null ? null : request.getOrderId(), request == null ? null : request.getOutOrderNo());
        return orderQueryService.getOrderDetail(
                resolveCampId(account, request == null ? null : request.getCampId(), operatorUserId),
                operatorUserId,
                orderId
        );
    }

    @Override
    public ChannelCallbackOrderOperationResponseVO cancel(
            String channelCode,
            ChannelCallbackOrderCancelRequest request,
            Long operatorUserId
    ) {
        ChannelOrderAccountVO account = requireAccount(channelCode, request == null ? null : request.getAccountId(), operatorUserId);
        Long orderId = resolveOrderId(account, request == null ? null : request.getOrderId(), request == null ? null : request.getOutOrderNo());
        OrderActionResponseVO actionResponse = orderActionService.cancelOrder(
                resolveCampId(account, request == null ? null : request.getCampId(), operatorUserId),
                orderId,
                operatorUserId,
                request == null ? null : request.getReason()
        );
        return operationResponse(account, request == null ? null : request.getOutOrderNo(), orderId, actionResponse);
    }

    @Override
    public ChannelCallbackOrderOperationResponseVO modify(
            String channelCode,
            ChannelCallbackOrderModifyRequest request,
            Long operatorUserId
    ) {
        ChannelOrderAccountVO account = requireAccount(channelCode, request == null ? null : request.getAccountId(), operatorUserId);
        Long orderId = resolveOrderId(account, request == null ? null : request.getOrderId(), request == null ? null : request.getOutOrderNo());
        throw new BusinessException(
                40001,
                "第三方订单修改入口已建立，但当前不支持直接改期/改价落库，请先使用取消重订或补充订单修改业务规则后再启用。orderId=" + orderId
        );
    }

    @Override
    public ChannelCallbackOrderOperationResponseVO syncStatus(
            String channelCode,
            ChannelCallbackOrderStatusSyncRequest request,
            Long operatorUserId
    ) {
        ChannelOrderAccountVO account = requireAccount(channelCode, request == null ? null : request.getAccountId(), operatorUserId);
        Long orderId = resolveOrderId(account, request == null ? null : request.getOrderId(), request == null ? null : request.getOutOrderNo());
        Long campId = resolveCampId(account, request == null ? null : request.getCampId(), operatorUserId);
        String normalizedStatus = normalizeStatus(request == null ? null : request.getChannelStatus());
        OrderActionResponseVO actionResponse = switch (normalizedStatus) {
            case STATUS_CANCELLED -> orderActionService.cancelOrder(campId, orderId, operatorUserId, request == null ? null : request.getReason());
            case STATUS_CHECKED_IN -> orderActionService.checkIn(campId, orderId, operatorUserId);
            case STATUS_COMPLETED -> orderActionService.checkOut(campId, orderId, operatorUserId);
            case STATUS_NO_SHOW -> orderActionService.markNoShow(campId, orderId, operatorUserId, request == null ? null : request.getReason());
            default -> throw new BusinessException(40001, "不支持的第三方订单状态: " + (request == null ? null : request.getChannelStatus()));
        };
        return operationResponse(account, request == null ? null : request.getOutOrderNo(), orderId, actionResponse);
    }

    private ChannelOrderAccountVO requireAccount(String channelCode, String accountId, Long operatorUserId) {
        Long parsedAccountId = parseRequiredLong(accountId, "accountId");
        ChannelOrderAccountVO account = channelOrderMapper.selectAccount(parsedAccountId);
        if (account == null) {
            throw new BusinessException(40001, "渠道账号不存在");
        }
        if (!"authorized".equals(account.getStatus())) {
            throw new BusinessException(40001, "渠道账号未授权");
        }
        String normalizedChannelCode = requireText(channelCode, "channelCode");
        if (!normalizedChannelCode.equals(account.getChannelCode())) {
            throw new BusinessException(40001, "请求的 channelCode 与渠道账号配置不一致");
        }
        resolveCampId(account, null, operatorUserId);
        return account;
    }

    private Long resolveCampId(ChannelOrderAccountVO account, String requestedCampId, Long operatorUserId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(operatorUserId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        Long parsedRequestedCampId = parseLong(requestedCampId);
        Long targetCampId = parsedRequestedCampId == null ? account.getCampId() : parsedRequestedCampId;
        if (!currentCampId.equals(targetCampId) || !currentCampId.equals(account.getCampId())) {
            throw new BusinessException(40301, "当前用户无权访问该渠道账号");
        }
        return targetCampId;
    }

    private Long resolveOrderId(ChannelOrderAccountVO account, String requestOrderId, String outOrderNo) {
        Long directOrderId = parseLong(requestOrderId);
        if (directOrderId != null) {
            Integer belongs = channelOrderMapper.countPmsOrderBelongsToAccount(account.getAccountId(), directOrderId);
            if (belongs == null || belongs < 1) {
                throw new BusinessException(40401, "订单不属于当前渠道账号");
            }
            return directOrderId;
        }
        String normalizedOutOrderNo = requireText(outOrderNo, "outOrderNo");
        Long pmsOrderId = channelOrderMapper.selectImportedPmsOrderId(account.getAccountId(), normalizedOutOrderNo);
        if (pmsOrderId == null) {
            throw new BusinessException(40401, "未找到已导入的第三方订单");
        }
        return pmsOrderId;
    }

    private ChannelCallbackOrderOperationResponseVO operationResponse(
            ChannelOrderAccountVO account,
            String outOrderNo,
            Long orderId,
            OrderActionResponseVO actionResponse
    ) {
        return ChannelCallbackOrderOperationResponseVO.of(
                String.valueOf(account.getAccountId()),
                outOrderNo,
                String.valueOf(orderId),
                actionResponse.getStatus(),
                actionResponse.getMessage()
        );
    }

    private String normalizeStatus(String value) {
        String normalized = requireText(value, "channelStatus").toLowerCase();
        return switch (normalized) {
            case "cancel", "canceled", "cancelled" -> STATUS_CANCELLED;
            case "check_in", "checked_in", "arrived" -> STATUS_CHECKED_IN;
            case "check_out", "checked_out", "completed", "finish", "finished" -> STATUS_COMPLETED;
            case "no_show", "noshow" -> STATUS_NO_SHOW;
            default -> normalized;
        };
    }

    private String requireText(String value, String fieldName) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, "缺少必填字段: " + fieldName);
        }
        return normalized;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        String normalized = requireText(value, fieldName);
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new BusinessException(40001, fieldName + " 格式不正确");
        }
    }

    private Long parseLong(String value) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new BusinessException(40001, "数字字段格式不正确: " + value);
        }
    }

    private Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = InputValidationUtils.trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }
}

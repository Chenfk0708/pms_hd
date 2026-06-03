package com.jeez.zp.order.service.impl;

import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.CouponMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.CouponService;
import com.jeez.zp.order.vo.CouponPageResponseVO;
import com.jeez.zp.order.vo.CouponRowVO;
import com.jeez.zp.order.vo.CouponSendConfigPageResponseVO;
import com.jeez.zp.order.vo.CouponSendConfigRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CouponMapper couponMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public CouponPageResponseVO getCoupons(Long campId, Long userId, Integer shelfStatus, Integer pageNum, Integer pageSize) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CouponRowVO> rows = couponMapper.selectCoupons(resolvedCampId, shelfStatus).stream()
                .peek(this::fillCouponDisplayFields)
                .toList();
        PageSlice<CouponRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        CouponPageResponseVO response = new CouponPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(pageSlice.pages());
        response.setHasNextPage(resolvedPageNum < pageSlice.pages());
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public CouponSendConfigPageResponseVO getCouponSendConfigs(Long campId, Long userId, Integer pageNum, Integer pageSize) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CouponSendConfigRowVO> rows = couponMapper.selectCouponSendConfigs(resolvedCampId).stream()
                .peek(this::fillSendConfigDisplayFields)
                .toList();
        PageSlice<CouponSendConfigRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        CouponSendConfigPageResponseVO response = new CouponSendConfigPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(pageSlice.pages());
        response.setHasNextPage(resolvedPageNum < pageSlice.pages());
        response.setList(pageSlice.items());
        return response;
    }

    private void fillCouponDisplayFields(CouponRowVO row) {
        Long thresholdAmount = defaultLong(row.getThresholdAmount());
        Long discountAmount = defaultLong(row.getDiscountAmount());
        row.setDiscountText("满 " + thresholdAmount + " 元减 " + discountAmount + " 元");
        row.setSendLimitText(defaultInt(row.getSendLimit()) + " 张");
        row.setPerUserLimitText(defaultInt(row.getPerUserLimit()) + " 张");
        row.setSendTimeText(formatDateTime(row.getCreatedAt()));
        row.setValidityTypeText("有效天数");
        row.setEffectiveTimeText("领取后7天有效");
        if (isBlank(row.getReceiveRuleText())) {
            row.setReceiveRuleText("所有人可以领");
        }
        row.setShelfStatusText(Integer.valueOf(1).equals(row.getShelfStatus()) ? "已上架" : "已下架");
    }

    private void fillSendConfigDisplayFields(CouponSendConfigRowVO row) {
        row.setSendMethod(resolveSendMethod(row.getSendType()));
        row.setCreatedAt(formatDateTime(row.getCreatedAtValue()));
        row.setRecordText("查看记录");
    }

    private String resolveSendMethod(String sendType) {
        String normalized = trimToNull(sendType);
        if ("member_tag".equals(normalized)) {
            return "会员标签定向派发";
        }
        if ("manual".equals(normalized)) {
            return "手动派发";
        }
        if ("auto".equals(normalized)) {
            return "自动派发";
        }
        return "会员标签定向派发";
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, pages, items.subList(fromIndex, toIndex));
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店优惠券数据");
        }
        return requestedCampId;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private record PageSlice<T>(long total, int pages, List<T> items) {
    }
}

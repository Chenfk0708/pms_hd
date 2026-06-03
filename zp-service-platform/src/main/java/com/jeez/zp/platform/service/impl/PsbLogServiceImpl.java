package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PsbLogMapper;
import com.jeez.zp.platform.service.PsbLogService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PsbLogPageDataVO;
import com.jeez.zp.platform.vo.PsbLogRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PsbLogServiceImpl implements PsbLogService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String RETRY_SUCCESS_REMARK = "重新上报成功，公安回执已更新";
    private static final String RETRY_SUCCESS_RECEIPT = "公安回执：重新上报成功";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PsbLogMapper psbLogMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PsbLogPageDataVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            String keyword,
            String bizType,
            String state,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;
        String normalizedKeyword = normalizeKeyword(keyword);

        long total = psbLogMapper.countPage(resolvedCampId, poiId, normalizedKeyword, normalizeBlank(bizType), normalizeBlank(state));
        List<PsbLogRowVO> rows = total == 0
                ? List.of()
                : psbLogMapper.selectPage(
                resolvedCampId,
                poiId,
                normalizedKeyword,
                normalizeBlank(bizType),
                normalizeBlank(state),
                offset,
                resolvedPageSize
        );

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);
        PsbLogPageDataVO response = new PsbLogPageDataVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(rows);
        return response;
    }

    @Override
    public PsbLogRowVO retry(Long campId, Long userId, Long guestId, String orderNo) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (guestId == null && normalizeBlank(orderNo) == null) {
            throw new BusinessException(40001, "公安上报日志标识不能为空");
        }

        PsbLogRowVO row = psbLogMapper.selectByGuestIdAndOrderNo(resolvedCampId, guestId, normalizeBlank(orderNo));
        if (row == null) {
            throw new BusinessException(40401, "公安上报日志不存在");
        }

        String now = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        row.setState("1");
        row.setRemark(RETRY_SUCCESS_REMARK);
        row.setReceiptMessage(RETRY_SUCCESS_RECEIPT);
        row.setUploadTime(now);
        row.setReportTime(now);
        return row;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private String normalizeKeyword(String keyword) {
        String normalized = normalizeBlank(keyword);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店公安上报日志");
        }
        return requestedCampId;
    }
}

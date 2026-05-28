package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.SmsChannelAccount;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SmsChannelAccountMapper;
import com.jeez.zp.platform.service.SmsAccountService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SmsAccountSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsAccountServiceImpl implements SmsAccountService {

    private static final int ENABLED = 1;

    private final SmsChannelAccountMapper smsChannelAccountMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public SmsAccountSummaryVO getSmsAccountSummary(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<SmsChannelAccount> accounts = smsChannelAccountMapper.selectList(new LambdaQueryWrapper<SmsChannelAccount>()
                .eq(SmsChannelAccount::getCampId, resolvedCampId)
                .eq(SmsChannelAccount::getStatus, ENABLED)
                .orderByDesc(SmsChannelAccount::getUpdatedAt, SmsChannelAccount::getSmsChannelAccountId));

        SmsAccountSummaryVO summary = new SmsAccountSummaryVO();
        summary.setCampId(String.valueOf(resolvedCampId));

        if (accounts.isEmpty()) {
            summary.setId("");
            summary.setTotalSmsCount("0");
            summary.setCurSmsCount("0");
            return summary;
        }

        SmsChannelAccount primary = accounts.get(0);
        long totalSmsCount = accounts.stream().mapToLong(this::safeTotalNum).sum();
        long currentSmsCount = accounts.stream().mapToLong(this::safeBalanceNum).sum();

        summary.setId(String.valueOf(primary.getSmsChannelAccountId()));
        summary.setTotalSmsCount(String.valueOf(totalSmsCount));
        summary.setCurSmsCount(String.valueOf(currentSmsCount));
        return summary;
    }

    private long safeTotalNum(SmsChannelAccount account) {
        return account.getTotalNum() == null ? 0L : account.getTotalNum();
    }

    private long safeBalanceNum(SmsChannelAccount account) {
        return account.getBalanceNum() == null ? 0L : account.getBalanceNum();
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
            throw new BusinessException(40301, "无权访问当前门店短信余额数据");
        }
        return requestedCampId;
    }
}

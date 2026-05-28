package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.SmsTemplate;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SmsTemplateMapper;
import com.jeez.zp.platform.service.SmsTemplateMsgConfigService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SmsTemplateMsgConfigItemVO;
import com.jeez.zp.platform.vo.SmsTemplateMsgConfigPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsTemplateMsgConfigServiceImpl implements SmsTemplateMsgConfigService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int DEFAULT_TEMPLATE_TYPE = 1;
    private static final int SIGN_POSITION_PREFIX = 0;
    private static final String STATUS_ENABLED = "enabled";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_PENDING = "pending";

    private final SmsTemplateMapper smsTemplateMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public SmsTemplateMsgConfigPageResponseVO getPage(Long campId, Long userId, Integer sendType, Integer pageNum, Integer pageSize) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        List<SmsTemplate> allTemplates = smsTemplateMapper.selectList(new LambdaQueryWrapper<SmsTemplate>()
                .eq(SmsTemplate::getCampId, resolvedCampId)
                .orderByDesc(SmsTemplate::getUpdatedAt, SmsTemplate::getSmsTemplateId));

        long total = allTemplates.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);
        int fromIndex = Math.max(0, (resolvedPageNum - 1) * resolvedPageSize);
        int toIndex = Math.min(allTemplates.size(), fromIndex + resolvedPageSize);

        List<SmsTemplateMsgConfigItemVO> items = fromIndex >= allTemplates.size()
                ? List.of()
                : allTemplates.subList(fromIndex, toIndex).stream()
                .map(this::toItem)
                .toList();

        SmsTemplateMsgConfigPageResponseVO response = new SmsTemplateMsgConfigPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        return response;
    }

    private SmsTemplateMsgConfigItemVO toItem(SmsTemplate template) {
        SmsTemplateMsgConfigItemVO item = new SmsTemplateMsgConfigItemVO();
        int sendStatus = mapSendStatus(template.getSendStatus());

        item.setSmsTemplateMsgConfigId(String.valueOf(template.getSmsTemplateId()));
        item.setType(DEFAULT_TEMPLATE_TYPE);
        item.setName(template.getTemplateTitle());
        item.setSendTimeText("");
        item.setPassContent(template.getTemplateContent());
        item.setAuditContent(formatAuditContent(template.getSignName()));
        item.setSendStatus(sendStatus);
        item.setIsEnabled(sendStatus);
        item.setSignPosition(SIGN_POSITION_PREFIX);
        item.setSignName(template.getSignName());
        item.setFixedContentPrefix(template.getTemplateContent());
        item.setFixedContentSuffix(null);
        item.setPassCustomContent("");
        item.setAuditCustomContent(null);
        item.setExampleParam(null);
        item.setAuditStatus(mapAuditStatus(template.getAuditStatus()));
        return item;
    }

    private String formatAuditContent(String signName) {
        if (signName == null || signName.isBlank()) {
            return null;
        }
        return "【" + signName + "】";
    }

    private int mapSendStatus(String sendStatus) {
        return STATUS_ENABLED.equalsIgnoreCase(sendStatus) ? 1 : 0;
    }

    private int mapAuditStatus(String auditStatus) {
        if (STATUS_APPROVED.equalsIgnoreCase(auditStatus)) {
            return 3;
        }
        if (STATUS_PENDING.equalsIgnoreCase(auditStatus)) {
            return 1;
        }
        return 0;
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
            throw new BusinessException(40301, "无权访问当前门店短信模板数据");
        }
        return requestedCampId;
    }
}

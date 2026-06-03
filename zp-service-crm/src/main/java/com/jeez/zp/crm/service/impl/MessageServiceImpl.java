package com.jeez.zp.crm.service.impl;

import com.jeez.zp.crm.dto.request.MessagePageRequest;
import com.jeez.zp.crm.exception.BusinessException;
import com.jeez.zp.crm.mapper.MessageMapper;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.service.MessageService;
import com.jeez.zp.crm.vo.MessageItemVO;
import com.jeez.zp.crm.vo.MessagePageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final MessageMapper messageMapper;
    private final CampAccessService campAccessService;

    @Override
    public MessagePageResponseVO getPage(MessagePageRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        int pageNum = normalizePageNum(request.getPage(), request.getPageNum(), request.getCurrent());
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : request.getPageSize();
        List<MessageItemVO> rows = messageMapper.selectMessages(campId, userId, trimToNull(request.getGroupType())).stream()
                .filter(row -> request.getIsRead() == null || Boolean.valueOf(request.getIsRead() == 1).equals(row.getIsRead()))
                .toList();
        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());

        MessagePageResponseVO response = new MessagePageResponseVO();
        response.setTotal((long) rows.size());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setList(rows.subList(fromIndex, toIndex));
        return response;
    }

    @Override
    public Integer getUnreadCount(Long campId, String groupType, Long userId) {
        Long resolvedCampId = campAccessService.resolveAccessibleCampId(campId, userId);
        return messageMapper.countUnread(resolvedCampId, userId, trimToNull(groupType));
    }

    @Override
    @Transactional
    public Boolean markRead(Long campId, Long messageId, Long userId) {
        campAccessService.resolveAccessibleCampId(campId, userId);
        if (messageId == null) {
            throw new BusinessException(40001, "messageId is required");
        }
        messageMapper.markRead(readId(messageId, userId), messageId, userId);
        return true;
    }

    @Override
    @Transactional
    public Boolean markAllRead(Long campId, String groupType, Long userId) {
        Long resolvedCampId = campAccessService.resolveAccessibleCampId(campId, userId);
        for (Long messageId : messageMapper.selectUnreadMessageIds(resolvedCampId, userId, trimToNull(groupType))) {
            messageMapper.markRead(readId(messageId, userId), messageId, userId);
        }
        return true;
    }

    private Long readId(Long messageId, Long userId) {
        return messageId * 100000 + userId;
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

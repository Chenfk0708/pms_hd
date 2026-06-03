package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.dto.request.MessagePageRequest;
import com.jeez.zp.crm.dto.request.MessageReadRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.MessageService;
import com.jeez.zp.crm.vo.MessagePageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemMessageController {

    private final MessageService messageService;

    @PostMapping("/systemMessage/page/get")
    public HudsonResponse<MessagePageResponseVO> getPage(@RequestBody MessagePageRequest request) {
        return HudsonResponse.success(
                messageService.getPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("system-message-page-get")
        );
    }

    @PostMapping("/systemMessage/unReadCount/get")
    public HudsonResponse<Integer> getUnreadCount(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                messageService.getUnreadCount(parseLong(request.getCampId()), request.getGroupType(), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("system-message-unread-count-get")
        );
    }

    @PostMapping("/systemMessage/read/update")
    public HudsonResponse<Boolean> markRead(@RequestBody MessageReadRequest request) {
        return HudsonResponse.success(
                messageService.markRead(parseLong(request.getCampId()), parseLong(request.getMessageId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("system-message-read-update")
        );
    }

    @PostMapping("/systemMessage/read/all")
    public HudsonResponse<Boolean> markAllRead(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                messageService.markAllRead(parseLong(request.getCampId()), request.getGroupType(), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("system-message-read-all")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}

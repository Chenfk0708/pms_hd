package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.dto.request.ImPhrasePageRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.ImSettingService;
import com.jeez.zp.crm.vo.ImWordsGroupTreeResponseVO;
import com.jeez.zp.crm.vo.ImWordsPageResponseVO;
import com.jeez.zp.crm.vo.ImYunxinUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImSettingController {

    private final ImSettingService imSettingService;

    @PostMapping("/imWordsGroup/tree/get")
    public HudsonResponse<ImWordsGroupTreeResponseVO> getPhraseGroupTree(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                imSettingService.getPhraseGroupTree(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("im-words-group-tree-get")
        );
    }

    @PostMapping("/imWords/page/get")
    public HudsonResponse<ImWordsPageResponseVO> getPhrasePage(@RequestBody ImPhrasePageRequest request) {
        return HudsonResponse.success(
                imSettingService.getPhrasePage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("im-words-page-get")
        );
    }

    @PostMapping("/imYunxinUser/get")
    public HudsonResponse<ImYunxinUserVO> getYunxinUser(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                imSettingService.getYunxinUser(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("im-yunxin-user-get")
        );
    }
}

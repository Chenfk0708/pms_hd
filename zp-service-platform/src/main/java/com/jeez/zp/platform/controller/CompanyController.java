package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CompanyInfoRequest;
import com.jeez.zp.platform.dto.request.CompanyInfoSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationUploadRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.CompanyService;
import com.jeez.zp.platform.vo.CompanyInfoVO;
import com.jeez.zp.platform.vo.CompanyQualificationUploadResultVO;
import com.jeez.zp.platform.vo.CompanyQualificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/company/info/get")
    public HudsonResponse<CompanyInfoVO> getInfo(@RequestBody CompanyInfoRequest request) {
        return HudsonResponse.success(
                companyService.getInfo(parseLong(request.getCampId()), LoginUserContext.requiredUserId(), request.getIncludeImages()),
                TraceIdFactory.next("company-info-get")
        );
    }

    @PostMapping("/company/info/save")
    public HudsonResponse<CompanyInfoVO> saveInfo(@RequestBody CompanyInfoSaveRequest request) {
        return HudsonResponse.success(
                companyService.saveInfo(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("company-info-save")
        );
    }

    @PostMapping("/company/qualification/get")
    public HudsonResponse<CompanyQualificationVO> getQualification(@RequestBody CompanyQualificationRequest request) {
        return HudsonResponse.success(
                companyService.getQualification(parseLong(request.getCampId()), LoginUserContext.requiredUserId(), request.getIncludeAssets()),
                TraceIdFactory.next("company-qualification-get")
        );
    }

    @PostMapping("/company/qualification/save")
    public HudsonResponse<CompanyQualificationVO> saveQualification(@RequestBody CompanyQualificationSaveRequest request) {
        return HudsonResponse.success(
                companyService.saveQualification(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("company-qualification-save")
        );
    }

    @PostMapping("/company/qualification/upload")
    public HudsonResponse<CompanyQualificationUploadResultVO> uploadQualification(@RequestBody CompanyQualificationUploadRequest request) {
        return HudsonResponse.success(
                companyService.uploadQualification(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("company-qualification-upload")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}

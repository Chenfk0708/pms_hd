package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.CompanyInfoSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationUploadRequest;
import com.jeez.zp.platform.vo.CompanyInfoVO;
import com.jeez.zp.platform.vo.CompanyQualificationUploadResultVO;
import com.jeez.zp.platform.vo.CompanyQualificationVO;

public interface CompanyService {

    CompanyInfoVO getInfo(Long campId, Long userId, Boolean includeImages);

    CompanyInfoVO saveInfo(CompanyInfoSaveRequest request, Long userId);

    CompanyQualificationVO getQualification(Long campId, Long userId, Boolean includeAssets);

    CompanyQualificationVO saveQualification(CompanyQualificationSaveRequest request, Long userId);

    CompanyQualificationUploadResultVO uploadQualification(CompanyQualificationUploadRequest request, Long userId);
}

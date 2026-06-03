package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.MemberSettingsBootstrapRequest;
import com.jeez.zp.platform.dto.request.MemberSettingsSaveRequest;
import com.jeez.zp.platform.dto.request.MemberWecomBindRequest;
import com.jeez.zp.platform.vo.MemberSettingMemberVO;
import com.jeez.zp.platform.vo.MemberSettingsResponseVO;

import java.util.List;

public interface MemberSettingService {

    MemberSettingsResponseVO bootstrap(MemberSettingsBootstrapRequest request, Long userId);

    List<MemberSettingMemberVO> save(MemberSettingsSaveRequest request, Long userId);

    List<MemberSettingMemberVO> bindWecom(MemberWecomBindRequest request, Long userId);
}

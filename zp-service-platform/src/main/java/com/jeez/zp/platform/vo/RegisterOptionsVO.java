package com.jeez.zp.platform.vo;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RegisterOptionsVO {

    Long campId;
    String campName;
    List<RoleSummaryVO> roles;
}

package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CampRolesResponseVO {

    private List<RoleSummaryVO> roles;
    private List<CampEmployeeOptionVO> employees;
}

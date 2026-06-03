package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthorityModuleVO {

    private String moduleName;
    private List<AuthorityItemVO> items;
}

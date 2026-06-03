package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class AuthorityItemVO {

    private String authorityId;
    private String authorityName;
    private String authorityCode;
    private Boolean excluded;
}

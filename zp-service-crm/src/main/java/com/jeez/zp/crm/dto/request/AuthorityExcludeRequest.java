package com.jeez.zp.crm.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AuthorityExcludeRequest {

    private String campId;
    private List<String> authorityIds;
}

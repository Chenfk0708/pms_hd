package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class WeComAccountsResponseVO {

    private Long total;
    private List<WeComAccountVO> accounts;
}

package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class SocialAccountsVO {
    private List<SocialAccountRowVO> list;
    private SocialPaginationVO pagination;
}

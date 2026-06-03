package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class SocialFilterOptionsVO {
    private List<SocialOptionVO> camps;
    private List<SocialOptionVO> projects;
}

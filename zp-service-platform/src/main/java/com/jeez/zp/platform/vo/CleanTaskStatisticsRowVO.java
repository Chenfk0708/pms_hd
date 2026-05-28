package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class CleanTaskStatisticsRowVO {

    private String cleanTime;
    private Long countNum;
    private Long countCost;
    private Long cleanTypeOneNum;
    private Long cleanTypeOneCost;
    private Long cleanTypeTwoNum;
    private Long cleanTypeTwoCost;
    private Long cleanTypeThreeNum;
    private Long cleanTypeThreeCost;
    private Long cleanTypeFourNum;
    private Long cleanTypeFourCost;
}

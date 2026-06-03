package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskStatisticsResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer current;
    private Integer size;
    private List<CleanTaskStatisticsRowVO> list;
}

package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PsbLogPageDataVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Boolean hasNextPage;
    private Integer pages;
    private List<PsbLogRowVO> list;
}

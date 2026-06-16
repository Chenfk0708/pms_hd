package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class ThirdPartyOtaPoiPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean hasNextPage;
    private Integer pages;
    private List<ThirdPartyOtaPoiVO> list;
}

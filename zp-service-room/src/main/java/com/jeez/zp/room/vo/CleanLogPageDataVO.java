package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanLogPageDataVO {

    private Long total;
    private List<CleanLogRowVO> list;
}

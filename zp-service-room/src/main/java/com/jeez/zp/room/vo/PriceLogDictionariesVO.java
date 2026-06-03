package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class PriceLogDictionariesVO {

    private List<PriceLogOptionVO> channels;
    private List<PriceLogOptionVO> adjustmentModes;
}

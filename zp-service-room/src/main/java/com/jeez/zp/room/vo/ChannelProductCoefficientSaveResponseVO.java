package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChannelProductCoefficientSaveResponseVO {

    private Integer savedCount;
    private List<ChannelProductCoefficientRowVO> items;
}

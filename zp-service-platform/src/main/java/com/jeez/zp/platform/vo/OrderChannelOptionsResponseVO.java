package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderChannelOptionsResponseVO {

    private List<OrderChannelOptionVO> select;
    private List<OrderChannelOptionVO> list;
}

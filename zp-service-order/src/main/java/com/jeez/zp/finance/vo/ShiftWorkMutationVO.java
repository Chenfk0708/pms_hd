package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkMutationVO {
    private String message;
    private List<ShiftWorkConfigVO> shiftConfigs;
    private List<ShiftWorkGoodsVO> goodsConfigs;
}

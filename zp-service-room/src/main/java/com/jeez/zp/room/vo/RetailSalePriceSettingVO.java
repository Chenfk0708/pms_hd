package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RetailSalePriceSettingVO {

    private Integer isInitPriceDisplay;
    private String pricePriceInterfaceDisplayType;
    private List<Object> priceSalePriceSettings;
}

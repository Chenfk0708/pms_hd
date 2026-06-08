package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class FullMarketingCommissionRowVO {
    private String productId;
    private String promotionPlanProductId;
    private String campId;
    private String name;
    private String mainPhotoMediaUrl;
    private Integer directRatio;
    private Integer parentRatio;
    private Integer type;
    private Integer state;
}

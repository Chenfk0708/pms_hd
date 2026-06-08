package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class PaymentGroupVO {
    private String paymentTypeGroupId;
    private Integer groupType;
    private String groupTypeName;
    private List<PaymentTypeVO> paymentTypes;
}

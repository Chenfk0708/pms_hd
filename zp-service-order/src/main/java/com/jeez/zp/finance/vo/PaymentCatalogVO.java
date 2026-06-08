package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class PaymentCatalogVO {
    private List<PaymentTypeVO> paymentTypes;
    private List<PaymentWayVO> paymentWays;
    private List<PaymentGroupVO> paymentGroups;
}

package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class PaymentSettingListVO {
    private Long total;
    private List<PaymentSettingVO> list;
}

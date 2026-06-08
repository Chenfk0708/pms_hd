package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class OtaDashboardVO {
    private OtaSummaryVO summary;
    private List<OtaAccountVO> accounts;
}

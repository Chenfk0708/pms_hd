package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class CustomerTagSummaryVO {

    private Integer groupCount;
    private Integer tagCount;
    private Integer coveredMembers;
    private Integer syncingGroups;
}

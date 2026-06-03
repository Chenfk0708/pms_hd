package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrmPendingItemVO {

    private String id;
    private String title;
    private String owner;
    private String dueTime;
    private String priority;
}

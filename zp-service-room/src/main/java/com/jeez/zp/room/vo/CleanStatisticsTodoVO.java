package com.jeez.zp.room.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanStatisticsTodoVO {

    private String id;
    private String title;
    private Integer count;
    private String action;
}

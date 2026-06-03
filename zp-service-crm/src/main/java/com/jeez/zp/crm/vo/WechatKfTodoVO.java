package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatKfTodoVO {

    private String id;
    private String title;
    private Integer count;
    private String action;
}

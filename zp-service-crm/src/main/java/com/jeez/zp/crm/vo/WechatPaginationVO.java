package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}

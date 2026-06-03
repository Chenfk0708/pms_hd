package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class WechatKfAccountPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<WechatKfAccountVO> list;
}

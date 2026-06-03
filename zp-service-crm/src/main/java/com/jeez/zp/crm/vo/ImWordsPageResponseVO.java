package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class ImWordsPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<ImWordsRowVO> list;
}

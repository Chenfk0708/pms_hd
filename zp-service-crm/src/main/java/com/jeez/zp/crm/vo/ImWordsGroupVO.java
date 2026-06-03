package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImWordsGroupVO {

    private String imWordsGroupId;
    private String name;
    private List<ImWordsGroupVO> children = new ArrayList<>();
}

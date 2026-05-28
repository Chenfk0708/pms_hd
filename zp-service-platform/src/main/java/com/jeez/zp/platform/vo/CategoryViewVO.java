package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryViewVO {

    private String categoryId;
    private String categoryName;
    private String id;
    private String name;
    private List<CategoryViewVO> children;
}

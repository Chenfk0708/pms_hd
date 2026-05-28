package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuProjectVO {

    private Long projectMenuId;
    private List<MenuNodeVO> menus;
    private List<MenuNodeVO> menuTree;
}

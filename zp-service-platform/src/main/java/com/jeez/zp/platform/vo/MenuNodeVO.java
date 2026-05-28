package com.jeez.zp.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuNodeVO {

    private String code;
    private String name;
    private String path;
    private List<MenuNodeVO> children;
}

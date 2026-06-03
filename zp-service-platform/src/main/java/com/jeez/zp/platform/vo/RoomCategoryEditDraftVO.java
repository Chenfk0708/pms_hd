package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryEditDraftVO {

    private String mode;
    private String title;
    private List<String> steps;
    private RoomCategoryEditFormVO form;
}

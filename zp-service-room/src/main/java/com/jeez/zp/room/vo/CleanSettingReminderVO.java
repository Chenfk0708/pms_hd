package com.jeez.zp.room.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanSettingReminderVO {

    private String id;
    private String title;
    private String description;
    private String severity;
}

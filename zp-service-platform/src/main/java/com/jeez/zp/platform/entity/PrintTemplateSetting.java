package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("print_template_setting")
public class PrintTemplateSetting {

    @TableId("print_template_setting_id")
    private Long printTemplateSettingId;
    private Long campId;
    private String sectionKey;
    private String paperType;
    private String selectedDocument;
    private String customText;
    private LocalDateTime updatedAt;
}

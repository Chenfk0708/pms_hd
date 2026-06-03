package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("media_resource")
public class MediaResource {

    @TableId("media_resource_id")
    private Long mediaResourceId;
    private Long campId;
    private Long parentId;
    private String path;
    private String name;
    private Integer isDir;
    private String format;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String url;
    private String bizType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}

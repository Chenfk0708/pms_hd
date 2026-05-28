package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_poi_rel")
public class ChannelPoiRel {

    @TableId("id")
    private Long id;
    private Long campId;
    private Long accountId;
    private Long poiId;
    private String outPoiId;
    private String syncStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

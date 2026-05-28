package com.jeez.zp.room.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("room_category_group")
public class RoomCategoryGroup {

    @TableId("group_id")
    private Long groupId;
    private Long campId;
    private Long poiId;
    private String groupName;
    private Integer sortNo;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

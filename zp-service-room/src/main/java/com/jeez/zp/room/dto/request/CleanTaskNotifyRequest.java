package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskNotifyRequest {

    private String campId;
    private List<String> taskIds;
}

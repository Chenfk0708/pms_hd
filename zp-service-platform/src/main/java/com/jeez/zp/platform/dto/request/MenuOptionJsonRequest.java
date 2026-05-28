package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MenuOptionJsonRequest {

    private List<String> menuIds;
}

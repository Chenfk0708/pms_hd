package com.jeez.zp.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSettingPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Integer total;
}

package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class OtaChannelDetailVO {
    private OtaAccountVO account;
    private List<OtaPoiRelVO> pois;
    private List<OtaRoomCategoryRelVO> roomCategories;
}

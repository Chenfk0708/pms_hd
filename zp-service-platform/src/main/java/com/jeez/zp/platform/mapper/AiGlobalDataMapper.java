package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.AiGlobalStrongReminderRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiGlobalDataMapper {

    List<AiGlobalStrongReminderRowVO> selectStrongReminderRows(@Param("campId") Long campId);
}

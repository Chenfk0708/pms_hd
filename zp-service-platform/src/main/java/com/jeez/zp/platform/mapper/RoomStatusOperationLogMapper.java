package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomStatusOperationLogQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomStatusOperationLogMapper {

    List<RoomStatusOperationLogQueryRowVO> selectRows(@Param("campId") Long campId);
}

package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.OtaAccountQueryRowVO;
import com.jeez.zp.platform.vo.OtaDetailRoomQueryRowVO;
import com.jeez.zp.platform.vo.OtaDetailStoreQueryRowVO;
import com.jeez.zp.platform.vo.OtaOptionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OtaMapper {

    List<OtaOptionVO> selectStoreOptions(@Param("campId") Long campId);

    List<OtaAccountQueryRowVO> selectAccounts(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId
    );

    List<OtaDetailStoreQueryRowVO> selectDetailStoreRows(
            @Param("campId") Long campId,
            @Param("accountIds") List<Long> accountIds
    );

    List<OtaDetailRoomQueryRowVO> selectDetailRoomRows(
            @Param("campId") Long campId,
            @Param("accountIds") List<Long> accountIds
    );
}

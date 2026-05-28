package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.WorkspaceDashboardOrderRowVO;
import com.jeez.zp.platform.vo.WorkspaceDashboardRoomRowVO;
import com.jeez.zp.platform.vo.WorkspaceOrderListRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkspaceDashboardMapper {

    List<WorkspaceDashboardRoomRowVO> selectRoomRows(
            @Param("campId") Long campId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("nextDayStart") LocalDateTime nextDayStart
    );

    List<WorkspaceDashboardOrderRowVO> selectOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );

    List<WorkspaceOrderListRowVO> selectWorkspaceOrders(
            @Param("campId") Long campId,
            @Param("keyword") String keyword
    );

    Integer countActiveRooms(@Param("campId") Long campId);

    Integer sumRoomCategoryCount(@Param("campId") Long campId);

    Integer countExceptionOrders(@Param("campId") Long campId);
}

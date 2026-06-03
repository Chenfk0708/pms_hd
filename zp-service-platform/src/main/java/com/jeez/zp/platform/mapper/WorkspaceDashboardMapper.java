package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.WorkspaceDashboardOrderRowVO;
import com.jeez.zp.platform.vo.WorkspaceDashboardRoomRowVO;
import com.jeez.zp.platform.vo.WorkspaceMemoItemVO;
import com.jeez.zp.platform.vo.WorkspaceOrderListRowVO;
import com.jeez.zp.platform.vo.WorkspaceProductDynamicRowVO;
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

    List<WorkspaceDashboardOrderRowVO> selectLifecycleOrderRows(
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

    List<WorkspaceProductDynamicRowVO> selectProductDynamics(@Param("campId") Long campId);

    Long countWorkspaceMemos(
            @Param("campId") Long campId,
            @Param("isHandle") Integer isHandle
    );

    List<WorkspaceMemoItemVO> selectWorkspaceMemos(
            @Param("campId") Long campId,
            @Param("isHandle") Integer isHandle,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    int insertWorkspaceMemo(
            @Param("memoId") Long memoId,
            @Param("campId") Long campId,
            @Param("userId") Long userId,
            @Param("content") String content
    );

    int updateWorkspaceMemoHandle(
            @Param("campId") Long campId,
            @Param("memoId") Long memoId,
            @Param("userId") Long userId,
            @Param("isHandle") Integer isHandle
    );

    WorkspaceMemoItemVO selectWorkspaceMemoById(
            @Param("campId") Long campId,
            @Param("memoId") Long memoId
    );
}

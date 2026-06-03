package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.WorkspaceAccommodationAnalysisVO;
import com.jeez.zp.platform.vo.WorkspaceBacklogItemVO;
import com.jeez.zp.platform.vo.WorkspaceCampFlowVO;
import com.jeez.zp.platform.vo.WorkspaceHomePageVO;
import com.jeez.zp.platform.vo.WorkspaceMemoItemVO;
import com.jeez.zp.platform.vo.WorkspaceMemoPageResponseVO;
import com.jeez.zp.platform.vo.WorkspaceOrdersResponseVO;

import java.util.List;

public interface WorkspaceDashboardService {

    WorkspaceHomePageVO getHomePage(Long campId, Long userId);

    WorkspaceAccommodationAnalysisVO getAccommodationAnalysis(Long campId, Long userId, String startDate, String endDate);

    WorkspaceCampFlowVO getCampFlow(Long campId, Long userId);

    WorkspaceOrdersResponseVO getOrders(Long campId, Long userId, String orderType, Integer page, Integer pageNum, Integer current, Integer pageSize, String keyword);

    WorkspaceMemoPageResponseVO getMemoPage(Long campId, Long userId, Integer page, Integer pageNum, Integer current, Integer pageSize, Integer isHandle);

    WorkspaceMemoItemVO addMemo(Long campId, Long userId, String content);

    WorkspaceMemoItemVO handleMemo(Long campId, Long userId, Long memoId, Integer isHandle);

    List<WorkspaceBacklogItemVO> getBacklogs(Long campId, Long userId);
}

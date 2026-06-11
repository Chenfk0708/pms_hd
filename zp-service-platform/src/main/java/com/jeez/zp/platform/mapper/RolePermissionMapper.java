package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RolePermissionGrantVO;
import com.jeez.zp.platform.vo.RolePermissionOptionVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;
import com.jeez.zp.platform.vo.CampEmployeeOptionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionMapper {

    List<RoleSummaryVO> selectRoleSummaries(@Param("campId") Long campId, @Param("keyword") String keyword);

    RoleSummaryVO selectRoleSummary(@Param("campId") Long campId, @Param("roleId") Long roleId);

    List<RolePermissionGrantVO> selectRolePermissionGrants(@Param("campId") Long campId, @Param("roleId") Long roleId);

    List<RolePermissionOptionVO> selectPermissionOptions();

    List<CampEmployeeOptionVO> selectCampEmployees(@Param("campId") Long campId);
}

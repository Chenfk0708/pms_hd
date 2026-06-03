package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CompanyProfileRowVO;
import com.jeez.zp.platform.vo.CompanyQualificationAssetRowVO;
import com.jeez.zp.platform.vo.CompanyQualificationRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompanyMapper {

    CompanyProfileRowVO selectProfile(@Param("campId") Long campId);

    CompanyQualificationRowVO selectQualification(
            @Param("campId") Long campId,
            @Param("documentType") String documentType
    );

    List<CompanyQualificationAssetRowVO> selectAssets(@Param("campId") Long campId);

    List<CompanyQualificationAssetRowVO> selectProfileImages(@Param("campId") Long campId);
}

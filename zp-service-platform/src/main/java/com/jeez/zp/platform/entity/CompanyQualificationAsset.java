package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_qualification_asset")
public class CompanyQualificationAsset {

    @TableId("asset_id")
    private Long assetId;
    private Long qualificationId;
    private Long campId;
    private String assetType;
    private String fileKind;
    private Long mediaId;
    private String fileName;
    private LocalDateTime uploadedAt;
}

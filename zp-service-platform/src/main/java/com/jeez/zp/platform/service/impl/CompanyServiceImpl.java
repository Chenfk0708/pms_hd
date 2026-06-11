package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.platform.dto.request.CompanyInfoSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyProfileRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationSaveRequest;
import com.jeez.zp.platform.dto.request.CompanyQualificationUploadRequest;
import com.jeez.zp.platform.entity.CompanyProfile;
import com.jeez.zp.platform.entity.CompanyQualification;
import com.jeez.zp.platform.entity.CompanyQualificationAsset;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CompanyMapper;
import com.jeez.zp.platform.mapper.CompanyProfileMapper;
import com.jeez.zp.platform.mapper.CompanyQualificationAssetEntityMapper;
import com.jeez.zp.platform.mapper.CompanyQualificationEntityMapper;
import com.jeez.zp.platform.mapper.MediaResourceMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CompanyService;
import com.jeez.zp.platform.vo.CompanyInfoImageVO;
import com.jeez.zp.platform.vo.CompanyInfoVO;
import com.jeez.zp.platform.vo.CompanyProfileRowVO;
import com.jeez.zp.platform.vo.CompanyQualificationAssetRowVO;
import com.jeez.zp.platform.vo.CompanyQualificationDocumentSectionVO;
import com.jeez.zp.platform.vo.CompanyQualificationFieldVO;
import com.jeez.zp.platform.vo.CompanyQualificationFileVO;
import com.jeez.zp.platform.vo.CompanyQualificationLegalIdentityVO;
import com.jeez.zp.platform.vo.CompanyQualificationLegalPhotoVO;
import com.jeez.zp.platform.vo.CompanyQualificationProfileVO;
import com.jeez.zp.platform.vo.CompanyQualificationRowVO;
import com.jeez.zp.platform.vo.CompanyQualificationUploadResultVO;
import com.jeez.zp.platform.vo.CompanyQualificationVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private static final int ACTIVE_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String DEFAULT_COMPANY_TYPE = "民宿";
    private static final String DEFAULT_DOCUMENT_TYPE = "resident_id_card";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> CITY_OPTIONS = List.of("深圳 / 福田", "深圳 / 宝安", "广州 / 天河", "上海 / 静安");

    private final CompanyMapper companyMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final CompanyQualificationEntityMapper companyQualificationEntityMapper;
    private final CompanyQualificationAssetEntityMapper companyQualificationAssetEntityMapper;
    private final MediaResourceMapper mediaResourceMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CompanyInfoVO getInfo(Long campId, Long userId, Boolean includeImages) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        CompanyProfileRowVO profile = companyMapper.selectProfile(resolvedCampId);
        if (profile == null) {
            return null;
        }
        return toInfoVO(profile, Boolean.TRUE.equals(includeImages) ? companyMapper.selectProfileImages(resolvedCampId) : List.of());
    }

    @Override
    @Transactional
    public CompanyInfoVO saveInfo(CompanyInfoSaveRequest request, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        CompanyProfileRequest profile = requireProfile(request.getProfile());
        upsertProfile(resolvedCampId, profile);
        return getInfo(resolvedCampId, userId, true);
    }

    @Override
    public CompanyQualificationVO getQualification(Long campId, Long userId, Boolean includeAssets) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        CompanyProfileRowVO profile = companyMapper.selectProfile(resolvedCampId);
        CompanyQualificationRowVO qualification = companyMapper.selectQualification(resolvedCampId, DEFAULT_DOCUMENT_TYPE);
        List<CompanyQualificationAssetRowVO> assets = Boolean.FALSE.equals(includeAssets)
                ? List.of()
                : companyMapper.selectAssets(resolvedCampId);
        return buildQualificationVO(profile, qualification, assets);
    }

    @Override
    @Transactional
    public CompanyQualificationVO saveQualification(CompanyQualificationSaveRequest request, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        CompanyProfileRequest profile = requireProfile(request.getProfile());

        String documentType = normalize(request.getLegalIdentity() == null ? null : request.getLegalIdentity().getDocumentType());
        if (!StringUtils.hasText(documentType)) {
            documentType = DEFAULT_DOCUMENT_TYPE;
        }
        String credentialType = InputValidationUtils.normalizeCredentialType(documentType);
        String documentNumber = normalize(request.getLegalIdentity() == null ? null : request.getLegalIdentity().getDocumentNumber());
        if (!InputValidationUtils.isValidCredential(credentialType, documentNumber)) {
            throw new BusinessException(40002, credentialErrorMessage(credentialType));
        }
        String legalPersonName = normalize(request.getLegalPersonName());
        if (StringUtils.hasText(legalPersonName) && !InputValidationUtils.isValidPersonName(legalPersonName)) {
            throw new BusinessException(40002, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        String legalPersonIdNumber = normalize(request.getLegalPersonIdNumber());
        if (!InputValidationUtils.isValidCredential(credentialType, legalPersonIdNumber)) {
            throw new BusinessException(40002, credentialErrorMessage(credentialType));
        }

        upsertProfile(resolvedCampId, profile);

        CompanyQualification qualification = findQualification(resolvedCampId, documentType);
        if (qualification == null) {
            qualification = new CompanyQualification();
            qualification.setQualificationId(IdWorker.getId());
            qualification.setCampId(resolvedCampId);
            qualification.setDocumentType(documentType);
            qualification.setStatus(ACTIVE_STATUS);
            qualification.setIsDeleted(NOT_DELETED);
        }
        qualification.setDocumentNumber(documentNumber);
        qualification.setLegalPersonName(legalPersonName);
        qualification.setLegalPersonIdNumber(legalPersonIdNumber);
        if (qualification.getLegalPersonIdNumber() == null) {
            qualification.setLegalPersonIdNumber(qualification.getDocumentNumber());
        }

        if (qualification.getCreatedAt() == null && qualification.getUpdatedAt() == null) {
            companyQualificationEntityMapper.insert(qualification);
        } else {
            companyQualificationEntityMapper.updateById(qualification);
        }
        return getQualification(resolvedCampId, userId, true);
    }

    @Override
    @Transactional
    public CompanyQualificationUploadResultVO uploadQualification(CompanyQualificationUploadRequest request, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        String target = requireText(request.getTarget(), "上传目标不能为空");
        String fileName = requireText(request.getFileName(), "文件名不能为空");
        String kind = StringUtils.hasText(request.getKind()) ? request.getKind().trim() : inferKind(fileName);

        CompanyQualification qualification = ensureQualification(resolvedCampId);
        CompanyQualificationAsset asset = new CompanyQualificationAsset();
        asset.setAssetId(IdWorker.getId());
        asset.setQualificationId(qualification.getQualificationId());
        asset.setCampId(resolvedCampId);
        asset.setAssetType(target);
        asset.setFileKind(kind);
        asset.setMediaId(resolveMediaId(request.getMediaId()));
        asset.setFileName(fileName);
        asset.setUploadedAt(LocalDateTime.now());
        companyQualificationAssetEntityMapper.insert(asset);

        CompanyQualificationFileVO file = toFileVO(asset);
        CompanyQualificationUploadResultVO result = new CompanyQualificationUploadResultVO();
        result.setFile(file);
        result.setViewModel(getQualification(resolvedCampId, userId, true));
        return result;
    }

    private CompanyInfoVO toInfoVO(CompanyProfileRowVO row, List<CompanyQualificationAssetRowVO> images) {
        CompanyInfoVO vo = new CompanyInfoVO();
        vo.setName(row.getCompanyName());
        vo.setType(row.getCompanyType());
        vo.setPhone(row.getPhone());
        vo.setCity(row.getCityText());
        vo.setAddress(row.getAddress());
        vo.setImages(images.stream().map(this::toInfoImageVO).toList());
        return vo;
    }

    private CompanyInfoImageVO toInfoImageVO(CompanyQualificationAssetRowVO row) {
        CompanyInfoImageVO image = new CompanyInfoImageVO();
        image.setId(String.valueOf(row.getAssetId()));
        image.setName(firstText(row.getFileName(), row.getMediaName(), "企业图片"));
        image.setUrl(row.getUrl());
        image.setUploadedAt(formatTime(row.getUploadedAt()));
        return image;
    }

    private CompanyQualificationVO buildQualificationVO(
            CompanyProfileRowVO profile,
            CompanyQualificationRowVO qualification,
            List<CompanyQualificationAssetRowVO> assets
    ) {
        CompanyQualificationVO vo = new CompanyQualificationVO();
        vo.setProvider("api");
        vo.setState(profile == null && qualification == null ? "empty" : "success");
        vo.setProfile(toQualificationProfile(profile));
        vo.setFields(buildFields(profile));
        vo.setCityOptions(CITY_OPTIONS);
        vo.setBusinessLicenses(buildBusinessLicenses(assets));
        vo.setLegalIdentity(buildLegalIdentity(qualification, assets));
        return vo;
    }

    private CompanyQualificationProfileVO toQualificationProfile(CompanyProfileRowVO row) {
        if (row == null) {
            return null;
        }
        CompanyQualificationProfileVO profile = new CompanyQualificationProfileVO();
        profile.setName(row.getCompanyName());
        profile.setType(row.getCompanyType());
        profile.setPhone(row.getPhone());
        profile.setCity(row.getCityText());
        profile.setAddress(row.getAddress());
        profile.setImages(List.of());
        return profile;
    }

    private List<CompanyQualificationFieldVO> buildFields(CompanyProfileRowVO profile) {
        return List.of(
                new CompanyQualificationFieldVO("企业名称", profile == null ? "暂未填写" : firstText(profile.getCompanyName(), "暂无企业名称")),
                new CompanyQualificationFieldVO("企业类型", profile == null ? "暂未填写" : firstText(profile.getCompanyType(), "暂无企业类型")),
                new CompanyQualificationFieldVO("联系电话", profile == null ? "暂未填写" : firstText(profile.getPhone(), "暂无联系电话")),
                new CompanyQualificationFieldVO("所在城市", profile == null ? "暂未填写" : firstText(profile.getCityText(), "暂无所在城市")),
                new CompanyQualificationFieldVO("详细地址", profile == null ? "暂未填写" : firstText(profile.getAddress(), "暂无详细地址"))
        );
    }

    private List<CompanyQualificationDocumentSectionVO> buildBusinessLicenses(List<CompanyQualificationAssetRowVO> assets) {
        return List.of(
                documentSection("businessLicense", "营业执照", List.of("查看示例"), "小于4MB，最多上传1张，支持jpeg、jpg、png格式", "上传", "image", 1, assets),
                documentSection("industryLicense", "商铺行业资质", List.of("公共场所许可证查看示例", "特种行业许可证查看示例", "食品经营许可证查看示例"), "小于4MB，支持jpeg、jpg、png格式", "上传", "image", 4, assets),
                documentSection("supplementLicense", "补充资质", List.of("查看示例", "行业补充资质说明"), "小于4MB，最多上传4张，支持jpeg、jpg、png格式", "上传", "image", 4, assets),
                documentSection("authorizationLetter", "商家授权承诺函", List.of("查看示例", "下载授权承诺函模板"), "小于4MB，仅支持PDF格式", "上传文件", "pdf", 1, assets)
        );
    }

    private CompanyQualificationDocumentSectionVO documentSection(
            String id,
            String title,
            List<String> links,
            String hint,
            String uploadLabel,
            String kind,
            Integer maxFiles,
            List<CompanyQualificationAssetRowVO> assets
    ) {
        CompanyQualificationDocumentSectionVO section = new CompanyQualificationDocumentSectionVO();
        section.setId(id);
        section.setTitle(title);
        section.setLinks(links);
        section.setHint(hint);
        section.setUploadLabel(uploadLabel);
        section.setKind(kind);
        section.setMaxFiles(maxFiles);
        section.setFiles(filterFiles(assets, id));
        return section;
    }

    private CompanyQualificationLegalIdentityVO buildLegalIdentity(CompanyQualificationRowVO qualification, List<CompanyQualificationAssetRowVO> assets) {
        CompanyQualificationLegalIdentityVO identity = new CompanyQualificationLegalIdentityVO();
        identity.setDocumentType(qualification == null ? DEFAULT_DOCUMENT_TYPE : qualification.getDocumentType());
        identity.setDocumentNumber(qualification == null ? "" : firstText(qualification.getDocumentNumber(), ""));
        identity.setPhotos(List.of(
                legalPhoto("legalFront", "证件人像面照片", assets),
                legalPhoto("legalBack", "证件国徽面照片", assets),
                legalPhoto("legalHandheld", "法人手持证件照", assets)
        ));
        return identity;
    }

    private CompanyQualificationLegalPhotoVO legalPhoto(String id, String label, List<CompanyQualificationAssetRowVO> assets) {
        CompanyQualificationLegalPhotoVO photo = new CompanyQualificationLegalPhotoVO();
        photo.setId(id);
        photo.setLabel(label);
        photo.setFiles(filterFiles(assets, id));
        return photo;
    }

    private List<CompanyQualificationFileVO> filterFiles(List<CompanyQualificationAssetRowVO> assets, String assetType) {
        return assets.stream()
                .filter(asset -> Objects.equals(assetType, asset.getAssetType()))
                .map(this::toFileVO)
                .toList();
    }

    private CompanyQualificationFileVO toFileVO(CompanyQualificationAsset asset) {
        CompanyQualificationFileVO file = new CompanyQualificationFileVO();
        file.setId(String.valueOf(asset.getAssetId()));
        file.setName(asset.getFileName());
        file.setKind(asset.getFileKind());
        file.setUploadedAt(formatTime(asset.getUploadedAt()));
        file.setSizeLabel("");
        return file;
    }

    private CompanyQualificationFileVO toFileVO(CompanyQualificationAssetRowVO row) {
        CompanyQualificationFileVO file = new CompanyQualificationFileVO();
        file.setId(String.valueOf(row.getAssetId()));
        file.setName(firstText(row.getFileName(), row.getMediaName(), "附件"));
        file.setKind(firstText(row.getFileKind(), "image"));
        file.setUploadedAt(formatTime(row.getUploadedAt()));
        file.setSizeLabel(formatSize(row.getSizeBytes()));
        return file;
    }

    private CompanyProfileRequest requireProfile(CompanyProfileRequest profile) {
        if (profile == null) {
            throw new BusinessException(40001, "企业资料不能为空");
        }
        if (!StringUtils.hasText(profile.getName())) {
            throw new BusinessException(40002, "企业名称不能为空");
        }
        if (!InputValidationUtils.isValidOptionalContactPhone(profile.getPhone())) {
            throw new BusinessException(40002, "联系电话格式不正确");
        }
        return profile;
    }

    private void upsertProfile(Long campId, CompanyProfileRequest request) {
        CompanyProfile profile = findProfile(campId);
        if (profile == null) {
            profile = new CompanyProfile();
            profile.setCompanyProfileId(IdWorker.getId());
            profile.setCampId(campId);
            profile.setStatus(ACTIVE_STATUS);
            profile.setIsDeleted(NOT_DELETED);
        }
        profile.setCompanyName(request.getName().trim());
        profile.setCompanyType(firstText(normalize(request.getType()), DEFAULT_COMPANY_TYPE));
        profile.setPhone(normalize(request.getPhone()));
        profile.setCityText(normalize(request.getCity()));
        profile.setAddress(normalize(request.getAddress()));

        if (profile.getCreatedAt() == null && profile.getUpdatedAt() == null) {
            companyProfileMapper.insert(profile);
        } else {
            companyProfileMapper.updateById(profile);
        }
    }

    private CompanyProfile findProfile(Long campId) {
        return companyProfileMapper.selectOne(new LambdaQueryWrapper<CompanyProfile>()
                .eq(CompanyProfile::getCampId, campId)
                .eq(CompanyProfile::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1"));
    }

    private CompanyQualification findQualification(Long campId, String documentType) {
        return companyQualificationEntityMapper.selectOne(new LambdaQueryWrapper<CompanyQualification>()
                .eq(CompanyQualification::getCampId, campId)
                .eq(CompanyQualification::getDocumentType, documentType)
                .eq(CompanyQualification::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1"));
    }

    private CompanyQualification ensureQualification(Long campId) {
        CompanyQualification qualification = findQualification(campId, DEFAULT_DOCUMENT_TYPE);
        if (qualification != null) {
            return qualification;
        }
        qualification = new CompanyQualification();
        qualification.setQualificationId(IdWorker.getId());
        qualification.setCampId(campId);
        qualification.setDocumentType(DEFAULT_DOCUMENT_TYPE);
        qualification.setStatus(ACTIVE_STATUS);
        qualification.setIsDeleted(NOT_DELETED);
        companyQualificationEntityMapper.insert(qualification);
        return qualification;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前租户企业资料");
        }
        return requestedCampId;
    }

    private Long resolveMediaId(String mediaId) {
        Long resolved = parseLong(mediaId);
        if (resolved == null) {
            return null;
        }
        if (mediaResourceMapper.selectById(resolved) == null) {
            throw new BusinessException(40003, "媒体资源不存在");
        }
        return resolved;
    }

    private String inferKind(String fileName) {
        return fileName.toLowerCase().endsWith(".pdf") ? "pdf" : "image";
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(40001, message);
        }
        return value.trim();
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Long.valueOf(value.trim());
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String firstText(String value, String fallback1, String fallback2) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return firstText(fallback1, fallback2);
    }

    private String credentialErrorMessage(String credentialType) {
        return "居民身份证".equals(credentialType) ? "居民身份证号格式不正确" : "证件号码格式不正确";
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatSize(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes <= 0) {
            return "";
        }
        double mb = sizeBytes / 1024.0 / 1024.0;
        if (mb >= 0.1) {
            return String.format(java.util.Locale.ROOT, "%.1fMB", mb);
        }
        return sizeBytes + "B";
    }
}

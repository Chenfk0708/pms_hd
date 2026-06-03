package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.platform.dto.request.PrintSettingsSaveRequest;
import com.jeez.zp.platform.entity.PrintTemplateSetting;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PrintTemplateSettingMapper;
import com.jeez.zp.platform.service.PrintSettingService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PrintDocumentOptionVO;
import com.jeez.zp.platform.vo.PrintSettingEmptyStateVO;
import com.jeez.zp.platform.vo.PrintSettingSectionVO;
import com.jeez.zp.platform.vo.PrintSettingsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrintSettingServiceImpl implements PrintSettingService {

    private static final List<PrintDocumentOptionVO> PAPER_OPTIONS = List.of(
            new PrintDocumentOptionVO("80mm", "小票（80mm）"),
            new PrintDocumentOptionVO("58mm", "小票（58mm）"),
            new PrintDocumentOptionVO("A4", "A4")
    );
    private static final List<PrintDocumentOptionVO> STAY_DOCUMENT_OPTIONS = List.of(
            new PrintDocumentOptionVO("stay-consume-short", "消费明细账单（短租）"),
            new PrintDocumentOptionVO("stay-register-short", "住宿登记账单（短租）"),
            new PrintDocumentOptionVO("stay-consume-long", "消费明细账单（长租）"),
            new PrintDocumentOptionVO("default_stay_80mm", "默认住宿打印模板")
    );
    private static final List<PrintDocumentOptionVO> RECEIPT_DOCUMENT_OPTIONS = List.of(
            new PrintDocumentOptionVO("receipt-default", "收款账单"),
            new PrintDocumentOptionVO("default_receipt_80mm", "默认收款账单模板")
    );

    private final PrintTemplateSettingMapper printTemplateSettingMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PrintSettingsResponseVO getPrintSettings(Long campId, Long userId) {
        return buildResponse(resolveAccessibleCampId(campId, userId));
    }

    @Override
    @Transactional
    public PrintSettingsResponseVO savePrintSetting(PrintSettingsSaveRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        String sectionKey = normalizeSection(request.getSection());
        String paperType = normalizePaperType(request.getPaperType());
        String selectedDocument = normalizeSelectedDocument(sectionKey, request.getSelectedDocument());
        String customText = request.getCustomText() == null ? "" : request.getCustomText().trim();
        if ("stay".equals(sectionKey) && !StringUtils.hasText(customText)) {
            throw new BusinessException(40002, "住宿打印提示文案不能为空");
        }

        PrintTemplateSetting existing = findSetting(campId, sectionKey, paperType);
        if (existing == null) {
            PrintTemplateSetting created = new PrintTemplateSetting();
            created.setPrintTemplateSettingId(IdWorker.getId());
            created.setCampId(campId);
            created.setSectionKey(sectionKey);
            created.setPaperType(paperType);
            created.setSelectedDocument(selectedDocument);
            created.setCustomText(customText);
            printTemplateSettingMapper.insert(created);
        } else {
            PrintTemplateSetting update = new PrintTemplateSetting();
            update.setPrintTemplateSettingId(existing.getPrintTemplateSettingId());
            update.setSelectedDocument(selectedDocument);
            update.setCustomText(customText);
            printTemplateSettingMapper.updateById(update);
        }

        return buildResponse(campId, sectionKey, paperType);
    }

    private PrintSettingsResponseVO buildResponse(Long campId) {
        return buildResponse(campId, null, null);
    }

    private PrintSettingsResponseVO buildResponse(Long campId, String preferredSection, String preferredPaperType) {
        List<PrintTemplateSetting> settings = printTemplateSettingMapper.selectList(new LambdaQueryWrapper<PrintTemplateSetting>()
                .eq(PrintTemplateSetting::getCampId, campId)
                .orderByAsc(PrintTemplateSetting::getSectionKey)
                .orderByAsc(PrintTemplateSetting::getPaperType)
                .orderByAsc(PrintTemplateSetting::getPrintTemplateSettingId));
        Map<String, PrintTemplateSetting> sectionSettings = chooseSectionSettings(settings, preferredSection, preferredPaperType);

        PrintSettingsResponseVO response = new PrintSettingsResponseVO();
        response.setSections(List.of(
                buildSection(
                        "stay",
                        "住宿打印",
                        "住宿打印设置",
                        "80mm",
                        "stay-consume-short",
                        STAY_DOCUMENT_OPTIONS,
                        "请填写文案",
                        "请您仔细核对金额，确认无误后签名确认，谢谢，欢迎您再次光临",
                        sectionSettings.get("stay")
                ),
                buildSection(
                        "receipt",
                        "收款账单",
                        "收款账单设置",
                        "A4",
                        "receipt-default",
                        RECEIPT_DOCUMENT_OPTIONS,
                        "请填写文案",
                        "",
                        sectionSettings.get("receipt")
                )
        ));
        response.setEmptyState(new PrintSettingEmptyStateVO(
                "当前还没有可用的打印模板配置",
                "请先恢复默认模板，再根据门店业务需要调整纸张、单据和提示文案。",
                "应用默认模板"
        ));
        return response;
    }

    private Map<String, PrintTemplateSetting> chooseSectionSettings(
            List<PrintTemplateSetting> settings,
            String preferredSection,
            String preferredPaperType
    ) {
        Map<String, PrintTemplateSetting> selected = new LinkedHashMap<>();
        for (PrintTemplateSetting setting : settings) {
            if (setting.getSectionKey() == null || selected.containsKey(setting.getSectionKey())) {
                continue;
            }
            selected.put(setting.getSectionKey(), setting);
        }
        if (preferredSection != null && preferredPaperType != null) {
            PrintTemplateSetting preferred = settings.stream()
                    .filter(item -> preferredSection.equals(item.getSectionKey()) && preferredPaperType.equals(item.getPaperType()))
                    .findFirst()
                    .orElse(null);
            if (preferred != null) {
                selected.put(preferredSection, preferred);
            }
        }
        return selected;
    }

    private PrintSettingSectionVO buildSection(
            String key,
            String title,
            String ariaLabel,
            String defaultPaperType,
            String defaultDocument,
            List<PrintDocumentOptionVO> documentOptions,
            String placeholder,
            String defaultCustomText,
            PrintTemplateSetting setting
    ) {
        PrintSettingSectionVO section = new PrintSettingSectionVO();
        section.setKey(key);
        section.setTitle(title);
        section.setAriaLabel(ariaLabel);
        section.setPaperType(setting == null || !StringUtils.hasText(setting.getPaperType()) ? defaultPaperType : setting.getPaperType());
        section.setPaperOptions(PAPER_OPTIONS);
        section.setSelectedDocument(setting == null || !StringUtils.hasText(setting.getSelectedDocument()) ? defaultDocument : setting.getSelectedDocument());
        section.setDocumentOptions(documentOptions);
        section.setCustomText(setting == null || setting.getCustomText() == null ? defaultCustomText : setting.getCustomText());
        section.setPlaceholder(placeholder);
        return section;
    }

    private PrintTemplateSetting findSetting(Long campId, String sectionKey, String paperType) {
        return printTemplateSettingMapper.selectOne(new LambdaQueryWrapper<PrintTemplateSetting>()
                .eq(PrintTemplateSetting::getCampId, campId)
                .eq(PrintTemplateSetting::getSectionKey, sectionKey)
                .eq(PrintTemplateSetting::getPaperType, paperType)
                .last("LIMIT 1"));
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
            throw new BusinessException(40301, "无权访问当前门店打印设置");
        }
        return requestedCampId;
    }

    private String normalizeSection(String section) {
        if ("stay".equals(section) || "receipt".equals(section)) {
            return section;
        }
        throw new BusinessException(40002, "section must be stay or receipt");
    }

    private String normalizePaperType(String paperType) {
        if ("80mm".equals(paperType) || "58mm".equals(paperType) || "A4".equals(paperType)) {
            return paperType;
        }
        throw new BusinessException(40002, "paperType is invalid");
    }

    private String normalizeSelectedDocument(String sectionKey, String selectedDocument) {
        if (!StringUtils.hasText(selectedDocument)) {
            throw new BusinessException(40002, "selectedDocument is required");
        }
        List<PrintDocumentOptionVO> options = "stay".equals(sectionKey) ? STAY_DOCUMENT_OPTIONS : RECEIPT_DOCUMENT_OPTIONS;
        boolean exists = options.stream().anyMatch(option -> option.getValue().equals(selectedDocument));
        if (!exists) {
            throw new BusinessException(40002, "selectedDocument is invalid");
        }
        return selectedDocument;
    }

    private Long parseLong(String value) {
        return StringUtils.hasText(value) ? Long.valueOf(value) : null;
    }
}

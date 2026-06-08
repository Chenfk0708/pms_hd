package com.jeez.zp.finance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.finance.dto.request.*;
import com.jeez.zp.finance.mapper.FinanceMapper;
import com.jeez.zp.finance.exception.BusinessException;
import com.jeez.zp.finance.service.CampAccessService;
import com.jeez.zp.finance.service.FinanceService;
import com.jeez.zp.finance.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SYSTEM_CHANNEL_STATE_CONFIG_KEY = "hudson.channels.custom.systemChannelStates";
    private static final long MIN_CUSTOM_CHANNEL_ID = 25550L;

    private final FinanceMapper financeMapper;
    private final CampAccessService campAccessService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CustomChannelDashboardVO getCustomChannelDashboard(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        return buildCustomChannelDashboard(campId);
    }

    @Override
    @Transactional
    public CustomChannelDashboardVO createCustomChannel(CustomChannelMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        String name = trimToNull(request.getName());
        String color = trimToNull(request.getColor());
        String colorName = trimToNull(request.getColorName());
        validateCustomChannelInput(name, color, colorName);

        ensureCustomChannelNameUnique(campId, name, null);
        Long customChannelId = nextCustomChannelId();
        String channelCode = generateChannelCode(name, customChannelId);
        ensureCustomChannelCodeUnique(campId, channelCode, null);
        financeMapper.insertCustomChannel(
                customChannelId,
                campId,
                name,
                channelCode,
                color,
                colorName,
                1,
                resolveOperatorName(userId),
                "已加入自定义渠道筛选与统计口径"
        );
        return buildCustomChannelDashboard(campId);
    }

    @Override
    @Transactional
    public CustomChannelDashboardVO updateCustomChannel(CustomChannelMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        if (request.getSystemChannels() != null && !request.getSystemChannels().isEmpty()) {
            saveSystemChannelStates(campId, userId, request.getSystemChannels());
            return buildCustomChannelDashboard(campId);
        }

        Long channelId = requireCustomChannelId(request.getChannelId());
        CustomChannelRowVO existing = requireCustomChannel(campId, channelId);
        Integer enabled = request.getEnabled();
        String name = trimToNull(request.getName());
        String color = trimToNull(request.getColor());
        String colorName = trimToNull(request.getColorName());

        if (enabled != null) {
            if (enabled != 0 && enabled != 1) {
                throw new BusinessException(40001, "enabled must be 0 or 1");
            }
            financeMapper.updateCustomChannel(campId, channelId, null, null, null, enabled);
            return buildCustomChannelDashboard(campId);
        }

        validateCustomChannelInput(name, color, colorName);
        ensureCustomChannelNameUnique(campId, name, channelId);
        financeMapper.updateCustomChannel(campId, channelId, name, color, colorName, null);
        return buildCustomChannelDashboard(campId);
    }

    @Override
    @Transactional
    public CustomChannelDashboardVO deleteCustomChannel(CustomChannelMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        Long channelId = requireCustomChannelId(request.getChannelId());
        requireCustomChannel(campId, channelId);
        financeMapper.softDeleteCustomChannel(campId, channelId);
        return buildCustomChannelDashboard(campId);
    }

    @Override
    public PaymentCatalogVO getPaymentWays(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        PaymentCatalogVO vo = new PaymentCatalogVO();
        vo.setPaymentWays(financeMapper.selectPaymentWays(campId));
        return vo;
    }

    @Override
    public PaymentCatalogVO getPaymentTypes(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        PaymentCatalogVO vo = new PaymentCatalogVO();
        vo.setPaymentTypes(financeMapper.selectPaymentTypes(campId));
        return vo;
    }

    @Override
    public PaymentCatalogVO getPaymentTypesV2(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        return buildPaymentTypesV2(campId);
    }

    @Override
    public PaymentCatalogVO createCustomPaymentType(PaymentTypeCreateRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        String paymentTypeName = trimToNull(request.getPaymentTypeName());
        if (paymentTypeName == null) {
            throw new BusinessException(40001, "paymentTypeName is required");
        }
        Integer groupType = request.getGroupType();
        if (groupType == null) {
            throw new BusinessException(40001, "groupType is required");
        }
        PaymentGroupVO group = financeMapper.selectPaymentGroupByType(campId, groupType);
        if (group == null) {
            throw new BusinessException(40401, "payment type group not found");
        }
        Long nextId = nextPaymentTypeId();
        financeMapper.insertPaymentType(
                nextId,
                campId,
                nextId(group),
                paymentTypeName,
                groupType,
                group.getGroupTypeName(),
                request.getBizType() == null ? 3 : request.getBizType(),
                request.getIsIncome() == null ? 1 : request.getIsIncome(),
                request.getIgnoreOrderGetItem() == null ? 1 : request.getIgnoreOrderGetItem(),
                userId
        );
        return buildPaymentTypesV2(campId);
    }

    private PaymentCatalogVO buildPaymentTypesV2(Long campId) {
        List<PaymentGroupVO> groups = financeMapper.selectPaymentGroups(campId);
        List<PaymentTypeVO> types = financeMapper.selectPaymentTypes(campId);
        for (PaymentGroupVO group : groups) {
            group.setPaymentTypes(types.stream().filter(type -> group.getGroupType().equals(type.getGroupType())).toList());
        }
        PaymentCatalogVO vo = new PaymentCatalogVO();
        vo.setPaymentGroups(groups);
        vo.setPaymentWays(financeMapper.selectPaymentWays(campId));
        return vo;
    }

    private Long nextPaymentTypeId() {
        Long maxId = financeMapper.selectMaxPaymentTypeId();
        long candidate = maxId == null ? 1L : maxId + 1L;
        return Math.max(candidate, 52050L);
    }

    private Long nextPaymentWayId() {
        Long maxId = financeMapper.selectMaxPaymentWayId();
        long candidate = maxId == null ? 1L : maxId + 1L;
        return Math.max(candidate, 58050L);
    }

    private Long nextCustomChannelId() {
        Long maxId = financeMapper.selectMaxCustomChannelId();
        long candidate = maxId == null ? 1L : maxId + 1L;
        return Math.max(candidate, MIN_CUSTOM_CHANNEL_ID);
    }

    private Long nextId(PaymentGroupVO group) {
        return Long.valueOf(group.getPaymentTypeGroupId());
    }

    @Override
    public PaymentSettingListVO listPaymentSettings(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<PaymentSettingVO> list = financeMapper.selectPaymentSettings(campId);
        list.forEach(this::parseSettingJson);
        PaymentSettingListVO vo = new PaymentSettingListVO();
        vo.setTotal((long) list.size());
        vo.setList(list);
        return vo;
    }

    @Override
    public PaymentSettingVO getPaymentSettingDetail(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        PaymentSettingVO vo = financeMapper.selectPaymentSetting(campId);
        if (vo != null) {
            parseSettingJson(vo);
        }
        return vo;
    }

    @Override
    public PaymentSettingMutationVO createPaymentSetting(PaymentSettingMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        String name = trimToNull(request.getName());
        if (name == null) {
            throw new BusinessException(40001, "name is required");
        }
        Long paymentWayId = nextPaymentWayId();
        Integer maxSortNo = financeMapper.selectMaxPaymentWaySortNo(campId);
        int sortNo = maxSortNo == null ? 1 : maxSortNo + 1;
        financeMapper.insertPaymentWay(paymentWayId, campId, name, "custom_" + paymentWayId, toPaymentWayStatus(request.getStatus()), sortNo);
        return mutationResult(String.valueOf(paymentWayId), "已新增支付方式：" + name, null);
    }

    @Override
    public PaymentSettingMutationVO updatePaymentSettingStatus(PaymentSettingMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        Long methodId = requiredMethodId(request.getMethodId());
        PaymentWayVO paymentWay = requiredPaymentWay(campId, methodId);
        Integer status = toPaymentWayStatus(request.getStatus());
        financeMapper.updatePaymentWayStatus(campId, methodId, status);
        String actionLabel = status == 1 ? "启用" : "停用";
        return mutationResult(String.valueOf(methodId), "已" + actionLabel + "支付方式：" + paymentWay.getPaymentWayName(), null);
    }

    @Override
    public PaymentSettingMutationVO updateDefaultPaymentSetting(PaymentSettingMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        Long methodId = requiredMethodId(request.getMethodId());
        PaymentWayVO paymentWay = requiredPaymentWay(campId, methodId);
        return mutationResult(String.valueOf(methodId), "已将" + paymentWay.getPaymentWayName() + "设为默认支付方式", null);
    }

    @Override
    public PaymentSettingMutationVO updatePaymentSettingSort(PaymentSettingMutationRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        Long methodId = requiredMethodId(request.getMethodId());
        String direction = trimToNull(request.getDirection());
        if (!"up".equals(direction) && !"down".equals(direction)) {
            throw new BusinessException(40001, "direction must be up or down");
        }
        PaymentWayVO current = requiredPaymentWay(campId, methodId);
        PaymentWayVO adjacent = financeMapper.selectAdjacentPaymentWay(campId, current.getSortNo(), direction);
        if (adjacent != null) {
            financeMapper.updatePaymentWaySortNo(campId, Long.valueOf(current.getPaymentWayId()), adjacent.getSortNo());
            financeMapper.updatePaymentWaySortNo(campId, Long.valueOf(adjacent.getPaymentWayId()), current.getSortNo());
        }
        String actionLabel = "up".equals(direction) ? "上移" : "下移";
        return mutationResult(String.valueOf(methodId), "已" + actionLabel + "支付方式：" + current.getPaymentWayName(), null);
    }

    @Override
    public PaymentSettingMutationVO exportPaymentSettings(PaymentSettingMutationRequest request, Long userId) {
        resolveCampId(request.getCampId(), userId);
        return mutationResult(null, "导出任务已创建", trimToNull(request.getExportAt()));
    }

    @Override
    public OrderLedgerDashboardVO getOrderLedgerDashboard(OrderLedgerDashboardRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        LocalDateTime beginTime = parseDateTime(request.getBeginTime());
        LocalDateTime endTime = parseDateTime(request.getEndTime());
        Long roomCategoryId = parseOptionalLong(request.getRoomCategoryId());
        List<OrderLedgerRecordVO> allRows = financeMapper.selectLedgerRecords(campId, beginTime, endTime, request.getIsIncome(), roomCategoryId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        int from = Math.min((pageNum - 1) * pageSize, allRows.size());
        int to = Math.min(from + pageSize, allRows.size());
        long total = defaultLong(financeMapper.countLedgerRecords(campId, beginTime, endTime, request.getIsIncome(), roomCategoryId));
        long offset = (long) (pageNum - 1) * pageSize;
        OrderLedgerSummaryVO summary = financeMapper.selectLedgerSummary(campId, beginTime, endTime, request.getIsIncome(), roomCategoryId);
        OrderLedgerDashboardVO vo = new OrderLedgerDashboardVO();
        vo.setSummary(summary);
        vo.setRecords(allRows.subList(from, to));
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setCostPricePages(toLedgerEntryPage(
                financeMapper.selectLedgerEntryRows(campId, beginTime, endTime, request.getIsIncome(), roomCategoryId, offset, pageSize),
                total,
                pageNum,
                pageSize
        ));
        vo.setIncome(summary == null ? 0D : summary.getTotalIncome());
        vo.setExpend(summary == null ? 0D : summary.getTotalExpense());
        vo.setNetIncome(summary == null ? 0D : summary.getNetIncome());
        return vo;
    }

    @Override
    public OtaDashboardVO getOtaDashboard(CampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<OtaAccountVO> accounts = financeMapper.selectOtaAccounts(campId);
        OtaSummaryVO summary = new OtaSummaryVO();
        summary.setTotalAccounts(accounts.size());
        summary.setAuthorizedAccounts((int) accounts.stream().filter(a -> "authorized".equals(a.getStatus())).count());
        summary.setLinkedPois(financeMapper.countOtaPois(campId));
        summary.setLinkedRoomCategories(financeMapper.countOtaRoomCategories(campId));
        OtaDashboardVO vo = new OtaDashboardVO();
        vo.setSummary(summary);
        vo.setAccounts(accounts);
        return vo;
    }

    @Override
    public OtaChannelDetailVO getOtaChannelDetail(OtaChannelDetailRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        Long accountId = parseLong(request.getAccountId());
        OtaChannelDetailVO vo = new OtaChannelDetailVO();
        vo.setAccount(financeMapper.selectOtaAccount(campId, accountId));
        vo.setPois(financeMapper.selectOtaPois(campId, accountId));
        vo.setRoomCategories(financeMapper.selectOtaRoomCategories(campId, accountId));
        return vo;
    }

    @Override
    public DistributionOrderPageVO getDistributionOrders(DistributionOrderPageRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        long offset = (long) (pageNum - 1) * pageSize;
        LocalDateTime bookingStart = parseOptionalDateStart(request.getBookingStartDate(), "bookingStartDate格式错误");
        LocalDateTime bookingEndExclusive = parseOptionalDateEndExclusive(request.getBookingEndDate(), "bookingEndDate格式错误");
        String keyword = trimToNull(request.getKeyword());
        String settledState = trimToNull(firstNonBlank(request.getSettledState(), request.getSettlementStatus()));
        Boolean breakTemp = request.getBreakTemp();

        long total = defaultLong(financeMapper.countDistributionOrders(campId, bookingStart, bookingEndExclusive, keyword, settledState, breakTemp));
        List<DistributionOrderVO> rows = total == 0
                ? List.of()
                : financeMapper.selectDistributionOrders(campId, bookingStart, bookingEndExclusive, keyword, settledState, breakTemp, offset, pageSize);

        DistributionOrderSummaryVO summary = summarizeDistributionOrders(rows);
        int pages = calculatePages((int) total, pageSize);

        DistributionOrderCampVO camp = new DistributionOrderCampVO();
        camp.setCampId(String.valueOf(campId));
        camp.setCampName(defaultString(financeMapper.selectCampName(campId)));

        DistributionOrderPaginationVO pagination = new DistributionOrderPaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        pagination.setPages(pages);
        pagination.setHasNextPage(pageNum < pages);

        DistributionOrderPageVO vo = new DistributionOrderPageVO();
        vo.setTotal(total);
        vo.setSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setPageNum(pageNum);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
        vo.setCamp(camp);
        vo.setSummary(summary);
        vo.setPagination(pagination);
        vo.setList(rows);
        return vo;
    }

    @Override
    public AccountBookPaymentWayPageVO getAccountBookPaymentWayPage(AccountBookPaymentWayPageRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<Long> poiIds = parseLongList(request.getPoiIds());
        LocalDate beginDate = parseDateBoundary(request.getBeginTime(), "beginTime格式错误");
        LocalDate endDate = parseDateBoundary(request.getEndTime(), "endTime格式错误");
        List<AccountBookPaymentWayQueryRowVO> queryRows = financeMapper.selectAccountBookPaymentWayRows(campId, beginDate, endDate, poiIds);

        Map<String, PaymentWayAmountVO> incomeByWay = new LinkedHashMap<>();
        Map<String, PaymentWayAmountVO> expendByWay = new LinkedHashMap<>();
        Map<String, Map<String, Long>> netCentByDate = new LinkedHashMap<>();
        Map<String, PaymentWayAmountVO> paymentWayTemplates = new LinkedHashMap<>();
        long totalIncomeCent = 0L;
        long totalExpendCent = 0L;

        for (AccountBookPaymentWayQueryRowVO row : queryRows) {
            String wayId = defaultString(row.getPaymentWayId());
            String wayName = defaultString(row.getPaymentWayName());
            long incomeCent = defaultLong(row.getIncomeAmountCent());
            long expendCent = defaultLong(row.getExpendAmountCent());
            totalIncomeCent += incomeCent;
            totalExpendCent += expendCent;
            paymentWayTemplates.putIfAbsent(wayId, paymentWayAmount(wayId, wayName, 0L));
            if (incomeCent > 0) {
                addAmount(incomeByWay, wayId, wayName, incomeCent);
            }
            if (expendCent > 0) {
                addAmount(expendByWay, wayId, wayName, expendCent);
            }
            Map<String, Long> dailyAmounts = netCentByDate.computeIfAbsent(row.getBizDate(), ignored -> new LinkedHashMap<>());
            dailyAmounts.put(wayId, defaultLong(dailyAmounts.get(wayId)) + incomeCent - expendCent);
        }

        List<PaymentWayAmountVO> paymentWays = new ArrayList<>(paymentWayTemplates.values());
        List<AccountBookPaymentWayRowVO> dataRows = new ArrayList<>();
        if (!queryRows.isEmpty()) {
            dataRows.add(accountBookRow("合计", buildPaymentWayNetAmounts(paymentWays, netCentByDate.values())));
        }
        for (Map.Entry<String, Map<String, Long>> entry : netCentByDate.entrySet()) {
            dataRows.add(accountBookRow(entry.getKey(), buildPaymentWayNetAmounts(paymentWays, List.of(entry.getValue()))));
        }

        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        List<AccountBookPaymentWayRowVO> pageRows = pageRows(dataRows, pageNum, pageSize);
        int pages = calculatePages(dataRows.size(), pageSize);

        AccountBookPaymentWayTotalInfoVO totalInfo = new AccountBookPaymentWayTotalInfoVO();
        totalInfo.setTotalIncomePrice(toAmount(totalIncomeCent));
        totalInfo.setTotalExpendPrice(toAmount(totalExpendCent));
        totalInfo.setNetIncome(toAmount(totalIncomeCent - totalExpendCent));

        AccountBookPaymentWayExtraInfoVO extraInfo = new AccountBookPaymentWayExtraInfoVO();
        extraInfo.setIncome(new ArrayList<>(incomeByWay.values()));
        extraInfo.setExpend(new ArrayList<>(expendByWay.values()));
        extraInfo.setTotalInfo(totalInfo);

        AccountBookPaymentWayPageVO vo = new AccountBookPaymentWayPageVO();
        vo.setTotal((long) dataRows.size());
        vo.setSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setPageNum(pageNum);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
        vo.setList(pageRows);
        vo.setExtraInfo(extraInfo);
        return vo;
    }

    @Override
    public Object getStatementOrders(StatementOrderRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<Long> poiIds = parseLongList(request.getPoiIds());
        if (trimToNull(request.getExportExcelMenuId()) != null) {
            return buildStatementExportUrl(campId, poiIds, request.getBookingStartDate(), request.getBookingEndDate());
        }

        int pageNum = normalizePageNum(request.getPageNum() == null ? request.getCurrent() : request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        LocalDateTime bookingStart = parseDateStart(request.getBookingStartDate(), "bookingStartDate格式错误");
        LocalDateTime bookingEndExclusive = parseDateEndExclusive(request.getBookingEndDate(), "bookingEndDate格式错误");
        long total = financeMapper.countStatementOrders(campId, poiIds, bookingStart, bookingEndExclusive, request.getBreakTemp());
        long offset = (long) (pageNum - 1) * pageSize;
        List<StatementOrderRowVO> list = total == 0
                ? List.of()
                : financeMapper.selectStatementOrders(campId, poiIds, bookingStart, bookingEndExclusive, request.getBreakTemp(), offset, pageSize)
                .stream()
                .map(this::toStatementOrderRow)
                .toList();
        int pages = calculatePages((int) total, pageSize);

        StatementOrderPageResponseVO vo = new StatementOrderPageResponseVO();
        vo.setTotal(total);
        vo.setSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setPageNum(pageNum);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
        vo.setList(list);
        return vo;
    }

    @Override
    public SocialOverviewVO getSocialOverview(SocialOverviewRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        String keyword = trimToNull(request.getKeyword());
        String status = trimToNull(request.getChannelStatus());
        String bizDate = trimToNull(request.getBizDate());
        int page = normalizePageNum(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());

        List<SocialAccountRowVO> accountRows = financeMapper.selectSocialAccounts(campId, keyword);
        accountRows.forEach(account -> account.setAuthorization(resolveSocialAuthorizations(account.getChannel())));

        List<SocialChannelVO> channels = buildSocialChannels(campId, bizDate);
        List<SocialChannelVO> filteredChannels = channels.stream()
                .filter(channel -> status == null || status.equals(channel.getStatus()))
                .filter(channel -> keyword == null || channel.getName().contains(keyword))
                .toList();

        SocialOverviewVO vo = new SocialOverviewVO();
        vo.setFilterOptions(buildSocialFilterOptions(campId));
        vo.setMetrics(buildSocialMetrics(filteredChannels));
        vo.setChannels(filteredChannels);
        vo.setTrend(buildSocialTrend(campId, bizDate));
        vo.setTodos(buildSocialTodos(filteredChannels));
        vo.setAccounts(buildSocialAccounts(accountRows, page, pageSize));
        vo.setQuickLinks(List.of(
                new SocialQuickLinkVO("房价管理", "/houseManage/houseCale"),
                new SocialQuickLinkVO("住宿订单", "/order/house-order/list"),
                new SocialQuickLinkVO("预售券订单", "/mallManagement/orderManagement")
        ));
        vo.setUpdatedAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        vo.setRequestEcho(buildSocialRequestEcho(request));
        return vo;
    }

    @Override
    public FullMarketingCommissionPageVO getFullMarketingCommissionProducts(FullMarketingCommissionPageRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        Integer type = parseTypeFilter(request.getType());
        List<FullMarketingCommissionRowVO> rows = financeMapper.selectFullMarketingCommissionRows(campId, type, trimToNull(request.getKeyword()));
        List<FullMarketingCommissionRowVO> pageRows = pageRows(rows, pageNum, pageSize);

        FullMarketingCommissionPageVO vo = new FullMarketingCommissionPageVO();
        fillFullMarketingPageFields(vo, (long) rows.size(), pageNum, pageSize);
        vo.setList(pageRows);
        vo.setRequestEcho(fullMarketingRequestEcho(campId, null, null, request.getType(), pageNum, pageSize, request.getKeyword()));
        return vo;
    }

    @Override
    public FullMarketingMetricVO getFullMarketingReport(FullMarketingReportRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        FullMarketingMetricVO metric = financeMapper.selectFullMarketingMetric(
                campId,
                trimToNull(request.getStartDate()),
                trimToNull(request.getEndDate()),
                parseTypeFilter(request.getType())
        );
        if (metric == null) {
            metric = new FullMarketingMetricVO();
        }
        metric.setTurnover(defaultDouble(metric.getTurnover()));
        metric.setCommission(defaultDouble(metric.getCommission()));
        metric.setOrderCount(metric.getOrderCount() == null ? 0 : metric.getOrderCount());
        metric.setRequestEcho(fullMarketingRequestEcho(campId, request.getStartDate(), request.getEndDate(), request.getType(), null, null, null));
        return metric;
    }

    @Override
    public FullMarketingProductSalePageVO getFullMarketingProductSales(FullMarketingReportRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        List<FullMarketingProductSaleRowVO> rows = financeMapper.selectFullMarketingProductSaleRows(
                campId,
                trimToNull(request.getStartDate()),
                trimToNull(request.getEndDate()),
                parseTypeFilter(request.getType())
        );
        List<FullMarketingProductSaleRowVO> pageRows = pageRows(rows, pageNum, pageSize);

        FullMarketingProductSalePageVO vo = new FullMarketingProductSalePageVO();
        fillFullMarketingPageFields(vo, (long) rows.size(), pageNum, pageSize);
        vo.setList(pageRows);
        vo.setRequestEcho(fullMarketingRequestEcho(campId, request.getStartDate(), request.getEndDate(), request.getType(), pageNum, pageSize, null));
        return vo;
    }

    @Override
    public ShiftWorkConfigPageVO getShiftWorkConfigs(PageCampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<ShiftWorkConfigVO> rows = financeMapper.selectShiftWorkConfigs(campId).stream()
                .map(this::toShiftWorkConfig)
                .toList();
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        List<ShiftWorkConfigVO> pageRows = pageRows(rows, pageNum, pageSize);

        ShiftWorkConfigPageVO vo = new ShiftWorkConfigPageVO();
        fillPageFields(vo, (long) rows.size(), pageNum, pageSize);
        vo.setList(pageRows);
        return vo;
    }

    @Override
    public ShiftWorkGoodsPageVO getShiftWorkGoods(PageCampRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<ShiftWorkGoodsVO> rows = financeMapper.selectShiftWorkGoods(campId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());

        ShiftWorkGoodsPageVO vo = new ShiftWorkGoodsPageVO();
        fillPageFields(vo, (long) rows.size(), pageNum, pageSize);
        vo.setList(pageRows(rows, pageNum, pageSize));
        return vo;
    }

    @Override
    @Transactional
    public ShiftWorkMutationVO saveShiftWorkConfigs(ShiftWorkConfigSaveRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<ShiftWorkConfigDraftRequest> drafts = request.getDrafts();
        if (drafts == null || drafts.isEmpty()) {
            throw new BusinessException(40001, "drafts is required");
        }

        List<Long> retainedIds = new ArrayList<>();
        long nextId = nextShiftWorkConfigId();
        Set<String> names = new LinkedHashSet<>();
        for (ShiftWorkConfigDraftRequest draft : drafts) {
            Long shiftConfigId = parseOptionalLong(draft.getId());
            if (shiftConfigId == null) {
                shiftConfigId = nextId++;
            }
            String name = requiredText(draft.getName(), "name is required");
            if (!names.add(name)) {
                throw new BusinessException(40001, "duplicate shift config name: " + name);
            }
            if (financeMapper.countActiveShiftWorkConfigByName(campId, name, shiftConfigId) > 0) {
                throw new BusinessException(40002, "shift config name already exists: " + name);
            }
            String startTime = normalizeClockTime(draft.getStartTime(), "startTime is required");
            String endTime = normalizeClockTime(draft.getEndTime(), "endTime is required");
            List<String> memberIds = normalizeMemberIds(draft.getMemberIds());
            if (memberIds.isEmpty()) {
                throw new BusinessException(40001, "memberIds is required");
            }
            for (String memberId : memberIds) {
                Long parsedMemberId = parseLong(memberId);
                if (parsedMemberId == null || financeMapper.countActiveCampMemberByUserId(campId, parsedMemberId) == 0) {
                    throw new BusinessException(40003, "shift member not found: " + memberId);
                }
            }

            financeMapper.upsertShiftWorkConfig(
                    shiftConfigId,
                    campId,
                    name,
                    startTime,
                    endTime,
                    toJson(memberIds)
            );
            retainedIds.add(shiftConfigId);
        }
        financeMapper.disableMissingShiftWorkConfigs(campId, retainedIds);

        ShiftWorkMutationVO vo = new ShiftWorkMutationVO();
        vo.setMessage("shift work configs saved");
        vo.setShiftConfigs(getAllShiftWorkConfigs(campId));
        return vo;
    }

    @Override
    @Transactional
    public ShiftWorkMutationVO saveShiftWorkGoods(ShiftWorkGoodsSaveRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<ShiftWorkGoodsDraftRequest> drafts = request.getDrafts();
        if (drafts == null || drafts.isEmpty()) {
            throw new BusinessException(40001, "drafts is required");
        }

        List<Long> retainedIds = new ArrayList<>();
        long nextId = nextShiftWorkGoodsId();
        Set<String> names = new LinkedHashSet<>();
        for (int index = 0; index < drafts.size(); index++) {
            ShiftWorkGoodsDraftRequest draft = drafts.get(index);
            Long shiftGoodsId = parseOptionalLong(draft.getId());
            if (shiftGoodsId == null) {
                shiftGoodsId = nextId++;
            }
            String name = requiredText(draft.getName(), "name is required");
            if (!names.add(name)) {
                throw new BusinessException(40001, "duplicate shift goods name: " + name);
            }
            if (financeMapper.countActiveShiftWorkGoodsByName(campId, name, shiftGoodsId) > 0) {
                throw new BusinessException(40002, "shift goods name already exists: " + name);
            }
            financeMapper.upsertShiftWorkGoods(shiftGoodsId, campId, name, index + 1);
            retainedIds.add(shiftGoodsId);
        }
        financeMapper.disableMissingShiftWorkGoods(campId, retainedIds);

        ShiftWorkMutationVO vo = new ShiftWorkMutationVO();
        vo.setMessage("shift work goods saved");
        vo.setGoodsConfigs(financeMapper.selectShiftWorkGoods(campId));
        return vo;
    }

    @Override
    public ShiftWorkReportPageVO getShiftWorkReports(ShiftWorkReportPageRequest request, Long userId) {
        Long campId = resolveCampId(request.getCampId(), userId);
        List<ShiftWorkReportRowVO> rows = financeMapper.selectShiftWorkReports(
                campId,
                trimToNull(request.getStartDate()),
                trimToNull(request.getEndDate()),
                parseOptionalLong(request.getHandoverUserId()),
                parseOptionalLong(request.getReceiverUserId())
        );
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        List<ShiftWorkReportRowVO> pageRows = pageRows(rows, pageNum, pageSize);
        int pages = calculatePages(rows.size(), pageSize);

        ShiftWorkReportPageVO vo = new ShiftWorkReportPageVO();
        vo.setRows(pageRows);
        vo.setList(pageRows);
        vo.setTotal((long) rows.size());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
        vo.setPagination(new ShiftWorkPaginationVO((long) rows.size(), pageSize, pageNum, pageNum, pageNum < pages, pages));
        return vo;
    }

    private List<ShiftWorkConfigVO> getAllShiftWorkConfigs(Long campId) {
        return financeMapper.selectShiftWorkConfigs(campId).stream()
                .map(this::toShiftWorkConfig)
                .toList();
    }

    private long nextShiftWorkConfigId() {
        Long maxId = financeMapper.selectMaxShiftWorkConfigId();
        long candidate = maxId == null ? 1L : maxId + 1L;
        return Math.max(candidate, 57350L);
    }

    private long nextShiftWorkGoodsId() {
        Long maxId = financeMapper.selectMaxShiftWorkGoodsId();
        long candidate = maxId == null ? 1L : maxId + 1L;
        return Math.max(candidate, 57450L);
    }

    private String requiredText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, message);
        }
        return normalized;
    }

    private String normalizeClockTime(String value, String message) {
        String normalized = requiredText(value, message);
        if (!normalized.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
            throw new BusinessException(40001, message.replace("required", "invalid"));
        }
        return normalized.length() == 5 ? normalized + ":00" : normalized;
    }

    private List<String> normalizeMemberIds(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::trimToNull)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(50001, "failed to serialize shift work payload");
        }
    }

    private Long resolveCampId(String campId, Long userId) {
        return campAccessService.resolveAccessibleCampId(parseLong(campId), userId);
    }

    private SocialFilterOptionsVO buildSocialFilterOptions(Long campId) {
        SocialFilterOptionsVO options = new SocialFilterOptionsVO();
        options.setCamps(List.of(
                new SocialOptionVO("全部门店", "all"),
                new SocialOptionVO("当前门店", String.valueOf(campId))
        ));
        options.setProjects(List.of(
                new SocialOptionVO("全部项目", "all"),
                new SocialOptionVO("日历房", "calendar-room"),
                new SocialOptionVO("预售券", "presale-ticket")
        ));
        return options;
    }

    private List<SocialChannelVO> buildSocialChannels(Long campId, String bizDate) {
        return List.of(
                buildSocialChannel(campId, 17001L, "douyin-lk", "抖音来客", "blue", bizDate),
                pendingSocialChannel("xiaohongshu", "小红书", "red", List.of("内容种草", "活动引流")),
                pendingSocialChannel("shipinhao", "视频号", "green", List.of("直播带货", "短视频")),
                pendingSocialChannel("douyin-special", "抖音特价酒店", "orange", List.of("特价房", "闪促活动"))
        );
    }

    private SocialChannelVO buildSocialChannel(Long campId, Long channelId, String id, String name, String accent, String bizDate) {
        int roomTypeCount = defaultInt(financeMapper.countChannelRoomCategories(campId, channelId));
        int linkedRoomTypeCount = defaultInt(financeMapper.countLinkedChannelRoomCategories(campId, channelId));
        int dailyOrders = defaultInt(financeMapper.countOrdersByChannelAndBizDate(campId, channelId, bizDate));
        boolean connected = linkedRoomTypeCount > 0 || dailyOrders > 0;

        SocialChannelVO channel = new SocialChannelVO();
        channel.setId(id);
        channel.setName(name);
        channel.setStatus(connected ? "connected" : "pending");
        channel.setRelation(connected ? "关联房型" + linkedRoomTypeCount + "/" + roomTypeCount : "待开通");
        channel.setSupport(List.of("日历房", "预售券"));
        channel.setAction(connected ? "管理渠道" : "订阅开通");
        channel.setAccent(accent);
        channel.setConversionRate(dailyOrders > 0 ? "100%" : "-");
        channel.setRoomTypeCount(roomTypeCount);
        channel.setLinkedRoomTypeCount(linkedRoomTypeCount);
        channel.setDailyOrders(dailyOrders);
        channel.setPendingTasks(buildSocialPendingTasks(roomTypeCount, linkedRoomTypeCount, dailyOrders));
        return channel;
    }

    private SocialChannelVO pendingSocialChannel(String id, String name, String accent, List<String> support) {
        SocialChannelVO channel = new SocialChannelVO();
        channel.setId(id);
        channel.setName(name);
        channel.setStatus("pending");
        channel.setRelation("待开通");
        channel.setSupport(support);
        channel.setAction("订阅开通");
        channel.setAccent(accent);
        channel.setConversionRate("-");
        channel.setRoomTypeCount(0);
        channel.setLinkedRoomTypeCount(0);
        channel.setDailyOrders(0);
        channel.setPendingTasks(List.of("确认订阅方案"));
        return channel;
    }

    private List<String> buildSocialPendingTasks(int roomTypeCount, int linkedRoomTypeCount, int dailyOrders) {
        List<String> tasks = new ArrayList<>();
        int pendingRoomTypes = Math.max(roomTypeCount - linkedRoomTypeCount, 0);
        if (pendingRoomTypes > 0) {
            tasks.add(pendingRoomTypes + " 个房型待授权");
        }
        if (dailyOrders == 0) {
            tasks.add("检查今日订单同步");
        }
        return tasks;
    }

    private List<String> resolveSocialAuthorizations(String channelName) {
        if (channelName != null && channelName.contains("抖音")) {
            return List.of("酒店行业预售券解决方案", "酒店行业日历房解决方案");
        }
        return List.of("社媒渠道运营");
    }

    private List<SocialMetricVO> buildSocialMetrics(List<SocialChannelVO> channels) {
        int connectedCount = (int) channels.stream().filter(channel -> "connected".equals(channel.getStatus())).count();
        int pendingCount = Math.max(channels.size() - connectedCount, 0);
        int dailyOrders = channels.stream().map(SocialChannelVO::getDailyOrders).mapToInt(this::defaultInt).sum();
        int taskCount = channels.stream()
                .map(SocialChannelVO::getPendingTasks)
                .filter(tasks -> tasks != null)
                .mapToInt(List::size)
                .sum();

        List<SocialMetricVO> metrics = new ArrayList<>();
        metrics.add(socialMetric("已直连渠道", String.valueOf(connectedCount), "实时渠道账户统计", "blue"));
        metrics.add(socialMetric("待开通渠道", String.valueOf(pendingCount), "根据当前筛选统计", "orange"));
        metrics.add(socialMetric("今日渠道订单", String.valueOf(dailyOrders), "来自订单真实数据", "green"));
        metrics.add(socialMetric("待处理事项", String.valueOf(taskCount), "渠道同步待办", "red"));
        return metrics;
    }

    private SocialMetricVO socialMetric(String label, String value, String change, String tone) {
        SocialMetricVO metric = new SocialMetricVO();
        metric.setLabel(label);
        metric.setValue(value);
        metric.setChange(change);
        metric.setTone(tone);
        return metric;
    }

    private List<SocialTrendPointVO> buildSocialTrend(Long campId, String bizDate) {
        SocialTrendPointVO point = new SocialTrendPointVO();
        point.setLabel(bizDate == null || bizDate.length() < 10 ? "today" : bizDate.substring(5));
        point.setDouyin(defaultInt(financeMapper.countOrdersByChannelAndBizDate(campId, 17001L, bizDate)));
        point.setXiaohongshu(0);
        point.setShipinhao(0);
        return List.of(point);
    }

    private List<SocialTodoVO> buildSocialTodos(List<SocialChannelVO> channels) {
        List<SocialTodoVO> todos = new ArrayList<>();
        int index = 1;
        for (SocialChannelVO channel : channels) {
            if (channel.getPendingTasks() == null) {
                continue;
            }
            for (String task : channel.getPendingTasks()) {
                SocialTodoVO todo = new SocialTodoVO();
                todo.setId("social-todo-" + index++);
                todo.setTitle(task);
                todo.setChannel(channel.getName());
                todo.setPriority("高");
                todo.setDueText("今日");
                todos.add(todo);
            }
        }
        return todos;
    }

    private SocialAccountsVO buildSocialAccounts(List<SocialAccountRowVO> rows, int page, int pageSize) {
        SocialAccountsVO accounts = new SocialAccountsVO();
        accounts.setList(pageRows(rows, page, pageSize));
        accounts.setPagination(new SocialPaginationVO(page, pageSize, (long) rows.size()));
        return accounts;
    }

    private Map<String, Object> buildSocialRequestEcho(SocialOverviewRequest request) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("bizDate", request.getBizDate());
        echo.put("campId", request.getCampId());
        echo.put("projectId", request.getProjectId());
        echo.put("channelStatus", request.getChannelStatus());
        echo.put("keyword", request.getKeyword());
        echo.put("page", request.getPage());
        echo.put("pageSize", request.getPageSize());
        return echo;
    }

    private Map<String, Object> fullMarketingRequestEcho(Long campId, String startDate, String endDate, String type, Integer pageNum, Integer pageSize, String keyword) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("campId", String.valueOf(campId));
        echo.put("startDate", startDate);
        echo.put("endDate", endDate);
        echo.put("type", type);
        echo.put("pageNum", pageNum);
        echo.put("pageSize", pageSize);
        echo.put("keyword", keyword);
        return echo;
    }

    private Integer parseTypeFilter(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || "all".equalsIgnoreCase(normalized)) {
            return null;
        }
        if ("calendar".equalsIgnoreCase(normalized)) {
            return 0;
        }
        if ("presale".equalsIgnoreCase(normalized)) {
            return 1;
        }
        return Integer.valueOf(normalized);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.valueOf(value);
    }

    private Long requiredMethodId(String value) {
        Long methodId = parseLong(value);
        if (methodId == null) {
            throw new BusinessException(40001, "methodId is required");
        }
        return methodId;
    }

    private Long parseOptionalLong(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || "all".equalsIgnoreCase(normalized)) {
            return null;
        }
        return Long.valueOf(normalized);
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::parseOptionalLong)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    private LocalDate parseDateBoundary(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            if (normalized.length() >= 10) {
                return LocalDate.parse(normalized.substring(0, 10));
            }
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, message);
        }
    }


private LocalDateTime parseOptionalDateStart(String value, String message) {
    String normalized = trimToNull(value);
    return normalized == null ? null : parseDateStart(normalized, message);
}

private LocalDateTime parseOptionalDateEndExclusive(String value, String message) {
    String normalized = trimToNull(value);
    return normalized == null ? null : parseDateEndExclusive(normalized, message);
}

    private LocalDateTime parseDateStart(String value, String message) {
        LocalDate date = parseDateBoundary(value, message);
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime parseDateEndExclusive(String value, String message) {
        LocalDate date = parseDateBoundary(value, message);
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private String buildStatementExportUrl(Long campId, List<Long> poiIds, String bookingStartDate, String bookingEndDate) {
        String poiScope = poiIds.isEmpty()
                ? "all"
                : poiIds.stream().map(String::valueOf).reduce((left, right) -> left + "-" + right).orElse("all");
        String start = normalizeDateLabel(bookingStartDate, "all-start");
        String end = normalizeDateLabel(bookingEndDate, "all-end");
        return "/downloads/report-storer-statement/" + campId + "/" + poiScope + "/" + start + "_" + end + ".xlsx";
    }

    private String normalizeDateLabel(String value, String fallback) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return fallback;
        }
        return parseDateBoundary(normalized, "日期格式错误").toString();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private CustomChannelDashboardVO buildCustomChannelDashboard(Long campId) {
        CustomChannelDashboardVO vo = new CustomChannelDashboardVO();
        vo.setSystemChannels(buildSystemChannels(campId));
        vo.setCustomChannels(financeMapper.selectCustomChannels(campId).stream().map(this::toCustomChannelRecord).toList());
        return vo;
    }

    private List<SystemChannelVO> buildSystemChannels(Long campId) {
        Map<String, Boolean> states = readSystemChannelStates(campId);
        List<SystemChannelVO> channels = new ArrayList<>();
        for (int index = 0; index < SYSTEM_CHANNEL_NAMES.size(); index++) {
            SystemChannelVO channel = new SystemChannelVO();
            String id = "system-" + String.format(Locale.ROOT, "%03d", index + 1);
            channel.setId(id);
            channel.setName(SYSTEM_CHANNEL_NAMES.get(index));
            channel.setColor(SYSTEM_CHANNEL_COLORS.get(index));
            channel.setEnabled(states.getOrDefault(id, true));
            channels.add(channel);
        }
        return channels;
    }

    private Map<String, Boolean> readSystemChannelStates(Long campId) {
        String rawConfig = financeMapper.selectSystemChannelStatesConfig(campId, SYSTEM_CHANNEL_STATE_CONFIG_KEY);
        if (rawConfig == null || rawConfig.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rawConfig, new TypeReference<Map<String, Boolean>>() {});
        } catch (JsonProcessingException ex) {
            return Map.of();
        }
    }

    private void saveSystemChannelStates(Long campId, Long userId, List<SystemChannelStateRequest> systemChannels) {
        Map<String, Boolean> states = new LinkedHashMap<>();
        for (SystemChannelStateRequest item : systemChannels) {
            String id = trimToNull(item.getId());
            if (id == null || item.getEnabled() == null) {
                continue;
            }
            states.put(id, item.getEnabled());
        }
        Long systemConfigId = financeMapper.selectSystemConfigId(campId, SYSTEM_CHANNEL_STATE_CONFIG_KEY);
        if (systemConfigId == null) {
            systemConfigId = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        }
        financeMapper.upsertSystemChannelStatesConfig(campId, SYSTEM_CHANNEL_STATE_CONFIG_KEY, writeJson(states), userId, systemConfigId);
    }

    private CustomChannelRecordVO toCustomChannelRecord(CustomChannelRowVO row) {
        CustomChannelRecordVO vo = new CustomChannelRecordVO();
        vo.setId(row.getId());
        vo.setName(row.getName());
        vo.setCode(row.getCode());
        vo.setColor(row.getColor());
        vo.setColorName(row.getColorName());
        vo.setEnabled(defaultInt(row.getStatus()) == 1);
        vo.setUpdatedAt(row.getUpdatedAt());
        vo.setOperator(defaultString(row.getOperator()));
        vo.setNote(defaultString(row.getNote()));
        return vo;
    }

    private void validateCustomChannelInput(String name, String color, String colorName) {
        if (name == null) {
            throw new BusinessException(40001, "name is required");
        }
        if (color == null) {
            throw new BusinessException(40001, "color is required");
        }
        if (colorName == null) {
            throw new BusinessException(40001, "colorName is required");
        }
    }

    private void ensureCustomChannelNameUnique(Long campId, String name, Long excludedId) {
        if (defaultLong(financeMapper.countCustomChannelName(campId, name, excludedId)) > 0) {
            throw new BusinessException(40009, "custom channel name already exists");
        }
    }

    private void ensureCustomChannelCodeUnique(Long campId, String code, Long excludedId) {
        if (defaultLong(financeMapper.countCustomChannelCode(campId, code, excludedId)) > 0) {
            throw new BusinessException(40009, "custom channel code already exists");
        }
    }

    private String generateChannelCode(String name, Long customChannelId) {
        String slug = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase(Locale.ROOT);
        if (slug.isBlank()) {
            slug = "custom";
        }
        return slug + "_" + customChannelId;
    }

    private Long requireCustomChannelId(String value) {
        Long channelId = parseLong(value);
        if (channelId == null) {
            throw new BusinessException(40001, "channelId is required");
        }
        return channelId;
    }

    private CustomChannelRowVO requireCustomChannel(Long campId, Long channelId) {
        CustomChannelRowVO row = financeMapper.selectCustomChannel(campId, channelId);
        if (row == null) {
            throw new BusinessException(40404, "custom channel not found");
        }
        return row;
    }

    private String resolveOperatorName(Long userId) {
        return userId == null ? "系统" : "用户" + userId;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "failed to serialize json");
        }
    }


private String firstNonBlank(String first, String second) {
    String normalizedFirst = trimToNull(first);
    return normalizedFirst != null ? normalizedFirst : trimToNull(second);
}

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private double toAmount(Long centValue) {
        if (centValue == null) {
            return 0D;
        }
        return BigDecimal.valueOf(centValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private PaymentWayAmountVO paymentWayAmount(String paymentWayId, String paymentWayName, Long amountCent) {
        PaymentWayAmountVO vo = new PaymentWayAmountVO();
        vo.setPaymentWayId(paymentWayId);
        vo.setPaymentWayName(paymentWayName);
        vo.setPrice(toAmount(amountCent));
        return vo;
    }

    private void addAmount(Map<String, PaymentWayAmountVO> amounts, String paymentWayId, String paymentWayName, long amountCent) {
        PaymentWayAmountVO current = amounts.get(paymentWayId);
        if (current == null) {
            amounts.put(paymentWayId, paymentWayAmount(paymentWayId, paymentWayName, amountCent));
            return;
        }
        current.setPrice(roundMoney(defaultDouble(current.getPrice()) + toAmount(amountCent)));
    }

    private List<PaymentWayAmountVO> buildPaymentWayNetAmounts(List<PaymentWayAmountVO> paymentWays, Iterable<Map<String, Long>> amountSources) {
        Map<String, Long> totalByWay = new LinkedHashMap<>();
        for (Map<String, Long> source : amountSources) {
            for (Map.Entry<String, Long> entry : source.entrySet()) {
                totalByWay.put(entry.getKey(), defaultLong(totalByWay.get(entry.getKey())) + defaultLong(entry.getValue()));
            }
        }
        List<PaymentWayAmountVO> amounts = new ArrayList<>();
        for (PaymentWayAmountVO way : paymentWays) {
            amounts.add(paymentWayAmount(way.getPaymentWayId(), way.getPaymentWayName(), defaultLong(totalByWay.get(way.getPaymentWayId()))));
        }
        return amounts;
    }

    private AccountBookPaymentWayRowVO accountBookRow(String date, List<PaymentWayAmountVO> amounts) {
        AccountBookPaymentWayRowVO vo = new AccountBookPaymentWayRowVO();
        vo.setDate(date);
        vo.setPaymentWayPriceDetailViews(amounts);
        return vo;
    }

    private LedgerEntryPageVO toLedgerEntryPage(List<LedgerEntryRowVO> rows, long total, int pageNum, int pageSize) {
        int pages = calculatePages((int) total, pageSize);
        LedgerEntryPageVO page = new LedgerEntryPageVO();
        page.setTotal(total);
        page.setSize(pageSize);
        page.setCurrent(pageNum);
        page.setExtraInfo(null);
        page.setPageNum(pageNum);
        page.setHasNextPage(pageNum < pages);
        page.setPages(pages);
        page.setList(rows);
        return page;
    }


private DistributionOrderSummaryVO summarizeDistributionOrders(List<DistributionOrderVO> rows) {
    double paidAmount = 0D;
    double serviceFee = 0D;
    double settlementAmount = 0D;
    double settledAmount = 0D;
    for (DistributionOrderVO row : rows) {
        paidAmount += defaultDouble(row.getPaidAmount());
        serviceFee += defaultDouble(row.getServiceFee());
        settlementAmount += defaultDouble(row.getSettlementAmount());
        settledAmount += defaultDouble(row.getSettledAmount());
    }
    DistributionOrderSummaryVO summary = new DistributionOrderSummaryVO();
    summary.setPaidAmount(roundAmount(paidAmount));
    summary.setServiceFee(roundAmount(serviceFee));
    summary.setSettlementAmount(roundAmount(settlementAmount));
    summary.setSettledAmount(roundAmount(settledAmount));
    summary.setInvoicePrice(summary.getPaidAmount());
    summary.setCommission(summary.getServiceFee());
    summary.setIncomePrice(summary.getSettlementAmount());
    summary.setSettledPrice(summary.getSettledAmount());
    return summary;
}

    private StatementOrderRowVO toStatementOrderRow(StatementOrderQueryRowVO row) {
        StatementOrderRowVO item = new StatementOrderRowVO();
        item.setOrderId(defaultString(row.getOrderId()));
        item.setOrderNo(defaultString(row.getOrderNo()));
        item.setCustomerInfo(defaultString(row.getCustomerInfo()));
        item.setCustomerName(defaultString(row.getCustomerName()));
        item.setMobile(defaultString(row.getMobile()));
        item.setProductType(defaultString(row.getProductType()));
        item.setProductTypeName(defaultString(row.getProductTypeName()));
        item.setProductName(defaultString(row.getProductName()));
        item.setBookingTime(defaultString(row.getBookingTime()));
        item.setBookingTimeStr(defaultString(row.getBookingTimeStr()));
        item.setChannelName(defaultString(row.getChannelName()));
        item.setPayableAmount(toAmount(row.getPayableAmountCent()));
        item.setPaidAmount(toAmount(row.getPaidAmountCent()));
        item.setDiscountAmount(toAmount(row.getDiscountAmountCent()));
        item.setRefundAmount(toAmount(row.getRefundAmountCent()));
        item.setPaymentFee(toAmount(row.getPaymentFeeCent()));
        item.setPlatformServiceFee(toAmount(row.getPlatformServiceFeeCent()));
        item.setDistributorCommission(toAmount(row.getDistributorCommissionCent()));
        item.setPaymentWayName(defaultString(row.getPaymentWayName()));
        item.setSettlementAmount(toAmount(row.getSettlementAmountCent()));
        return item;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private double roundAmount(double value) {
        return roundMoney(value);
    }

    private double roundMoney(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static final List<String> SYSTEM_CHANNEL_NAMES = List.of(
            "自来客", "路客云聚合", "携程", "美团", "飞猪", "去哪儿", "同程旅行", "马蜂窝", "途家民宿", "小猪民宿",
            "爱彼迎", "榛果民宿", "木鸟民宿", "一家民宿", "住多多", "Airbnb", "微信", "电话", "官网", "58同城", "抖音", "小红书",
            "快手", "视频号", "微信小程序", "支付宝小程序", "公众号", "企业协议", "会员", "门店散客", "长租客户", "团购", "美团酒店", "Agoda",
            "Booking", "Expedia", "Homeaway", "Vrbo", "去哪儿民宿", "携程民宿", "飞猪民宿", "同程艺龙", "抖音团购", "快手团购", "微信商城",
            "本地生活联盟", "渠道分销", "旅行社", "公司协议", "客服代订", "PMS同步RW", "路客优选", "渠道同步", "云渠道YZ",
            "云渠道ZD", "云渠道PY", "云渠道CQ", "社群", "私域分销", "民宿管家", "渠道MO", "渠道PC", "小程序", "美宿联盟",
            "渠道YK", "散客", "线下订单", "Hotelbeds", "MG", "渠道KT", "渠道LM"
    );

    private static final List<String> SYSTEM_CHANNEL_COLORS = List.of(
            "#20527f",
            "#6f89d1", "#263f86", "#ffc20c", "#08a6c8", "#ff6827", "#ff6a21", "#ff5561", "#edc36b", "#f0c46a", "#edc36b",
            "#fb3d70", "#ff792b", "#6ed331", "#801d72", "#ff1e3b", "#f52325", "#cf3737", "#0868e5", "#24c2df", "#ff7900",
            "#3268e4", "#20a719", "#f00000", "#e6291f", "#ff2814", "#ff0635", "#ff6841", "#40516a", "#13aee0", "#fc1d4e",
            "#5057df", "#ffe000", "#801d72", "#d9d9d9", "#d9461c", "#0076b6", "#095fe0", "#d9d9d9", "#bfc8d8", "#108df0",
            "#211c1d", "#314f88", "#ff9018", "#d79c2c", "#e25747", "#09bd72", "#1cc8df", "#0cbc72", "#07c676", "#14b7c1",
            "#0db5bf", "#08bd69", "#20527f", "#20527f", "#20527f", "#20527f", "#20527f", "#22539a", "#18cbed", "#1dc7ef",
            "#d9d9d9", "#1f5284", "#08ba65", "#0fb8de", "#20527f", "#20527f", "#20527f", "#20527f", "#20509a", "#20509a"
    );

    private Integer toPaymentWayStatus(String value) {
        String status = trimToNull(value);
        if ("enabled".equals(status)) {
            return 1;
        }
        if ("disabled".equals(status)) {
            return 0;
        }
        throw new BusinessException(40001, "status must be enabled or disabled");
    }

    private PaymentWayVO requiredPaymentWay(Long campId, Long paymentWayId) {
        PaymentWayVO paymentWay = financeMapper.selectPaymentWay(campId, paymentWayId);
        if (paymentWay == null) {
            throw new BusinessException(40404, "payment method not found");
        }
        return paymentWay;
    }

    private PaymentSettingMutationVO mutationResult(String methodId, String message, String exportAt) {
        PaymentSettingMutationVO vo = new PaymentSettingMutationVO();
        vo.setMethodId(methodId);
        vo.setMessage(message);
        vo.setExportAt(exportAt);
        return vo;
    }

    private void parseSettingJson(PaymentSettingVO vo) {
        vo.setNightAudit(parseJsonObject(vo.getNightAudit()));
        vo.setAmortize(parseJsonObject(vo.getAmortize()));
        vo.setVendible(parseJsonObject(vo.getVendible()));
    }

    private Object parseJsonObject(Object value) {
        if (!(value instanceof String text) || text.isBlank()) {
            return value;
        }
        try {
            return objectMapper.readValue(text, Object.class);
        } catch (JsonProcessingException ex) {
            return value;
        }
    }

    private ShiftWorkConfigVO toShiftWorkConfig(ShiftWorkConfigRowVO row) {
        ShiftWorkConfigVO vo = new ShiftWorkConfigVO();
        vo.setId(row.getId());
        vo.setShiftWorkConfigId(row.getId());
        vo.setName(row.getName());
        vo.setShiftName(row.getName());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        vo.setMemberIds(parseStringArray(row.getMemberIdsJson()));
        vo.setMemberNames(splitNames(row.getMemberNamesText()));
        vo.setUpdatedAt(row.getUpdatedAt());
        return vo;
    }

    private List<String> parseStringArray(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return List.of();
        }
        try {
            List<?> parsed = objectMapper.readValue(jsonText, List.class);
            return parsed.stream()
                    .map(String::valueOf)
                    .filter(value -> !value.isBlank())
                    .toList();
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private List<String> splitNames(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (String item : text.split("[?，]")) {
            String normalized = item.trim();
            if (!normalized.isBlank()) {
                names.add(normalized);
            }
        }
        return names;
    }

    private <T> List<T> pageRows(List<T> rows, int pageNum, int pageSize) {
        int from = Math.min((pageNum - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());
        return rows.subList(from, to);
    }

    private int calculatePages(int total, int pageSize) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    private void fillPageFields(ShiftWorkConfigPageVO vo, Long total, int pageNum, int pageSize) {
        int pages = calculatePages(total.intValue(), pageSize);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
    }

    private void fillPageFields(ShiftWorkGoodsPageVO vo, Long total, int pageNum, int pageSize) {
        int pages = calculatePages(total.intValue(), pageSize);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
    }

    private void fillFullMarketingPageFields(FullMarketingCommissionPageVO vo, Long total, int pageNum, int pageSize) {
        int pages = calculatePages(total.intValue(), pageSize);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
    }

    private void fillFullMarketingPageFields(FullMarketingProductSalePageVO vo, Long total, int pageNum, int pageSize) {
        int pages = calculatePages(total.intValue(), pageSize);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(pages);
        vo.setHasNextPage(pageNum < pages);
    }
}

package com.jeez.zp.finance.service;

import com.jeez.zp.finance.dto.request.*;
import com.jeez.zp.finance.vo.*;

public interface FinanceService {
    CustomChannelDashboardVO getCustomChannelDashboard(CampRequest request, Long userId);
    CustomChannelDashboardVO createCustomChannel(CustomChannelMutationRequest request, Long userId);
    CustomChannelDashboardVO updateCustomChannel(CustomChannelMutationRequest request, Long userId);
    CustomChannelDashboardVO deleteCustomChannel(CustomChannelMutationRequest request, Long userId);
    PaymentCatalogVO getPaymentWays(CampRequest request, Long userId);
    PaymentCatalogVO getPaymentTypes(CampRequest request, Long userId);
    PaymentCatalogVO getPaymentTypesV2(CampRequest request, Long userId);
    PaymentCatalogVO createCustomPaymentType(PaymentTypeCreateRequest request, Long userId);
    PaymentSettingListVO listPaymentSettings(CampRequest request, Long userId);
    PaymentSettingVO getPaymentSettingDetail(CampRequest request, Long userId);
    PaymentSettingMutationVO createPaymentSetting(PaymentSettingMutationRequest request, Long userId);
    PaymentSettingMutationVO updatePaymentSettingStatus(PaymentSettingMutationRequest request, Long userId);
    PaymentSettingMutationVO updateDefaultPaymentSetting(PaymentSettingMutationRequest request, Long userId);
    PaymentSettingMutationVO updatePaymentSettingSort(PaymentSettingMutationRequest request, Long userId);
    PaymentSettingMutationVO exportPaymentSettings(PaymentSettingMutationRequest request, Long userId);
    OrderLedgerDashboardVO getOrderLedgerDashboard(OrderLedgerDashboardRequest request, Long userId);
    OtaDashboardVO getOtaDashboard(CampRequest request, Long userId);
    OtaChannelDetailVO getOtaChannelDetail(OtaChannelDetailRequest request, Long userId);
    DistributionOrderPageVO getDistributionOrders(DistributionOrderPageRequest request, Long userId);
    AccountBookPaymentWayPageVO getAccountBookPaymentWayPage(AccountBookPaymentWayPageRequest request, Long userId);
    Object getStatementOrders(StatementOrderRequest request, Long userId);
    SocialOverviewVO getSocialOverview(SocialOverviewRequest request, Long userId);
    FullMarketingCommissionPageVO getFullMarketingCommissionProducts(FullMarketingCommissionPageRequest request, Long userId);
    FullMarketingMetricVO getFullMarketingReport(FullMarketingReportRequest request, Long userId);
    FullMarketingProductSalePageVO getFullMarketingProductSales(FullMarketingReportRequest request, Long userId);
    ShiftWorkConfigPageVO getShiftWorkConfigs(PageCampRequest request, Long userId);
    ShiftWorkGoodsPageVO getShiftWorkGoods(PageCampRequest request, Long userId);
    ShiftWorkMutationVO saveShiftWorkConfigs(ShiftWorkConfigSaveRequest request, Long userId);
    ShiftWorkMutationVO saveShiftWorkGoods(ShiftWorkGoodsSaveRequest request, Long userId);
    ShiftWorkReportPageVO getShiftWorkReports(ShiftWorkReportPageRequest request, Long userId);
}

package com.jeez.zp.finance.controller;

import com.jeez.zp.finance.api.HudsonResponse;
import com.jeez.zp.finance.api.TraceIdFactory;
import com.jeez.zp.finance.dto.request.*;
import com.jeez.zp.finance.security.LoginUserContext;
import com.jeez.zp.finance.service.FinanceService;
import com.jeez.zp.finance.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping("/channels/custom/list")
    public HudsonResponse<CustomChannelDashboardVO> getCustomChannelDashboard(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getCustomChannelDashboard(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("channels-custom-list"));
    }

    @PostMapping("/channels/custom/create")
    public HudsonResponse<CustomChannelDashboardVO> createCustomChannel(@RequestBody CustomChannelMutationRequest request) {
        return HudsonResponse.success(financeService.createCustomChannel(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("channels-custom-create"));
    }

    @PostMapping("/channels/custom/update")
    public HudsonResponse<CustomChannelDashboardVO> updateCustomChannel(@RequestBody CustomChannelMutationRequest request) {
        return HudsonResponse.success(financeService.updateCustomChannel(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("channels-custom-update"));
    }

    @PostMapping("/channels/custom/delete")
    public HudsonResponse<CustomChannelDashboardVO> deleteCustomChannel(@RequestBody CustomChannelMutationRequest request) {
        return HudsonResponse.success(financeService.deleteCustomChannel(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("channels-custom-delete"));
    }

    @PostMapping("/paymentWays/get")
    public HudsonResponse<PaymentCatalogVO> getPaymentWays(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getPaymentWays(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-ways-get"));
    }

    @PostMapping("/paymentTypes/get")
    public HudsonResponse<PaymentCatalogVO> getPaymentTypes(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getPaymentTypes(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-types-get"));
    }

    @PostMapping("/paymentTypes/get/v2")
    public HudsonResponse<PaymentCatalogVO> getPaymentTypesV2(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getPaymentTypesV2(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-types-get-v2"));
    }

    @PostMapping("/paymentTypes/custom/create")
    public HudsonResponse<PaymentCatalogVO> createCustomPaymentType(@RequestBody PaymentTypeCreateRequest request) {
        return HudsonResponse.success(financeService.createCustomPaymentType(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-types-custom-create"));
    }

    @PostMapping("/paymentSettings/list")
    public HudsonResponse<PaymentSettingListVO> listPaymentSettings(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.listPaymentSettings(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-list"));
    }

    @PostMapping("/paymentSettings/detail")
    public HudsonResponse<PaymentSettingVO> getPaymentSettingDetail(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getPaymentSettingDetail(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-detail"));
    }

    @PostMapping("/paymentSettings/create")
    public HudsonResponse<PaymentSettingMutationVO> createPaymentSetting(@RequestBody PaymentSettingMutationRequest request) {
        return HudsonResponse.success(financeService.createPaymentSetting(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-create"));
    }

    @PostMapping("/paymentSettings/status/update")
    public HudsonResponse<PaymentSettingMutationVO> updatePaymentSettingStatus(@RequestBody PaymentSettingMutationRequest request) {
        return HudsonResponse.success(financeService.updatePaymentSettingStatus(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-status-update"));
    }

    @PostMapping("/paymentSettings/default/update")
    public HudsonResponse<PaymentSettingMutationVO> updateDefaultPaymentSetting(@RequestBody PaymentSettingMutationRequest request) {
        return HudsonResponse.success(financeService.updateDefaultPaymentSetting(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-default-update"));
    }

    @PostMapping("/paymentSettings/sort/update")
    public HudsonResponse<PaymentSettingMutationVO> updatePaymentSettingSort(@RequestBody PaymentSettingMutationRequest request) {
        return HudsonResponse.success(financeService.updatePaymentSettingSort(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-sort-update"));
    }

    @PostMapping("/paymentSettings/export")
    public HudsonResponse<PaymentSettingMutationVO> exportPaymentSettings(@RequestBody PaymentSettingMutationRequest request) {
        return HudsonResponse.success(financeService.exportPaymentSettings(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("payment-settings-export"));
    }

    @PostMapping("/orderLedger/dashboard/get")
    public HudsonResponse<OrderLedgerDashboardVO> getOrderLedgerDashboard(@RequestBody OrderLedgerDashboardRequest request) {
        return HudsonResponse.success(financeService.getOrderLedgerDashboard(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("order-ledger-dashboard-get"));
    }

    @PostMapping("/ota/dashboard/get")
    public HudsonResponse<OtaDashboardVO> getOtaDashboard(@RequestBody CampRequest request) {
        return HudsonResponse.success(financeService.getOtaDashboard(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("ota-dashboard-get"));
    }

    @PostMapping("/ota/channel/detail/get")
    public HudsonResponse<OtaChannelDetailVO> getOtaChannelDetail(@RequestBody OtaChannelDetailRequest request) {
        return HudsonResponse.success(financeService.getOtaChannelDetail(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("ota-channel-detail-get"));
    }

    @PostMapping("/distribution/orders/page/get")
    public HudsonResponse<DistributionOrderPageVO> getDistributionOrders(@RequestBody DistributionOrderPageRequest request) {
        return HudsonResponse.success(financeService.getDistributionOrders(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("distribution-orders-page-get"));
    }

    @PostMapping("/accountBookPaymentWay/page/get")
    public HudsonResponse<AccountBookPaymentWayPageVO> getAccountBookPaymentWayPage(@RequestBody AccountBookPaymentWayPageRequest request) {
        return HudsonResponse.success(financeService.getAccountBookPaymentWayPage(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("account-book-payment-way-page-get"));
    }

    @PostMapping("/report/storer/statement/get")
    public HudsonResponse<Object> getStatementOrders(@RequestBody StatementOrderRequest request) {
        return HudsonResponse.success(financeService.getStatementOrders(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("report-storer-statement-get"));
    }

    @PostMapping("/channels/social/overview")
    public HudsonResponse<SocialOverviewVO> getSocialOverview(@RequestBody SocialOverviewRequest request) {
        return HudsonResponse.success(financeService.getSocialOverview(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("channels-social-overview"));
    }

    @PostMapping("/promotionPlanProducts/page/get")
    public HudsonResponse<FullMarketingCommissionPageVO> getPromotionPlanProducts(@RequestBody FullMarketingCommissionPageRequest request) {
        return HudsonResponse.success(financeService.getFullMarketingCommissionProducts(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("promotion-plan-products-page-get"));
    }

    @PostMapping("/report/promotion/get")
    public HudsonResponse<FullMarketingMetricVO> getPromotionReport(@RequestBody FullMarketingReportRequest request) {
        return HudsonResponse.success(financeService.getFullMarketingReport(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("report-promotion-get"));
    }

    @PostMapping("/report/promotion/productSale/page/get")
    public HudsonResponse<FullMarketingProductSalePageVO> getPromotionProductSales(@RequestBody FullMarketingReportRequest request) {
        return HudsonResponse.success(financeService.getFullMarketingProductSales(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("report-promotion-product-sale-page-get"));
    }

    @PostMapping("/shiftWorkConfig/page/get")
    public HudsonResponse<ShiftWorkConfigPageVO> getShiftWorkConfigs(@RequestBody PageCampRequest request) {
        return HudsonResponse.success(financeService.getShiftWorkConfigs(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("shift-work-config-page-get"));
    }

    @PostMapping("/shiftWorkGoods/page/get")
    public HudsonResponse<ShiftWorkGoodsPageVO> getShiftWorkGoods(@RequestBody PageCampRequest request) {
        return HudsonResponse.success(financeService.getShiftWorkGoods(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("shift-work-goods-page-get"));
    }

    @PostMapping("/shiftWorkConfig/save")
    public HudsonResponse<ShiftWorkMutationVO> saveShiftWorkConfigs(@RequestBody ShiftWorkConfigSaveRequest request) {
        return HudsonResponse.success(financeService.saveShiftWorkConfigs(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("shift-work-config-save"));
    }

    @PostMapping("/shiftWorkGoods/save")
    public HudsonResponse<ShiftWorkMutationVO> saveShiftWorkGoods(@RequestBody ShiftWorkGoodsSaveRequest request) {
        return HudsonResponse.success(financeService.saveShiftWorkGoods(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("shift-work-goods-save"));
    }

    @PostMapping("/shiftWorkReport/page/get")
    public HudsonResponse<ShiftWorkReportPageVO> getShiftWorkReports(@RequestBody ShiftWorkReportPageRequest request) {
        return HudsonResponse.success(financeService.getShiftWorkReports(request, LoginUserContext.requiredUserId()), TraceIdFactory.next("shift-work-report-page-get"));
    }
}

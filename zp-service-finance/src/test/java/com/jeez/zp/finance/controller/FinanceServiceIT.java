package com.jeez.zp.finance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FinanceServiceIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final long POI_ID = 11001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void financeEndpoints_shouldExposePaymentSettingsLedgerOtaAndDistributionFromRealTables() throws Exception {
        seedFinanceData();

        mockMvc.perform(get("/finance-service/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("finance-service-ok"));

        mockMvc.perform(post("/paymentWays/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paymentWays[?(@.paymentWayId == '53001')].paymentWayName").value("Finance Test WeChat"));

        mockMvc.perform(post("/paymentTypes/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId == '52001')].paymentTypeName").value("Finance Test Room Income"));

        mockMvc.perform(post("/paymentTypes/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentGroups[?(@.groupType == 91)].groupTypeName").value("Finance Test Group"))
                .andExpect(jsonPath("$.data.paymentWays[?(@.paymentWayId == '53001')].paymentWayName").value("Finance Test WeChat"));

        mockMvc.perform(post("/paymentSettings/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].campId").value("10001"));

        mockMvc.perform(post("/paymentSettings/detail")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.campId").value("10001"))
                .andExpect(jsonPath("$.data.nightAudit.enabled").value(true));

        mockMvc.perform(post("/orderLedger/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "beginTime":"2026-05-18 09:59:00",
                                  "endTime":"2026-05-18 10:01:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalIncome").value(123.45))
                .andExpect(jsonPath("$.data.records[?(@.id == '54001')].projectLabel").value("Finance Test Room Income"));

        mockMvc.perform(post("/ota/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accounts[?(@.accountId == '55001')].channelName").value("Finance Test OTA"))
                .andExpect(jsonPath("$.data.summary.authorizedAccounts", greaterThanOrEqualTo(1)));

        mockMvc.perform(post("/ota/channel/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","accountId":"55001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account.accountId").value("55001"))
                .andExpect(jsonPath("$.data.roomCategories[?(@.roomCategoryId == '22001')].shelfStatus").value("on_shelf"));

        mockMvc.perform(post("/distribution/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","pageNum":1,"pageSize":10,"settlementStatus":"settled"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.list[?(@.distributionOrderId == '56001')].commissionPrice").value(45.67));


        mockMvc.perform(post("/paymentTypes/custom/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "groupType":91,
                                  "groupName":"Finance Test Group",
                                  "paymentTypeName":"Finance Test Custom Income",
                                  "isIncome":1,
                                  "bizType":3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paymentGroups[?(@.groupType == 91)].paymentTypes[?(@.paymentTypeName == 'Finance Test Custom Income')].isCustom").value(1));
    }

    @Test
    @Timeout(60)
    void shiftWorkEndpoints_shouldExposeShiftSettingsAndReportsFromRealTables() throws Exception {
        seedShiftWorkData();

        mockMvc.perform(post("/shiftWorkConfig/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("57001"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD早班"))
                .andExpect(jsonPath("$.data.list[0].memberIds[0]").value("12001"))
                .andExpect(jsonPath("$.data.list[0].memberNames[0]").value("系统管理员"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(post("/shiftWorkGoods/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("57101"))
                .andExpect(jsonPath("$.data.list[0].goodsName").value("TDD房卡"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(post("/shiftWorkReport/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "startDate":"2026-05-18",
                                  "endDate":"2026-05-18"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.rows[0].id").value("57201"))
                .andExpect(jsonPath("$.data.rows[0].shiftName").value("TDD早班"))
                .andExpect(jsonPath("$.data.rows[0].handoverUserId").value("12001"))
                .andExpect(jsonPath("$.data.rows[0].handoverUserName").value("系统管理员"))
                .andExpect(jsonPath("$.data.rows[0].workReport").exists())
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }

    @Test
    @Timeout(60)
    void socialOverview_shouldExposeSocialChannelMetricsFromRealChannelTables() throws Exception {
        seedSocialOverviewData();

        mockMvc.perform(post("/channels/social/overview")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bizDate":"2026-05-18",
                                  "campId":"10001",
                                  "projectId":"all",
                                  "channelStatus":"connected",
                                  "keyword":"抖音",
                                  "page":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.filterOptions.projects[?(@.value == 'calendar-room')].label").value("日历房"))
                .andExpect(jsonPath("$.data.metrics[?(@.label == '已直连渠道')].value").value("1"))
                .andExpect(jsonPath("$.data.metrics[?(@.label == '今日渠道订单')].value").value("2"))
                .andExpect(jsonPath("$.data.channels.length()").value(1))
                .andExpect(jsonPath("$.data.channels[0].id").value("douyin-lk"))
                .andExpect(jsonPath("$.data.channels[0].name").value("抖音来客"))
                .andExpect(jsonPath("$.data.channels[0].status").value("connected"))
                .andExpect(jsonPath("$.data.channels[0].relation").value("关联房型2/3"))
                .andExpect(jsonPath("$.data.channels[0].roomTypeCount").value(3))
                .andExpect(jsonPath("$.data.channels[0].linkedRoomTypeCount").value(2))
                .andExpect(jsonPath("$.data.channels[0].dailyOrders").value(2))
                .andExpect(jsonPath("$.data.accounts.list[0].id").value("59001"))
                .andExpect(jsonPath("$.data.accounts.list[0].channel").value("抖音来客"))
                .andExpect(jsonPath("$.data.accounts.list[0].authorization[0]").value("酒店行业预售券解决方案"))
                .andExpect(jsonPath("$.data.accounts.pagination.total").value(1))
                .andExpect(jsonPath("$.data.quickLinks[?(@.path == '/houseManage/houseCale')].label").value("房价管理"));
    }

    @Test
    @Timeout(60)
    void accountBookPaymentWayPageGet_shouldAggregateLedgerByPaymentWayAndDate() throws Exception {
        seedAccountBookPaymentWayData();

        mockMvc.perform(post("/accountBookPaymentWay/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "beginTime":"2026-05-18",
                                  "endTime":"2026-05-18",
                                  "poiIds":["60010"],
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.extraInfo.income[0].paymentWayId").value("60001"))
                .andExpect(jsonPath("$.data.extraInfo.income[0].paymentWayName").value("TDD平台代收"))
                .andExpect(jsonPath("$.data.extraInfo.income[0].price").value(closeTo(1002.54, 0.001)))
                .andExpect(jsonPath("$.data.extraInfo.income[1].paymentWayId").value("60002"))
                .andExpect(jsonPath("$.data.extraInfo.income[1].price").value(closeTo(45.67, 0.001)))
                .andExpect(jsonPath("$.data.extraInfo.expend[0].paymentWayId").value("60001"))
                .andExpect(jsonPath("$.data.extraInfo.expend[0].price").value(closeTo(12.34, 0.001)))
                .andExpect(jsonPath("$.data.extraInfo.totalInfo.totalIncomePrice").value(closeTo(1048.21, 0.001)))
                .andExpect(jsonPath("$.data.extraInfo.totalInfo.totalExpendPrice").value(closeTo(12.34, 0.001)))
                .andExpect(jsonPath("$.data.extraInfo.totalInfo.netIncome").value(closeTo(1035.87, 0.001)))
                .andExpect(jsonPath("$.data.list[0].date").value("合计"))
                .andExpect(jsonPath("$.data.list[0].paymentWayPriceDetailViews[0].paymentWayId").value("60001"))
                .andExpect(jsonPath("$.data.list[0].paymentWayPriceDetailViews[0].price").value(closeTo(990.20, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paymentWayPriceDetailViews[1].paymentWayId").value("60002"))
                .andExpect(jsonPath("$.data.list[0].paymentWayPriceDetailViews[1].price").value(closeTo(45.67, 0.001)))
                .andExpect(jsonPath("$.data.list[1].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.list[1].paymentWayPriceDetailViews[0].price").value(closeTo(990.20, 0.001)));

        mockMvc.perform(post("/accountBookPaymentWay/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "beginTime":"2026-05-18 00:00:00",
                                  "endTime":"2026-05-18 00:00:00",
                                  "poiIds":["60010"],
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.extraInfo.totalInfo.netIncome").value(closeTo(1035.87, 0.001)));
    }

    @Test
    @Timeout(60)
    void orderLedgerDashboardGet_shouldExposeLedgerEntryFrontendContract() throws Exception {
        seedLedgerEntryFrontendData();

        mockMvc.perform(post("/orderLedger/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":5,
                                  "beginTime":"2026-05-20 00:00:00",
                                  "endTime":"2026-05-20 23:59:59",
                                  "roomCategoryId":"62021"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.income").value(closeTo(188.00, 0.001)))
                .andExpect(jsonPath("$.data.expend").value(closeTo(34.00, 0.001)))
                .andExpect(jsonPath("$.data.netIncome").value(closeTo(154.00, 0.001)))
                .andExpect(jsonPath("$.data.costPricePages.total").value(2))
                .andExpect(jsonPath("$.data.costPricePages.size").value(5))
                .andExpect(jsonPath("$.data.costPricePages.current").value(1))
                .andExpect(jsonPath("$.data.costPricePages.pageNum").value(1))
                .andExpect(jsonPath("$.data.costPricePages.pages").value(1))
                .andExpect(jsonPath("$.data.costPricePages.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.costPricePages.list[1].accountName").value("Ledger Frontend Room Income"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].isIncome").value(1))
                .andExpect(jsonPath("$.data.costPricePages.list[1].amount").value(closeTo(188.00, 0.001)))
                .andExpect(jsonPath("$.data.costPricePages.list[1].paymentWayName").value("Ledger Frontend Pay"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].roomCategoryName").value("Ledger Frontend Room"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].roomName").value("620A"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].note").value("ledger income note"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].operatorName").value("Ledger Tester"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].channelName").value("Ledger Frontend OTA"))
                .andExpect(jsonPath("$.data.costPricePages.list[1].gmtCreate").value("2026-05-20 10:15:00"));

        mockMvc.perform(post("/orderLedger/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":5,
                                  "beginTime":"2026-05-20 00:00:00",
                                  "endTime":"2026-05-20 23:59:59",
                                  "roomCategoryId":"62021",
                                  "isIncome":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.income").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.expend").value(closeTo(34.00, 0.001)))
                .andExpect(jsonPath("$.data.netIncome").value(closeTo(-34.00, 0.001)))
                .andExpect(jsonPath("$.data.costPricePages.total").value(1))
                .andExpect(jsonPath("$.data.costPricePages.list[0].id").value("62032"))
                .andExpect(jsonPath("$.data.costPricePages.list[0].isIncome").value(0));
    }

    @Test
    @Timeout(60)
    void reportStorerStatementGet_shouldReturnMallStatementOrdersFromFinanceService() throws Exception {
        seedStatementOrderData();

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiIds":["65011"],
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "breakTemp":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.list[0].orderId").value("OUT-FIN-STMT-65051"))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("OUT-FIN-STMT-65051"))
                .andExpect(jsonPath("$.data.list[0].customerInfo").value("Finance Statement Guest / 18800001111"))
                .andExpect(jsonPath("$.data.list[0].customerName").value("Finance Statement Guest"))
                .andExpect(jsonPath("$.data.list[0].mobile").value("18800001111"))
                .andExpect(jsonPath("$.data.list[0].productType").value("预售券"))
                .andExpect(jsonPath("$.data.list[0].productTypeName").value("预售券"))
                .andExpect(jsonPath("$.data.list[0].productName").value("Finance Statement Coupon"))
                .andExpect(jsonPath("$.data.list[0].bookingTime").value("2026-05-18 12:00:00"))
                .andExpect(jsonPath("$.data.list[0].bookingTimeStr").value("2026-05-18 12:00:00"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("品牌小程序"))
                .andExpect(jsonPath("$.data.list[0].payableAmount").value(closeTo(456.78, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paidAmount").value(closeTo(456.78, 0.001)))
                .andExpect(jsonPath("$.data.list[0].discountAmount").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].refundAmount").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paymentFee").value(closeTo(3.21, 0.001)))
                .andExpect(jsonPath("$.data.list[0].platformServiceFee").value(closeTo(4.56, 0.001)))
                .andExpect(jsonPath("$.data.list[0].distributorCommission").value(closeTo(7.89, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paymentWayName").value("Finance Statement Pay"))
                .andExpect(jsonPath("$.data.list[0].settlementAmount").value(closeTo(441.12, 0.001)));

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":9999,
                                  "current":1,
                                  "breakTemp":false,
                                  "exportExcelMenuId":"1732967098146951178"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", containsString("/downloads/report-storer-statement/10001/")))
                .andExpect(jsonPath("$.data", containsString(".xlsx")));
    }

    @Test
    @Timeout(60)
    void paymentSettingMutations_shouldPersistPaymentWayChangesAndReturnSuccessEnvelope() throws Exception {
        resetPaymentSettingMutationData();
        seedPaymentSettingMutationData();

        mockMvc.perform(post("/paymentSettings/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","name":"TDD新增支付方式","status":"enabled"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("已新增支付方式：TDD新增支付方式"));
        Integer createdStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM payment_way WHERE camp_id = ? AND payment_way_name = ? AND is_deleted = 0",
                Integer.class,
                CAMP_ID,
                "TDD新增支付方式"
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, createdStatus);

        mockMvc.perform(post("/paymentSettings/sort/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","methodId":"58012","direction":"up"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("已上移支付方式：TDD排序方式B"));
        Integer sortA = jdbcTemplate.queryForObject(
                "SELECT sort_no FROM payment_way WHERE payment_way_id = ?",
                Integer.class,
                58011L
        );
        Integer sortB = jdbcTemplate.queryForObject(
                "SELECT sort_no FROM payment_way WHERE payment_way_id = ?",
                Integer.class,
                58012L
        );
        org.junit.jupiter.api.Assertions.assertEquals(20, sortA);
        org.junit.jupiter.api.Assertions.assertEquals(10, sortB);

        mockMvc.perform(post("/paymentSettings/status/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","methodId":"58011","status":"disabled"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("已停用支付方式：TDD排序方式A"));
        Integer disabledStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM payment_way WHERE payment_way_id = ?",
                Integer.class,
                58011L
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, disabledStatus);

        mockMvc.perform(post("/paymentSettings/default/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","methodId":"58012"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("已将TDD排序方式B设为默认支付方式"));

        mockMvc.perform(post("/paymentSettings/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","exportAt":"2026-05-20T01:20:00+08:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("导出任务已创建"));
    }


    @Test
    @Timeout(60)
    void shiftWorkMutations_shouldReplaceActiveConfigsAndGoods() throws Exception {
        resetShiftWorkMutationData();
        seedShiftWorkMutationData();

        mockMvc.perform(post("/shiftWorkConfig/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "drafts":[
                                    {"id":"57301","name":"TDD_SHIFT_UPDATED","startTime":"07:30","endTime":"15:30","memberIds":["12001"]},
                                    {"name":"TDD_SHIFT_NIGHT","startTime":"15:30","endTime":"23:30","memberIds":["12001"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("shift work configs saved"))
                .andExpect(jsonPath("$.data.shiftConfigs[?(@.name == 'TDD_SHIFT_UPDATED')].memberIds[0]").value("12001"))
                .andExpect(jsonPath("$.data.shiftConfigs[?(@.name == 'TDD_SHIFT_NIGHT')].startTime").value("15:30"));

        Integer oldStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM shift_work_config WHERE shift_config_id = ?",
                Integer.class,
                57302L
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, oldStatus);

        String updatedMemberIds = jdbcTemplate.queryForObject(
                "SELECT CAST(member_ids_json AS CHAR) FROM shift_work_config WHERE shift_config_id = ?",
                String.class,
                57301L
        );
        org.junit.jupiter.api.Assertions.assertEquals("[\"12001\"]", updatedMemberIds);

        mockMvc.perform(post("/shiftWorkGoods/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "drafts":[
                                    {"id":"57401","name":"TDD_GOODS_CARD"},
                                    {"name":"TDD_GOODS_KEY"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("shift work goods saved"))
                .andExpect(jsonPath("$.data.goodsConfigs[?(@.name == 'TDD_GOODS_CARD')].id").value("57401"))
                .andExpect(jsonPath("$.data.goodsConfigs[?(@.name == 'TDD_GOODS_KEY')].name").value("TDD_GOODS_KEY"));

        Integer oldGoodsStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM shift_work_goods WHERE shift_goods_id = ?",
                Integer.class,
                57402L
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, oldGoodsStatus);
    }


    private void seedFinanceData() {
        resetFinanceData();
        jdbcTemplate.update("INSERT INTO payment_type_group (payment_type_group_id,camp_id,group_type,group_name,biz_type,is_income,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,0,1,0)", 51001L, CAMP_ID, 91, "Finance Test Group", 3, 1);
        jdbcTemplate.update("INSERT INTO payment_type (payment_type_id,camp_id,payment_type_group_id,payment_type_name,group_type,group_name,biz_type,is_income,is_custom,ignore_order_get_item,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,1,0,0)", 52001L, CAMP_ID, 51001L, "Finance Test Room Income", 91, "Finance Test Group", 3, 1, 0, 0);
        jdbcTemplate.update("INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,0,1,0)", 53001L, CAMP_ID, "Finance Test WeChat", "finance_test_wechat", "online");
        jdbcTemplate.update("INSERT INTO finance_setting (setting_id,camp_id,night_audit_json,amortize_json,vendible_json,updated_by) VALUES (?,?,?,?,?,?)", 53101L, CAMP_ID, "{\"enabled\":true}", "{\"mode\":\"daily\"}", "{\"enabled\":false}", USER_ID);
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 54001L, CAMP_ID, POI_ID, "income", 52001L, 53001L, null, "manual", 54001L, 12345L, Timestamp.valueOf(LocalDateTime.of(2026,5,18,10,0)), USER_ID, "Finance Tester", Date.valueOf(LocalDate.of(2026,5,18)), "Finance ledger seed");
        jdbcTemplate.update("INSERT INTO channel_account (account_id,camp_id,channel_id,channel_name,account_name,out_account_id,status,authorized_at,config_json) VALUES (?,?,?,?,?,?,?,?,?)", 55001L, CAMP_ID, 88001L, "Finance Test OTA", "Finance OTA Account", "OUT55001", "authorized", Timestamp.valueOf(LocalDateTime.of(2026,5,1,10,0)), "{\"level\":\"gold\"}");
        jdbcTemplate.update("INSERT INTO channel_poi_rel (id,camp_id,account_id,poi_id,out_poi_id,sync_status) VALUES (?,?,?,?,?,?)", 55101L, CAMP_ID, 55001L, POI_ID, "OUTPOI", "synced");
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 22001L, CAMP_ID, POI_ID, "Finance OTA Room", "Finance OTA Room", 1, 2, 38800L, 38800L, 38800L, 14, 23, 12, "finance ota", "finance ota", "finance ota room", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO channel_room_category_rel (id,camp_id,account_id,room_category_id,out_room_category_id,project_type,shelf_status,audit_status) VALUES (?,?,?,?,?,?,?,?)", 55201L, CAMP_ID, 55001L, 22001L, "OUTROOM", "calendar_room", "on_shelf", "approved");
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 56021L, CAMP_ID, POI_ID, "Finance Distribution Room", "Finance Distribution Room", 1, 2, 45678L, 45678L, 45678L, 14, 23, 12, "finance dist", "finance dist", "finance dist room", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO order_main (order_id,camp_id,poi_id,room_category_id,room_id,channel_id,goods_id,order_no,out_order_no,order_type,status,guest_name,guest_mobile,start_at,end_at,day_num,total_price_cent,discount_price_cent,total_pay_price_cent,refund_price_cent,commission_price_cent,payment_fee_cent,platform_service_fee_cent,distribution_commission_cent,settlement_amount_cent,payment_status,payment_type_id,payment_way_id,source_type,remark,created_at,updated_at,created_by,updated_by,is_deleted,version_no) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,0)", 28002L, CAMP_ID, POI_ID, 56021L, null, 55001L, null, "FIN-DIST-28002", "OUT-FIN-DIST-28002", "daily_room", "booked", "Finance Distribution Guest", "18800003333", Timestamp.valueOf(LocalDateTime.of(2026,5,19,14,0)), Timestamp.valueOf(LocalDateTime.of(2026,5,20,12,0)), 1, 45678L, 0L, 45678L, 0L, 4567L, 0L, 0L, 4567L, 41111L, "paid", null, 53001L, "channel", "?????", Timestamp.valueOf(LocalDateTime.of(2026,5,19,10,0)), Timestamp.valueOf(LocalDateTime.of(2026,5,19,10,5)), USER_ID, USER_ID);
        jdbcTemplate.update("INSERT INTO distribution_order (distribution_order_id,camp_id,source_order_id,channel_id,commission_price_cent,settlement_status,settled_at) VALUES (?,?,?,?,?,?,?)", 56001L, CAMP_ID, 28002L, 88001L, 4567L, "settled", Timestamp.valueOf(LocalDateTime.of(2026,5,19,10,0)));
    }

    private void seedSocialOverviewData() {
        resetSocialOverviewData();
        jdbcTemplate.update("INSERT INTO channel_account (account_id,camp_id,channel_id,channel_name,account_name,out_account_id,status,authorized_at,updated_at,config_json) VALUES (?,?,?,?,?,?,?,?,?,?)", 59001L, CAMP_ID, 17001L, "抖音来客", "抖音测试账号", "DY-59001", "authorized", Timestamp.valueOf(LocalDateTime.of(2026,5,1,10,0)), Timestamp.valueOf(LocalDateTime.of(2026,5,18,9,40)), "{\"scene\":\"social\"}");
        jdbcTemplate.update("INSERT INTO channel_poi_rel (id,camp_id,account_id,poi_id,out_poi_id,sync_status) VALUES (?,?,?,?,?,?)", 59011L, CAMP_ID, 59001L, POI_ID, "DY-POI-1", "synced");
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 22001L, CAMP_ID, POI_ID, "Social Room A", "Social Room A", 1, 2, 28800L, 28800L, 28800L, 14, 23, 12, "social a", "social a", "social room a", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 22002L, CAMP_ID, POI_ID, "Social Room B", "Social Room B", 1, 2, 38800L, 38800L, 38800L, 14, 23, 12, "social b", "social b", "social room b", 2, 1, 0);
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 22003L, CAMP_ID, POI_ID, "Social Room C", "Social Room C", 1, 2, 48800L, 48800L, 48800L, 14, 23, 12, "social c", "social c", "social room c", 3, 1, 0);
        jdbcTemplate.update("INSERT INTO room (room_id,camp_id,poi_id,room_category_id,room_name,room_no,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?)", 23001L, CAMP_ID, POI_ID, 22001L, "2301", "2301", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO room (room_id,camp_id,poi_id,room_category_id,room_name,room_no,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?)", 23002L, CAMP_ID, POI_ID, 22002L, "2302", "2302", 1, 2, 0);
        jdbcTemplate.update("INSERT INTO channel_room_category_rel (id,camp_id,account_id,room_category_id,out_room_category_id,project_type,shelf_status,audit_status) VALUES (?,?,?,?,?,?,?,?)", 59021L, CAMP_ID, 59001L, 22001L, "DY-RC-1", "calendar_room", "on_shelf", "approved");
        jdbcTemplate.update("INSERT INTO channel_room_category_rel (id,camp_id,account_id,room_category_id,out_room_category_id,project_type,shelf_status,audit_status) VALUES (?,?,?,?,?,?,?,?)", 59022L, CAMP_ID, 59001L, 22002L, "DY-RC-2", "calendar_room", "on_shelf", "approved");
        jdbcTemplate.update("INSERT INTO channel_room_category_rel (id,camp_id,account_id,room_category_id,out_room_category_id,project_type,shelf_status,audit_status) VALUES (?,?,?,?,?,?,?,?)", 59023L, CAMP_ID, 59001L, 22003L, "DY-RC-3", "calendar_room", "off_shelf", "pending");
        jdbcTemplate.update("INSERT INTO order_main (order_id,camp_id,poi_id,room_category_id,room_id,channel_id,order_no,out_order_no,order_type,status,guest_name,start_at,end_at,day_num,total_price_cent,total_pay_price_cent,payment_status,source_type,created_at,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)", 59101L, CAMP_ID, POI_ID, 22001L, 23001L, 17001L, "SOCIAL-59101", "SOCIAL-OUT-59101", "house", "booked", "Social Guest A", Timestamp.valueOf(LocalDateTime.of(2026,5,18,15,0)), Timestamp.valueOf(LocalDateTime.of(2026,5,19,12,0)), 1, 28800L, 28800L, "paid", "douyin", Timestamp.valueOf(LocalDateTime.of(2026,5,18,10,0)));
        jdbcTemplate.update("INSERT INTO order_main (order_id,camp_id,poi_id,room_category_id,room_id,channel_id,order_no,out_order_no,order_type,status,guest_name,start_at,end_at,day_num,total_price_cent,total_pay_price_cent,payment_status,source_type,created_at,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)", 59102L, CAMP_ID, POI_ID, 22002L, 23002L, 17001L, "SOCIAL-59102", "SOCIAL-OUT-59102", "house", "booked", "Social Guest B", Timestamp.valueOf(LocalDateTime.of(2026,5,18,16,0)), Timestamp.valueOf(LocalDateTime.of(2026,5,19,12,0)), 1, 38800L, 38800L, "paid", "douyin", Timestamp.valueOf(LocalDateTime.of(2026,5,18,11,0)));
    }

    private void resetSocialOverviewData() {
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 59101 AND 59199");
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE id BETWEEN 59021 AND 59099");
        jdbcTemplate.update("DELETE FROM room WHERE room_id BETWEEN 23001 AND 23009");
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id BETWEEN 22001 AND 22003");
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE id BETWEEN 59011 AND 59019");
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id BETWEEN 59001 AND 59009");
    }

    private void seedAccountBookPaymentWayData() {
        resetAccountBookPaymentWayData();
        jdbcTemplate.update("INSERT INTO pms_poi (poi_id,camp_id,poi_name,is_availability,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?)", 60010L, CAMP_ID, "TDD收支汇总门店", 1, 1, 1, 0);
        jdbcTemplate.update("INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,0)", 60001L, CAMP_ID, "TDD平台代收", "tdd_collect", "online", 1);
        jdbcTemplate.update("INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,0)", 60002L, CAMP_ID, "TDD支付宝", "tdd_alipay", "online", 2);
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 60011L, CAMP_ID, 60010L, "income", null, 60001L, null, "manual", 60011L, 100254L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 10, 0)), USER_ID, "Finance Tester", Date.valueOf(LocalDate.of(2026, 5, 18)), "account book income");
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 60012L, CAMP_ID, 60010L, "expense", null, 60001L, null, "manual", 60012L, 1234L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 12, 0)), USER_ID, "Finance Tester", Date.valueOf(LocalDate.of(2026, 5, 18)), "account book expense");
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 60013L, CAMP_ID, 60010L, "income", null, 60002L, null, "manual", 60013L, 4567L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 18, 0)), USER_ID, "Finance Tester", Date.valueOf(LocalDate.of(2026, 5, 18)), "account book alipay");
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 60014L, CAMP_ID, 60010L, "income", null, 60001L, null, "manual", 60014L, 99999L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 9, 0)), USER_ID, "Finance Tester", Date.valueOf(LocalDate.of(2026, 5, 19)), "out of selected date");
    }

    private void resetAccountBookPaymentWayData() {
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE ledger_entry_id BETWEEN 60011 AND 60099");
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id BETWEEN 60001 AND 60009");
        jdbcTemplate.update("DELETE FROM pms_poi WHERE poi_id = 60010");
    }


    private void seedLedgerEntryFrontendData() {
        resetLedgerEntryFrontendData();
        jdbcTemplate.update("INSERT INTO pms_poi (poi_id,camp_id,poi_name,is_availability,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?)", 62010L, CAMP_ID, "Ledger Frontend Store", 1, 1, 1, 0);
        jdbcTemplate.update("INSERT INTO payment_type_group (payment_type_group_id,camp_id,group_type,group_name,biz_type,is_income,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,0,1,0)", 62000L, CAMP_ID, 92, "Ledger Frontend Income Group", 3, 1);
        jdbcTemplate.update("INSERT INTO payment_type_group (payment_type_group_id,camp_id,group_type,group_name,biz_type,is_income,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,1,0)", 62003L, CAMP_ID, 93, "Ledger Frontend Expense Group", 3, 0);
        jdbcTemplate.update("INSERT INTO payment_type (payment_type_id,camp_id,payment_type_group_id,payment_type_name,group_type,group_name,biz_type,is_income,is_custom,ignore_order_get_item,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,1,0,0)", 62001L, CAMP_ID, 62000L, "Ledger Frontend Room Income", 92, "Ledger Frontend Income Group", 3, 1, 0, 0);
        jdbcTemplate.update("INSERT INTO payment_type (payment_type_id,camp_id,payment_type_group_id,payment_type_name,group_type,group_name,biz_type,is_income,is_custom,ignore_order_get_item,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,1,1,0)", 62002L, CAMP_ID, 62003L, "Ledger Frontend Cleaning Expense", 93, "Ledger Frontend Expense Group", 3, 0, 0, 0);
        jdbcTemplate.update("INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,0)", 62011L, CAMP_ID, "Ledger Frontend Pay", "ledger_frontend_pay", "online", 1);
        jdbcTemplate.update("INSERT INTO channel_account (account_id,camp_id,channel_id,channel_name,account_name,out_account_id,status,authorized_at,config_json) VALUES (?,?,?,?,?,?,?,?,?)", 62051L, CAMP_ID, 62052L, "Ledger Frontend OTA", "Ledger Frontend OTA Account", "LEDGER-OTA", "authorized", Timestamp.valueOf(LocalDateTime.of(2026, 5, 1, 10, 0)), "{\"scene\":\"ledger\"}");
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 62021L, CAMP_ID, 62010L, "Ledger Frontend Room", "Ledger Frontend Room", 1, 2, 18800L, 18800L, 18800L, 14, 23, 12, "ledger highlight", "ledger nearby", "ledger room", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO room (room_id,camp_id,poi_id,room_category_id,room_name,room_no,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?)", 62022L, CAMP_ID, 62010L, 62021L, "620A", "620A", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO order_main (order_id,camp_id,poi_id,room_category_id,room_id,channel_id,order_no,out_order_no,order_type,status,guest_name,guest_mobile,start_at,end_at,day_num,total_price_cent,total_pay_price_cent,payment_status,source_type,created_at,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)", 62041L, CAMP_ID, 62010L, 62021L, 62022L, 62051L, "LEDGER-ORDER-62041", "LEDGER-OUT-62041", "daily_room", "completed", "Ledger Guest", "18800006204", Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 14, 0)), Timestamp.valueOf(LocalDateTime.of(2026, 5, 21, 12, 0)), 1, 18800L, 18800L, "paid", "ota", Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 9, 0)));
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 62031L, CAMP_ID, 62010L, "income", 62001L, 62011L, 62041L, "order", 62041L, 18800L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 10, 15)), USER_ID, "Ledger Tester", Date.valueOf(LocalDate.of(2026, 5, 20)), "ledger income note");
        jdbcTemplate.update("INSERT INTO ledger_entry (ledger_entry_id,camp_id,poi_id,entry_type,payment_type_id,payment_way_id,order_id,source_type,source_id,amount_cent,occurred_at,operator_id,operator_name,biz_date,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 62032L, CAMP_ID, 62010L, "expense", 62002L, 62011L, 62041L, "manual", 62032L, 3400L, Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 11, 30)), USER_ID, "Ledger Tester", Date.valueOf(LocalDate.of(2026, 5, 20)), "ledger expense note");
    }

    private void resetLedgerEntryFrontendData() {
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE ledger_entry_id BETWEEN 62031 AND 62039");
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id = 62041");
        jdbcTemplate.update("DELETE FROM room WHERE room_id = 62022");
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = 62021");
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = 62051");
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id = 62011");
        jdbcTemplate.update("DELETE FROM payment_type WHERE payment_type_id IN (62001, 62002)");
        jdbcTemplate.update("DELETE FROM payment_type_group WHERE payment_type_group_id IN (62000, 62003)");
        jdbcTemplate.update("DELETE FROM pms_poi WHERE poi_id = 62010");
    }

    private void seedStatementOrderData() {
        resetStatementOrderData();
        jdbcTemplate.update("INSERT INTO pms_poi (poi_id,camp_id,poi_name,is_availability,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?)", 65011L, CAMP_ID, "TDD小程序结算门店", 1, 1, 1, 0);
        jdbcTemplate.update("""
                INSERT INTO room_category (
                    room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,
                    weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,
                    latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,
                    article_description,sort_no,status,is_deleted
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                65021L, CAMP_ID, 65011L, "Finance Statement Room", "Finance Statement Room", 1, 2,
                45678L, 45678L, 45678L, 14, 23, 12, "statement highlight", "statement nearby",
                "finance statement room category", 1, 1, 0);
        jdbcTemplate.update("""
                INSERT INTO goods_main (
                    goods_id,camp_id,goods_type,name,category_name,selling_price_cent,original_price_cent,
                    settlement_price_cent,stock,stock_mode,shelf_status,status,remark,is_deleted
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                65031L, CAMP_ID, "coupon", "Finance Statement Coupon", "Finance Statement Product Type",
                45678L, 45678L, 44112L, 10, "manual", "on_shelf", "published", "finance statement goods", 0);
        jdbcTemplate.update("INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,0)", 65041L, CAMP_ID, "Finance Statement Pay", "finance_statement_pay", "online", 1);
        insertStatementOrder(65051L, "FIN-STMT-65051", "OUT-FIN-STMT-65051", "mall", "Finance Statement Guest", "18800001111", LocalDateTime.of(2026, 5, 18, 12, 0), 45678L, 0L, 45678L, 0L, 321L, 456L, 789L, 44112L);
        insertStatementOrder(65052L, "FIN-STMT-65052", "OUT-FIN-STMT-65052", "frontdesk", "Finance Frontdesk Guest", "18800002222", LocalDateTime.of(2026, 5, 18, 13, 0), 12345L, 0L, 12345L, 0L, 0L, 0L, 0L, 12345L);
    }

    private void insertStatementOrder(
            long orderId,
            String orderNo,
            String outOrderNo,
            String sourceType,
            String guestName,
            String guestMobile,
            LocalDateTime createdAt,
            long totalPriceCent,
            long discountPriceCent,
            long totalPayPriceCent,
            long refundPriceCent,
            long paymentFeeCent,
            long platformServiceFeeCent,
            long distributorCommissionCent,
            long settlementAmountCent
    ) {
        jdbcTemplate.update("""
                INSERT INTO order_main (
                    order_id,camp_id,poi_id,room_category_id,room_id,channel_id,goods_id,
                    order_no,out_order_no,order_type,status,guest_name,guest_mobile,start_at,end_at,
                    day_num,total_price_cent,discount_price_cent,total_pay_price_cent,refund_price_cent,
                    commission_price_cent,payment_fee_cent,platform_service_fee_cent,distribution_commission_cent,
                    settlement_amount_cent,payment_status,payment_type_id,payment_way_id,source_type,remark,
                    created_at,updated_at,created_by,updated_by,is_deleted,version_no
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,0)
                """,
                orderId, CAMP_ID, 65011L, 65021L, null, 17001L, 65031L,
                orderNo, outOrderNo, "coupon_order", "completed", guestName, guestMobile,
                Timestamp.valueOf(createdAt.plusDays(1)), Timestamp.valueOf(createdAt.plusDays(2)), 1,
                totalPriceCent, discountPriceCent, totalPayPriceCent, refundPriceCent,
                distributorCommissionCent, paymentFeeCent, platformServiceFeeCent, distributorCommissionCent,
                settlementAmountCent, "paid", null, 65041L, sourceType, "finance statement seed",
                Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt.plusMinutes(5)), USER_ID, USER_ID);
    }

    private void resetStatementOrderData() {
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 65051 AND 65099");
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id BETWEEN 65041 AND 65049");
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id BETWEEN 65031 AND 65039");
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id BETWEEN 65021 AND 65029");
        jdbcTemplate.update("DELETE FROM pms_poi WHERE poi_id = 65011");
    }

    private void resetFinanceData() {
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE ledger_entry_id BETWEEN 54001 AND 54099 OR order_id BETWEEN 28002 AND 28099");
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 28002 AND 28099");
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE order_id BETWEEN 28002 AND 28099");
        jdbcTemplate.update("DELETE FROM distribution_order WHERE distribution_order_id BETWEEN 56001 AND 56099 OR source_order_id BETWEEN 28002 AND 28099");
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 28002 AND 28099");
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE id BETWEEN 55201 AND 55299");
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id BETWEEN 56021 AND 56099 OR room_category_id = 22001");
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE id BETWEEN 55101 AND 55199");
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id BETWEEN 55001 AND 55099");
        jdbcTemplate.update("DELETE FROM finance_setting WHERE setting_id BETWEEN 53101 AND 53199 OR camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id BETWEEN 53001 AND 53099");
        jdbcTemplate.update("DELETE FROM payment_type WHERE payment_type_id BETWEEN 52001 AND 52099 OR payment_type_name = 'Finance Test Custom Income'");
        jdbcTemplate.update("DELETE FROM payment_type_group WHERE payment_type_group_id BETWEEN 51001 AND 51099");
    }

    private void seedShiftWorkData() {
        resetShiftWorkData();
        jdbcTemplate.update("""
                INSERT INTO shift_work_config (
                    shift_config_id,
                    camp_id,
                    name,
                    start_time,
                    end_time,
                    member_ids_json,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), 1, ?)
                """,
                57001L,
                CAMP_ID,
                "TDD早班",
                "08:00:00",
                "16:00:00",
                "[12001]",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 8, 0))
        );
        jdbcTemplate.update("""
                INSERT INTO shift_work_goods (
                    shift_goods_id,
                    camp_id,
                    name,
                    sort_no,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, 1, 1, ?)
                """,
                57101L,
                CAMP_ID,
                "TDD房卡",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 8, 30))
        );
        jdbcTemplate.update("""
                INSERT INTO shift_record (
                    shift_record_id,
                    camp_id,
                    shift_config_id,
                    handover_member_id,
                    successor_member_id,
                    summary_json,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), ?)
                """,
                57201L,
                CAMP_ID,
                57001L,
                14001L,
                14001L,
                """
                        {
                          "generalIncome": 123.45,
                          "netIncome": 100.00,
                          "totalExpenditure": 23.45,
                          "remark": "TDD交班摘要",
                          "workGoods": [
                            {"id":"57101","goodsName":"TDD房卡","goodsNumber":3,"remark":"齐全"}
                          ],
                          "workIncomeSourceList": [
                            {"sourceName":"房费","income":123.45,"expend":0,"remark":"现金"}
                          ],
                          "paymentTypeList": [
                            {"paymentName":"现金","income":123.45,"expend":0,"remark":"前台"}
                          ]
                        }
                        """,
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 16, 5))
        );
    }

    private void resetShiftWorkData() {
        jdbcTemplate.update("DELETE FROM shift_record WHERE shift_record_id BETWEEN 57201 AND 57299");
        jdbcTemplate.update("DELETE FROM shift_work_goods WHERE shift_goods_id BETWEEN 57101 AND 57199");
        jdbcTemplate.update("DELETE FROM shift_work_config WHERE shift_config_id BETWEEN 57001 AND 57099");
    }


    private void seedShiftWorkMutationData() {
        jdbcTemplate.update("""
                INSERT INTO shift_work_config (
                    shift_config_id,
                    camp_id,
                    name,
                    start_time,
                    end_time,
                    member_ids_json,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), 1, ?)
                """,
                57301L,
                CAMP_ID,
                "TDD_SHIFT_OLD_MORNING",
                "08:00:00",
                "16:00:00",
                "[12001]",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 8, 0))
        );
        jdbcTemplate.update("""
                INSERT INTO shift_work_config (
                    shift_config_id,
                    camp_id,
                    name,
                    start_time,
                    end_time,
                    member_ids_json,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), 1, ?)
                """,
                57302L,
                CAMP_ID,
                "TDD_SHIFT_OLD_NIGHT",
                "16:00:00",
                "23:00:00",
                "[12001]",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 16, 0))
        );
        jdbcTemplate.update("""
                INSERT INTO shift_work_goods (
                    shift_goods_id,
                    camp_id,
                    name,
                    sort_no,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, ?, 1, ?)
                """,
                57401L,
                CAMP_ID,
                "TDD_GOODS_OLD_CARD",
                1,
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 8, 30))
        );
        jdbcTemplate.update("""
                INSERT INTO shift_work_goods (
                    shift_goods_id,
                    camp_id,
                    name,
                    sort_no,
                    status,
                    updated_at
                ) VALUES (?, ?, ?, ?, 1, ?)
                """,
                57402L,
                CAMP_ID,
                "TDD_GOODS_OLD_RADIO",
                2,
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 8, 35))
        );
    }

    private void resetShiftWorkMutationData() {
        jdbcTemplate.update("DELETE FROM shift_work_goods WHERE shift_goods_id BETWEEN 57401 AND 57499 OR name IN ('TDD_GOODS_OLD_CARD','TDD_GOODS_OLD_RADIO','TDD_GOODS_CARD','TDD_GOODS_KEY')");
        jdbcTemplate.update("DELETE FROM shift_work_config WHERE shift_config_id BETWEEN 57301 AND 57399 OR name IN ('TDD_SHIFT_OLD_MORNING','TDD_SHIFT_OLD_NIGHT','TDD_SHIFT_UPDATED','TDD_SHIFT_NIGHT')");
    }


    private void seedPaymentSettingMutationData() {
        jdbcTemplate.update("""
                INSERT INTO payment_way (
                    payment_way_id,
                    camp_id,
                    payment_way_name,
                    payment_way_code,
                    way_type,
                    sort_no,
                    status,
                    is_deleted
                ) VALUES (?, ?, ?, ?, 'custom', ?, 1, 0)
                """,
                58011L,
                CAMP_ID,
                "TDD排序方式A",
                "tdd_sort_a",
                10
        );
        jdbcTemplate.update("""
                INSERT INTO payment_way (
                    payment_way_id,
                    camp_id,
                    payment_way_name,
                    payment_way_code,
                    way_type,
                    sort_no,
                    status,
                    is_deleted
                ) VALUES (?, ?, ?, ?, 'custom', ?, 1, 0)
                """,
                58012L,
                CAMP_ID,
                "TDD排序方式B",
                "tdd_sort_b",
                20
        );
    }

    private void resetPaymentSettingMutationData() {
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id BETWEEN 58001 AND 58099 OR payment_way_name = 'TDD新增支付方式'");
    }
}

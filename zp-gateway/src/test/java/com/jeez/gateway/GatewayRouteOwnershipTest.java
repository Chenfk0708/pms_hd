package com.jeez.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GatewayRouteOwnershipTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    @Timeout(60)
    void domainServiceRoutesAreEvaluatedBeforePlatformFallback() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        assertRouteBefore(routes, "room-service", "platform-service");
        assertRouteBefore(routes, "order-service", "platform-service");
        assertRouteBefore(routes, "crm-service", "platform-service");
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsCleanTaskActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String pathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/cleanTask/**"),
                () -> "room-service route must own all cleanTask actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/clean/statistics/**"),
                () -> "room-service route must own clean statistics contract endpoints, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsCleanManageSettingActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String pathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/cleanManage/cleanSetting/**"),
                () -> "room-service route must own cleanManage cleanSetting actions, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsCleanLogActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String pathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/cleanLog/**"),
                () -> "room-service route must own cleanLog actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/cleanManage/cleanLog/**"),
                () -> "room-service route must own cleanManage cleanLog actions, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsPriceLogActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String pathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/houseManage/logs/price/**"),
                () -> "room-service route must own price log actions, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsPriceConfigAndRetailPriceActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String pathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/roomCategoryPricings/get"),
                () -> "room-service route must own roomCategoryPricings get, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/roomCategoryRules/get"),
                () -> "room-service route must own roomCategoryRules get, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/roomCategoryPrice/**"),
                () -> "room-service route must own roomCategoryPrice actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/systemConfig/price/**"),
                () -> "room-service route must own price systemConfig actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/roomCategoryStatuses/roomCategory/get"),
                () -> "room-service route must own retail roomCategory status actions, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void roomRouteOwnsRoomCategoryPhotoUploadWithoutStealingPlatformRoomCategoryActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition roomRoute = findRoute(routes, "room-service");
        assertNotNull(roomRoute, "Missing gateway route: room-service");
        String roomPathPatterns = roomRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(roomPathPatterns.contains("/roomCategory/photo/upload"),
                () -> "room-service route must own room category photo upload, actual Path predicate: " + roomPathPatterns);
        assertFalse(roomPathPatterns.contains("/roomCategory/**"),
                () -> "room-service route must not steal platform roomCategory actions, actual Path predicate: " + roomPathPatterns);

        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String platformPathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(platformPathPatterns.contains("/roomCategory/**"),
                () -> "platform-service route must keep owning existing roomCategory actions, actual Path predicate: " + platformPathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsCampRolesForShiftPages() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/campRoles/get"),
                () -> "platform-service route must own campRoles options, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsFinanceSystemConfigMutations() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/systemConfigs/**"),
                () -> "platform-service route must own finance systemConfig mutations through systemConfigs wildcard, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsWorkspaceEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/report/accommodation/get"),
                () -> "platform-service route must own income report endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/report/profit/get/v2"),
                () -> "platform-service route must own profit report query endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/statistics/profit-report/export"),
                () -> "platform-service route must own profit report export endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/statistics/profit-report/export/download"),
                () -> "platform-service route must own profit report export download endpoint, actual Path predicate: " + pathPatterns);

        assertTrue(pathPatterns.contains("/globalRadar/**"),
                () -> "platform-service route must own AI global radar action endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/campFlow/get"),
                () -> "platform-service route must own workspace traffic endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/memo/page/get"),
                () -> "platform-service route must own workspace memo query endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/memo/add"),
                () -> "platform-service route must own workspace memo create endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/memo/handle"),
                () -> "platform-service route must own workspace memo handle endpoint, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsPsbLogEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/checkinGuestPsbLog/**"),
                () -> "platform-service route must own PSB log query and retry endpoints, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsSortSettingEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/roomCategory/**"),
                () -> "platform-service route must own room category sort actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/channelRoomCategories/**"),
                () -> "platform-service route must own goods sort actions, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/weiRoomCategories/**"),
                () -> "platform-service route must own goods list actions, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsOtaEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String platformPathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(platformPathPatterns.contains("/ota/**"),
                () -> "platform-service route must own OTA endpoints through ota wildcard, actual Path predicate: "
                        + platformPathPatterns);

        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String orderPathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertFalse(orderPathPatterns.contains("/ota/dashboard/get"),
                () -> "order-service route must not shadow platform OTA dashboard endpoint, actual Path predicate: "
                        + orderPathPatterns);
        assertFalse(orderPathPatterns.contains("/ota/channel/detail/get"),
                () -> "order-service route must not shadow platform OTA channel detail endpoint, actual Path predicate: "
                        + orderPathPatterns);
    }


    @Test
    @Timeout(60)
    void platformRouteOwnsVersionSubscriptionEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/version/**"),
                () -> "platform-service route must own version subscription endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/user/**"),
                () -> "platform-service route must own user secret API key endpoints, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsShiftWorkPages() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/shiftWorkConfig/page/get"),
                () -> "order-service route must own shiftWorkConfig page, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/shiftWorkConfig/save"),
                () -> "order-service route must own shiftWorkConfig save, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/shiftWorkGoods/page/get"),
                () -> "order-service route must own shiftWorkGoods page, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/shiftWorkGoods/save"),
                () -> "order-service route must own shiftWorkGoods save, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/shiftWorkReport/page/get"),
                () -> "order-service route must own shiftWorkReport page, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsPaymentSettingMutations() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/paymentSettings/create"),
                () -> "order-service route must own paymentSettings create, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/paymentSettings/status/update"),
                () -> "order-service route must own paymentSettings status update, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/paymentSettings/default/update"),
                () -> "order-service route must own paymentSettings default update, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/paymentSettings/sort/update"),
                () -> "order-service route must own paymentSettings sort update, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/paymentSettings/export"),
                () -> "order-service route must own paymentSettings export, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/paymentTypes/custom/create"),
                () -> "order-service route must own custom payment type create, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsSocialChannelOverview() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/channels/social/overview"),
                () -> "order-service route must own social channel overview, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/channels/custom/list"),
                () -> "order-service route must own custom channel list, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/channels/custom/create"),
                () -> "order-service route must own custom channel create, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/channels/custom/update"),
                () -> "order-service route must own custom channel update, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/channels/custom/delete"),
                () -> "order-service route must own custom channel delete, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsFullMarketingReports() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/promotionPlanProducts/page/get"),
                () -> "order-service route must own full marketing commission products, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/report/promotion/get"),
                () -> "order-service route must own full marketing promotion metrics, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/report/promotion/productSale/page/get"),
                () -> "order-service route must own full marketing product sales, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsLedgerSummaryAndStatementOrderReports() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/accountBookPaymentWay/page/get"),
                () -> "order-service route must own total ledger summary, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/orderLedger/dashboard/get"),
                () -> "order-service route must own ledger entry/order ledger dashboard, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/report/storer/statement/get"),
                () -> "order-service route must own storer statement order report, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsCouponPages() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/coupons/page/get"),
                () -> "order-service route must own coupon list page, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/couponSendConfigs/page/get"),
                () -> "order-service route must own coupon send config page, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsChangeRoomActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/orders/*/change-room/options"),
                () -> "order-service route must own change-room options, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/orders/*/change-room"),
                () -> "order-service route must own change-room submit, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsSkipStockActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/orders/*/skip-stock"),
                () -> "order-service route must own skip-stock action, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsNoShowActions() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/orders/*/mark-no-show"),
                () -> "order-service route must own mark-no-show action, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void orderRouteOwnsChannelOrderImport() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition orderRoute = findRoute(routes, "order-service");
        assertNotNull(orderRoute, "Missing gateway route: order-service");
        String pathPatterns = orderRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/channelOrders/import"),
                () -> "order-service route must own channel order import, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void crmRouteOwnsCustomerTagAndWeComAccountEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition crmRoute = findRoute(routes, "crm-service");
        assertNotNull(crmRoute, "Missing gateway route: crm-service");
        String pathPatterns = crmRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/memberTagGroup/**"),
                () -> "crm-service route must own member tag group endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/wxCpOpen/accounts/get"),
                () -> "crm-service route must own WeCom account sync endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/wxcp/kfAccount/**"),
                () -> "crm-service route must own WeCom customer-service account endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/scrm/sidebarPreview/**"),
                () -> "crm-service route must own SCRM sidebar preview endpoints, actual Path predicate: " + pathPatterns);
    }


    @Test
    @Timeout(60)
    void crmRouteOwnsImSettingEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition crmRoute = findRoute(routes, "crm-service");
        assertNotNull(crmRoute, "Missing gateway route: crm-service");
        String pathPatterns = crmRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/imWordsGroup/**"),
                () -> "crm-service route must own IM phrase group endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/imWords/**"),
                () -> "crm-service route must own IM phrase list endpoints, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/imYunxinUser/get"),
                () -> "crm-service route must own IM Yunxin user endpoint, actual Path predicate: " + pathPatterns);
    }

    @Test
    @Timeout(60)
    void platformRouteOwnsCommonsAndUserShortcutEndpoints() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(routes);
        RouteDefinition platformRoute = findRoute(routes, "platform-service");
        assertNotNull(platformRoute, "Missing gateway route: platform-service");
        String pathPatterns = platformRoute.getPredicates().get(0).getArgs().values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        assertTrue(pathPatterns.contains("/commons/get"),
                () -> "platform-service route must own commons dictionary endpoint, actual Path predicate: " + pathPatterns);
        assertTrue(pathPatterns.contains("/systemConfigs/**"),
                () -> "platform-service route must own user shortcut endpoint through systemConfigs wildcard, actual Path predicate: " + pathPatterns);
    }

    private static void assertRouteBefore(List<RouteDefinition> routes, String routeId, String fallbackRouteId) {
        int routeIndex = indexOf(routes, routeId);
        int fallbackRouteIndex = indexOf(routes, fallbackRouteId);
        assertTrue(routeIndex >= 0, () -> "Missing gateway route: " + routeId);
        assertTrue(fallbackRouteIndex >= 0, () -> "Missing gateway route: " + fallbackRouteId);
        assertTrue(routeIndex < fallbackRouteIndex,
                () -> routeId + " must be declared before " + fallbackRouteId + " to avoid being shadowed");
    }

    private static int indexOf(List<RouteDefinition> routes, String routeId) {
        for (int i = 0; i < routes.size(); i++) {
            if (routeId.equals(routes.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private static RouteDefinition findRoute(List<RouteDefinition> routes, String routeId) {
        for (RouteDefinition route : routes) {
            if (routeId.equals(route.getId())) {
                return route;
            }
        }
        return null;
    }
}

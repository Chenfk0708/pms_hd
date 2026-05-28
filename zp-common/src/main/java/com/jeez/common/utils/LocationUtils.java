package com.jeez.common.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 地理位置工具类
 * 支持根据IP地址获取地理位置信息
 *
 * @author jeez
 * @since 2025-12-15
 */
@Slf4j
public class LocationUtils {

    // 模拟的IP地理位置数据库（实际项目中应该调用第三方API）
    private static final Pattern CHINA_IP_PATTERN = Pattern.compile("^(1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|115|116|117|118|119|120|121|122|123|124|125|126|127)\\.\\d+\\.\\d+\\.\\d+");

    private static final String DEFAULT_CITY = "北京市";
    private static final double DEFAULT_LNG = 116.404;
    private static final double DEFAULT_LAT = 39.915;

    /**
     * 位置信息类
     */
    @Data
    public static class LocationInfo {
        private String country = "中国";
        private String province = "北京";
        private String city = DEFAULT_CITY;
        private String district = "朝阳区";
        private String address = "北京市朝阳区";
        private String cityCode = "110000";
        private String districtCode = "110105";
        private double longitude = DEFAULT_LNG;
        private double latitude = DEFAULT_LAT;
    }

    /**
     * 根据IP地址获取地理位置信息
     * 这里是模拟实现，实际项目中应该调用第三方API（如百度地图、高德地图等）
     *
     * @param ip IP地址
     * @return 位置信息
     */
    public static LocationInfo getLocationByIp(String ip) {
        if (ip == null || ip.equals("unknown")) {
            log.warn("IP地址无效: {}", ip);
            return null;
        }

        try {
            // 这里应该调用第三方API获取地理位置信息
            // 为了演示，这里使用简单的模拟逻辑

            LocationInfo location = new LocationInfo();

            // 根据IP网段简单判断地区（仅作演示）
            if (ip.startsWith("120.")) {
                // 假设120开头的是广东地区
                location.setProvince("广东省");
                location.setCity("广州市");
                location.setDistrict("天河区");
                location.setAddress("广东省广州市天河区");
                location.setCityCode("440100");
                location.setDistrictCode("440106");
                location.setLongitude(113.264);
                location.setLatitude(23.129);
            } else if (ip.startsWith("122.")) {
                // 假设122开头的是上海地区
                location.setProvince("上海市");
                location.setCity("上海市");
                location.setDistrict("浦东新区");
                location.setAddress("上海市浦东新区");
                location.setCityCode("310000");
                location.setDistrictCode("310115");
                location.setLongitude(121.544);
                location.setLatitude(31.229);
            } else if (ip.startsWith("114.")) {
                // 假设114开头的是四川地区
                location.setProvince("四川省");
                location.setCity("成都市");
                location.setDistrict("武侯区");
                location.setAddress("四川省成都市武侯区");
                location.setCityCode("510100");
                location.setDistrictCode("510107");
                location.setLongitude(104.066);
                location.setLatitude(30.572);
            } else {
                // 默认返回北京
                log.debug("使用默认位置信息，IP: {}", ip);
            }

            log.info("根据IP {} 获取位置信息: {}", ip, location.getAddress());
            return location;

        } catch (Exception e) {
            log.error("根据IP {} 获取位置信息失败", ip, e);
            return null;
        }
    }

    /**
     * 调用第三方API获取IP地理位置（示例）
     * 实际项目中可以使用百度地图API、高德地图API等
     *
     * @param ip IP地址
     * @return JSON格式的位置信息
     */
    private static String callLocationApi(String ip) {
        try {
            // 示例：调用免费IP地理位置API
            String apiUrl = "http://ip.taobao.com/service/getIpInfo.php?ip=" + ip;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            log.error("调用IP地理位置API失败", e);
            return null;
        }
    }

    /**
     * 计算两点之间的距离（单位：米）
     * 使用Haversine公式计算地球表面两点间的大圆距离
     *
     * @param lat1 第一点的纬度
     * @param lng1 第一点的经度
     * @param lat2 第二点的纬度
     * @param lng2 第二点的经度
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 计算两点之间的距离（单位：公里）
     *
     * @param lat1 第一点的纬度
     * @param lng1 第一点的经度
     * @param lat2 第二点的纬度
     * @param lng2 第二点的经度
     * @return 距离（公里）
     */
    public static double calculateDistanceInKm(double lat1, double lng1, double lat2, double lng2) {
        return calculateDistance(lat1, lng1, lat2, lng2) / 1000.0;
    }

    /**
     * 格式化距离显示
     * 小于1公里显示米，大于等于1公里显示公里
     *
     * @param distanceInMeters 距离（米）
     * @return 格式化后的距离字符串
     */
    public static String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format("%.0f米", distanceInMeters);
        } else {
            return String.format("%.1f公里", distanceInMeters / 1000.0);
        }
    }

    /**
     * 批量计算多个位置到某个位置的距离
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param locations 多个目标位置的坐标数组 [lat, lng, lat, lng, ...]
     * @return 距离数组（米）
     */
    public static double[] calculateDistances(double fromLat, double fromLng, double[] locations) {
        if (locations.length % 2 != 0) {
            throw new IllegalArgumentException("坐标数组长度必须是偶数");
        }

        double[] distances = new double[locations.length / 2];
        for (int i = 0; i < locations.length; i += 2) {
            distances[i / 2] = calculateDistance(fromLat, fromLng, locations[i], locations[i + 1]);
        }
        return distances;
    }

    /**
     * 检查某个位置是否在指定半径范围内
     *
     * @param centerLat 中心点纬度
     * @param centerLng 中心点经度
     * @param targetLat 目标点纬度
     * @param targetLng 目标点经度
     * @param radiusMeters 半径（米）
     * @return 是否在范围内
     */
    public static boolean isWithinRadius(double centerLat, double centerLng,
                                        double targetLat, double targetLng,
                                        double radiusMeters) {
        double distance = calculateDistance(centerLat, centerLng, targetLat, targetLng);
        return distance <= radiusMeters;
    }

    /**
     * 根据地址解析经纬度（示例）
     * 实际项目中应该调用地图API
     *
     * @param address 地址
     * @return [经度, 纬度]
     */
    public static double[] geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            return new double[]{DEFAULT_LNG, DEFAULT_LAT};
        }

        // 这里应该调用第三方地图API进行地址解析
        // 为了演示，返回北京坐标
        return new double[]{DEFAULT_LNG, DEFAULT_LAT};
    }
}
package com.jeez.common.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp工具类
 * 提供HTTP GET、POST、PUT、DELETE等常用请求方法
 */
public class OkHttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);

    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    /**
     * GET请求
     *
     * @param url 请求URL
     * @return 响应字符串
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * GET请求（带请求头）
     *
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String get(String url, Map<String, String> headers) {
        return get(url, headers, null);
    }

    /**
     * GET请求（带请求头和查询参数）
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param queryParams 查询参数
     * @return 响应字符串
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> queryParams) {
        try {
            // 构建URL和查询参数
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            if (queryParams != null) {
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }

            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build())
                    .get();

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = requestBuilder.build();
            Response response = DEFAULT_CLIENT.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            logger.error("GET请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("GET请求失败", e);
        }
    }

    /**
     * POST请求（JSON格式）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @return 响应字符串
     */
    public static String post(String url, String jsonBody) {
        return post(url, jsonBody, "application/json", null);
    }

    /**
     * POST请求（带请求头）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param contentType 内容类型
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String post(String url, String jsonBody, String contentType, Map<String, String> headers) {
        try {
            MediaType mediaType = MediaType.parse(contentType != null ? contentType : "application/json");
            RequestBody body = RequestBody.create(jsonBody, mediaType);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body);

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = requestBuilder.build();
            Response response = DEFAULT_CLIENT.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            logger.error("POST请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("POST请求失败", e);
        }
    }

    /**
     * POST请求（表单格式）
     *
     * @param url 请求URL
     * @param formData 表单数据
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, String> formData) {
        return postForm(url, formData, null);
    }

    /**
     * POST请求（表单格式，带请求头）
     *
     * @param url 请求URL
     * @param formData 表单数据
     * @param headers 额外请求头
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, String> formData, Map<String, String> headers) {
        try {
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (formData != null) {
                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
            }

            RequestBody body = formBuilder.build();
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body);

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = requestBuilder.build();
            Response response = DEFAULT_CLIENT.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            logger.error("POST表单请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("POST表单请求失败", e);
        }
    }

    /**
     * PUT请求（JSON格式）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @return 响应字符串
     */
    public static String put(String url, String jsonBody) {
        return put(url, jsonBody, null);
    }

    /**
     * PUT请求（带请求头）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String put(String url, String jsonBody, Map<String, String> headers) {
        try {
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .put(body);

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = requestBuilder.build();
            Response response = DEFAULT_CLIENT.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            logger.error("PUT请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("PUT请求失败", e);
        }
    }

    /**
     * DELETE请求
     *
     * @param url 请求URL
     * @return 响应字符串
     */
    public static String delete(String url) {
        return delete(url, null);
    }

    /**
     * DELETE请求（带请求头）
     *
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String delete(String url, Map<String, String> headers) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .delete();

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            Request request = requestBuilder.build();
            Response response = DEFAULT_CLIENT.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            logger.error("DELETE请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("DELETE请求失败", e);
        }
    }

    /**
     * 获取默认的OkHttpClient实例
     *
     * @return OkHttpClient实例
     */
    public static OkHttpClient getDefaultClient() {
        return DEFAULT_CLIENT;
    }

    /**
     * 创建自定义的OkHttpClient实例
     *
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout 读取超时时间（秒）
     * @param writeTimeout 写入超时时间（秒）
     * @return OkHttpClient实例
     */
    public static OkHttpClient createCustomClient(long connectTimeout, long readTimeout, long writeTimeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
}
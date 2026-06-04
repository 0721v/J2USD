package com.giftcard.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

/**
 * OKX API V5 客户端
 * 实现 HMAC SHA256 签名认证，支持 GET/POST 请求
 */
@Component
public class OkxApiClient {

    private static final String BASE_URL = "https://www.okx.com";

    @Autowired
    private PaymentConfigServiceImpl paymentConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取配置了 HTTPS 的 RestTemplate
     * 支持通过环境变量设置代理：HTTP_PROXY=http://host:port
     */
    private RestTemplate getRestTemplate() {
        try {
            // 配置信任所有证书（仅用于开发环境）
            var sslFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslFactory)
                    .build();

            // 检查是否配置了代理
            String proxyUrl = System.getenv("HTTP_PROXY");
            if (proxyUrl == null || proxyUrl.isEmpty()) {
                proxyUrl = System.getenv("http_proxy");
            }

            CloseableHttpClient httpClient;
            if (proxyUrl != null && !proxyUrl.isEmpty()) {
                System.out.println("[OKX] 使用代理: " + proxyUrl);
                // 解析代理地址
                java.net.URL url = new java.net.URL(proxyUrl);
                HttpHost proxy = new HttpHost(url.getHost(), url.getPort());
                httpClient = HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .setProxy(proxy)
                        .build();
            } else {
                httpClient = HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .build();
            }

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);
            return new RestTemplate(factory);
        } catch (Exception e) {
            System.err.println("[OKX] RestTemplate 创建失败，使用默认配置: " + e.getMessage());
            return new RestTemplate();
        }
    }

    /**
     * 构建带认证信息的请求头
     */
    private HttpHeaders buildHeaders(String method, String requestPath, String body) {
        // 使用 getOkxConfig 获取配置，支持加密字段自动解密
        Map<String, String> config = paymentConfigService.getOkxConfig();
        String apiKey = config.get("api_key");
        String passphrase = config.get("passphrase");
        // OKX 要求 UTC 时间的 ISO8601 格式，如：2024-01-01T08:00:00.000Z
        // 使用 UTC 时区确保与 OKX 服务器时间一致
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("OK-ACCESS-KEY", apiKey);
        headers.set("OK-ACCESS-SIGN", sign(timestamp, method, requestPath, body));
        headers.set("OK-ACCESS-TIMESTAMP", timestamp);
        headers.set("OK-ACCESS-PASSPHRASE", passphrase);
        headers.set("x-simulated-trading", "0");

        return headers;
    }

    /**
     * 生成 HMAC SHA256 签名
     * 签名内容：timestamp + method + requestPath + body
     * 结果使用 Base64 编码
     */
    private String sign(String timestamp, String method, String requestPath, String body) {
        try {
            String message = timestamp + method.toUpperCase() + requestPath + (body == null ? "" : body);
            System.out.println("[OKX] 签名内容: " + message);
            Mac mac = Mac.getInstance("HmacSHA256");
            // 使用 getOkxConfig 获取配置，支持加密字段自动解密
            Map<String, String> config = paymentConfigService.getOkxConfig();
            String secretKey = config.get("secret_key");
            System.out.println("[OKX] SecretKey 长度: " + (secretKey != null ? secretKey.length() : 0));
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hash);
            System.out.println("[OKX] 签名结果: " + signature);
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("OKX API 签名失败", e);
        }
    }

    /**
     * 发送 GET 请求
     */
    public JsonNode get(String requestPath) {
        try {
            String url = BASE_URL + requestPath;
            HttpHeaders headers = buildHeaders("GET", requestPath, null);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            System.out.println("[OKX] GET URL: " + url);
            ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
            System.out.println("[OKX] GET 响应状态: " + response.getStatusCode());
            System.out.println("[OKX] GET 响应内容: " + response.getBody());
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            System.err.println("[OKX] GET 请求失败: " + e.getMessage());
            throw new RuntimeException("OKX API GET 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 GET 请求（带查询参数）
     */
    public JsonNode get(String requestPath, String queryString) {
        try {
            String fullPath = queryString != null && !queryString.isEmpty()
                    ? requestPath + "?" + queryString
                    : requestPath;
            String url = BASE_URL + fullPath;
            HttpHeaders headers = buildHeaders("GET", fullPath, null);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            System.out.println("[OKX] GET URL: " + url);
            System.out.println("[OKX] 签名路径: " + fullPath);
            ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
            System.out.println("[OKX] GET 响应状态: " + response.getStatusCode());
            System.out.println("[OKX] GET 响应内容: " + response.getBody());
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            System.err.println("[OKX] GET 请求失败: " + e.getMessage());
            throw new RuntimeException("OKX API GET 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 POST 请求
     */
    public JsonNode post(String requestPath, Object body) {
        try {
            String url = BASE_URL + requestPath;
            String bodyStr = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpHeaders headers = buildHeaders("POST", requestPath, bodyStr);
            HttpEntity<String> entity = new HttpEntity<>(bodyStr, headers);

            ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("[OKX] POST 响应状态: " + response.getStatusCode());
            System.out.println("[OKX] POST 响应内容: " + response.getBody());       
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            System.err.println("[OKX] POST 请求失败: " + e.getMessage());
            throw new RuntimeException("OKX API POST 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询充值记录（带时间范围）
     * GET /api/v5/asset/deposit-history
     * @param ccy 币种，如 USDT
     * @param type 充值类型：4 表示链上充值
     * @param beginTime 开始时间（毫秒时间戳）
     * @param endTime 结束时间（毫秒时间戳）
     * @return 充值记录列表
     */
    public JsonNode getDepositHistory(String ccy, String type, Long beginTime, Long endTime) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("ccy=").append(ccy);
        if (type != null && !type.isEmpty()) {
            queryBuilder.append("&type=").append(type);
        }
        if (beginTime != null) {
            queryBuilder.append("&beginTime=").append(beginTime);
        }
        if (endTime != null) {
            queryBuilder.append("&endTime=").append(endTime);
        }
        // 限制返回5条
        queryBuilder.append("&limit=5");

        String queryString = queryBuilder.toString();
        System.out.println("[OKX] 查询充值记录: " + queryString);
        JsonNode result = get("/api/v5/asset/deposit-history", queryString);
        System.out.println("[OKX] 充值记录响应: " + result.toString());
        return result;
    }

    /**
     * 查询充值记录（简化版，查询最近 N 分钟）
     */
    public JsonNode getDepositHistoryRecent(String ccy, String type, int minutes) {
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - (minutes * 60 * 1000L);
        return getDepositHistory(ccy, type, beginTime, endTime);
    }

    /**
     * 通过交易ID查询充值详情
     * GET /api/v5/asset/deposit-history
     * @param txId 交易哈希
     * @return 充值详情
     */
    public JsonNode getDepositByTxId(String txId) {
        String queryString = "txId=" + txId;
        System.out.println("[OKX] 查询充值详情: txId=" + txId);
        JsonNode result = get("/api/v5/asset/deposit-history", queryString);
        System.out.println("[OKX] 充值详情响应: " + result.toString());
        return result;
    }

    /**
     * 查询账户账单（包含充值、转账等）
     * GET /api/v5/account/bills
     * @param ccy 币种，如 USDT（可选，不传则查所有币种）
     * @param beginTime 开始时间（毫秒时间戳）
     * @param endTime 结束时间（毫秒时间戳）
     * @return 账单列表
     */
    public JsonNode getBills(String ccy, Long beginTime, Long endTime) {
        StringBuilder queryBuilder = new StringBuilder();
        // 不传 ccy 参数，查所有币种的账单
        if (beginTime != null) {
            queryBuilder.append("beginTime=").append(beginTime);
        }
        if (endTime != null) {
            if (queryBuilder.length() > 0) queryBuilder.append("&");
            queryBuilder.append("endTime=").append(endTime);
        }
        // 限制返回5条
        if (queryBuilder.length() > 0) queryBuilder.append("&");
        queryBuilder.append("limit=5");

        String queryString = queryBuilder.toString();
        System.out.println("[OKX] 查询账户账单: " + queryString);
        JsonNode result = get("/api/v5/account/bills", queryString);
        System.out.println("[OKX] 账户账单响应: " + result.toString());
        return result;
    }

    /**
     * 查询账户账单（简化版，查询最近 N 分钟）
     */
    public JsonNode getBillsRecent(String ccy, int minutes) {
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - (minutes * 60 * 1000L);
        return getBills(ccy, beginTime, endTime);
    }

    /**
     * 通过账单ID查询详情
     * GET /api/v5/account/bills
     * @param billId 账单ID
     * @return 账单详情
     */
    public JsonNode getBillById(String billId) {
        String queryString = "billId=" + billId;
        System.out.println("[OKX] 查询账单详情: billId=" + billId);
        JsonNode result = get("/api/v5/account/bills", queryString);
        System.out.println("[OKX] 账单详情响应: " + result.toString());
        return result;
    }
}

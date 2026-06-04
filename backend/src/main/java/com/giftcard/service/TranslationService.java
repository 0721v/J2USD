package com.giftcard.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 翻译服务 - 使用 MyMemory 免费翻译API
 * 支持中、英、日、韩四种语言互译
 */
@Service
public class TranslationService {
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    
    private final HttpClient httpClient;
    
    // 语言代码映射
    private static final Map<String, String> LANG_MAP = new HashMap<>();
    static {
        LANG_MAP.put("zh", "zh-CN");
        LANG_MAP.put("en", "en");
        LANG_MAP.put("ja", "ja");
        LANG_MAP.put("ko", "ko");
    }
    
    public TranslationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * 翻译文本
     * @param text 要翻译的文本
     * @param sourceLang 源语言代码 (zh/en/ja/ko)
     * @param targetLang 目标语言代码 (zh/en/ja/ko)
     * @return 翻译后的文本，失败返回原文本
     */
    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        if (sourceLang.equals(targetLang)) {
            return text;
        }
        
        try {
            String sourceCode = LANG_MAP.getOrDefault(sourceLang, sourceLang);
            String targetCode = LANG_MAP.getOrDefault(targetLang, targetLang);
            String langPair = sourceCode + "|" + targetCode;
            
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = API_URL + "?q=" + encodedText + "&langpair=" + langPair;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = JSON.parseObject(response.body());
                JSONObject responseData = jsonResponse.getJSONObject("responseData");
                if (responseData != null) {
                    String translatedText = responseData.getString("translatedText");
                    if (translatedText != null && !translatedText.isEmpty()) {
                        return translatedText;
                    }
                }
            }
            
            log.warn("Translation API returned non-200 or empty response: status={}, body={}", 
                    response.statusCode(), response.body());
            return text;
            
        } catch (Exception e) {
            log.error("Translation failed: text={}, source={}, target={}", text, sourceLang, targetLang, e);
            return text;
        }
    }
    
    /**
     * 批量翻译多个文本
     * @param texts 要翻译的文本数组
     * @param sourceLang 源语言代码
     * @param targetLang 目标语言代码
     * @return 翻译后的文本数组
     */
    public String[] translateBatch(String[] texts, String sourceLang, String targetLang) {
        if (texts == null) {
            return new String[0];
        }
        String[] results = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            results[i] = translate(texts[i], sourceLang, targetLang);
        }
        return results;
    }
}

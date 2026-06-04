package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.service.TranslationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻译API控制器
 */
@RestController
@RequestMapping("/api/translate")
public class TranslateController {
    
    private final TranslationService translationService;
    
    public TranslateController(TranslationService translationService) {
        this.translationService = translationService;
    }
    
    /**
     * 翻译文本
     * POST /api/translate
     * Body: { "text": "要翻译的文本", "sourceLang": "zh", "targetLang": "en" }
     */
    @PostMapping
    public Result<Map<String, String>> translate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sourceLang = request.getOrDefault("sourceLang", "zh");
        String targetLang = request.getOrDefault("targetLang", "en");
        
        if (text == null || text.trim().isEmpty()) {
            return Result.error("翻译文本不能为空");
        }
        
        String translated = translationService.translate(text, sourceLang, targetLang);
        
        Map<String, String> result = new HashMap<>();
        result.put("original", text);
        result.put("translated", translated);
        result.put("sourceLang", sourceLang);
        result.put("targetLang", targetLang);
        
        return Result.success(result);
    }
    
    /**
     * 一键翻译到所有语言
     * POST /api/translate/all
     * Body: { "text": "要翻译的文本", "sourceLang": "zh" }
     * 返回: { "zh": "...", "en": "...", "ja": "...", "ko": "..." }
     */
    @PostMapping("/all")
    public Result<Map<String, String>> translateAll(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sourceLang = request.getOrDefault("sourceLang", "zh");
        
        if (text == null || text.trim().isEmpty()) {
            return Result.error("翻译文本不能为空");
        }
        
        String[] targetLangs = {"zh", "en", "ja", "ko"};
        Map<String, String> results = new HashMap<>();
        
        for (String targetLang : targetLangs) {
            results.put(targetLang, translationService.translate(text, sourceLang, targetLang));
        }
        
        return Result.success(results);
    }
}

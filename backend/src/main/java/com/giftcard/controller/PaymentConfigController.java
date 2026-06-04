package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.entity.PaymentConfig;
import com.giftcard.service.PaymentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment-config")
public class PaymentConfigController {
    
    @Autowired
    private PaymentConfigService paymentConfigService;
    
    /**
     * 获取所有支付配置
     */
    @GetMapping
    public Result<Map<String, Object>> getAllConfigs() {
        List<PaymentConfig> configs = paymentConfigService.getAllConfigs();
        
        // 隐藏敏感信息（加密字段）
        for (PaymentConfig config : configs) {
            if (config.getIsEncrypted() == 1 && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                config.setConfigValue("******");
            }
        }
        
        // 按类型分组
        Map<String, List<PaymentConfig>> grouped = new HashMap<>();
        for (PaymentConfig config : configs) {
            grouped.computeIfAbsent(config.getConfigType(), k -> new java.util.ArrayList<>()).add(config);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("configs", grouped);
        
        // 同时返回启用状态
        Map<String, Boolean> enabledStatus = new HashMap<>();
        enabledStatus.put("wechat", paymentConfigService.isPaymentEnabled("wechat"));
        enabledStatus.put("alipay", paymentConfigService.isPaymentEnabled("alipay"));
        enabledStatus.put("trc20", paymentConfigService.isPaymentEnabled("trc20"));
        enabledStatus.put("okx", paymentConfigService.isPaymentEnabled("okx"));
        result.put("enabledStatus", enabledStatus);
        
        return Result.success(result);
    }
    
    /**
     * 按类型获取配置
     */
    @GetMapping("/type/{type}")
    public Result<Map<String, Object>> getConfigsByType(@PathVariable String type) {
        List<PaymentConfig> configs = paymentConfigService.getConfigsByType(type);
        
        // 判断该类型是否启用
        boolean isEnabled = paymentConfigService.isPaymentEnabled(type);
        
        // 隐藏敏感信息
        for (PaymentConfig config : configs) {
            if (config.getIsEncrypted() == 1 && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                config.setConfigValue("******");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("configs", configs);
        result.put("isEnabled", isEnabled);
        result.put("type", type);
        
        return Result.success(result);
    }
    
    /**
     * 更新单个配置
     */
    @PutMapping("/{id}")
    public Result<String> updateConfig(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        
        String configValue = params.get("configValue");
        boolean success = paymentConfigService.updateConfig(id, configValue);
        
        if (success) {
            return Result.success("Configuration updated successfully", null);
        } else {
            return Result.error("Failed to update configuration");
        }
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/type/{type}")
    public Result<String> batchUpdateConfigs(
            @PathVariable String type,
            @RequestBody Map<String, String> configs) {
        
        boolean success = paymentConfigService.batchUpdateConfigs(type, configs);
        
        if (success) {
            return Result.success("Configuration updated successfully", null);
        } else {
            return Result.error("Failed to update configuration");
        }
    }
    
    /**
     * 启用/禁用支付方式
     */
    @PutMapping("/toggle/{type}")
    public Result<String> togglePayment(
            @PathVariable String type,
            @RequestBody Map<String, Boolean> params) {
        
        boolean enabled = params.getOrDefault("enabled", false);
        boolean success = paymentConfigService.togglePaymentEnabled(type, enabled);
        
        if (success) {
            String message = enabled ? "Payment method enabled" : "Payment method disabled";
            return Result.success(message, null);
        } else {
            return Result.error("Failed to toggle payment method");
        }
    }
    
    /**
     * 获取支付方式启用状态
     */
    @GetMapping("/status")
    public Result<Map<String, Boolean>> getPaymentStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("wechat", paymentConfigService.isPaymentEnabled("wechat"));
        status.put("alipay", paymentConfigService.isPaymentEnabled("alipay"));
        status.put("trc20", paymentConfigService.isPaymentEnabled("trc20"));
        status.put("okx", paymentConfigService.isPaymentEnabled("okx"));
        return Result.success(status);
    }
    
    /**
     * 公开接口：获取 TRC20 收款钱包地址（前台用户可见）
     */
    @GetMapping("/trc20/public")
    public Result<Map<String, String>> getTrc20PublicConfig() {
        List<PaymentConfig> configs = paymentConfigService.getConfigsByType("trc20");
        Map<String, String> result = new HashMap<>();
        for (PaymentConfig config : configs) {
            if ("wallet_address".equals(config.getConfigKey())) {
                result.put("wallet_address", config.getConfigValue());
            }
        }
        return Result.success(result);
    }
    
    /**
     * 公开接口：获取 OKX 收款钱包地址（前台用户可见）
     */
    @GetMapping("/okx/public")
    public Result<Map<String, String>> getOkxPublicConfig() {
        List<PaymentConfig> configs = paymentConfigService.getConfigsByType("okx");
        Map<String, String> result = new HashMap<>();
        for (PaymentConfig config : configs) {
            if ("wallet_address".equals(config.getConfigKey())) {
                result.put("wallet_address", config.getConfigValue());
            }
        }
        return Result.success(result);
    }
}

package com.giftcard.service;

import com.giftcard.entity.PaymentConfig;
import java.util.List;
import java.util.Map;

public interface PaymentConfigService {
    
    /**
     * 获取所有支付配置
     */
    List<PaymentConfig> getAllConfigs();
    
    /**
     * 按类型获取配置
     */
    List<PaymentConfig> getConfigsByType(String configType);
    
    /**
     * 获取配置值
     */
    String getConfigValue(String configType, String configKey);
    
    /**
     * 获取微信支付完整配置
     */
    Map<String, String> getWechatConfig();
    
    /**
     * 获取支付宝完整配置
     */
    Map<String, String> getAlipayConfig();
    
    /**
     * 获取TRC20完整配置
     */
    Map<String, String> getTrc20Config();
    
    /**
     * 获取OKX完整配置
     */
    Map<String, String> getOkxConfig();
    
    /**
     * 更新配置
     */
    boolean updateConfig(Long id, String configValue);
    
    /**
     * 批量更新配置
     */
    boolean batchUpdateConfigs(String configType, Map<String, String> configs);
    
    /**
     * 启用/禁用支付方式
     */
    boolean togglePaymentEnabled(String configType, boolean enabled);
    
    /**
     * 检查支付方式是否启用
     */
    boolean isPaymentEnabled(String configType);
    
    /**
     * 加密敏感配置值
     */
    String encryptValue(String value);
    
    /**
     * 解密敏感配置值
     */
    String decryptValue(String encryptedValue);
}

package com.giftcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.giftcard.entity.PaymentConfig;
import com.giftcard.mapper.PaymentConfigMapper;
import com.giftcard.service.PaymentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PaymentConfigServiceImpl implements PaymentConfigService {
    
    private static final String ENCRYPT_KEY = "GiftCardPlatformSecret"; // 实际应从环境变量读取
    
    @Autowired
    private PaymentConfigMapper paymentConfigMapper;
    
    @Override
    public List<PaymentConfig> getAllConfigs() {
        return paymentConfigMapper.selectList(new LambdaQueryWrapper<>());
    }
    
    @Override
    public List<PaymentConfig> getConfigsByType(String configType) {
        return paymentConfigMapper.selectByType(configType);
    }
    
    @Override
    public String getConfigValue(String configType, String configKey) {
        PaymentConfig config = paymentConfigMapper.selectByTypeAndKey(configType, configKey);
        if (config != null) {
            if (config.getIsEncrypted() == 1) {
                return decryptValue(config.getConfigValue());
            }
            return config.getConfigValue();
        }
        return null;
    }
    
    @Override
    @Cacheable(value = "wechatConfig", unless = "#result == null")
    public Map<String, String> getWechatConfig() {
        return getConfigMap("wechat");
    }
    
    @Override
    @Cacheable(value = "alipayConfig", unless = "#result == null")
    public Map<String, String> getAlipayConfig() {
        return getConfigMap("alipay");
    }
    
    @Override
    @Cacheable(value = "trc20Config", unless = "#result == null")
    public Map<String, String> getTrc20Config() {
        return getConfigMap("trc20");
    }
    
    @Override
    @Cacheable(value = "okxConfig", unless = "#result == null")
    public Map<String, String> getOkxConfig() {
        return getConfigMap("okx");
    }
    
    private Map<String, String> getConfigMap(String configType) {
        List<PaymentConfig> configs = paymentConfigMapper.selectByType(configType);
        Map<String, String> result = new HashMap<>();
        for (PaymentConfig config : configs) {
            // 返回所有配置项的值，不管 is_enabled 状态
            // is_enabled 只用于判断整个支付方式是否启用，不影响单个配置项的读取
            String value = config.getConfigValue();
            if (config.getIsEncrypted() == 1 && value != null && !value.isEmpty()) {
                value = decryptValue(value);
            }
            result.put(config.getConfigKey(), value);
        }
        return result;
    }
    
    @Override
    @CacheEvict(value = {"wechatConfig", "alipayConfig", "trc20Config", "okxConfig"}, allEntries = true)
    public boolean updateConfig(Long id, String configValue) {
        PaymentConfig config = paymentConfigMapper.selectById(id);
        if (config == null) {
            return false;
        }
        
        // 如果是加密字段，先加密
        if (config.getIsEncrypted() == 1 && configValue != null && !configValue.isEmpty()) {
            configValue = encryptValue(configValue);
        }
        
        config.setConfigValue(configValue);
        return paymentConfigMapper.updateById(config) > 0;
    }
    
    @Override
    @CacheEvict(value = {"wechatConfig", "alipayConfig", "trc20Config", "okxConfig"}, allEntries = true)
    public boolean batchUpdateConfigs(String configType, Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configKey = entry.getKey();
            String value = entry.getValue();
            
            PaymentConfig config = paymentConfigMapper.selectByTypeAndKey(configType, configKey);
            
            if (config != null) {
                // 更新已有配置
                if (config.getIsEncrypted() == 1 && value != null && !value.isEmpty()) {
                    value = encryptValue(value);
                }
                config.setConfigValue(value);
                paymentConfigMapper.updateById(config);
            } else {
                // 创建新配置
                PaymentConfig newConfig = new PaymentConfig();
                newConfig.setConfigType(configType);
                newConfig.setConfigKey(configKey);
                newConfig.setConfigValue(value);
                newConfig.setIsEncrypted(0); // 默认不加密
                newConfig.setIsEnabled(1); // 默认启用
                newConfig.setDescription(configKey + " for " + configType);
                paymentConfigMapper.insert(newConfig);
            }
        }
        return true;
    }
    
    @Override
    @CacheEvict(value = {"wechatConfig", "alipayConfig", "trc20Config", "okxConfig"}, allEntries = true)
    public boolean togglePaymentEnabled(String configType, boolean enabled) {
        return paymentConfigMapper.updateEnabledByType(configType, enabled ? 1 : 0) > 0;
    }
    
    @Override
    public boolean isPaymentEnabled(String configType) {
        List<PaymentConfig> configs = paymentConfigMapper.selectByType(configType);
        return configs.stream().anyMatch(c -> c.getIsEnabled() == 1);
    }
    
    @Override
    public String encryptValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                Arrays.copyOf(ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8), 16), "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    @Override
    public String decryptValue(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                Arrays.copyOf(ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8), 16), "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}

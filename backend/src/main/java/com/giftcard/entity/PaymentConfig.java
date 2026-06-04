package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("payment_configs")
public class PaymentConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String configType;
    
    private String configKey;
    
    private String configValue;
    
    private Integer isEncrypted;
    
    private String description;
    
    private Integer isEnabled;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public String getConfigType() { return configType; }
    public String getConfigKey() { return configKey; }
    public String getConfigValue() { return configValue; }
    public Integer getIsEncrypted() { return isEncrypted; }
    public String getDescription() { return description; }
    public Integer getIsEnabled() { return isEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setConfigType(String configType) { this.configType = configType; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public void setIsEncrypted(Integer isEncrypted) { this.isEncrypted = isEncrypted; }
    public void setDescription(String description) { this.description = description; }
    public void setIsEnabled(Integer isEnabled) { this.isEnabled = isEnabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

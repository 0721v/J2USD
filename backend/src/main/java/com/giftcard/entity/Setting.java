package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("settings")
public class Setting {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String settingKey;
    private String settingValue;
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

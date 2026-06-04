package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("categories")
public class Category {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String nameZh;
    private String nameEn;
    private String nameJa;
    private String nameKo;
    
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionJa;
    private String descriptionKo;
    
    @TableField(exist = false)
    private String iconUrl;
    
    private Integer sortOrder;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public String getNameZh() { return nameZh; }
    public String getNameEn() { return nameEn; }
    public String getNameJa() { return nameJa; }
    public String getNameKo() { return nameKo; }
    public String getDescriptionZh() { return descriptionZh; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionJa() { return descriptionJa; }
    public String getDescriptionKo() { return descriptionKo; }
    public String getIconUrl() { return iconUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public Integer getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setNameJa(String nameJa) { this.nameJa = nameJa; }
    public void setNameKo(String nameKo) { this.nameKo = nameKo; }
    public void setDescriptionZh(String descriptionZh) { this.descriptionZh = descriptionZh; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public void setDescriptionJa(String descriptionJa) { this.descriptionJa = descriptionJa; }
    public void setDescriptionKo(String descriptionKo) { this.descriptionKo = descriptionKo; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setStatus(Integer status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNameByLang(String lang) {
        return switch (lang) {
            case "en" -> nameEn;
            case "ja" -> nameJa;
            case "ko" -> nameKo;
            default -> nameZh;
        };
    }
    
    public String getDescriptionByLang(String lang) {
        return switch (lang) {
            case "en" -> descriptionEn;
            case "ja" -> descriptionJa;
            case "ko" -> descriptionKo;
            default -> descriptionZh;
        };
    }
}

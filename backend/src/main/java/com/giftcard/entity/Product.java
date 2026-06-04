package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "products", autoResultMap = true)
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("category_id")
    private Long categoryId;
    
    @TableField("name_zh")
    private String nameZh;
    @TableField("name_en")
    private String nameEn;
    @TableField("name_ja")
    private String nameJa;
    @TableField("name_ko")
    private String nameKo;
    
    @TableField("description_zh")
    private String descriptionZh;
    @TableField("description_en")
    private String descriptionEn;
    @TableField("description_ja")
    private String descriptionJa;
    @TableField("description_ko")
    private String descriptionKo;
    
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    
    private Integer stockQuantity;
    private Integer soldQuantity;
    private String imageUrl;
    private Integer sortOrder;
    private Integer status;
    
    @TableField(exist = false)
private Category category;

@TableField(exist = false)
private String name;  // 当前语言的名称

@TableField(exist = false)
private String description;  // 当前语言的描述
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getNameZh() { return nameZh; }
    public String getNameEn() { return nameEn; }
    public String getNameJa() { return nameJa; }
    public String getNameKo() { return nameKo; }
    public String getDescriptionZh() { return descriptionZh; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionJa() { return descriptionJa; }
    public String getDescriptionKo() { return descriptionKo; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public String getCurrency() { return currency; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getSoldQuantity() { return soldQuantity; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public Integer getStatus() { return status; }
    public Category getCategory() { return category; }
    
    public void setName(String name) { this.name = name; }
    
    public String getName() { return name; }
    
    public void setDescription(String description) { this.description = description; }
    
    public String getDescription() { return description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setNameJa(String nameJa) { this.nameJa = nameJa; }
    public void setNameKo(String nameKo) { this.nameKo = nameKo; }
    public void setDescriptionZh(String descriptionZh) { this.descriptionZh = descriptionZh; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public void setDescriptionJa(String descriptionJa) { this.descriptionJa = descriptionJa; }
    public void setDescriptionKo(String descriptionKo) { this.descriptionKo = descriptionKo; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setSoldQuantity(Integer soldQuantity) { this.soldQuantity = soldQuantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setStatus(Integer status) { this.status = status; }
    public void setCategory(Category category) { this.category = category; }
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

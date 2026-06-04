package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("gift_cards")
public class GiftCard {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String cardCode;
    private String cardSecret;
    
    private Integer status;
    private Long orderId;
    private LocalDateTime usedAt;
    private String usedBy;
    private LocalDateTime validUntil;
    
    @TableField(exist = false)
    private Product product;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getCardCode() { return cardCode; }
    public String getCardSecret() { return cardSecret; }
    public Integer getStatus() { return status; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public String getUsedBy() { return usedBy; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public Product getProduct() { return product; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
    public void setCardSecret(String cardSecret) { this.cardSecret = cardSecret; }
    public void setStatus(Integer status) { this.status = status; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public void setUsedBy(String usedBy) { this.usedBy = usedBy; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public void setProduct(Product product) { this.product = product; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

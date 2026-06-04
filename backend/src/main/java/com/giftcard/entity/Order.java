package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String currency;
    
    private String paymentMethod;
    private Integer paymentStatus;
    private LocalDateTime paymentTime;
    private String paymentTrxId;
    private String paymentInfo;
    
    private String customerEmail;
    private String customerPhone;
    private String queryPassword;
    
    private Integer status;
    private String ipAddress;
    private String userAgent;
    
    @TableField(exist = false)
    private Product product;
    
    @TableField(exist = false)
    private List<GiftCard> giftCards;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // Getters
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public Integer getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public String getPaymentTrxId() { return paymentTrxId; }
    public String getPaymentInfo() { return paymentInfo; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getQueryPassword() { return queryPassword; }
    public Integer getStatus() { return status; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Product getProduct() { return product; }
    public List<GiftCard> getGiftCards() { return giftCards; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public void setPaymentTrxId(String paymentTrxId) { this.paymentTrxId = paymentTrxId; }
    public void setPaymentInfo(String paymentInfo) { this.paymentInfo = paymentInfo; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setQueryPassword(String queryPassword) { this.queryPassword = queryPassword; }
    public void setStatus(Integer status) { this.status = status; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setProduct(Product product) { this.product = product; }
    public void setGiftCards(List<GiftCard> giftCards) { this.giftCards = giftCards; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

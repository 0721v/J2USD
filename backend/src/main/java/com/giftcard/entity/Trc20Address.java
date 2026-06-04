package com.giftcard.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("trc20_addresses")
public class Trc20Address {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String address;
    private String privateKeyEncrypted;
    private Integer status;
    private Long orderId;
    private LocalDateTime assignedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // Getters
    public Long getId() { return id; }
    public String getAddress() { return address; }
    public String getPrivateKeyEncrypted() { return privateKeyEncrypted; }
    public Integer getStatus() { return status; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setAddress(String address) { this.address = address; }
    public void setPrivateKeyEncrypted(String privateKeyEncrypted) { this.privateKeyEncrypted = privateKeyEncrypted; }
    public void setStatus(Integer status) { this.status = status; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

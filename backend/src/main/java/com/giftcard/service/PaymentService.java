package com.giftcard.service;

import com.giftcard.entity.Order;
import java.util.Map;

public interface PaymentService {
    
    /**
     * 创建支付
     */
    Map<String, Object> createPayment(Order order, String lang);
    
    /**
     * 查询支付状态
     */
    Map<String, Object> queryPaymentStatus(String orderNo);
    
    /**
     * 处理支付回调
     */
    boolean handleNotify(String paymentMethod, String notifyData);
    
    /**
     * 关闭支付
     */
    boolean closePayment(String orderNo);
}

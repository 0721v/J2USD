package com.giftcard.service;

import com.giftcard.entity.Order;
import java.util.Map;

public interface OrderService {
    
    /**
     * 创建订单
     */
    Map<String, Object> createOrder(Long productId, Integer quantity, String paymentMethod, 
                                    String email, String phone, String queryPassword, String ip, String userAgent, String lang);
    
    /**
     * 获取订单详情
     */
    Order getOrderByNo(String orderNo);
    
    /**
     * 查询订单状态
     */
    Map<String, Object> queryOrderStatus(String orderNo, String lang);
    
    /**
     * 获取订单支付信息（用于继续支付）
     */
    Map<String, Object> getPaymentInfo(String orderNo, String lang);
    
    /**
     * 切换订单支付方式
     */
    Map<String, Object> switchPaymentMethod(String orderNo, String paymentMethod, String lang);
    
    /**
     * 确认支付（用户手动确认）
     */
    Map<String, Object> confirmPayment(String orderNo, String lang);
    
    /**
     * 通过交易哈希确认支付
     */
    Map<String, Object> confirmPaymentByTxId(String orderNo, String txId, String lang);
    
    /**
     * 处理支付成功
     */
    boolean handlePaymentSuccess(String orderNo, String trxId);
    
    /**
     * 取消过期订单
     */
    void cancelExpiredOrders();
    
    /**
     * 使用兑换码
     */
    Map<String, Object> redeemCard(String cardCode, String usedBy, String lang);
    
    /**
     * 通过邮箱和查询密码查询订单卡密
     */
    Map<String, Object> queryOrdersByEmailAndPassword(String email, String queryPassword, String lang, int page, int size);
}

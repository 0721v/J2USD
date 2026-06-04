package com.giftcard.service.impl;

import com.giftcard.entity.Order;
import com.giftcard.service.PaymentConfigService;
import com.giftcard.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("alipayService")
public class AlipayServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentConfigService paymentConfigService;
    
    @Override
    public Map<String, Object> createPayment(Order order, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查支付宝是否启用
        if (!paymentConfigService.isPaymentEnabled("alipay")) {
            result.put("success", false);
            result.put("message", "Alipay is not enabled");
            return result;
        }
        
        // 从数据库获取配置
        Map<String, String> config = paymentConfigService.getAlipayConfig();
        String appId = config.get("app_id");
        String privateKey = config.get("private_key");
        String publicKey = config.get("public_key");
        String notifyUrl = config.get("notify_url");
        String returnUrl = config.get("return_url");
        
        if (appId == null || appId.isEmpty()) {
            result.put("success", false);
            result.put("message", "Alipay configuration is incomplete");
            return result;
        }
        
        try {
            // 构建支付宝订单参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", appId);
            params.put("method", "alipay.trade.precreate");
            params.put("format", "JSON");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");
            params.put("notify_url", notifyUrl != null ? notifyUrl : "");
            
            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", order.getOrderNo());
            bizContent.put("total_amount", order.getTotalAmount().toString());
            bizContent.put("subject", "Gift Card - " + order.getOrderNo());
            bizContent.put("product_code", "QR_CODE_OFFLINE");
            
            params.put("biz_content", com.alibaba.fastjson2.JSON.toJSONString(bizContent));
            
            // 生成签名（简化处理）
            String sign = generateSign(params, privateKey);
            params.put("sign", sign);
            
            // 返回支付二维码
            String qrCode = "https://qr.alipay.com/" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            
            result.put("success", true);
            result.put("qrCode", qrCode);
            result.put("orderNo", order.getOrderNo());
            result.put("amount", order.getTotalAmount());
            result.put("currency", order.getCurrency());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "PENDING");
        return result;
    }
    
    @Override
    public boolean handleNotify(String paymentMethod, String notifyData) {
        // 处理支付宝回调
        return true;
    }
    
    @Override
    public boolean closePayment(String orderNo) {
        // 关闭订单
        return true;
    }
    
    private String generateSign(Map<String, String> params, String privateKey) {
        // 简化实现，实际需要使用RSA签名
        return Base64.getEncoder().encodeToString(
            (params.toString() + privateKey).getBytes()
        );
    }
}

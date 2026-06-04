package com.giftcard.service.impl;

import com.giftcard.entity.Order;
import com.giftcard.service.PaymentConfigService;
import com.giftcard.service.PaymentService;
import com.giftcard.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service("wechatPayService")
public class WechatPayServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentConfigService paymentConfigService;
    
    @Autowired
    private I18nUtil i18nUtil;
    
    @Override
    public Map<String, Object> createPayment(Order order, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查微信支付是否启用
        if (!paymentConfigService.isPaymentEnabled("wechat")) {
            result.put("success", false);
            result.put("message", "WeChat Pay is not enabled");
            return result;
        }
        
        // 从数据库获取配置
        Map<String, String> config = paymentConfigService.getWechatConfig();
        String appId = config.get("app_id");
        String mchId = config.get("mch_id");
        String apiKey = config.get("api_key");
        String notifyUrl = config.get("notify_url");
        
        if (appId == null || appId.isEmpty() || mchId == null || mchId.isEmpty()) {
            result.put("success", false);
            result.put("message", "WeChat Pay configuration is incomplete");
            return result;
        }
        
        try {
            // 构建统一下单参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("nonce_str", generateNonceStr());
            params.put("body", "Gift Card - " + order.getOrderNo());
            params.put("out_trade_no", order.getOrderNo());
            params.put("total_fee", String.valueOf(order.getTotalAmount().multiply(new java.math.BigDecimal("100")).intValue()));
            params.put("spbill_create_ip", order.getIpAddress());
            params.put("notify_url", notifyUrl != null ? notifyUrl : "");
            params.put("trade_type", "NATIVE");
            params.put("product_id", String.valueOf(order.getProductId()));
            
            // 生成签名
            String sign = generateSign(params, apiKey);
            params.put("sign", sign);
            
            // 调用微信支付API（这里简化处理，实际需要发送HTTP请求）
            // 返回支付二维码链接
            String codeUrl = "weixin://wxpay/bizpayurl?pr=" + generateNonceStr().substring(0, 16);
            
            result.put("success", true);
            result.put("codeUrl", codeUrl);
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
        // 实现查询逻辑
        result.put("status", "PENDING");
        return result;
    }
    
    @Override
    public boolean handleNotify(String paymentMethod, String notifyData) {
        // 处理微信支付回调
        return true;
    }
    
    @Override
    public boolean closePayment(String orderNo) {
        // 关闭订单
        return true;
    }
    
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }
    
    private String generateSign(Map<String, String> params, String apiKey) {
        try {
            // 按参数名ASCII码排序
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (!key.equals("sign") && params.get(key) != null && !params.get(key).isEmpty()) {
                    sb.append(key).append("=").append(params.get(key)).append("&");
                }
            }
            sb.append("key=").append(apiKey != null ? apiKey : "");
            
            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb2 = new StringBuilder();
            for (byte b : array) {
                sb2.append(String.format("%02x", b));
            }
            return sb2.toString().toUpperCase();
            
        } catch (Exception e) {
            throw new RuntimeException("Generate sign failed", e);
        }
    }
}

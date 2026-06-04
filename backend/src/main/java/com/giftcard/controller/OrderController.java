package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<Map<String, Object>> create(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "zh") String lang,
            HttpServletRequest request) {
        
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        String paymentMethod = (String) params.get("paymentMethod");
        String email = (String) params.get("email");
        String phone = (String) params.get("phone");
        String queryPassword = (String) params.get("queryPassword");
        
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        Map<String, Object> result = orderService.createOrder(
            productId, quantity, paymentMethod, email, phone, queryPassword, ip, userAgent, lang
        );
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 查询订单状态
     */
    @GetMapping("/{orderNo}")
    public Result<Map<String, Object>> query(
            @PathVariable String orderNo,
            @RequestParam(defaultValue = "zh") String lang) {
        
        Map<String, Object> result = orderService.queryOrderStatus(orderNo, lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 获取订单支付信息（用于继续支付）
     */
    @GetMapping("/{orderNo}/payment-info")
    public Result<Map<String, Object>> getPaymentInfo(
            @PathVariable String orderNo,
            @RequestParam(defaultValue = "zh") String lang) {
        
        Map<String, Object> result = orderService.getPaymentInfo(orderNo, lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 切换订单支付方式
     */
    @PostMapping("/{orderNo}/switch-payment")
    public Result<Map<String, Object>> switchPayment(
            @PathVariable String orderNo,
            @RequestBody Map<String, String> params,
            @RequestParam(defaultValue = "zh") String lang) {
        
        String paymentMethod = params.get("paymentMethod");
        
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return Result.error("支付方式不能为空");
        }
        
        Map<String, Object> result = orderService.switchPaymentMethod(orderNo, paymentMethod, lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 手动确认支付（用户点击"我已支付"后调用）
     */
    @PostMapping("/{orderNo}/confirm-payment")
    public Result<Map<String, Object>> confirmPayment(
            @PathVariable String orderNo,
            @RequestParam(defaultValue = "zh") String lang) {
        
        Map<String, Object> result = orderService.confirmPayment(orderNo, lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 通过交易哈希（TXID）确认支付
     */
    @PostMapping("/{orderNo}/confirm-by-txid")
    public Result<Map<String, Object>> confirmByTxId(
            @PathVariable String orderNo,
            @RequestBody Map<String, String> params,
            @RequestParam(defaultValue = "zh") String lang) {
        
        String txId = params.get("txId");
        
        if (txId == null || txId.trim().isEmpty()) {
            return Result.error("交易哈希不能为空");
        }
        
        Map<String, Object> result = orderService.confirmPaymentByTxId(orderNo, txId.trim(), lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 兑换码兑换
     */
    @PostMapping("/redeem")
    public Result<Map<String, Object>> redeem(
            @RequestBody Map<String, String> params,
            @RequestParam(defaultValue = "zh") String lang,
            HttpServletRequest request) {
        
        String cardCode = params.get("cardCode");
        String usedBy = params.getOrDefault("usedBy", getClientIp(request));
        
        Map<String, Object> result = orderService.redeemCard(cardCode, usedBy, lang);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    /**
     * 通过邮箱和查询密码查询订单卡密
     */
    @PostMapping("/query-by-email")
    public Result<Map<String, Object>> queryByEmail(
            @RequestBody Map<String, String> params,
            @RequestParam(defaultValue = "zh") String lang,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        String email = params.get("email");
        String queryPassword = params.get("queryPassword");
        
        if (email == null || email.isEmpty() || queryPassword == null || queryPassword.isEmpty()) {
            return Result.error("邮箱和查询密码不能为空");
        }
        
        Map<String, Object> result = orderService.queryOrdersByEmailAndPassword(email, queryPassword, lang, page, size);
        
        if (!(Boolean) result.get("success")) {
            return Result.error((String) result.get("message"));
        }
        
        return Result.success(result);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

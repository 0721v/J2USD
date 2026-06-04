package com.giftcard.service.impl;

import com.giftcard.entity.GiftCard;
import com.giftcard.entity.Order;
import com.giftcard.entity.Product;
import com.giftcard.entity.Setting;
import com.giftcard.mapper.GiftCardMapper;
import com.giftcard.mapper.OrderMapper;
import com.giftcard.mapper.ProductMapper;
import com.giftcard.mapper.SettingMapper;
import com.giftcard.service.OrderService;
import com.giftcard.service.PaymentService;
import com.giftcard.util.I18nUtil;
import com.giftcard.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private GiftCardMapper giftCardMapper;

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private OrderNoUtil orderNoUtil;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    @Qualifier("wechatPayService")
    private PaymentService wechatPayService;

    @Autowired
    @Qualifier("alipayService")
    private PaymentService alipayService;

    @Autowired
    @Qualifier("trc20PayService")
    private PaymentService trc20PayService;

    @Autowired
    @Qualifier("okxPayService")
    private PaymentService okxPayService;

    /**
     * 从数据库获取订单过期时间（分钟），默认30分钟
     */
    private int getOrderExpireMinutes() {
        Setting setting = settingMapper.selectByKey("order_expire_minutes");
        if (setting != null && setting.getSettingValue() != null) {
            try {
                return Integer.parseInt(setting.getSettingValue());
            } catch (NumberFormatException e) {
                System.err.println("[OrderService] 订单过期时间配置错误，使用默认值30分钟");
            }
        }
        return 30; // 默认30分钟
    }
    
    @Override
    @Transactional
    public Map<String, Object> createOrder(Long productId, Integer quantity, String paymentMethod,
                                           String email, String phone, String queryPassword, String ip, String userAgent, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查商品
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "product.not_found"));
            return result;
        }
        
        // 检查库存
        Integer availableStock = giftCardMapper.countAvailableCards(productId);
        if (availableStock < quantity) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "product.insufficient_stock"));
            return result;
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNoUtil.generateOrderNo());
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(quantity)));
        order.setCurrency(product.getCurrency());
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(0);
        order.setCustomerEmail(email);
        order.setCustomerPhone(phone);
        order.setQueryPassword(queryPassword);
        order.setStatus(0);
        order.setIpAddress(ip);
        order.setUserAgent(userAgent);
        
        orderMapper.insert(order);
        
        // 创建支付
        PaymentService paymentService = getPaymentService(paymentMethod);
        Map<String, Object> paymentResult = paymentService.createPayment(order, lang);
        
        if (!(Boolean) paymentResult.get("success")) {
            String errorMsg = paymentResult.containsKey("message") ? (String) paymentResult.get("message") : "Failed to create payment";
            throw new RuntimeException(errorMsg);
        }
        
        result.put("success", true);
        result.put("orderNo", order.getOrderNo());
        result.put("paymentMethod", order.getPaymentMethod());
        result.put("amount", order.getTotalAmount());
        result.put("currency", order.getCurrency());
        result.put("expireMinutes", getOrderExpireMinutes());
        result.putAll(paymentResult);
        
        return result;
    }
    
    @Override
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }
    
    @Override
    public Map<String, Object> queryOrderStatus(String orderNo, String lang) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderMapper.selectByOrderNo(orderNo);
        
        if (order == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "order.not_found"));
            return result;
        }
        
        result.put("success", true);
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("paymentStatus", order.getPaymentStatus());
        result.put("createdAt", order.getCreatedAt());
        result.put("paymentMethod", order.getPaymentMethod());
        result.put("totalAmount", order.getTotalAmount());
        result.put("currency", order.getCurrency());
        result.put("productId", order.getProductId());
        
        // 如果已支付，返回兑换码
        if (order.getStatus() >= 2) {
            List<GiftCard> cards = giftCardMapper.selectByOrderId(order.getId());
            result.put("cards", cards);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getPaymentInfo(String orderNo, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderMapper.selectByOrderNo(orderNo);
        
        if (order == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "order.not_found"));
            return result;
        }
        
        if (order.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "订单已支付或已取消");
            return result;
        }
        
        // 获取支付服务并创建支付信息
        PaymentService paymentService = getPaymentService(order.getPaymentMethod());
        Map<String, Object> paymentResult = paymentService.createPayment(order, lang);
        
        if (!(Boolean) paymentResult.get("success")) {
            result.put("success", false);
            result.put("message", paymentResult.get("message"));
            return result;
        }
        
        result.put("success", true);
        result.put("address", paymentResult.get("address"));
        result.put("amount", paymentResult.get("amount"));
        result.put("currency", paymentResult.get("currency"));
        result.put("network", paymentResult.get("network"));
        result.put("contract", paymentResult.get("contract"));
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> confirmPaymentByTxId(String orderNo, String txId, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderMapper.selectByOrderNo(orderNo);
        
        if (order == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "order.not_found"));
            return result;
        }
        
        if (order.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "订单已支付或已取消");
            return result;
        }

        // 检查是否已匹配过（避免重复确认）
        if (order.getPaymentTrxId() != null && !order.getPaymentTrxId().isEmpty()) {
            result.put("success", true);
            result.put("message", "订单已确认支付");
            return result;
        }

        // 对于 TRC20 支付，通过 TXID 查询区块链验证交易
        if ("trc20".equals(order.getPaymentMethod())) {
            Trc20PayServiceImpl trc20Service = (Trc20PayServiceImpl) getPaymentService("trc20");
            
            // 获取配置的收款地址
            Map<String, Object> verifyResult = trc20Service.verifyPaymentByTxId(
                txId, null, order.getTotalAmount());
            
            if ((Boolean) verifyResult.get("success") && (Boolean) verifyResult.get("paid")) {
                // 区块链确认已支付，更新订单状态
                boolean success = handlePaymentSuccess(orderNo, txId);
                
                if (success) {
                    result.put("success", true);
                    result.put("message", "支付确认成功");
                    result.put("orderNo", orderNo);
                    result.put("status", 2);
                    result.put("trxId", txId);
                    result.put("amount", verifyResult.get("amount"));
                } else {
                    result.put("success", false);
                    result.put("message", "支付确认失败，请稍后重试");
                }
            } else {
                result.put("success", false);
                result.put("message", verifyResult.get("message"));
            }
        } else if ("okx".equals(order.getPaymentMethod())) {
            // OKX 支付，通过 TXID 查询 OKX API 验证交易，增加时间校验
            OkxPayServiceImpl okxService = (OkxPayServiceImpl) getPaymentService("okx");
            // 计算订单创建时间（毫秒）
            Long orderCreatedTime = order.getCreatedAt() != null
                    ? order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : null;

            Map<String, Object> verifyResult = okxService.verifyPaymentByTxId(
                txId, null, order.getTotalAmount(), orderCreatedTime);
            
            if ((Boolean) verifyResult.get("success") && (Boolean) verifyResult.get("paid")) {
                boolean success = handlePaymentSuccess(orderNo, txId);
                
                if (success) {
                    result.put("success", true);
                    result.put("message", "支付确认成功");
                    result.put("orderNo", orderNo);
                    result.put("status", 2);
                    result.put("trxId", txId);
                    result.put("amount", verifyResult.get("amount"));
                } else {
                    result.put("success", false);
                    result.put("message", "支付确认失败，请稍后重试");
                }
            } else {
                result.put("success", false);
                result.put("message", verifyResult.get("message"));
            }
        } else {
            result.put("success", false);
            result.put("message", "该支付方式暂不支持交易哈希确认");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> switchPaymentMethod(String orderNo, String paymentMethod, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderMapper.selectByOrderNo(orderNo);
        
        if (order == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "order.not_found"));
            return result;
        }
        
        if (order.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "订单已支付或已取消，无法切换支付方式");
            return result;
        }
        
        // 关闭原支付
        PaymentService oldPaymentService = getPaymentService(order.getPaymentMethod());
        oldPaymentService.closePayment(orderNo);
        
        // 更新订单支付方式
        order.setPaymentMethod(paymentMethod);
        orderMapper.updateById(order);
        
        // 获取新支付方式的支付信息
        PaymentService newPaymentService = getPaymentService(paymentMethod);
        Map<String, Object> paymentResult = newPaymentService.createPayment(order, lang);
        
        if (!(Boolean) paymentResult.get("success")) {
            result.put("success", false);
            result.put("message", paymentResult.get("message"));
            return result;
        }
        
        // 构造返回数据
        result.put("success", true);
        result.put("orderNo", orderNo);
        result.put("amount", order.getTotalAmount());
        result.put("totalAmount", order.getTotalAmount());
        result.put("currency", order.getCurrency());
        result.put("productId", order.getProductId());
        result.put("paymentMethod", paymentMethod);
        result.put("paymentData", paymentResult);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> confirmPayment(String orderNo, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderMapper.selectByOrderNo(orderNo);
        
        if (order == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "order.not_found"));
            return result;
        }
        
        if (order.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "订单已支付或已取消");
            return result;
        }
        
        // 对于 TRC20 支付，查询区块链验证交易
        if ("trc20".equals(order.getPaymentMethod())) {
            // 获取 TRC20 支付服务
            Trc20PayServiceImpl trc20Service = (Trc20PayServiceImpl) getPaymentService("trc20");
            
            // 查询区块链验证支付
            Map<String, Object> verifyResult = trc20Service.verifyPayment(orderNo, order.getTotalAmount());
            
            if ((Boolean) verifyResult.get("success") && (Boolean) verifyResult.get("paid")) {
                // 区块链确认已支付，更新订单状态
                String trxId = (String) verifyResult.get("trxId");
                boolean success = handlePaymentSuccess(orderNo, trxId);
                
                if (success) {
                    result.put("success", true);
                    result.put("message", "支付确认成功");
                    result.put("orderNo", orderNo);
                    result.put("status", 2); // 已完成
                    result.put("trxId", trxId);
                    result.put("amount", verifyResult.get("amount"));
                } else {
                    result.put("success", false);
                    result.put("message", "支付确认失败，请稍后重试");
                }
            } else {
                // 区块链未查询到支付
                result.put("success", false);
                result.put("message", "未查询到支付记录，请确认转账已完成");
                result.put("paid", false);
            }
        } else if ("okx".equals(order.getPaymentMethod())) {
            // OKX 支付，通过 OKX API 查询充值记录验证
            OkxPayServiceImpl okxService = (OkxPayServiceImpl) getPaymentService("okx");
            
            Map<String, Object> verifyResult = okxService.verifyPayment(orderNo, order.getTotalAmount());
            
            if ((Boolean) verifyResult.get("success") && (Boolean) verifyResult.get("paid")) {
                String txId = (String) verifyResult.get("txId");
                boolean success = handlePaymentSuccess(orderNo, txId);
                
                if (success) {
                    result.put("success", true);
                    result.put("message", "支付确认成功");
                    result.put("orderNo", orderNo);
                    result.put("status", 2);
                    result.put("trxId", txId);
                    result.put("amount", verifyResult.get("amount"));
                } else {
                    result.put("success", false);
                    result.put("message", "支付确认失败，请稍后重试");
                }
            } else {
                result.put("success", false);
                result.put("message", "未查询到支付记录，请确认转账已完成");
                result.put("paid", false);
            }
        } else {
            // 其他支付方式（微信/支付宝）的确认逻辑
            result.put("success", false);
            result.put("message", "该支付方式暂不支持自动确认");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean handlePaymentSuccess(String orderNo, String trxId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        
        // 支付成功，锁定并发放兑换码
        List<GiftCard> availableCards = giftCardMapper.selectAvailableCards(order.getProductId(), order.getQuantity());
        if (availableCards == null || availableCards.size() < order.getQuantity()) {
            // 库存不足，不允许支付成功
            System.err.println("[OrderService] 支付成功但库存不足，订单: " + orderNo + ", 期望: " + order.getQuantity() + ", 实际: " + (availableCards == null ? 0 : availableCards.size()));
            return false;
        }
        for (GiftCard card : availableCards) {
            giftCardMapper.lockCard(card.getId(), order.getId());
        }
        
        // 更新订单状态
        orderMapper.updatePaymentStatus(
            order.getId(), 
            3, // 已完成
            1, // 已支付
            LocalDateTime.now(),
            trxId
        );
        
        // 扣减库存
        productMapper.deductStock(order.getProductId(), order.getQuantity());
        
        return true;
    }
    
    @Override
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(getOrderExpireMinutes());
        List<Order> expiredOrders = orderMapper.selectExpiredOrders(expireTime);
        
        for (Order order : expiredOrders) {
            // 关闭支付
            PaymentService paymentService = getPaymentService(order.getPaymentMethod());
            paymentService.closePayment(order.getOrderNo());
            
            // 更新订单状态为已取消
            order.setStatus(4);
            orderMapper.updateById(order);
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> redeemCard(String cardCode, String usedBy, String lang) {
        Map<String, Object> result = new HashMap<>();
        
        GiftCard card = giftCardMapper.selectByCardCode(cardCode);
        if (card == null) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "card.invalid"));
            return result;
        }
        
        if (card.getStatus() != 1) {
            result.put("success", false);
            result.put("message", i18nUtil.getMessage(lang, "card.used"));
            return result;
        }
        
        // 标记为已使用
        giftCardMapper.markAsUsed(cardCode, usedBy);
        
        result.put("success", true);
        result.put("message", i18nUtil.getMessage(lang, "card.success"));
        result.put("cardCode", cardCode);
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryOrdersByEmailAndPassword(String email, String queryPassword, String lang, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        // 查询总数
        int total = orderMapper.countByEmailAndPassword(email, queryPassword);

        if (total == 0) {
            result.put("success", false);
            result.put("message", "未找到订单，请检查邮箱和查询密码");
            return result;
        }

        int totalPages = (int) Math.ceil((double) total / size);
        int offset = (page - 1) * size;

        // 分页查询订单
        List<Order> orders = orderMapper.selectByEmailAndPasswordPage(email, queryPassword, size, offset);

        // 检查并处理过期未支付订单
        LocalDateTime now = LocalDateTime.now();
        int expireMinutes = getOrderExpireMinutes();
        for (Order order : orders) {
            // 状态为0（待支付）且已过期
            if (order.getStatus() != null && order.getStatus() == 0
                    && order.getCreatedAt() != null
                    && order.getCreatedAt().plusMinutes(expireMinutes).isBefore(now)) {
                System.out.println("[OrderService] 订单 " + order.getOrderNo() + " 已过期，标记为取消");
                order.setStatus(4); // 已取消
                orderMapper.updateById(order);
            }

            // 获取每个订单的兑换码（仅已完成的订单）
            if (order.getStatus() != null && order.getStatus() == 3) {
                List<GiftCard> cards = giftCardMapper.selectByOrderId(order.getId());
                order.setGiftCards(cards);
            }
        }

        result.put("success", true);
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", totalPages);

        return result;
    }
    
    private PaymentService getPaymentService(String paymentMethod) {
        return switch (paymentMethod) {
            case "wechat" -> wechatPayService;
            case "alipay" -> alipayService;
            case "trc20" -> trc20PayService;
            case "okx" -> okxPayService;
            default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        };
    }
}

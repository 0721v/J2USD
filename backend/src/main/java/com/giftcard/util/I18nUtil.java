package com.giftcard.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class I18nUtil {
    
    private static final Map<String, Map<String, String>> MESSAGES = new HashMap<>();
    
    static {
        // Chinese
        Map<String, String> zh = new HashMap<>();
        zh.put("order.created", "订单创建成功");
        zh.put("order.not_found", "订单不存在");
        zh.put("order.expired", "订单已过期");
        zh.put("order.paid", "订单已支付");
        zh.put("product.not_found", "商品不存在");
        zh.put("product.sold_out", "商品已售罄");
        zh.put("product.insufficient_stock", "库存不足");
        zh.put("payment.pending", "等待支付");
        zh.put("payment.success", "支付成功");
        zh.put("payment.failed", "支付失败");
        zh.put("card.invalid", "无效的兑换码");
        zh.put("card.used", "兑换码已使用");
        zh.put("card.success", "兑换成功");
        zh.put("auth.failed", "认证失败");
        zh.put("auth.unauthorized", "未授权访问");
        zh.put("system.error", "系统错误");
        MESSAGES.put("zh", zh);
        
        // English
        Map<String, String> en = new HashMap<>();
        en.put("order.created", "Order created successfully");
        en.put("order.not_found", "Order not found");
        en.put("order.expired", "Order has expired");
        en.put("order.paid", "Order already paid");
        en.put("product.not_found", "Product not found");
        en.put("product.sold_out", "Product sold out");
        en.put("product.insufficient_stock", "Insufficient stock");
        en.put("payment.pending", "Payment pending");
        en.put("payment.success", "Payment successful");
        en.put("payment.failed", "Payment failed");
        en.put("card.invalid", "Invalid card code");
        en.put("card.used", "Card already used");
        en.put("card.success", "Card redeemed successfully");
        en.put("auth.failed", "Authentication failed");
        en.put("auth.unauthorized", "Unauthorized access");
        en.put("system.error", "System error");
        MESSAGES.put("en", en);
        
        // Japanese
        Map<String, String> ja = new HashMap<>();
        ja.put("order.created", "注文が作成されました");
        ja.put("order.not_found", "注文が見つかりません");
        ja.put("order.expired", "注文の有効期限が切れました");
        ja.put("order.paid", "注文は既に支払い済みです");
        ja.put("product.not_found", "商品が見つかりません");
        ja.put("product.sold_out", "商品は売り切れです");
        ja.put("product.insufficient_stock", "在庫が不足しています");
        ja.put("payment.pending", "支払い待ち");
        ja.put("payment.success", "支払いが完了しました");
        ja.put("payment.failed", "支払いに失敗しました");
        ja.put("card.invalid", "無効なギフトコードです");
        ja.put("card.used", "ギフトコードは既に使用されています");
        ja.put("card.success", "ギフトコードを使用しました");
        ja.put("auth.failed", "認証に失敗しました");
        ja.put("auth.unauthorized", "未承認のアクセスです");
        ja.put("system.error", "システムエラー");
        MESSAGES.put("ja", ja);
        
        // Korean
        Map<String, String> ko = new HashMap<>();
        ko.put("order.created", "주문이 생성되었습니다");
        ko.put("order.not_found", "주문을 찾을 수 없습니다");
        ko.put("order.expired", "주문이 만료되었습니다");
        ko.put("order.paid", "이미 결제된 주문입니다");
        ko.put("product.not_found", "상품을 찾을 수 없습니다");
        ko.put("product.sold_out", "상품이 품절되었습니다");
        ko.put("product.insufficient_stock", "재고가 부족합니다");
        ko.put("payment.pending", "결제 대기 중");
        ko.put("payment.success", "결제가 완료되었습니다");
        ko.put("payment.failed", "결제에 실패했습니다");
        ko.put("card.invalid", "유효하지 않은 기프트 코드입니다");
        ko.put("card.used", "이미 사용된 기프트 코드입니다");
        ko.put("card.success", "기프트 코드 사용이 완료되었습니다");
        ko.put("auth.failed", "인증에 실패했습니다");
        ko.put("auth.unauthorized", "인증되지 않은 접근입니다");
        ko.put("system.error", "시스템 오류");
        MESSAGES.put("ko", ko);
    }
    
    public String getMessage(String lang, String key) {
        Map<String, String> messages = MESSAGES.getOrDefault(lang, MESSAGES.get("zh"));
        return messages.getOrDefault(key, key);
    }
    
    public String getMessage(String lang, String key, Object... args) {
        String message = getMessage(lang, key);
        if (args != null && args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }
}

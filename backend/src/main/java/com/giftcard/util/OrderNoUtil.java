package com.giftcard.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class OrderNoUtil {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    
    public String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = String.format("%04d", RANDOM.nextInt(10000));
        return "GC" + timestamp + random;
    }
    
    public String generateCardCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        // Format: XXXX-XXXX-XXXX-XXXX
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append("-");
            }
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
}

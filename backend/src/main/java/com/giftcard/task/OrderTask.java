package com.giftcard.task;

import com.giftcard.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单定时任务
 * - 每小时检查过期订单并自动取消
 * - 释放锁定的兑换码回可用状态
 */
@Component
public class OrderTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTask.class);

    @Autowired
    private OrderService orderService;

    /**
     * 每小时执行一次，取消过期订单并释放库存
     * cron: 0 0 * * * * = 每小时的第0分第0秒
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cancelExpiredOrders() {
        logger.info("=== 开始执行过期订单取消任务 ===");
        try {
            orderService.cancelExpiredOrders();
            logger.info("=== 过期订单取消任务执行完成 ===");
        } catch (Exception e) {
            logger.error("过期订单取消任务执行失败", e);
        }
    }
}

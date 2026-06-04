package com.giftcard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration implements CommandLineRunner {
    
    // @Autowired
    // private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) {
        // try {
        //     // 检查 query_password 字段是否存在
        //     Integer count = jdbcTemplate.queryForObject(
        //         "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'query_password'",
        //         Integer.class
        //     );
        //     if (count != null && count > 0) {
        //         System.out.println("query_password 字段已存在");
        //         return;
        //     }
        // } catch (Exception e) {
        //     System.out.println("检查字段是否存在时出错: " + e.getMessage());
        // }
        
        // // 字段不存在，添加它
        // try {
        //     jdbcTemplate.execute(
        //         "ALTER TABLE orders ADD COLUMN query_password VARCHAR(255) NULL COMMENT '查询密码' AFTER customer_phone"
        //     );
        //     System.out.println("成功添加 query_password 字段到 orders 表");
        // } catch (Exception ex) {
        //     System.err.println("添加 query_password 字段失败: " + ex.getMessage());
        // }
    }
}

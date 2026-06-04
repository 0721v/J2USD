-- 添加 query_password 字段到 orders 表
ALTER TABLE orders ADD COLUMN query_password VARCHAR(255) NULL COMMENT '查询密码' AFTER customer_phone;

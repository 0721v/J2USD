/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50729
Source Host           : 127.0.0.1:3306
Source Database       : 1111

Target Server Type    : MYSQL
Target Server Version : 50729
File Encoding         : 65001

Date: 2026-06-11 10:21:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `admins`
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'admin' COMMENT '角色: admin-管理员, super-超级管理员',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES ('1', 'admin', 'admin123', 'admin@example.com', 'super', '1', '2026-06-07 10:33:33', '36.143.3.196', '2026-05-28 17:56:35', '2026-06-07 10:33:32');

-- ----------------------------
-- Table structure for `categories`
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name_zh` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文名称',
  `name_en` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '英文名称',
  `name_ja` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日文名称',
  `name_ko` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '韩文名称',
  `description_zh` text COLLATE utf8mb4_unicode_ci COMMENT '中文描述',
  `description_en` text COLLATE utf8mb4_unicode_ci COMMENT '英文描述',
  `description_ja` text COLLATE utf8mb4_unicode_ci COMMENT '日文描述',
  `description_ko` text COLLATE utf8mb4_unicode_ci COMMENT '韩文描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES ('6', 'USD', 'USD', 'USD', 'USD', '默认', '', '', '', '0', '1', null, null);
INSERT INTO `categories` VALUES ('7', 'CNY', 'CNY', 'CNY', 'CNY', '', '', '', '', '1', '1', null, null);

-- ----------------------------
-- Table structure for `gift_cards`
-- ----------------------------
DROP TABLE IF EXISTS `gift_cards`;
CREATE TABLE `gift_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `card_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换码',
  `card_secret` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '卡密（可选，用于双重验证）',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-未使用 1-已售出 2-已使用 3-已过期',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `used_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用者标识',
  `valid_until` timestamp NULL DEFAULT NULL COMMENT '有效期至',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_code` (`card_code`),
  KEY `idx_product` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `gift_cards_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码表';

-- ----------------------------
-- Records of gift_cards
-- ----------------------------

-- ----------------------------
-- Table structure for `orders`
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `quantity` int(11) DEFAULT '1' COMMENT '购买数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'CNY' COMMENT '货币',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式: wechat, alipay, trc20',
  `payment_status` tinyint(4) DEFAULT '0' COMMENT '支付状态: 0-未支付 1-已支付 2-支付失败 3-已取消 4-已退款',
  `payment_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `payment_trx_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方支付流水号',
  `payment_info` json DEFAULT NULL COMMENT '支付相关信息（如TRC20地址、支付二维码等）',
  `customer_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户邮箱',
  `customer_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户手机',
  `query_password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '查询密码',
  `status` tinyint(4) DEFAULT '0' COMMENT '订单状态: 0-待支付 1-已支付 2-处理中 3-已完成 4-已取消',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单IP',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '用户代理',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_product` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ----------------------------
-- Records of orders
-- ----------------------------

-- ----------------------------
-- Table structure for `order_gift_cards`
-- ----------------------------
DROP TABLE IF EXISTS `order_gift_cards`;
CREATE TABLE `order_gift_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `gift_card_id` bigint(20) NOT NULL COMMENT '兑换码ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_card` (`order_id`,`gift_card_id`),
  KEY `gift_card_id` (`gift_card_id`),
  CONSTRAINT `order_gift_cards_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `order_gift_cards_ibfk_2` FOREIGN KEY (`gift_card_id`) REFERENCES `gift_cards` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单兑换码关联表';

-- ----------------------------
-- Records of order_gift_cards
-- ----------------------------

-- ----------------------------
-- Table structure for `payment_configs`
-- ----------------------------
DROP TABLE IF EXISTS `payment_configs`;
CREATE TABLE `payment_configs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型: wechat, alipay, trc20',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键名',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `is_encrypted` tinyint(4) DEFAULT '0' COMMENT '是否加密: 0-否 1-是',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置说明',
  `is_enabled` tinyint(4) DEFAULT '1' COMMENT '是否启用: 0-禁用 1-启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_key` (`config_type`,`config_key`),
  KEY `idx_type` (`config_type`),
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付配置表';

-- ----------------------------
-- Records of payment_configs
-- ----------------------------
INSERT INTO `payment_configs` VALUES ('1', 'wechat', 'app_id', 'test123wx123456789wx123456789', '0', '微信支付AppID', '0', '2026-05-28 17:57:00', '2026-05-29 17:57:12');
INSERT INTO `payment_configs` VALUES ('2', 'wechat', 'mch_id', '123456', '0', '微信支付商户号', '0', '2026-05-28 17:57:00', '2026-05-29 17:57:12');
INSERT INTO `payment_configs` VALUES ('3', 'wechat', 'api_key', '', '0', '微信支付API密钥', '0', '2026-05-28 17:57:00', '2026-05-30 19:42:29');
INSERT INTO `payment_configs` VALUES ('4', 'wechat', 'api_v3_key', '', '0', '微信支付APIv3密钥', '0', '2026-05-28 17:57:00', '2026-05-30 19:42:31');
INSERT INTO `payment_configs` VALUES ('5', 'wechat', 'serial_no', '', '0', '商户证书序列号', '0', '2026-05-28 17:57:00', '2026-05-29 17:57:12');
INSERT INTO `payment_configs` VALUES ('6', 'wechat', 'private_key', '', '0', '商户私钥', '0', '2026-05-28 17:57:00', '2026-05-30 19:42:27');
INSERT INTO `payment_configs` VALUES ('7', 'wechat', 'notify_url', '', '0', '微信支付回调地址', '0', '2026-05-28 17:57:00', '2026-05-29 17:57:12');
INSERT INTO `payment_configs` VALUES ('8', 'alipay', 'app_id', '', '0', '支付宝AppID', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('9', 'alipay', 'private_key', '', '0', '支付宝应用私钥', '0', '2026-05-28 17:57:00', '2026-05-30 19:42:33');
INSERT INTO `payment_configs` VALUES ('10', 'alipay', 'public_key', '', '0', '支付宝公钥', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('11', 'alipay', 'alipay_public_key', '', '0', '支付宝支付宝公钥', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('12', 'alipay', 'gateway_url', 'https://openapi.alipay.com/gateway.do', '0', '支付宝网关地址', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('13', 'alipay', 'notify_url', '', '0', '支付宝回调地址', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('14', 'alipay', 'return_url', '', '0', '支付宝同步返回地址', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('15', 'alipay', 'sign_type', 'RSA2', '0', '签名类型', '0', '2026-05-28 17:57:00', '2026-05-29 12:02:49');
INSERT INTO `payment_configs` VALUES ('16', 'trc20', 'usdt_contract', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', '0', 'USDT合约地址', '0', '2026-05-28 17:57:00', '2026-05-30 18:19:57');
INSERT INTO `payment_configs` VALUES ('17', 'trc20', 'api_key', '6lBh5wM5qqsIjTg0PqmmJmHlIE/60ZLOPVXua4X3g/19tiFOQjSOvW2WIRFrdUPG3/AK5h8v4NyS52Ge9Tva4Uij0v2CBurIMFQzju9G4yMkE3igozBfxx0otswdh3koEECjSZ8NkNsY6e/vXLNd4TC9iBFH1lJkD7lDyeHcv1WiE+3H1L2B6rHp8DkNCo6CRDYNi6oOxrfg07/W3eIjQYJlc3a4otEhX1/ThuM91DdSy+7X7ch3Hg5jEatD3YzFj7nLPrKZUKZ82Ot4R3YDPvlHf1A2AWw2b45gILFKw7uk//Zk5zTt/GAuGlCE1hgnMlNV5didhZn+sqz+lDqXqfTFz2htcElCiqU74T7FaYMGgZZpC4qEyXJfScDOqrloITRjYYZCroOqyzHXmFkRKA==', '0', 'TRON API密钥', '0', '2026-05-28 17:57:00', '2026-05-30 19:42:35');
INSERT INTO `payment_configs` VALUES ('18', 'trc20', 'api_url', 'https://api.trongrid.io', '0', 'TRON API地址', '0', '2026-05-28 17:57:00', '2026-05-30 18:19:57');
INSERT INTO `payment_configs` VALUES ('19', 'trc20', 'confirm_blocks', '19', '0', '确认区块数', '0', '2026-05-28 17:57:00', '2026-05-30 18:19:57');
INSERT INTO `payment_configs` VALUES ('20', 'trc20', 'exchange_rate', '6.8', '0', 'USDT/CNY汇率', '0', '2026-05-28 17:57:00', '2026-05-30 18:19:57');
INSERT INTO `payment_configs` VALUES ('21', 'trc20', 'wallet_address', 'TPPvPcb11G3BS5wRkxAjRQ2BbNCg3TUZ2H', '0', 'wallet_address for trc20', '0', '2026-05-28 17:57:00', '2026-05-30 19:24:58');
INSERT INTO `payment_configs` VALUES ('22', 'okx', 'api_key', '', '0', 'OKX API Key', '1', '2026-05-30 18:04:44', '2026-06-10 10:01:29');
INSERT INTO `payment_configs` VALUES ('23', 'okx', 'secret_key', '', '0', 'OKX Secret Key', '1', '2026-05-30 18:04:44', '2026-06-10 10:01:30');
INSERT INTO `payment_configs` VALUES ('24', 'okx', 'passphrase', '', '0', 'OKX Passphrase', '1', '2026-05-30 18:04:44', '2026-06-10 10:01:31');
INSERT INTO `payment_configs` VALUES ('25', 'okx', 'wallet_address', '', '0', 'OKX 收款地址', '1', '2026-05-30 18:04:44', '2026-06-10 10:01:33');
INSERT INTO `payment_configs` VALUES ('26', 'okx', 'exchange_rate', '7', '0', 'CNY 转 USDT 汇率', '1', '2026-05-30 18:04:45', '2026-05-30 18:04:45');

-- ----------------------------
-- Table structure for `payment_records`
-- ----------------------------
DROP TABLE IF EXISTS `payment_records`;
CREATE TABLE `payment_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '货币',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待支付 1-支付成功 2-支付失败 3-已退款',
  `trx_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易ID',
  `trx_data` json DEFAULT NULL COMMENT '第三方返回的完整数据',
  `paid_at` timestamp NULL DEFAULT NULL COMMENT '支付完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_trx_id` (`trx_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `payment_records_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ----------------------------
-- Records of payment_records
-- ----------------------------

-- ----------------------------
-- Table structure for `products`
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `name_zh` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文名称',
  `name_en` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '英文名称',
  `name_ja` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日文名称',
  `name_ko` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '韩文名称',
  `description_zh` text COLLATE utf8mb4_unicode_ci COMMENT '中文描述',
  `description_en` text COLLATE utf8mb4_unicode_ci COMMENT '英文描述',
  `description_ja` text COLLATE utf8mb4_unicode_ci COMMENT '日文描述',
  `description_ko` text COLLATE utf8mb4_unicode_ci COMMENT '韩文描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'CNY' COMMENT '货币: CNY, USD',
  `stock_quantity` int(11) DEFAULT '0' COMMENT '库存数量',
  `sold_quantity` int(11) DEFAULT '0' COMMENT '已售数量',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 0-下架 1-上架 2-售罄',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_sort` (`sort_order`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES ('1', '6', '测试套餐', 'Test Package', 'テストパッケージ', '테스트 패키지', '', null, null, null, '10.00', null, 'USD', '0', '0', '', '0', '1', null, '2026-06-11 10:20:49');
INSERT INTO `products` VALUES ('2', '6', '初级套餐', 'Basic Package', 'スターターパッケージ', '스타터 패키지', '', null, null, null, '20.00', null, 'USD', '0', '0', '', '0', '1', null, '2026-06-11 10:20:47');
INSERT INTO `products` VALUES ('3', '6', '中级套餐', 'Intermediate Plan', '中間プラン', '중간 계획', '', null, null, null, '50.00', null, 'USD', '0', '0', '', '0', '1', null, '2026-06-11 10:20:47');
INSERT INTO `products` VALUES ('4', '6', '高级套餐', 'Pro Package', 'プレミアムプラン', '프리미엄 요금제', '', null, null, null, '100.00', null, 'USD', '0', '0', '', '0', '1', null, '2026-06-11 10:20:46');
INSERT INTO `products` VALUES ('5', '6', '0.01U', '0.01 U', '0.01 U', '0.01 U', '', null, null, null, '0.01', null, 'USD', '0', '0', '', '0', '1', '2026-06-07 11:09:00', '2026-06-11 10:20:55');

-- ----------------------------
-- Table structure for `settings`
-- ----------------------------
DROP TABLE IF EXISTS `settings`;
CREATE TABLE `settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `setting_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ----------------------------
-- Records of settings
-- ----------------------------
INSERT INTO `settings` VALUES ('1', 'site_name_zh', '兰雀Pro', '网站名称-中文', '2026-05-28 17:56:50', '2026-06-04 10:13:05');
INSERT INTO `settings` VALUES ('2', 'site_name_en', '兰雀Pro', '网站名称-英文', '2026-05-28 17:56:50', '2026-06-04 10:13:05');
INSERT INTO `settings` VALUES ('3', 'site_name_ja', '兰雀Pro', '网站名称-日文', '2026-05-28 17:56:50', '2026-06-04 10:13:06');
INSERT INTO `settings` VALUES ('4', 'site_name_ko', '兰雀Pro', '网站名称-韩文', '2026-05-28 17:56:50', '2026-06-04 10:13:07');
INSERT INTO `settings` VALUES ('5', 'order_expire_minutes', '30', '订单过期时间(分钟)', '2026-05-28 17:56:50', '2026-05-28 17:56:50');
INSERT INTO `settings` VALUES ('6', 'default_language', 'zh', '默认语言', '2026-05-28 17:56:50', '2026-05-28 17:56:50');
INSERT INTO `settings` VALUES ('7', 'default_lang', 'zh', null, '2026-05-29 11:55:05', '2026-06-04 10:13:08');
INSERT INTO `settings` VALUES ('8', 'order_expire', '30', null, '2026-05-29 11:55:06', '2026-06-04 10:13:09');
INSERT INTO `settings` VALUES ('9', 'site_logo', '', null, '2026-05-29 12:15:03', '2026-06-04 10:13:07');

-- ----------------------------
-- Table structure for `trc20_addresses`
-- ----------------------------
DROP TABLE IF EXISTS `trc20_addresses`;
CREATE TABLE `trc20_addresses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'TRC20地址',
  `private_key_encrypted` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密后的私钥',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-未使用 1-已分配 2-已禁用',
  `order_id` bigint(20) DEFAULT NULL COMMENT '当前分配给的订单ID',
  `assigned_at` timestamp NULL DEFAULT NULL COMMENT '分配时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address` (`address`),
  KEY `idx_status` (`status`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TRC20地址池';

-- ----------------------------
-- Records of trc20_addresses
-- ----------------------------

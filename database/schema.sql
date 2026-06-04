-- 兑换码发卡平台数据库结构
-- 支持多语种: 中文、英文、日文、韩文

-- 创建数据库
CREATE DATABASE IF NOT EXISTS giftcard_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE giftcard_platform;

-- 商品分类表
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_zh VARCHAR(100) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(100) NOT NULL COMMENT '英文名称',
    name_ja VARCHAR(100) NOT NULL COMMENT '日文名称',
    name_ko VARCHAR(100) NOT NULL COMMENT '韩文名称',
    description_zh TEXT COMMENT '中文描述',
    description_en TEXT COMMENT '英文描述',
    description_ja TEXT COMMENT '日文描述',
    description_ko TEXT COMMENT '韩文描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name_zh VARCHAR(200) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(200) NOT NULL COMMENT '英文名称',
    name_ja VARCHAR(200) NOT NULL COMMENT '日文名称',
    name_ko VARCHAR(200) NOT NULL COMMENT '韩文名称',
    description_zh TEXT COMMENT '中文描述',
    description_en TEXT COMMENT '英文描述',
    description_ja TEXT COMMENT '日文描述',
    description_ko TEXT COMMENT '韩文描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10, 2) COMMENT '原价',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币: CNY, USD',
    stock_quantity INT DEFAULT 0 COMMENT '库存数量',
    sold_quantity INT DEFAULT 0 COMMENT '已售数量',
    image_url VARCHAR(500) COMMENT '商品图片',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架 2-售罄',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 兑换码表
CREATE TABLE gift_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    card_code VARCHAR(100) NOT NULL COMMENT '兑换码',
    card_secret VARCHAR(255) COMMENT '卡密（可选，用于双重验证）',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未使用 1-已售出 2-已使用 3-已过期',
    order_id BIGINT COMMENT '关联订单ID',
    used_at TIMESTAMP NULL COMMENT '使用时间',
    used_by VARCHAR(100) COMMENT '使用者标识',
    valid_until TIMESTAMP NULL COMMENT '有效期至',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_card_code (card_code),
    INDEX idx_product (product_id),
    INDEX idx_status (status),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码表';

-- 订单表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式: wechat, alipay, trc20',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付 2-支付失败 3-已退款',
    payment_time TIMESTAMP NULL COMMENT '支付时间',
    payment_trx_id VARCHAR(200) COMMENT '第三方支付流水号',
    payment_info JSON COMMENT '支付相关信息（如TRC20地址、支付二维码等）',
    customer_email VARCHAR(100) COMMENT '客户邮箱',
    customer_phone VARCHAR(20) COMMENT '客户手机',
    status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-处理中 3-已完成 4-已取消',
    ip_address VARCHAR(50) COMMENT '下单IP',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_product (product_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单与兑换码关联表
CREATE TABLE order_gift_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    gift_card_id BIGINT NOT NULL COMMENT '兑换码ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (gift_card_id) REFERENCES gift_cards(id),
    UNIQUE KEY uk_order_card (order_id, gift_card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单兑换码关联表';

-- 支付记录表
CREATE TABLE payment_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    currency VARCHAR(10) NOT NULL COMMENT '货币',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-支付成功 2-支付失败 3-已退款',
    trx_id VARCHAR(200) COMMENT '第三方交易ID',
    trx_data JSON COMMENT '第三方返回的完整数据',
    paid_at TIMESTAMP NULL COMMENT '支付完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order (order_id),
    INDEX idx_trx_id (trx_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- TRC20支付地址池
CREATE TABLE trc20_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address VARCHAR(100) NOT NULL COMMENT 'TRC20地址',
    private_key_encrypted VARCHAR(500) COMMENT '加密后的私钥',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未使用 1-已分配 2-已禁用',
    order_id BIGINT COMMENT '当前分配给的订单ID',
    assigned_at TIMESTAMP NULL COMMENT '分配时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_address (address),
    INDEX idx_status (status),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TRC20地址池';

-- 管理员表
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) DEFAULT 'admin' COMMENT '角色: admin-管理员, super-超级管理员',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 支付配置表 - 管理员后台可配置
CREATE TABLE payment_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型: wechat, alipay, trc20',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键名',
    config_value TEXT COMMENT '配置值',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
    description VARCHAR(255) COMMENT '配置说明',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_key (config_type, config_key),
    INDEX idx_type (config_type),
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付配置表';

-- 系统配置表
CREATE TABLE settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL COMMENT '配置键',
    setting_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认分类
INSERT INTO categories (name_zh, name_en, name_ja, name_ko, description_zh, description_en, description_ja, description_ko, sort_order) VALUES
('游戏点卡', 'Game Cards', 'ゲームカード', '게임 카드', '各类游戏充值卡', 'Various game recharge cards', '各種ゲームチャージカード', '다양한 게임 충전 카드', 1),
('视频会员', 'Video Membership', '動画会員', '영상 멤버십', '视频平台会员充值', 'Video platform membership', '動画プラットフォーム会員', '비디오 플랫폼 멤버십', 2),
('音乐会员', 'Music Membership', '音楽会員', '음악 멤버십', '音乐平台会员充值', 'Music platform membership', '音楽プラットフォーム会員', '음악 플랫폼 멤버십', 3),
('软件授权', 'Software License', 'ソフトウェアライセンス', '소프트웨어 라이선스', '软件授权码', 'Software license keys', 'ソフトウェアライセンスキー', '소프트웨어 라이선스 키', 4);

-- 插入默认管理员 (密码: admin123)
-- 注意：生产环境请使用 BCrypt 加密后的密码
INSERT INTO admins (username, password_hash, email, role) VALUES
('admin', 'admin123', 'admin@example.com', 'super');

-- 插入默认系统配置
INSERT INTO settings (setting_key, setting_value, description) VALUES
('site_name_zh', '兑换码发卡平台', '网站名称-中文'),
('site_name_en', 'Gift Card Platform', '网站名称-英文'),
('site_name_ja', 'ギフトカードプラットフォーム', '网站名称-日文'),
('site_name_ko', '기프트 카드 플랫폼', '网站名称-韩文'),
('order_expire_minutes', '30', '订单过期时间(分钟)'),
('default_language', 'zh', '默认语言');

-- 插入默认支付配置（空值，需要管理员在后台配置）
INSERT INTO payment_configs (config_type, config_key, config_value, description, is_encrypted) VALUES
-- 微信支付配置
('wechat', 'app_id', '', '微信支付AppID', 0),
('wechat', 'mch_id', '', '微信支付商户号', 0),
('wechat', 'api_key', '', '微信支付API密钥', 1),
('wechat', 'api_v3_key', '', '微信支付APIv3密钥', 1),
('wechat', 'serial_no', '', '商户证书序列号', 0),
('wechat', 'private_key', '', '商户私钥', 1),
('wechat', 'notify_url', '', '微信支付回调地址', 0),

-- 支付宝配置
('alipay', 'app_id', '', '支付宝AppID', 0),
('alipay', 'private_key', '', '支付宝应用私钥', 1),
('alipay', 'public_key', '', '支付宝公钥', 0),
('alipay', 'alipay_public_key', '', '支付宝支付宝公钥', 0),
('alipay', 'gateway_url', 'https://openapi.alipay.com/gateway.do', '支付宝网关地址', 0),
('alipay', 'notify_url', '', '支付宝回调地址', 0),
('alipay', 'return_url', '', '支付宝同步返回地址', 0),
('alipay', 'sign_type', 'RSA2', '签名类型', 0),

-- TRC20/USDT配置
('trc20', 'usdt_contract', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 'USDT合约地址', 0),
('trc20', 'api_key', '', 'TRON API密钥', 1),
('trc20', 'api_url', 'https://api.trongrid.io', 'TRON API地址', 0),
('trc20', 'confirm_blocks', '19', '确认区块数', 0),
('trc20', 'exchange_rate', '7.2', 'USDT/CNY汇率', 0);

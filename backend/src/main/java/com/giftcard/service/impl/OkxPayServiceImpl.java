package com.giftcard.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.giftcard.entity.Order;
import com.giftcard.mapper.OrderMapper;
import com.giftcard.service.PaymentConfigService;
import com.giftcard.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * OKX 交易所支付服务实现
 * 通过 OKX API 查询链上充值记录来验证 USDT 支付
 */
@Service("okxPayService")
public class OkxPayServiceImpl implements PaymentService {

    @Autowired
    private PaymentConfigService paymentConfigService;

    @Autowired
    private OkxApiClient okxApiClient;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public Map<String, Object> createPayment(Order order, String lang) {
        Map<String, Object> result = new HashMap<>();

        // 检查 OKX 支付是否启用
        if (!paymentConfigService.isPaymentEnabled("okx")) {
            result.put("success", false);
            result.put("message", "OKX payment is not enabled");
            return result;
        }

        // 从数据库获取 OKX 配置
        Map<String, String> config = paymentConfigService.getOkxConfig();
        String walletAddress = config.get("wallet_address");
        String exchangeRate = config.getOrDefault("exchange_rate", "7.2");

        // 检查钱包地址是否配置
        if (walletAddress == null || walletAddress.isEmpty()) {
            result.put("success", false);
            result.put("message", "OKX wallet address not configured");
            return result;
        }

        try {
            // 计算 USDT 金额
            BigDecimal usdtAmount = order.getTotalAmount();
            if ("CNY".equals(order.getCurrency())) {
                BigDecimal rate = new BigDecimal(exchangeRate);
                usdtAmount = order.getTotalAmount().divide(rate, 6, RoundingMode.HALF_UP);
            }

            result.put("success", true);
            result.put("address", walletAddress);
            result.put("amount", usdtAmount);
            result.put("currency", "USDT");
            result.put("network", "OKX");
            result.put("orderNo", order.getOrderNo());
            result.put("expireMinutes", 30);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "PENDING");
        result.put("confirmations", 0);
        return result;
    }

    /**
     * 验证支付 - 通过 OKX API 查询充值记录和内部转账
     * 同时查询链上充值和站内转账，匹配金额和收款地址
     */
    public Map<String, Object> verifyPayment(String orderNo, BigDecimal expectedAmount) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("paid", false);

        System.out.println("[OKX] 开始验证支付，订单: " + orderNo + ", 期望金额: " + expectedAmount);

        try {
            // 获取订单信息
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                result.put("message", "订单不存在");
                return result;
            }

            // 检查是否已匹配过（避免重复匹配）
            if (order.getPaymentTrxId() != null && !order.getPaymentTrxId().isEmpty()) {
                System.out.println("[OKX] 订单 " + orderNo + " 已匹配过，交易ID: " + order.getPaymentTrxId());
                result.put("success", true);
                result.put("paid", true);
                result.put("message", "订单已确认支付");
                return result;
            }

            // 获取配置的收款地址
            Map<String, String> config = paymentConfigService.getOkxConfig();
            String walletAddress = config.get("wallet_address");

            if (walletAddress == null || walletAddress.isEmpty()) {
                result.put("message", "OKX wallet address not configured");
                return result;
            }

            // 计算查询时间范围：从订单创建时间到当前时间
            // OKX API 要求 Unix 毫秒时间戳（UTC）
            long endTime = System.currentTimeMillis();
            long beginTime;
            
            // 默认查询最近 2 小时（OKX 账单可能有延迟）
            long defaultLookbackMs = 2 * 60 * 60 * 1000L;
            
            if (order.getCreatedAt() != null) {
                // 将数据库中的时间转换为毫秒时间戳
                // 注意：假设数据库存储的是本地时间（北京时间 UTC+8）
                java.time.LocalDateTime localCreateTime = java.time.LocalDateTime.of(
                        order.getCreatedAt().getYear(),
                        order.getCreatedAt().getMonth(),
                        order.getCreatedAt().getDayOfMonth(),
                        order.getCreatedAt().getHour(),
                        order.getCreatedAt().getMinute(),
                        order.getCreatedAt().getSecond()
                );
                
                // 使用系统默认时区转换（北京时间 UTC+8）
                java.time.ZoneId systemZone = java.time.ZoneId.systemDefault();
                beginTime = localCreateTime.atZone(systemZone).toInstant().toEpochMilli();
                
                // 时区调试日志
                System.out.println("[OKX] 时区信息: 系统时区=" + systemZone + 
                    ", UTC偏移=" + java.time.ZonedDateTime.now().getOffset());
                System.out.println("[OKX] 订单创建时间(LocalDateTime): " + localCreateTime);
                System.out.println("[OKX] 转换后的时间戳: " + beginTime + 
                    " -> " + java.time.Instant.ofEpochMilli(beginTime).atZone(systemZone));
                
                // 如果订单创建时间太久远（超过2小时），使用默认查询范围
                if (endTime - beginTime > defaultLookbackMs) {
                    System.out.println("[OKX] 订单创建时间超过2小时，使用默认查询范围");
                    beginTime = endTime - defaultLookbackMs;
                }
            } else {
                beginTime = endTime - defaultLookbackMs;
            }

            System.out.println("[OKX] 查询时间范围: beginTime=" + beginTime + 
                " (" + new java.util.Date(beginTime) + "), endTime=" + endTime + 
                " (" + new java.util.Date(endTime) + "), 窗口=" + ((endTime - beginTime) / 60000) + "分钟");

            // 1. 查询链上充值记录（不限类型，兼容 Omni/TRC20/ERC20）
            System.out.println("[OKX] === 查询链上充值记录 ===");
            JsonNode depositResponse = okxApiClient.getDepositHistory("USDT", null, beginTime, endTime);
            if (checkAndProcessResponse(depositResponse, walletAddress, expectedAmount, result, "链上充值")) {
                return result; // 找到匹配的记录
            }

            // 2. 查询账户账单（包含站内转账）
            System.out.println("[OKX] === 查询账户账单 ===");
            JsonNode billsResponse = okxApiClient.getBills("USDT", beginTime, endTime);
            if (checkAndProcessResponse(billsResponse, walletAddress, expectedAmount, result, "账户账单")) {
                return result; // 找到匹配的记录
            }

            result.put("message", "未查询到匹配的充值或转账记录");

        } catch (Exception e) {
            System.err.println("[OKX] 验证支付失败: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "OKX API 查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查响应并处理记录
     */
    private boolean checkAndProcessResponse(JsonNode response, String walletAddress, BigDecimal expectedAmount, Map<String, Object> result, String type) {
        if (response == null) {
            System.out.println("[OKX] " + type + "查询失败，无响应数据");
            return false;
        }

        // 检查 API 返回码
        String code = response.has("code") ? response.get("code").asText() : "";
        if (!"0".equals(code)) {
            String msg = response.has("msg") ? response.get("msg").asText() : "Unknown error";
            System.err.println("[OKX] " + type + " API 错误: code=" + code + ", msg=" + msg);
            return false;
        }

        // 解析记录列表
        JsonNode data = response.get("data");
        if (data == null || !data.isArray() || data.size() == 0) {
            System.out.println("[OKX] " + type + "未查询到记录");
            return false;
        }

        System.out.println("[OKX] " + type + "查询到 " + data.size() + " 条记录");

        // 遍历记录，查找匹配的记录
        for (int i = 0; i < data.size(); i++) {
            JsonNode record = data.get(i);
            String recordType = record.has("type") ? record.get("type").asText() : "";

            // 链上充值记录（deposit-history）的字段
            String toAddr = record.has("to") ? record.get("to").asText() : "";
            String chain = record.has("chain") ? record.get("chain").asText() : "";
            String amountStr = record.has("amt") ? record.get("amt").asText() : "";
            String state = record.has("state") ? record.get("state").asText() : "";
            String txId = record.has("txId") ? record.get("txId").asText() : "";
            String depId = record.has("depId") ? record.get("depId").asText() : "";

            // 账户账单记录（bills）的字段名不同
            String billId = record.has("billId") ? record.get("billId").asText() : "";
            if (amountStr.isEmpty()) amountStr = record.has("sz") ? record.get("sz").asText() : "";
            if (state.isEmpty() && ("1".equals(recordType) || "13".equals(recordType))) {
                state = "2"; // bills API 中 type=1(充值)或13(转入)表示已完成
            }

            // 确定唯一标识：链上用 txId，内部交易用 billId
            String uniqueId = !txId.isEmpty() ? txId : billId;

            System.out.println("[OKX] " + type + "记录[" + i + "]: chain=" + chain + ", to=" + (toAddr.isEmpty() ? "(空)" : toAddr) +
                ", amt=" + amountStr + ", state=" + state + ", txId=" + txId + ", billId=" + billId + ", depId=" + depId);

            // 检查唯一标识是否已被其他订单使用（txId 或 billId 有一个存在就不认）
            if (!uniqueId.isEmpty() && orderMapper.countByUniqueId(uniqueId) > 0) {
                System.out.println("[OKX] " + type + "唯一标识已被其他订单使用: " + uniqueId);
                continue;
            }

            // 链上充值（state=2 表示已完成）：只检查 状态 + 金额 + 唯一标识
            if ("2".equals(state)) {
                try {
                    BigDecimal depositAmount = new BigDecimal(amountStr);
                    if (depositAmount.compareTo(expectedAmount) == 0) {
                        System.out.println("[OKX] " + type + "验证成功! txId=" + txId + ", amount=" + depositAmount);
                        result.put("success", true);
                        result.put("paid", true);
                        result.put("txId", uniqueId); // 保存唯一标识
                        result.put("amount", depositAmount);
                        result.put("confirmations", 1);
                        result.put("message", "Payment verified via OKX " + type);
                        return true;
                    } else {
                        System.out.println("[OKX] " + type + "金额不匹配: 期望=" + expectedAmount + ", 实际=" + depositAmount);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[OKX] 金额解析失败: " + amountStr);
                }
                continue;
            }

            // 账户账单（站内转账/充值）：只检查 类型 + 金额 + 唯一标识
            if ("1".equals(recordType) || "13".equals(recordType)) {
                if (amountStr.isEmpty()) continue;

                try {
                    BigDecimal depositAmount = new BigDecimal(amountStr);
                    if (depositAmount.compareTo(expectedAmount) == 0) {
                        System.out.println("[OKX] " + type + "验证成功! billId=" + billId + ", amount=" + depositAmount);
                        result.put("success", true);
                        result.put("paid", true);
                        result.put("txId", uniqueId); // 保存唯一标识
                        result.put("amount", depositAmount);
                        result.put("message", "Payment verified via OKX " + type);
                        return true;
                    } else {
                        System.out.println("[OKX] " + type + "金额不匹配: 期望=" + expectedAmount + ", 实际=" + depositAmount);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[OKX] 金额解析失败: " + amountStr);
                }
            }
        }

        return false;
    }

    /**
     * 通过交易哈希（TXID）验证支付
     * 直接查询 OKX API 获取指定 txId 的充值详情，增加时间±5分钟校验
     */
    public Map<String, Object> verifyPaymentByTxId(String txId, String expectedAddress, BigDecimal expectedAmount, Long orderCreatedTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("paid", false);

        System.out.println("[OKX] 通过 TXID 查询交易: " + txId);
        System.out.println("[OKX] 期望收款地址: " + expectedAddress + ", 期望金额: " + expectedAmount);
        if (orderCreatedTime != null) {
            System.out.println("[OKX] 订单创建时间: " + new java.util.Date(orderCreatedTime) + ", 允许时间范围: ±5分钟");
        }

        try {
            // 获取配置的收款地址
            Map<String, String> config = paymentConfigService.getOkxConfig();
            String walletAddress = config.get("wallet_address");

            if (walletAddress == null || walletAddress.isEmpty()) {
                result.put("message", "OKX wallet address not configured");
                return result;
            }

            // 通过 txId 查询充值详情
            JsonNode response = okxApiClient.getDepositByTxId(txId);

            if (response == null) {
                result.put("message", "OKX API 查询失败，无响应数据");
                return result;
            }

            // 检查 API 返回码
            String code = response.has("code") ? response.get("code").asText() : "";
            if (!"0".equals(code)) {
                String msg = response.has("msg") ? response.get("msg").asText() : "Unknown error";
                System.err.println("[OKX] API 错误: code=" + code + ", msg=" + msg);
                result.put("message", "OKX API error: " + msg);
                return result;
            }

            // 解析充值记录
            JsonNode data = response.get("data");
            if (data == null || !data.isArray() || data.size() == 0) {
                result.put("message", "未查询到该交易记录，请确认交易哈希正确");
                return result;
            }

            JsonNode deposit = data.get(0);

            String depAddr = deposit.has("to") ? deposit.get("to").asText() : "";
            String amountStr = deposit.has("amt") ? deposit.get("amt").asText() : "";
            String state = deposit.has("state") ? deposit.get("state").asText() : "";
            String ccy = deposit.has("ccy") ? deposit.get("ccy").asText() : "";
            String depositTimeStr = deposit.has("ts") ? deposit.get("ts").asText() : "";

            System.out.println("[OKX] 充值详情: to=" + depAddr + ", amt=" + amountStr
                    + ", state=" + state + ", ccy=" + ccy + ", ts=" + depositTimeStr);

            // 检查币种
            if (!"USDT".equals(ccy)) {
                result.put("message", "该交易不是 USDT 充值");
                return result;
            }

            // 检查收款地址是否匹配
            if (!walletAddress.equals(depAddr)) {
                result.put("message", "收款地址不匹配");
                return result;
            }

            // 检查充值状态（2 表示已到账）
            if (!"2".equals(state)) {
                String stateMsg = switch (state) {
                    case "0" -> "等待中";
                    case "1" -> "充值中（等待确认）";
                    case "2" -> "已到账";
                    case "3" -> "充值失败";
                    default -> "未知状态(" + state + ")";
                };
                result.put("message", "充值状态: " + stateMsg);
                return result;
            }

            // 检查时间是否在允许范围内（订单创建时间 ±5分钟）
            if (orderCreatedTime != null && !depositTimeStr.isEmpty()) {
                long depositTime = Long.parseLong(depositTimeStr);
                long timeDiff = depositTime - orderCreatedTime;
                long fiveMinutes = 5 * 60 * 1000L; // 5分钟毫秒数

                System.out.println("[OKX] 时间校验: 充值时间差=" + timeDiff + "ms (" + (timeDiff / 60000.0) + "分钟)");

                if (Math.abs(timeDiff) > fiveMinutes) {
                    result.put("message", "交易时间不在允许范围内（订单创建后±5分钟），请确认是最新转账");
                    return result;
                }
            }

            // 检查金额是否精确匹配
            if (amountStr == null || amountStr.isEmpty()) {
                result.put("message", "无法获取充值金额");
                return result;
            }

            BigDecimal depositAmount = new BigDecimal(amountStr);

            if (depositAmount.compareTo(expectedAmount) == 0) {
                System.out.println("[OKX] TXID 验证成功! txId=" + txId + ", amount=" + depositAmount);
                result.put("success", true);
                result.put("paid", true);
                result.put("txId", txId);
                result.put("amount", depositAmount);
                result.put("toAddress", depAddr);
                result.put("message", "交易验证成功");
            } else {
                result.put("message", "充值金额不匹配，期望: " + expectedAmount + " USDT，实际: " + depositAmount + " USDT");
            }

        } catch (Exception e) {
            System.err.println("[OKX] 查询交易失败: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "OKX API 查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public boolean handleNotify(String paymentMethod, String notifyData) {
        // OKX 通过轮询查询，不需要回调
        return true;
    }

    @Override
    public boolean closePayment(String orderNo) {
        // OKX 支付无需特殊关闭操作
        return true;
    }
}

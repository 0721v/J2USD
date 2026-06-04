package com.giftcard.service.impl;

import com.giftcard.entity.Order;
import com.giftcard.entity.Trc20Address;
import com.giftcard.mapper.Trc20AddressMapper;
import com.giftcard.service.PaymentConfigService;
import com.giftcard.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("trc20PayService")
public class Trc20PayServiceImpl implements PaymentService {

    @Autowired
    private PaymentConfigService paymentConfigService;

    @Autowired
    private Trc20AddressMapper trc20AddressMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TRONGRID_API = "https://api.trongrid.io";
    private static final String USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    @Override
    @Transactional
    public Map<String, Object> createPayment(Order order, String lang) {
        Map<String, Object> result = new HashMap<>();

        // 检查TRC20是否启用
        if (!paymentConfigService.isPaymentEnabled("trc20")) {
            result.put("success", false);
            result.put("message", "TRC20 payment is not enabled");
            return result;
        }

        // 从数据库获取配置
        Map<String, String> config = paymentConfigService.getTrc20Config();
        String walletAddress = config.get("wallet_address");
        String usdtContract = config.get("usdt_contract");
        String exchangeRate = config.getOrDefault("exchange_rate", "7.2");

        // 检查钱包地址是否配置
        if (walletAddress == null || walletAddress.isEmpty()) {
            result.put("success", false);
            result.put("message", "TRC20 wallet address not configured");
            return result;
        }

        try {
            // 计算USDT金额
            BigDecimal usdtAmount = order.getTotalAmount();
            if ("CNY".equals(order.getCurrency())) {
                // 使用配置的汇率
                BigDecimal rate = new BigDecimal(exchangeRate);
                usdtAmount = order.getTotalAmount().divide(rate, 6, RoundingMode.HALF_UP);
            }

            result.put("success", true);
            result.put("address", walletAddress);
            result.put("amount", usdtAmount);
            result.put("currency", "USDT");
            result.put("network", "TRC20");
            result.put("contract", usdtContract != null ? usdtContract : USDT_CONTRACT);
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
     * 查询指定地址的 USDT 交易记录，检查是否收到对应订单的转账
     */
    public Map<String, Object> checkPaymentByAddress(String orderNo, String address, BigDecimal expectedAmount) {
        Map<String, Object> result = new HashMap<>();
        result.put("paid", false);
        result.put("confirmations", 0);
        result.put("trxId", null);

        System.out.println("[TRON] 开始查询支付状态，订单: " + orderNo + ", 地址: " + address + ", 金额: " + expectedAmount);

        try {
            // 调用 TRON API 查询交易记录
            String url = TRONGRID_API + "/v1/accounts/" + address + "/transactions/trc20?limit=20&contract_address=" + USDT_CONTRACT;
            System.out.println("[TRON] 请求 URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            System.out.println("[TRON] API 响应: " + (body != null ? body.toString() : "null"));

            if (body == null || !body.containsKey("data")) {
                return result;
            }

            List<Map<String, Object>> transactions = (List<Map<String, Object>>) body.get("data");
            System.out.println("[TRON] 交易数量: " + (transactions != null ? transactions.size() : 0));

            for (Map<String, Object> tx : transactions) {
                System.out.println("[TRON] 检查交易: " + tx);
                // 解析交易信息
                Map<String, Object> tokenInfo = (Map<String, Object>) tx.get("token_info");
                if (tokenInfo == null || !USDT_CONTRACT.equals(tokenInfo.get("address"))) {
                    continue;
                }

                // 获取转账金额（注意：TRC20 金额需要除以 10^6）
                String amountStr = (String) tx.get("value");
                System.out.println("[TRON] 交易金额字符串: " + amountStr);
                if (amountStr == null) continue;

                BigDecimal amount = new BigDecimal(amountStr).divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
                System.out.println("[TRON] 转换后金额: " + amount + ", 期望金额: " + expectedAmount);

                // 检查金额是否匹配（允许 0.01 USDT 的误差）
                if (amount.compareTo(expectedAmount) >= 0) {
                    System.out.println("[TRON] 金额匹配，检查交易状态");
                    // 检查交易确认数
                    Map<String, Object> ret = (Map<String, Object>) tx.get("ret");
                    System.out.println("[TRON] 交易状态: " + ret);
                    if (ret != null && "SUCCESS".equals(ret.get("contractRet"))) {
                        System.out.println("[TRON] 支付确认成功!");
                        result.put("paid", true);
                        result.put("trxId", tx.get("transaction_id"));
                        result.put("amount", amount);
                        result.put("confirmations", 1); // TRON 通常一个区块即确认
                        result.put("timestamp", tx.get("block_timestamp"));
                        return result;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("查询 TRON 交易失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证支付 - 查询区块链确认交易
     */
    public Map<String, Object> verifyPayment(String orderNo, BigDecimal expectedAmount) {
        Map<String, Object> result = new HashMap<>();

        // 获取配置的钱包地址
        Map<String, String> config = paymentConfigService.getTrc20Config();
        String walletAddress = config.get("wallet_address");

        if (walletAddress == null || walletAddress.isEmpty()) {
            result.put("success", false);
            result.put("message", "Wallet address not configured");
            return result;
        }

        // 查询区块链交易
        Map<String, Object> checkResult = checkPaymentByAddress(orderNo, walletAddress, expectedAmount);

        if ((Boolean) checkResult.get("paid")) {
            result.put("success", true);
            result.put("paid", true);
            result.put("trxId", checkResult.get("trxId"));
            result.put("amount", checkResult.get("amount"));
            result.put("confirmations", checkResult.get("confirmations"));
            result.put("message", "Payment verified");
        } else {
            result.put("success", false);
            result.put("paid", false);
            result.put("message", "Payment not found");
        }

        return result;
    }

    /**
     * 通过交易哈希（TXID）验证支付
     * 查询 TRON 区块链确认该交易是否真实存在且金额匹配
     */
    public Map<String, Object> verifyPaymentByTxId(String txId, String expectedAddress, BigDecimal expectedAmount) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        System.out.println("[TRON] 通过 TXID 查询交易: " + txId);
        System.out.println("[TRON] 期望收款地址: " + expectedAddress + ", 期望金额: " + expectedAmount);

        try {
            // 调用 TRON API 查询交易详情
            String url = TRONGRID_API + "/v1/transactions/" + txId;
            System.out.println("[TRON] 请求 URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                result.put("message", "查询交易失败，无响应数据");
                return result;
            }

            System.out.println("[TRON] 交易详情响应: " + body);

            // 检查交易是否成功
            Map<String, Object> ret = (Map<String, Object>) body.get("ret");
            if (ret == null || !"SUCCESS".equals(ret.get("contractRet"))) {
                result.put("message", "交易未成功或仍在确认中");
                return result;
            }

            // 检查交易类型是否为 TRC20 转账
            List<Map<String, Object>> contracts = (List<Map<String, Object>>) body.get("contractRet");
            if (contracts == null) {
                // 尝试从 raw_data.contract 获取
                Map<String, Object> rawData = (Map<String, Object>) body.get("raw_data");
                if (rawData != null) {
                    List<Map<String, Object>> contractList = (List<Map<String, Object>>) rawData.get("contract");
                    if (contractList != null && !contractList.isEmpty()) {
                        Map<String, Object> contract = contractList.get(0);
                        Map<String, Object> parameter = (Map<String, Object>) contract.get("parameter");
                        if (parameter != null) {
                            Map<String, Object> value = (Map<String, Object>) parameter.get("value");
                            if (value != null) {
                                // 检查合约类型
                                String type = (String) contract.get("type");
                                System.out.println("[TRON] 合约类型: " + type);

                                if ("TriggerSmartContract".equals(type)) {
                                    // TRC20 转账
                                    String toAddress = (String) value.get("to");
                                    // TRON 地址可能是 base64 或 base58check 格式
                                    String data = (String) value.get("data");

                                    System.out.println("[TRON] to 地址: " + toAddress);
                                    System.out.println("[TRON] data: " + data);

                                    // 解析 data 获取转账金额（TRC20 transfer 的 data 前 8 字节是 method ID，后面是参数）
                                    // transfer(address,uint256) 的 method ID 是 0xa9059cbb
                                    if (data != null && data.length() > 136) {
                                        // 去掉 method ID (8字符) 和 address padding (64字符)
                                        String amountHex = data.substring(136, 200);
                                        BigDecimal amount = new BigDecimal(new java.math.BigInteger(amountHex, 16))
                                                .divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);

                                        System.out.println("[TRON] 解析转账金额: " + amount);

                                        // 检查金额是否匹配（允许 1% 的误差）
                                        BigDecimal minAmount = expectedAmount.multiply(new BigDecimal("0.99"));
                                        if (amount.compareTo(minAmount) >= 0) {
                                            result.put("success", true);
                                            result.put("paid", true);
                                            result.put("trxId", txId);
                                            result.put("amount", amount);
                                            result.put("toAddress", toAddress);
                                            result.put("message", "交易验证成功");
                                            return result;
                                        } else {
                                            result.put("message", "转账金额不匹配，期望: " + expectedAmount + " USDT，实际: " + amount + " USDT");
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 备用方案：通过 TRC20 交易记录 API 查询
            System.out.println("[TRON] 尝试通过 TRC20 交易 API 查询...");
            String trc20Url = TRONGRID_API + "/v1/transactions/" + txId + "/trc20";
            ResponseEntity<Map> trc20Response = restTemplate.getForEntity(trc20Url, Map.class);
            Map<String, Object> trc20Body = trc20Response.getBody();

            if (trc20Body != null) {
                System.out.println("[TRON] TRC20 交易详情: " + trc20Body);
            }

            result.put("message", "无法解析交易信息，请确认交易哈希正确");

        } catch (Exception e) {
            System.err.println("[TRON] 查询交易失败: " + e.getMessage());
            e.printStackTrace();
            result.put("message", "查询交易失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public boolean handleNotify(String paymentMethod, String notifyData) {
        // TRC20通过轮询查询，不需要回调
        return true;
    }

    @Override
    public boolean closePayment(String orderNo) {
        // 释放地址
        return true;
    }

    /**
     * 查询地址余额和交易
     */
    public Map<String, Object> checkAddressBalance(String address) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用 TRON API 查询余额
            String url = TRONGRID_API + "/v1/accounts/" + address;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> account = data.get(0);
                    result.put("balance", account.get("balance"));
                    result.put("trc20", account.get("trc20"));
                }
            }
        } catch (Exception e) {
            System.err.println("查询余额失败: " + e.getMessage());
            result.put("balance", "0");
        }
        return result;
    }
}

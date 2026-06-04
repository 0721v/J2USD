package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.entity.Admin;
import com.giftcard.entity.Category;
import com.giftcard.entity.GiftCard;
import com.giftcard.entity.Order;
import com.giftcard.entity.Product;
import com.giftcard.mapper.AdminMapper;
import com.giftcard.mapper.CategoryMapper;
import com.giftcard.mapper.GiftCardMapper;
import com.giftcard.mapper.OrderMapper;
import com.giftcard.mapper.ProductMapper;
import com.giftcard.util.JwtUtil;
import com.giftcard.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
private GiftCardMapper giftCardMapper;

@Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private OrderNoUtil orderNoUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 初始化管理员账号（仅用于开发测试）
     */
    @PostMapping("/init")
    public Result<Map<String, Object>> initAdmin() {
        // 使用 MyBatis Plus 查询所有管理员（包括 status=0 的）
        Admin existing = adminMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Admin>()
                .eq("username", "admin")
        );
        
        if (existing != null) {
            // 更新现有管理员的密码和状态
            existing.setPasswordHash("admin123");
            existing.setStatus(1);
            existing.setUpdatedAt(LocalDateTime.now());
            adminMapper.updateById(existing);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Admin password reset successfully");
            result.put("username", "admin");
            result.put("password", "admin123");
            return Result.success(result);
        }
        
        // 创建默认管理员
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash("admin123"); // 明文密码，开发测试用
        admin.setEmail("admin@example.com");
        admin.setRole("super");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        adminMapper.insert(admin);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Admin created successfully");
        result.put("username", "admin");
        result.put("password", "admin123");
        return Result.success(result);
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        
        String username = params.get("username");
        String password = params.get("password");
        
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return Result.error("Invalid username or password");
        }
        
        // 检查密码是否匹配（支持明文密码，用于初始化）
        boolean passwordMatch = false;
        if (admin.getPasswordHash().startsWith("$2a$") || admin.getPasswordHash().startsWith("$2b$") || admin.getPasswordHash().startsWith("$2y$")) {
            // BCrypt 哈希
            passwordMatch = passwordEncoder.matches(password, admin.getPasswordHash());
        } else {
            // 明文密码（仅用于开发测试）
            passwordMatch = password.equals(admin.getPasswordHash());
        }
        
        if (!passwordMatch) {
            return Result.error("Invalid username or password");
        }
        
        // 更新登录信息
        adminMapper.updateLoginInfo(admin.getId(), LocalDateTime.now(), request.getRemoteAddr());
        
        // 生成JWT
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole());
        
        return Result.success("Login successful", result);
    }
    
    /**
     * 检查登录状态（验证 token 是否有效）
     */
    @GetMapping("/check-auth")
    public Result<Map<String, Object>> checkAuth(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        
        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        } else if (token != null && !token.isEmpty()) {
            jwtToken = token;
        }
        
        if (jwtToken == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("authenticated", false);
            return Result.success(result);
        }
        
        try {
            jwtUtil.verifyToken(jwtToken);
            String username = jwtUtil.getUsernameFromToken(jwtToken);
            Map<String, Object> result = new HashMap<>();
            result.put("authenticated", true);
            result.put("username", username);
            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("authenticated", false);
            return Result.success(result);
        }
    }
    
    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<Map<String, Object>> addProduct(@RequestBody Product product) {
        product.setSoldQuantity(0);
        product.setStatus(1);
        productMapper.insert(product);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        return Result.success("Product added", result);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {
        
        product.setId(id);
        productMapper.updateById(product);
        
        return Result.success("Product updated", null);
    }
    
    /**
     * 批量添加兑换码（支持自动生成或手动填写，支持有效期设置）
     */
    @PostMapping("/giftcards/batch")
    public Result<Map<String, Object>> batchAddCards(@RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        
        // 获取手动填写的兑换码列表
        @SuppressWarnings("unchecked")
        List<String> cardCodes = (List<String>) params.get("cardCodes");
        
        // 获取有效期（null 或空字符串表示永久有效）
        String expireAtStr = params.get("expireAt") != null ? params.get("expireAt").toString() : null;
        LocalDateTime expireAt = null;
        if (expireAtStr != null && !expireAtStr.isEmpty() && !"permanent".equals(expireAtStr)) {
            try {
                expireAt = LocalDateTime.parse(expireAtStr + "T23:59:59");
            } catch (Exception e) {
                // 解析失败则视为永久有效
                expireAt = null;
            }
        }
        
        int successCount = 0;
        int totalCount = 0;
        List<String> failedCodes = new ArrayList<>();
        
        if (cardCodes != null && !cardCodes.isEmpty()) {
            // 手动填写模式
            totalCount = cardCodes.size();
            for (String code : cardCodes) {
                if (code == null || code.trim().isEmpty()) continue;
                
                GiftCard card = new GiftCard();
                card.setProductId(productId);
                card.setCardCode(code.trim());
                card.setStatus(0);
                card.setValidUntil(expireAt);
                card.setCreatedAt(LocalDateTime.now());
                card.setUpdatedAt(LocalDateTime.now());
                
                try {
                    giftCardMapper.insert(card);
                    successCount++;
                } catch (Exception e) {
                    failedCodes.add(code.trim());
                }
            }
        } else {
            // 自动生成模式
            Integer count = Integer.valueOf(params.get("count").toString());
            totalCount = count;
            
            for (int i = 0; i < count; i++) {
                GiftCard card = new GiftCard();
                card.setProductId(productId);
                card.setCardCode(orderNoUtil.generateCardCode());
                card.setStatus(0);
                card.setValidUntil(expireAt);
                card.setCreatedAt(LocalDateTime.now());
                card.setUpdatedAt(LocalDateTime.now());
                
                try {
                    giftCardMapper.insert(card);
                    successCount++;
                } catch (Exception e) {
                    // 可能重复，重试一次
                    card.setCardCode(orderNoUtil.generateCardCode());
                    try {
                        giftCardMapper.insert(card);
                        successCount++;
                    } catch (Exception e2) {
                        // 忽略
                    }
                }
            }
        }
        
        // 更新商品库存
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStockQuantity(product.getStockQuantity() + successCount);
            productMapper.updateById(product);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", totalCount - successCount);
        result.put("failedCodes", failedCodes);
        return Result.success("Cards added", result);
    }
    
    /**
     * 获取兑换码列表
     */
    @GetMapping("/giftcards")
    public Result<Map<String, Object>> listCards(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        // 查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftCard> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftCard>()
                .eq(productId != null, GiftCard::getProductId, productId)
                .eq(status != null, GiftCard::getStatus, status);
        
        // 总条数
        Long total = giftCardMapper.selectCount(queryWrapper);
        
        // 分页查询
        List<GiftCard> cards = giftCardMapper.selectList(
            queryWrapper
                .orderByDesc(GiftCard::getCreatedAt)
                .last("LIMIT " + (page - 1) * size + ", " + size)
        );
        
        // 关联商品名称
        for (GiftCard card : cards) {
            Product product = productMapper.selectById(card.getProductId());
            if (product != null) {
                card.setProduct(product);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("cards", cards);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        
        return Result.success(result);
    }
    
    /**
     * 删除兑换码
     */
    @DeleteMapping("/giftcards/{id}")
    public Result<Map<String, Object>> deleteCard(@PathVariable Long id) {
        GiftCard card = giftCardMapper.selectById(id);
        if (card == null) {
            return Result.notFound("Card not found");
        }
        
        if (card.getStatus() != 0) {
            return Result.error("Cannot delete used or sold card");
        }
        
        giftCardMapper.deleteById(id);
        
        // 减少库存
        Product product = productMapper.selectById(card.getProductId());
        if (product != null) {
            product.setStockQuantity(Math.max(0, product.getStockQuantity() - 1));
            productMapper.updateById(product);
        }
        
        return Result.success("Card deleted", null);
    }
    
    // ========== 商品管理 ==========
    
    /**
     * 获取所有商品列表（管理后台用）
     */
    @GetMapping("/products")
    public Result<Map<String, Object>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreatedAt);
        
        // 获取总数
        Long total = productMapper.selectCount(queryWrapper);
        
        // 获取分页数据
        List<Product> products = productMapper.selectList(
            queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size)
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        
        return Result.success(result);
    }
    
    /**
     * 获取商品详情（管理后台用）
     */
    @GetMapping("/products/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.notFound("Product not found");
        }
        return Result.success(product);
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.notFound("Product not found");
        }
        
        // 检查是否有库存
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            return Result.error("Cannot delete product with stock");
        }
        
        productMapper.deleteById(id);
        return Result.success("Product deleted", null);
    }
    
    /**
     * 更新商品状态（上架/下架）
     */
    @PutMapping("/products/{id}/status")
    public Result<String> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.notFound("Product not found");
        }
        
        Integer status = params.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("Invalid status");
        }
        
        product.setStatus(status);
        productMapper.updateById(product);
        
        return Result.success("Status updated", null);
    }
    
    // ========== 分类管理 ==========
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<Map<String, Object>> listCategories() {
        try {
            List<Category> categories = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                    .orderByDesc(Category::getSortOrder)
                    .orderByDesc(Category::getCreatedAt)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("categories", categories);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.notFound("Category not found");
        }
        return Result.success(category);
    }
    
    /**
     * 添加分类
     */
    @PostMapping("/categories")
    public Result<Map<String, Object>> addCategory(@RequestBody Category category) {
        category.setStatus(1);
        // createdAt和updatedAt由MyBatis-Plus自动填充
        categoryMapper.insert(category);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", category.getId());
        return Result.success("Category added", result);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public Result<String> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {
        
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            return Result.notFound("Category not found");
        }
        
        category.setId(id);
        // updatedAt由MyBatis-Plus自动填充
        categoryMapper.updateById(category);
        
        return Result.success("Category updated", null);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.notFound("Category not found");
        }
        
        // 检查分类下是否有商品
        Long productCount = productMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id)
        );
        
        if (productCount > 0) {
            return Result.error("Cannot delete category with products");
        }
        
        categoryMapper.deleteById(id);
        return Result.success("Category deleted", null);
    }
    
    /**
     * 更新分类状态
     */
    @PutMapping("/categories/{id}/status")
    public Result<String> updateCategoryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.notFound("Category not found");
        }
        
        Integer status = params.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("Invalid status");
        }
        
        category.setStatus(status);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        
        return Result.success("Status updated", null);
    }
    
    // ========== 订单管理 ==========
    
    /**
     * 获取订单列表（管理后台用）
     */
    @GetMapping("/orders")
    public Result<Map<String, Object>> listOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .like(orderNo != null && !orderNo.isEmpty(), Order::getOrderNo, orderNo)
                .eq(status != null, Order::getStatus, status)
                .eq(paymentStatus != null, Order::getPaymentStatus, paymentStatus)
                .orderByDesc(Order::getCreatedAt);
        
        Long total = orderMapper.selectCount(queryWrapper);
        
        List<Order> orders = orderMapper.selectList(
            queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size)
        );
        
        // 关联商品信息
        for (Order order : orders) {
            if (order.getProductId() != null) {
                order.setProduct(productMapper.selectById(order.getProductId()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        
        return Result.success(result);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.notFound("Order not found");
        }
        // 关联商品信息
        if (order.getProductId() != null) {
            order.setProduct(productMapper.selectById(order.getProductId()));
        }
        return Result.success(order);
    }
    
    /**
     * 更新订单状态
     */
    @PutMapping("/orders/{id}/status")
    public Result<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.notFound("Order not found");
        }
        
        Integer status = params.get("status");
        if (status != null) {
            order.setStatus(status);
        }
        
        Integer paymentStatus = params.get("paymentStatus");
        if (paymentStatus != null) {
            order.setPaymentStatus(paymentStatus);
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return Result.success("Order status updated", null);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/orders/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.notFound("Order not found");
        }
        orderMapper.deleteById(id);
        return Result.success("Order deleted", null);
    }
}

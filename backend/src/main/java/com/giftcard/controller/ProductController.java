package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.entity.Category;
import com.giftcard.entity.Product;
import com.giftcard.mapper.CategoryMapper;
import com.giftcard.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private com.giftcard.mapper.GiftCardMapper giftCardMapper;
    
    /**
     * 获取商品列表
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "zh") String lang) {
        
        List<Product> products;
        if (categoryId != null) {
            products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, categoryId)
                    .eq(Product::getStatus, 1)
                    .orderByAsc(Product::getSortOrder)
            );
        } else {
            products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
                    .orderByAsc(Product::getSortOrder)
            );
        }
        
        // 根据语言过滤名称和描述，并计算实际库存
        for (Product product : products) {
            // 先获取当前语言的名称和描述（必须在清空字段之前）
            String localizedName = product.getNameByLang(lang);
            String localizedDesc = product.getDescriptionByLang(lang);
            
            // 计算实际可用库存
            Integer actualStock = giftCardMapper.countAvailableCards(product.getId());
            product.setStockQuantity(actualStock != null ? actualStock : 0);
            
            // 清空多语言字段以减少响应大小
            product.setNameZh(null);
            product.setNameEn(null);
            product.setNameJa(null);
            product.setNameKo(null);
            product.setDescriptionZh(null);
            product.setDescriptionEn(null);
            product.setDescriptionJa(null);
            product.setDescriptionKo(null);
            
            // 设置本地化的单一名称和描述
            product.setName(localizedName);
            product.setDescription(localizedDesc);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("lang", lang);
        
        return Result.success(result);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "zh") String lang) {
        
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.notFound("Product not found");
        }
        
        // 获取实际可用库存（从 gift_cards 表计算）
        Integer stock = giftCardMapper.countAvailableCards(id);
        if (stock == null) {
            stock = 0;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getNameByLang(lang));
        result.put("description", product.getDescriptionByLang(lang));
        result.put("price", product.getPrice());
        result.put("originalPrice", product.getOriginalPrice());
        result.put("currency", product.getCurrency());
        result.put("stock", stock);
        result.put("imageUrl", product.getImageUrl());
        result.put("lang", lang);
        
        return Result.success(result);
    }
    
    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public Result<Map<String, Object>> categories(
            @RequestParam(defaultValue = "zh") String lang) {
        
        List<Category> categories = categoryMapper.selectActiveCategories();
        
        // 根据语言处理，返回本地化的name字段
        for (Category category : categories) {
            category.setNameZh(category.getNameByLang(lang));
            category.setNameEn(null);
            category.setNameJa(null);
            category.setNameKo(null);
            category.setDescriptionZh(category.getDescriptionByLang(lang));
            category.setDescriptionEn(null);
            category.setDescriptionJa(null);
            category.setDescriptionKo(null);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("lang", lang);
        
        return Result.success(result);
    }
}

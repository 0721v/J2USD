package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 使用 BaseMapper.selectList + LambdaQueryWrapper，由 MyBatis-Plus 自动处理驼峰映射
    
    @Update("UPDATE products SET stock_quantity = stock_quantity - #{quantity}, " +
            "sold_quantity = sold_quantity + #{quantity} " +
            "WHERE id = #{productId} AND stock_quantity >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    @Update("UPDATE products SET status = CASE WHEN stock_quantity = 0 THEN 2 ELSE status END")
    int updateSoldOutStatus();
}

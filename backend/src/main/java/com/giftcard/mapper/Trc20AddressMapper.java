package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Trc20Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface Trc20AddressMapper extends BaseMapper<Trc20Address> {
    
    @Select("SELECT * FROM trc20_addresses WHERE status = 0 LIMIT 1")
    Trc20Address selectOneAvailable();
    
    @Update("UPDATE trc20_addresses SET status = 1, order_id = #{orderId}, assigned_at = #{assignedAt} " +
            "WHERE id = #{id} AND status = 0")
    int assignToOrder(@Param("id") Long id, 
                     @Param("orderId") Long orderId, 
                     @Param("assignedAt") LocalDateTime assignedAt);
    
    @Select("SELECT * FROM trc20_addresses WHERE order_id = #{orderId}")
    Trc20Address selectByOrderId(@Param("orderId") Long orderId);
    
    @Update("UPDATE trc20_addresses SET status = 0, order_id = NULL, assigned_at = NULL " +
            "WHERE order_id = #{orderId}")
    int releaseAddress(@Param("orderId") Long orderId);
}

package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    
    @Select("SELECT * FROM orders WHERE status = 0 AND (created_at < #{expireTime} OR created_at IS NULL)")
    List<Order> selectExpiredOrders(@Param("expireTime") LocalDateTime expireTime);
    
    @Update("UPDATE orders SET status = #{status}, payment_status = #{paymentStatus}, " +
            "payment_time = #{paymentTime}, payment_trx_id = #{paymentTrxId} " +
            "WHERE id = #{orderId}")
    int updatePaymentStatus(@Param("orderId") Long orderId, 
                           @Param("status") Integer status,
                           @Param("paymentStatus") Integer paymentStatus,
                           @Param("paymentTime") LocalDateTime paymentTime,
                           @Param("paymentTrxId") String paymentTrxId);
    
    @Select("SELECT o.*, p.name_zh, p.name_en, p.name_ja, p.name_ko " +
            "FROM orders o " +
            "LEFT JOIN products p ON o.product_id = p.id " +
            "WHERE o.customer_email = #{email} " +
            "ORDER BY o.created_at DESC")
    List<Order> selectByCustomerEmail(@Param("email") String email);
    
    @Select("SELECT o.*, p.name_zh, p.name_en, p.name_ja, p.name_ko " +
            "FROM orders o " +
            "LEFT JOIN products p ON o.product_id = p.id " +
            "WHERE o.customer_email = #{email} AND o.query_password = #{queryPassword} " +
            "ORDER BY o.created_at DESC")
    List<Order> selectByEmailAndPassword(@Param("email") String email, @Param("queryPassword") String queryPassword);

    @Select("SELECT COUNT(*) FROM orders WHERE customer_email = #{email} AND query_password = #{queryPassword}")
    int countByEmailAndPassword(@Param("email") String email, @Param("queryPassword") String queryPassword);

    @Select("SELECT o.*, p.name_zh, p.name_en, p.name_ja, p.name_ko " +
            "FROM orders o " +
            "LEFT JOIN products p ON o.product_id = p.id " +
            "WHERE o.customer_email = #{email} AND o.query_password = #{queryPassword} " +
            "ORDER BY o.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Order> selectByEmailAndPasswordPage(@Param("email") String email, @Param("queryPassword") String queryPassword,
                                              @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM orders WHERE payment_trx_id = #{txId} AND status >= 2")
    int countByTxId(@Param("txId") String txId);

    @Select("SELECT COUNT(*) FROM orders WHERE payment_trx_id = #{uniqueId} AND status >= 2")
    int countByUniqueId(@Param("uniqueId") String uniqueId);
}

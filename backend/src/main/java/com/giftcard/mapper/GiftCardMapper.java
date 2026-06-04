package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.GiftCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface GiftCardMapper extends BaseMapper<GiftCard> {
    
    @Select("SELECT * FROM gift_cards WHERE product_id = #{productId} AND status = 0 " +
            "LIMIT #{limit}")
    List<GiftCard> selectAvailableCards(@Param("productId") Long productId, 
                                         @Param("limit") Integer limit);
    
    @Select("SELECT COUNT(*) FROM gift_cards WHERE product_id = #{productId} AND status = 0")
    Integer countAvailableCards(@Param("productId") Long productId);
    
    @Update("UPDATE gift_cards SET status = 1, order_id = #{orderId} " +
            "WHERE id = #{cardId} AND status = 0")
    int lockCard(@Param("cardId") Long cardId, @Param("orderId") Long orderId);
    
    @Select("SELECT * FROM gift_cards WHERE order_id = #{orderId}")
    List<GiftCard> selectByOrderId(@Param("orderId") Long orderId);
    
    @Select("SELECT * FROM gift_cards WHERE card_code = #{cardCode}")
    GiftCard selectByCardCode(@Param("cardCode") String cardCode);
    
    @Update("UPDATE gift_cards SET status = 2, used_at = NOW(), used_by = #{usedBy} " +
            "WHERE card_code = #{cardCode} AND status = 1")
    int markAsUsed(@Param("cardCode") String cardCode, @Param("usedBy") String usedBy);
}

package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.PaymentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PaymentConfigMapper extends BaseMapper<PaymentConfig> {
    
    @Select("SELECT * FROM payment_configs WHERE config_type = #{configType} ORDER BY id")
    List<PaymentConfig> selectByType(@Param("configType") String configType);
    
    @Select("SELECT * FROM payment_configs WHERE config_type = #{configType} AND config_key = #{configKey}")
    PaymentConfig selectByTypeAndKey(@Param("configType") String configType, @Param("configKey") String configKey);
    
    @Select("SELECT config_value FROM payment_configs WHERE config_type = #{configType} AND config_key = #{configKey} AND is_enabled = 1")
    String getValue(@Param("configType") String configType, @Param("configKey") String configKey);
    
    @Select("SELECT * FROM payment_configs WHERE is_enabled = 1")
    List<PaymentConfig> selectAllEnabled();
    
    @Update("UPDATE payment_configs SET config_value = #{value}, updated_at = NOW() WHERE config_type = #{type} AND config_key = #{key}")
    int updateValue(@Param("type") String type, @Param("key") String key, @Param("value") String value);
    
    @Update("UPDATE payment_configs SET is_enabled = #{enabled}, updated_at = NOW() WHERE config_type = #{type}")
    int updateEnabledByType(@Param("type") String type, @Param("enabled") Integer enabled);
}

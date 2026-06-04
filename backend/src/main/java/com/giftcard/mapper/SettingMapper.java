package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Setting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SettingMapper extends BaseMapper<Setting> {
    
    @Select("SELECT * FROM settings ORDER BY id")
    List<Setting> selectAll();
    
    @Select("SELECT * FROM settings WHERE setting_key = #{key}")
    Setting selectByKey(@Param("key") String key);
}

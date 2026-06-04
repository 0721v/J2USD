package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    @Select("SELECT * FROM categories WHERE status = 1 ORDER BY sort_order ASC")
    List<Category> selectActiveCategories();
}

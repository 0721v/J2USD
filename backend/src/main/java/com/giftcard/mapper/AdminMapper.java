package com.giftcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.giftcard.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    
    @Select("SELECT * FROM admins WHERE username = #{username} AND status = 1")
    Admin selectByUsername(@Param("username") String username);
    
    @Update("UPDATE admins SET last_login_at = #{loginTime}, last_login_ip = #{ip} " +
            "WHERE id = #{adminId}")
    int updateLoginInfo(@Param("adminId") Long adminId, 
                       @Param("loginTime") LocalDateTime loginTime,
                       @Param("ip") String ip);
    
    @Update("UPDATE admins SET password_hash = #{passwordHash}, updated_at = #{updatedAt} WHERE id = #{adminId}")
    int updatePassword(@Param("adminId") Long adminId, 
                      @Param("passwordHash") String passwordHash,
                      @Param("updatedAt") LocalDateTime updatedAt);
}

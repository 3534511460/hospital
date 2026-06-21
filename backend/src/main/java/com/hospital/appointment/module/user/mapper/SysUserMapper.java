package com.hospital.appointment.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.user.model.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    SysUser selectByPhone(String phone);
}

package com.hospital.appointment.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.user.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}

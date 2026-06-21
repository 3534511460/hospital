package com.hospital.appointment.module.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.appointment.model.NoShowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface NoShowRecordMapper extends BaseMapper<NoShowRecord> {

    @Select("SELECT COUNT(*) FROM no_show_record WHERE user_id = #{userId} AND no_show_date >= #{startDate}")
    int countRecentNoShows(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("SELECT MAX(blocked_until) FROM no_show_record WHERE user_id = #{userId} AND blocked_until >= CURDATE()")
    LocalDate getActiveBlockDate(@Param("userId") Long userId);
}

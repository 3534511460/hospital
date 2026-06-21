package com.hospital.appointment.module.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.hospital.model.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select("SELECT * FROM announcement WHERE publish_status = 1 ORDER BY is_top DESC, create_time DESC")
    List<Announcement> selectPublished();
}

package com.hospital.appointment.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.message.model.SiteMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SiteMessageMapper extends BaseMapper<SiteMessage> {

    @Select("SELECT * FROM site_message WHERE receiver_id = #{receiverId} ORDER BY create_time DESC")
    List<SiteMessage> findByReceiverId(@Param("receiverId") Long receiverId);

    @Select("SELECT COUNT(*) FROM site_message WHERE receiver_id = #{receiverId} AND is_read = 0")
    int countUnread(@Param("receiverId") Long receiverId);
}

package com.hospital.appointment.module.hospital.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Integer publishStatus;
    private Long publisherId;
    private LocalDateTime publishedAt;
    private Integer isTop;
    private Integer viewCount;
    private String ext1;
    private String ext2;
    private String ext3;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

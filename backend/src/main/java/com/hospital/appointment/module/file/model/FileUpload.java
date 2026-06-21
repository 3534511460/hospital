package com.hospital.appointment.module.file.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_upload")
public class FileUpload {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String originalName;
    private String storedName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private String fileExt;
    private String bizType;
    private String bizId;
    private String storageType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

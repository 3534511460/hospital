package com.hospital.appointment.module.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {
    Map<String, String> upload(MultipartFile file, Long userId, String bizType);
    List<Map<String, String>> uploadBatch(MultipartFile[] files, Long userId, String bizType);
}

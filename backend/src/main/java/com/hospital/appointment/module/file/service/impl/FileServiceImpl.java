package com.hospital.appointment.module.file.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.file.mapper.FileUploadMapper;
import com.hospital.appointment.module.file.model.FileUpload;
import com.hospital.appointment.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final FileUploadMapper fileUploadMapper;
    private final String uploadPath;
    private final Set<String> allowedExtensions;
    private final long maxSize;

    public FileServiceImpl(FileUploadMapper fileUploadMapper,
                           @Value("${app.file.upload-path}") String uploadPath,
                           @Value("${app.file.allowed-extensions}") String allowedExt,
                           @Value("${app.file.max-size}") long maxSize) {
        this.fileUploadMapper = fileUploadMapper;
        this.uploadPath = uploadPath;
        this.allowedExtensions = new HashSet<>(Arrays.asList(allowedExt.split(",")));
        this.maxSize = maxSize;
    }

    @Override
    public Map<String, String> upload(MultipartFile file, Long userId, String bizType) {
        validate(file);
        return doUpload(file, userId, bizType);
    }

    @Override
    public List<Map<String, String>> uploadBatch(MultipartFile[] files, Long userId, String bizType) {
        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            validate(file);
            results.add(doUpload(file, userId, bizType));
        }
        return results;
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw BusinessException.badRequest("文件不能为空");
        if (file.getSize() > maxSize) throw BusinessException.badRequest("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");

        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!allowedExtensions.contains(ext)) {
                throw BusinessException.badRequest("不支持的文件类型: " + ext);
            }
        }
    }

    private Map<String, String> doUpload(MultipartFile file, Long userId, String bizType) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String storedName = UUID.randomUUID().toString() + ext;

            Path dir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(dir);

            Path dest = dir.resolve(storedName);
            file.transferTo(dest.toFile());

            String relativePath = dateDir + "/" + storedName;

            FileUpload record = new FileUpload();
            record.setUserId(userId);
            record.setOriginalName(originalName);
            record.setStoredName(storedName);
            record.setFilePath(relativePath);
            record.setFileUrl("/api/v1/file/download/" + relativePath.replace("\\", "/"));
            record.setFileSize(file.getSize());
            record.setFileType(file.getContentType());
            record.setFileExt(ext.replace(".", ""));
            record.setBizType(bizType);
            record.setStorageType("LOCAL");
            fileUploadMapper.insert(record);

            Map<String, String> result = new HashMap<>();
            result.put("url", record.getFileUrl());
            result.put("originalName", originalName);
            result.put("id", record.getId().toString());
            return result;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }
}

package com.hospital.appointment.module.file.controller;

import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.file.service.FileService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public R<?> upload(@RequestParam("file") MultipartFile file,
                       @RequestParam(defaultValue = "COMMON") String bizType) {
        return R.ok(fileService.upload(file, UserContext.getUserId(), bizType));
    }

    @PostMapping("/upload-batch")
    public R<?> uploadBatch(@RequestParam("files") MultipartFile[] files,
                            @RequestParam(defaultValue = "COMMON") String bizType) {
        return R.ok(fileService.uploadBatch(files, UserContext.getUserId(), bizType));
    }
}

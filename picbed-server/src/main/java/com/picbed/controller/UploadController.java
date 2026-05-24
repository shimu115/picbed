package com.picbed.controller;

import com.picbed.dto.Result;
import com.picbed.dto.UploadSignatureRequest;
import com.picbed.entity.ImageInfo;
import com.picbed.service.ImageService;
import com.picbed.service.OssService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
public class UploadController {

    @Autowired
    private OssService ossService;

    @Autowired
    private ImageService imageService;

    @PostMapping("/api/upload/signature")
    public ResponseEntity<Result<Map<String, Object>>> getUploadSignature(
            @Valid @RequestBody UploadSignatureRequest request) {

        Optional<ImageInfo> existing = imageService.findByMd5Hash(request.getMd5());
        if (existing.isPresent()) {
            ImageInfo img = existing.get();
            log.info("Dedup hit: md5={}, existing image id={}", request.getMd5(), img.getId());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("exists", true);
            data.put("ossKey", img.getOssKey());
            data.put("accessUrl", img.getOssUrl());
            data.put("filename", img.getFilename());
            data.put("fileSize", img.getFileSize());
            return ResponseEntity.ok(Result.success(data));
        }

        Map<String, Object> sigData = ossService.generateUploadSignature(
                request.getFilename(), request.getContentType(), request.getMd5());
        sigData.put("exists", false);
        return ResponseEntity.ok(Result.success(sigData));
    }
}

package com.picbed.controller;

import com.picbed.dto.Result;
import com.picbed.dto.UploadSignatureRequest;
import com.picbed.dto.UploadSignatureResponse;
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
    public ResponseEntity<Result<UploadSignatureResponse>> getUploadSignature(
            @Valid @RequestBody UploadSignatureRequest request) {

        Optional<ImageInfo> existing = imageService.findByMd5Hash(request.getMd5());
        if (existing.isPresent()) {
            ImageInfo img = existing.get();
            log.info("Dedup hit: md5={}, existing image id={}", request.getMd5(), img.getId());
            UploadSignatureResponse data = new UploadSignatureResponse();
            data.setExists(true);
            data.setOssKey(img.getOssKey());
            data.setAccessUrl(img.getOssUrl());
            data.setFilename(img.getFilename());
            data.setFileSize(img.getFileSize());
            return ResponseEntity.ok(Result.success(data));
        }

        UploadSignatureResponse sigData = ossService.generateUploadSignature(
                request.getFilename(), request.getContentType(), request.getMd5());
        sigData.setExists(false);
        return ResponseEntity.ok(Result.success(sigData));
    }
}

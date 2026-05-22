package com.picbed.controller;

import com.picbed.dto.ApiResponse;
import com.picbed.dto.UploadSignatureRequest;
import com.picbed.service.OssService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadController {

    private final OssService ossService;

    public UploadController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/api/upload/signature")
    public ResponseEntity<ApiResponse<?>> getUploadSignature(
            @Valid @RequestBody UploadSignatureRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(ossService.generateUploadSignature(
                        request.getFilename(), request.getContentType())));
    }
}

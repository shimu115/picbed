package com.picbed.controller;

import com.picbed.dto.Result;
import com.picbed.dto.UploadSignatureRequest;
import com.picbed.service.OssService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private OssService ossService;

    @PostMapping("/api/upload/signature")
    public ResponseEntity<Result<Map<String, Object>>> getUploadSignature(
            @Valid @RequestBody UploadSignatureRequest request) {
        return ResponseEntity.ok(
                Result.success(ossService.generateUploadSignature(
                        request.getFilename(), request.getContentType())));
    }
}

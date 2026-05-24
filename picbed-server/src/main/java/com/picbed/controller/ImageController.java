package com.picbed.controller;

import com.picbed.dto.BatchDeleteRequest;
import com.picbed.dto.ImageSaveRequest;
import com.picbed.dto.Result;
import com.picbed.entity.ImageInfo;
import com.picbed.service.ImageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/api/public/images")
    public ResponseEntity<Result<Map<String, Object>>> listImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ImageInfo> result = imageService.listImages(page, size);
        return ResponseEntity.ok(Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        )));
    }

    @GetMapping("/api/public/images/{id}")
    public ResponseEntity<Result<ImageInfo>> getImage(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(imageService.getImage(id)));
    }

    @PostMapping("/api/admin/images")
    public ResponseEntity<Result<ImageInfo>> saveImage(
            @Valid @RequestBody ImageSaveRequest request) {
        return ResponseEntity.ok(Result.success(imageService.saveImage(request)));
    }

    @DeleteMapping("/api/admin/images/{id}")
    public ResponseEntity<Result<Void>> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(Result.success());
    }

    @DeleteMapping("/api/admin/images/batch")
    public ResponseEntity<Result<Map<String, Object>>> batchDelete(
            @Valid @RequestBody BatchDeleteRequest request) {
        int count = imageService.batchDelete(request.getIds());
        return ResponseEntity.ok(Result.success(Map.of("deletedCount", count)));
    }
}

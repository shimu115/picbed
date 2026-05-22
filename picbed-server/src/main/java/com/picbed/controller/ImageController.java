package com.picbed.controller;

import com.picbed.dto.ApiResponse;
import com.picbed.dto.BatchDeleteRequest;
import com.picbed.dto.ImageSaveRequest;
import com.picbed.entity.ImageInfo;
import com.picbed.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/api/public/images")
    public ResponseEntity<ApiResponse<?>> listImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ImageInfo> result = imageService.listImages(page, size);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        )));
    }

    @GetMapping("/api/public/images/{id}")
    public ResponseEntity<ApiResponse<?>> getImage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(imageService.getImage(id)));
    }

    @PostMapping("/api/admin/images")
    public ResponseEntity<ApiResponse<?>> saveImage(@Valid @RequestBody ImageSaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(imageService.saveImage(request)));
    }

    @DeleteMapping("/api/admin/images/{id}")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/api/admin/images/batch")
    public ResponseEntity<ApiResponse<?>> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        int count = imageService.batchDelete(request.getIds());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("deletedCount", count)));
    }
}

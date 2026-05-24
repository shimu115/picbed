package com.picbed.controller;

import com.picbed.dto.BatchDeleteRequest;
import com.picbed.dto.ImageSaveRequest;
import com.picbed.dto.Result;
import com.picbed.entity.ImageInfo;
import com.picbed.exception.ForbiddenException;
import com.picbed.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/api/admin/images")
    public ResponseEntity<Result<Map<String, Object>>> listManagedImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long tokenId = (Long) request.getAttribute("tokenId");
        String tokenRole = (String) request.getAttribute("tokenRole");
        Page<ImageInfo> result = imageService.listImagesByOwner(page, size, tokenId, tokenRole);
        return ResponseEntity.ok(Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        )));
    }

    @PostMapping("/api/admin/images")
    public ResponseEntity<Result<ImageInfo>> saveImage(
            @Valid @RequestBody ImageSaveRequest request,
            HttpServletRequest httpRequest) {
        Long tokenId = (Long) httpRequest.getAttribute("tokenId");
        return ResponseEntity.ok(Result.success(imageService.saveImage(request, tokenId)));
    }

    @DeleteMapping("/api/admin/images/{id}")
    public ResponseEntity<Result<Void>> deleteImage(@PathVariable Long id,
                                                     HttpServletRequest request) {
        checkOwnership(id, request);
        imageService.deleteImage(id);
        return ResponseEntity.ok(Result.success());
    }

    @DeleteMapping("/api/admin/images/batch")
    public ResponseEntity<Result<Map<String, Object>>> batchDelete(
            @Valid @RequestBody BatchDeleteRequest batchRequest,
            HttpServletRequest request) {
        List<Long> ids = batchRequest.getIds();
        for (Long id : ids) {
            checkOwnership(id, request);
        }
        int count = imageService.batchDelete(ids);
        return ResponseEntity.ok(Result.success(Map.of("deletedCount", count)));
    }

    private void checkOwnership(Long imageId, HttpServletRequest request) {
        String tokenRole = (String) request.getAttribute("tokenRole");
        if ("ADMIN".equalsIgnoreCase(tokenRole)) {
            return;
        }
        Long tokenId = (Long) request.getAttribute("tokenId");
        ImageInfo image = imageService.getImage(imageId);
        if (!tokenId.equals(image.getTokenId())) {
            log.warn("Ownership check failed: tokenId={} attempted to delete image id={} owned by tokenId={}",
                    tokenId, imageId, image.getTokenId());
            throw new ForbiddenException("You can only manage your own images");
        }
    }
}

package com.picbed.dto;

import com.picbed.entity.ImageInfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageDTO {
    private Long id;
    private String filename;
    private String ossKey;
    private String ossUrl;
    private String contentType;
    private String md5Hash;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private Long tokenId;
    private String uploadedBy;
    private Boolean isPublished;
    private LocalDateTime createdAt;

    public static ImageDTO from(ImageInfo img, String uploadedBy) {
        ImageDTO dto = new ImageDTO();
        dto.setId(img.getId());
        dto.setFilename(img.getFilename());
        dto.setOssKey(img.getOssKey());
        dto.setOssUrl(img.getOssUrl());
        dto.setContentType(img.getContentType());
        dto.setMd5Hash(img.getMd5Hash());
        dto.setFileSize(img.getFileSize());
        dto.setWidth(img.getWidth());
        dto.setHeight(img.getHeight());
        dto.setTokenId(img.getTokenId());
        dto.setUploadedBy(uploadedBy);
        dto.setIsPublished(img.getIsPublished());
        dto.setCreatedAt(img.getCreatedAt());
        return dto;
    }
}

package com.picbed.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Positive;

public class ImageSaveRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String ossKey;

    @NotBlank
    private String ossUrl;

    @NotBlank
    private String contentType;

    @NotNull
    @Positive
    private Long fileSize;

    private Integer width;
    private Integer height;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOssKey() { return ossKey; }
    public void setOssKey(String ossKey) { this.ossKey = ossKey; }

    public String getOssUrl() { return ossUrl; }
    public void setOssUrl(String ossUrl) { this.ossUrl = ossUrl; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
}

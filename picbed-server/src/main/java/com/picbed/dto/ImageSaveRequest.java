package com.picbed.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ImageSaveRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String ossKey;

    @NotBlank
    private String ossUrl;

    @NotBlank
    private String contentType;

    @NotBlank
    @Size(min = 32, max = 64)
    private String md5;

    @NotNull
    @Positive
    private Long fileSize;

    private Boolean published = false;

    private Integer width;
    private Integer height;
}

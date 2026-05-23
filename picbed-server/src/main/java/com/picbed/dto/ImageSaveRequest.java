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

    @NotNull
    @Positive
    private Long fileSize;

    private Integer width;
    private Integer height;
}

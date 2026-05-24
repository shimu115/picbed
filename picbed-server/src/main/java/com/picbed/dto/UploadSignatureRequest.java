package com.picbed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UploadSignatureRequest {

    @NotBlank
    @Size(max = 500)
    private String filename;

    @NotBlank
    @Size(max = 100)
    private String contentType;

    @NotBlank
    @Size(min = 32, max = 64)
    private String md5;
}

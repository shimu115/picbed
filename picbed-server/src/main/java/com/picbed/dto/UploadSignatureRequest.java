package com.picbed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UploadSignatureRequest {

    @NotBlank
    @Size(max = 500)
    private String filename;

    @NotBlank
    @Size(max = 100)
    private String contentType;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}

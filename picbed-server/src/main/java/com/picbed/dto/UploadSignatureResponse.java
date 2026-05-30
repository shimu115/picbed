package com.picbed.dto;

import lombok.Data;

@Data
public class UploadSignatureResponse {

    private Boolean exists;
    private String ossKey;
    private String accessUrl;
    private String filename;
    private Long fileSize;
    private String uploadUrl;
    private String expiresAt;
}

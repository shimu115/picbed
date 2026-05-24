package com.picbed.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.picbed.config.OssProperties;
import com.picbed.exception.OssOperationException;
import com.picbed.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OssService {

    @Autowired
    private OSS ossClient;
    @Autowired
    private OssProperties ossProperties;

    public Map<String, Object> generateUploadSignature(String filename, String contentType, String md5) {
        if (!OssUtil.ImageType.isAllowedContentType(contentType)) {
            log.warn("Upload signature rejected: unsupported content type '{}' for '{}'", contentType, filename);
            throw new IllegalArgumentException(
                    "Unsupported content type: " + contentType
                    + ". Allowed: " + OssUtil.ImageType.allowedContentTypes());
        }

        String ossKey = OssUtil.generateOssKey(md5, filename);

        Date expiration = new Date(System.currentTimeMillis() + Duration.ofMinutes(5).toMillis());
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(ossProperties.getBucketName(), ossKey);
        req.setMethod(com.aliyun.oss.HttpMethod.PUT);
        req.setExpiration(expiration);
        req.setContentType(contentType);

        URL signedUrl = ossClient.generatePresignedUrl(req);
        String accessUrl = OssUtil.getPublicUrl(
                ossProperties.getBucketName(), ossProperties.getEndpoint(),
                ossProperties.getCustomDomain(), ossKey);

        log.info("Generated upload signature for '{}' -> ossKey={}", filename, ossKey);

        Map<String, Object> result = new HashMap<>();
        result.put("ossKey", ossKey);
        result.put("uploadUrl", signedUrl.toString());
        result.put("accessUrl", accessUrl);
        result.put("expiresAt", expiration.toInstant().toString());
        return result;
    }

    public void deleteObject(String ossKey) {
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), ossKey);
            log.info("Deleted OSS object: {}", ossKey);
        } catch (Exception e) {
            log.error("Failed to delete OSS object: {}", ossKey, e);
            throw new OssOperationException("Failed to delete OSS object: " + ossKey, e);
        }
    }
}

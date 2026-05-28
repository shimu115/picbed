package com.picbed.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OssProperties {

    private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String customDomain;
}

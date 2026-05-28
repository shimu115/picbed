package com.picbed.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    @Autowired
    private OssProperties ossProperties;

    @Bean
    public OSS ossClient() {
        String endpoint = ossProperties.getEndpoint();
        if (!endpoint.startsWith("http")) {
            endpoint = "https://" + endpoint;
        }
        return new OSSClientBuilder().build(
                endpoint,
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
    }
}

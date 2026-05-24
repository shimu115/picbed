package com.picbed.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "images")
public class ImageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String filename;

    @Column(name = "oss_key", nullable = false, unique = true, length = 500)
    private String ossKey;

    @Column(name = "oss_url", nullable = false, length = 2000)
    private String ossUrl;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "md5_hash", unique = true, length = 32)
    private String md5Hash;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

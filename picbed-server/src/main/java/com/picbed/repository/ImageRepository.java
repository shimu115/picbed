package com.picbed.repository;

import com.picbed.entity.ImageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageInfo, Long> {

    Optional<ImageInfo> findByOssKey(String ossKey);
}

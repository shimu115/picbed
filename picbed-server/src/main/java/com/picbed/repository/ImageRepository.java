package com.picbed.repository;

import com.picbed.entity.ImageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageInfo, Long> {

    Optional<ImageInfo> findByOssKey(String ossKey);

    Optional<ImageInfo> findByMd5Hash(String md5Hash);

    Page<ImageInfo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ImageInfo> findByIsPublishedOrderByCreatedAtDesc(Boolean isPublished, Pageable pageable);

    Page<ImageInfo> findByTokenIdOrderByCreatedAtDesc(Long tokenId, Pageable pageable);

    Page<ImageInfo> findByTokenIdAndIsPublishedOrderByCreatedAtDesc(Long tokenId, Boolean isPublished, Pageable pageable);

    // Filename search
    Page<ImageInfo> findByIsPublishedTrueAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(String filename, Pageable pageable);

    Page<ImageInfo> findByFilenameContainingIgnoreCaseOrderByCreatedAtDesc(String filename, Pageable pageable);

    Page<ImageInfo> findByIsPublishedAndFilenameContainingIgnoreCaseOrderByCreatedAtDesc(Boolean published, String filename, Pageable pageable);

    // TokenId-based search (for username lookup)
    Page<ImageInfo> findByTokenIdInAndIsPublishedTrueOrderByCreatedAtDesc(List<Long> tokenIds, Pageable pageable);

    Page<ImageInfo> findByTokenIdInOrderByCreatedAtDesc(List<Long> tokenIds, Pageable pageable);

    Page<ImageInfo> findByTokenIdInAndIsPublishedOrderByCreatedAtDesc(List<Long> tokenIds, Boolean published, Pageable pageable);
}

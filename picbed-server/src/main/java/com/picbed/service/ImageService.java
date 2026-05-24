package com.picbed.service;

import com.picbed.dto.ImageSaveRequest;
import com.picbed.entity.ImageInfo;
import com.picbed.exception.NotFoundException;
import com.picbed.exception.OssOperationException;
import com.picbed.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private OssService ossService;

    @Transactional
    public ImageInfo saveImage(ImageSaveRequest request, Long tokenId) {
        ImageInfo info = new ImageInfo();
        info.setFilename(request.getFilename());
        info.setOssKey(request.getOssKey());
        info.setOssUrl(request.getOssUrl());
        info.setContentType(request.getContentType());
        info.setMd5Hash(request.getMd5());
        info.setFileSize(request.getFileSize());
        info.setWidth(request.getWidth());
        info.setHeight(request.getHeight());
        info.setTokenId(tokenId);
        ImageInfo saved = imageRepository.save(info);
        log.info("Saved image '{}' (id={}, size={}, tokenId={})", saved.getFilename(), saved.getId(), saved.getFileSize(), tokenId);
        return saved;
    }

    public Page<ImageInfo> listImages(int page, int size) {
        return imageRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Page<ImageInfo> listImagesByOwner(int page, int size, Long tokenId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return imageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return imageRepository.findByTokenIdOrderByCreatedAtDesc(tokenId, PageRequest.of(page, size));
    }

    public java.util.Optional<ImageInfo> findByMd5Hash(String md5Hash) {
        return imageRepository.findByMd5Hash(md5Hash);
    }

    public ImageInfo getImage(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found: " + id));
    }

    @Transactional
    public void deleteImage(Long id) {
        ImageInfo info = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found: " + id));
        try {
            ossService.deleteObject(info.getOssKey());
        } catch (OssOperationException e) {
            log.error("Failed to delete OSS object for image '{}' (id={}, ossKey={})",
                    info.getFilename(), id, info.getOssKey(), e);
        }
        imageRepository.delete(info);
        log.info("Deleted image '{}' (id={})", info.getFilename(), id);
    }

    @Transactional
    public int batchDelete(List<Long> ids) {
        log.info("Batch deleting {} images", ids.size());
        int count = 0;
        for (Long id : ids) {
            try {
                deleteImage(id);
                count++;
            } catch (NotFoundException ignored) {
                log.warn("Skipped already-deleted image id={} in batch delete", id);
            }
        }
        log.info("Batch delete completed: {}/{} images deleted", count, ids.size());
        return count;
    }
}

package com.picbed.service;

import com.picbed.dto.ImageDTO;
import com.picbed.dto.ImageSaveRequest;
import com.picbed.entity.ImageInfo;
import com.picbed.entity.Token;
import com.picbed.exception.NotFoundException;
import com.picbed.exception.OssOperationException;
import com.picbed.repository.ImageRepository;
import com.picbed.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private TokenRepository tokenRepository;
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
        info.setIsPublished(request.getPublished() != null && request.getPublished());
        ImageInfo saved = imageRepository.save(info);
        log.info("Saved image '{}' (id={}, size={}, tokenId={}, published={})",
                saved.getFilename(), saved.getId(), saved.getFileSize(), tokenId, saved.getIsPublished());
        return saved;
    }

    public Page<ImageDTO> listPublishedImages(int page, int size) {
        Page<ImageInfo> result = imageRepository.findByIsPublishedOrderByCreatedAtDesc(true,
                PageRequest.of(page, size));
        return toDtoPage(result);
    }

    public Page<ImageDTO> listImagesByOwner(int page, int size, Long tokenId, String role, Boolean published) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ImageInfo> result;
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (published != null) {
                result = imageRepository.findByIsPublishedOrderByCreatedAtDesc(published, pageRequest);
            } else {
                result = imageRepository.findAllByOrderByCreatedAtDesc(pageRequest);
            }
        } else {
            if (published != null) {
                result = imageRepository.findByTokenIdAndIsPublishedOrderByCreatedAtDesc(tokenId, published, pageRequest);
            } else {
                result = imageRepository.findByTokenIdOrderByCreatedAtDesc(tokenId, pageRequest);
            }
        }
        return toDtoPage(result);
    }

    private Page<ImageDTO> toDtoPage(Page<ImageInfo> page) {
        List<ImageInfo> images = page.getContent();
        List<Long> tokenIds = images.stream()
                .map(ImageInfo::getTokenId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> nameMap = tokenRepository.findAllById(tokenIds).stream()
                .collect(Collectors.toMap(Token::getId, Token::getName));
        List<ImageDTO> dtos = images.stream()
                .map(img -> ImageDTO.from(img, nameMap.getOrDefault(img.getTokenId(), "")))
                .toList();
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
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

    @Transactional
    public int batchPublish(List<Long> ids, boolean published) {
        log.info("Batch {} {} images", published ? "publishing" : "unpublishing", ids.size());
        int count = 0;
        for (Long id : ids) {
            ImageInfo image = imageRepository.findById(id).orElse(null);
            if (image != null) {
                image.setIsPublished(published);
                imageRepository.save(image);
                count++;
            } else {
                log.warn("Skipped non-existent image id={} in batch publish", id);
            }
        }
        log.info("Batch publish completed: {}/{} images set published={}", count, ids.size(), published);
        return count;
    }
}

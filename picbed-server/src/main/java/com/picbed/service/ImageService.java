package com.picbed.service;

import com.picbed.dto.ImageSaveRequest;
import com.picbed.entity.ImageInfo;
import com.picbed.exception.NotFoundException;
import com.picbed.repository.ImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final OssService ossService;

    public ImageService(ImageRepository imageRepository, OssService ossService) {
        this.imageRepository = imageRepository;
        this.ossService = ossService;
    }

    @Transactional
    public ImageInfo saveImage(ImageSaveRequest request) {
        ImageInfo info = new ImageInfo();
        info.setFilename(request.getFilename());
        info.setOssKey(request.getOssKey());
        info.setOssUrl(request.getOssUrl());
        info.setContentType(request.getContentType());
        info.setFileSize(request.getFileSize());
        info.setWidth(request.getWidth());
        info.setHeight(request.getHeight());
        return imageRepository.save(info);
    }

    public Page<ImageInfo> listImages(int page, int size) {
        return imageRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public ImageInfo getImage(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found: " + id));
    }

    @Transactional
    public void deleteImage(Long id) {
        ImageInfo info = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found: " + id));
        ossService.deleteObject(info.getOssKey());
        imageRepository.delete(info);
    }

    @Transactional
    public int batchDelete(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            try {
                deleteImage(id);
                count++;
            } catch (NotFoundException ignored) {
                // skip already-deleted images
            }
        }
        return count;
    }
}

package com.picbed.service;

import com.picbed.dto.AppSettingsDTO;
import com.picbed.entity.AppSetting;
import com.picbed.repository.AppSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {

    @Autowired
    private AppSettingRepository settingRepository;

    private AppSetting getOrCreate() {
        return settingRepository.findById(1L).orElseGet(() -> {
            AppSetting s = new AppSetting();
            return settingRepository.save(s);
        });
    }

    public AppSettingsDTO getSettings() {
        AppSetting s = getOrCreate();
        AppSettingsDTO dto = new AppSettingsDTO();
        dto.setUploadSizeLimitEnabled(s.getUploadSizeLimitEnabled());
        dto.setUploadSizeLimitMb(s.getUploadSizeLimitMb());
        return dto;
    }

    @Transactional
    public AppSettingsDTO updateSettings(AppSettingsDTO dto) {
        AppSetting s = getOrCreate();
        s.setUploadSizeLimitEnabled(dto.getUploadSizeLimitEnabled());
        s.setUploadSizeLimitMb(dto.getUploadSizeLimitMb());
        settingRepository.save(s);
        return getSettings();
    }

    public boolean isSizeLimitEnabled() {
        AppSetting s = getOrCreate();
        return Boolean.TRUE.equals(s.getUploadSizeLimitEnabled());
    }

    public long getMaxSizeBytes() {
        AppSetting s = getOrCreate();
        return s.getUploadSizeLimitMb().longValue() * 1024 * 1024;
    }
}

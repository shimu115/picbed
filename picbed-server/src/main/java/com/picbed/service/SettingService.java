package com.picbed.service;

import com.picbed.config.QuartzConfig;
import com.picbed.dto.AppSettingsDTO;
import com.picbed.entity.AppSetting;
import com.picbed.repository.AppSettingRepository;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {

    private static final Logger log = LoggerFactory.getLogger(SettingService.class);

    @Autowired
    private AppSettingRepository settingRepository;

    @Autowired(required = false)
    private QuartzConfig quartzConfig;

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
        dto.setTokenRefreshCron(s.getTokenRefreshCron());
        return dto;
    }

    @Transactional
    public AppSettingsDTO updateSettings(AppSettingsDTO dto) {
        AppSetting s = getOrCreate();
        s.setUploadSizeLimitEnabled(dto.getUploadSizeLimitEnabled());
        s.setUploadSizeLimitMb(dto.getUploadSizeLimitMb());
        if (dto.getTokenRefreshCron() != null && !dto.getTokenRefreshCron().isBlank()) {
            s.setTokenRefreshCron(dto.getTokenRefreshCron());
        }
        settingRepository.save(s);

        if (dto.getTokenRefreshCron() != null && !dto.getTokenRefreshCron().isBlank()
                && quartzConfig != null) {
            try {
                quartzConfig.reschedule(dto.getTokenRefreshCron());
            } catch (SchedulerException e) {
                log.error("Failed to reschedule token refresh job", e);
            }
        }

        return getSettings();
    }

    public String getTokenRefreshCron() {
        AppSetting s = getOrCreate();
        String cron = s.getTokenRefreshCron();
        return (cron != null && !cron.isBlank()) ? cron : "0 0 2 */3 * ?";
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

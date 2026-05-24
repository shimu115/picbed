package com.picbed.controller;

import com.picbed.dto.AppSettingsDTO;
import com.picbed.dto.Result;
import com.picbed.service.SettingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping("/api/settings")
    public ResponseEntity<Result<AppSettingsDTO>> getPublicSettings() {
        return ResponseEntity.ok(Result.success(settingService.getSettings()));
    }

    @GetMapping("/api/admin/settings")
    public ResponseEntity<Result<AppSettingsDTO>> getSettings() {
        return ResponseEntity.ok(Result.success(settingService.getSettings()));
    }

    @PutMapping("/api/admin/settings")
    public ResponseEntity<Result<AppSettingsDTO>> updateSettings(@Valid @RequestBody AppSettingsDTO dto) {
        return ResponseEntity.ok(Result.success(settingService.updateSettings(dto)));
    }
}

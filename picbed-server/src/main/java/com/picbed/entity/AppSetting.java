package com.picbed.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "app_settings")
public class AppSetting {

    @Id
    private Long id = 1L;

    @Column(name = "upload_size_limit_enabled", nullable = false)
    private Boolean uploadSizeLimitEnabled = false;

    @Column(name = "upload_size_limit_mb", nullable = false)
    private Integer uploadSizeLimitMb = 50;

    @Column(name = "token_refresh_cron", length = 50)
    private String tokenRefreshCron = "0 0 2 */3 * ?";

    @Column(name = "token_auto_refresh_enabled", nullable = false)
    private Boolean tokenAutoRefreshEnabled = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

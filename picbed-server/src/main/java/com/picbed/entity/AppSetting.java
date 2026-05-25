package com.picbed.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getUploadSizeLimitEnabled() { return uploadSizeLimitEnabled; }
    public void setUploadSizeLimitEnabled(Boolean uploadSizeLimitEnabled) { this.uploadSizeLimitEnabled = uploadSizeLimitEnabled; }

    public Integer getUploadSizeLimitMb() { return uploadSizeLimitMb; }
    public void setUploadSizeLimitMb(Integer uploadSizeLimitMb) { this.uploadSizeLimitMb = uploadSizeLimitMb; }

    public String getTokenRefreshCron() { return tokenRefreshCron; }
    public void setTokenRefreshCron(String tokenRefreshCron) { this.tokenRefreshCron = tokenRefreshCron; }

    public Boolean getTokenAutoRefreshEnabled() { return tokenAutoRefreshEnabled; }
    public void setTokenAutoRefreshEnabled(Boolean tokenAutoRefreshEnabled) { this.tokenAutoRefreshEnabled = tokenAutoRefreshEnabled; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

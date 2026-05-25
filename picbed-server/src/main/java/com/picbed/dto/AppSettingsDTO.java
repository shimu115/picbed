package com.picbed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AppSettingsDTO {

    @NotNull
    private Boolean uploadSizeLimitEnabled;

    @NotNull
    @Positive
    private Integer uploadSizeLimitMb;

    private String tokenRefreshCron;

    @NotNull
    private Boolean tokenAutoRefreshEnabled;
}

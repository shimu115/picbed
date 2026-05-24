package com.picbed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchPublishRequest {

    @NotNull
    @Size(min = 1)
    private List<Long> ids;

    @NotNull
    private Boolean published;
}

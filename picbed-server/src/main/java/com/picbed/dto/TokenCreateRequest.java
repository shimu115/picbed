package com.picbed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TokenCreateRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    private String role;
}

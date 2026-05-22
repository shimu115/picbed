package com.picbed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TokenCreateRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

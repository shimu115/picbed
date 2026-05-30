package com.picbed.dto;

import lombok.Data;

@Data
public class SessionResponse {

    private Long id;
    private String name;
    private String role;
    private String email;
    private Boolean valid;
}

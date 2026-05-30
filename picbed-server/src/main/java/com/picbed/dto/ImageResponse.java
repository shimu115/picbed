package com.picbed.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImageResponse <T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
}

package com.picbed.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(200).data(data).build();
    }

    public static <T> Result<T> success() {
        return Result.<T>builder().code(200).build();
    }

    public static <T> Result<T> error(String msg, Integer code) {
        return Result.<T>builder().code(code).msg(msg).build();
    }

    public static <T> Result<T> error(String msg) {
        return Result.<T>builder().code(500).msg(msg).build();
    }
}

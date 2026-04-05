package com.finance.finance_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

//Generic uniform API response wrapper for all endpoints

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final String error;
    private final T data;

    @Builder.Default
    private final LocalDateTime timestamp=LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message,T data){
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String error){
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();

    }

}

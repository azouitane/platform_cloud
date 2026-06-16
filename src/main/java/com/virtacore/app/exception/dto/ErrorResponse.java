package com.virtacore.app.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private int status;
    private String message;
    private Map<String, String> errors;
    private String path;
    private LocalDateTime timestamp;

    public static ErrorResponse of(
            int status,
            String message,
            String path
    ) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse ofValidation(
            Map<String, String> errors,
            String path
    ) {
        return ErrorResponse.builder()
                .success(false)
                .status(400)
                .message("Validation error")
                .errors(errors)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
package com.ticketflow.common.dtos;

import java.time.Instant;
import java.util.List;

public record StandardErrorDTO(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDTO> fieldErrors
) {
    public StandardErrorDTO(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    public StandardErrorDTO(int status, String error, String message, String path, List<FieldErrorDTO> fieldErrors) {
        this(Instant.now(), status, error, message, path, fieldErrors != null ? fieldErrors : List.of());
    }
}

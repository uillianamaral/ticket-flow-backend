package com.ticketflow.common.dtos;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponseDTO<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDTO<T> from(Page<T> springPage) {
        return new PageResponseDTO<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.isFirst(),
            springPage.isLast()
        );
    }
}

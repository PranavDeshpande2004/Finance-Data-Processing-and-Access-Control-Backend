package com.finance.finance_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import org.springframework.data.domain.Page;

//Generic paginated response wrapper

@Getter
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PagedResponse<T>fromPage(Page<T>page){
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

    }
}

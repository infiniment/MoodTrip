package com.moodTrip.spring.global.common.util;

public record PageResult<T>(
        java.util.List<T> content, int page, int size, long totalElements, int totalPages
) {
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> p) {
        return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
    public static <T> PageResult<T> singlePage(java.util.List<T> list, int page, int size) {
        int total = list.size();
        int totalPages = Math.max(1, (int)Math.ceil(total / (double)size));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        return new PageResult<>(list, safePage, size, total, totalPages);
    }
}
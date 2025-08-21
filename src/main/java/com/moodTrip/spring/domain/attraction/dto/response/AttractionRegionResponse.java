package com.moodTrip.spring.domain.attraction.dto.response;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionRegionResponse {
    private List<AttractionResponse> list;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private boolean hasNext;

    public static AttractionRegionResponse of(Page<Attraction> p) {
        List<AttractionResponse> list = p.getContent()
                .stream().map(AttractionResponse::from).toList();
        return AttractionRegionResponse.builder()
                .list(list)
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .page(p.getNumber())
                .size(p.getSize())
                .build();
    }
}


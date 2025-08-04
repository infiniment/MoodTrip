package com.moodTrip.spring.domain.support.dto.response;

import com.moodTrip.spring.domain.support.entity.Faq;
import lombok.Builder;

@Builder
public record FaqResponse(
        Long id,
        String category,
        String title,
        String content,
        int views,
        int helpful,
        int notHelpful
) {
    public static FaqResponse from(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .title(faq.getTitle())
                .content(faq.getContent())
                .views(faq.getViews())
                .helpful(faq.getHelpful())
                .notHelpful(faq.getNotHelpful())
                .build();
    }
}

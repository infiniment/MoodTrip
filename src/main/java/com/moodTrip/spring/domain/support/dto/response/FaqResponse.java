package com.moodTrip.spring.domain.support.dto.response;

import lombok.Builder;
import com.moodTrip.spring.domain.admin.entity.Faq;

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
//    public static FaqResponse from(Faq faq) {
//        return FaqResponse.builder()
//                .id(faq.getId())
//                .category(faq.getCategory())
//                .title(faq.getTitle())
//                .content(faq.getContent())
//                .views(faq.getViews())
//                .helpful(faq.getHelpful())
//                .notHelpful(faq.getNotHelpful())
//                .build();
//    }
}

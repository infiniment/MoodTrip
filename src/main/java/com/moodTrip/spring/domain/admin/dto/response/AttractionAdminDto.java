package com.moodTrip.spring.domain.admin.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AttractionAdminDto {
    private Long attractionId;
    private Long contentId;
    private String title;
    private String addr1;
    private String categoryName;
    private String emotionTags;
    private LocalDateTime createdTime;
    private String status = "공개";
    private String statusClass = "status active";
}
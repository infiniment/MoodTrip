package com.moodTrip.spring.domain.support.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {
    private Long id;  // noticeId 대신 id 사용
    private String title;
    private String content;
    private String classification;  // 추가
    private Boolean isVisible;
    private Boolean isImportant;
    private LocalDateTime registeredDate;
    private Integer viewCount;  // 추가
}
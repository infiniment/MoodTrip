package com.moodTrip.spring.domain.support.dto.response;

import com.moodTrip.spring.domain.admin.entity.Attachment;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NotificationResponse {
    private Long id;  // noticeId 대신 id 사용
    private String title;
    private String content;
    private String classification;
    private Boolean isVisible;
    private Boolean isImportant;
    private LocalDateTime registeredDate;
    private Integer viewCount;
    private List<Attachment> attachments;
}
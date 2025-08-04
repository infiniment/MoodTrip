package com.moodTrip.spring.domain.admin.dto.response;
import com.moodTrip.spring.domain.admin.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long noticeId;
    private String title;
    private String content;
    private String classification;
    private Boolean isImportant;
    private Boolean isVisible;
    private List<Attachment> attachments;
}

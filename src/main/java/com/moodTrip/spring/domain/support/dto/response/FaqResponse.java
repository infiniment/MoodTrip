package com.moodTrip.spring.domain.support.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FaqResponse {
    private Long id;
    private String category;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer helpful;
    private Integer notHelpful;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}


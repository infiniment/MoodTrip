package com.moodTrip.spring.domain.admin.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatchingDto {
    private Long roomId;
    private String roomName;
    private String creatorNickname;
    private int currentCount;
    private int maxCount;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;

    private int travelPeriod;       // 여행 기간 (며칠인지)
    private String regionName;      // 광역단위 지역명
    private String attractionTitle; // 관광지 이름
    private String status;          // 진행중 / 완료 / 취소

    private LocalDateTime createdAt;   // 생성일
    private List<String> participants; // 참여자 목록
}



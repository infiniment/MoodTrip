package com.moodTrip.spring.domain.rooms.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방 생성 요청 DTO")
public class RoomRequest {

    @NotNull
    @Schema(description = "여행 목적지 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    private DestinationDto destination;

    @Size(min = 1)
    @Schema(description = "선택된 감정 리스트", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<EmotionDto> emotions;

    @NotNull
    @Schema(description = "여행 일정 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    private ScheduleDto schedule;

    @Min(1)
    @Schema(description = "최대 인원 수", example = "4")
    private int maxParticipants;

    @NotBlank
    @Schema(description = "방 이름", example = "힐링 여행 메이트")
    private String roomName;

    @NotBlank
    @Schema(description = "방 설명", example = "혼자 여행하는 사람들과 함께 떠나는 힐링 여행")
    private String roomDescription;

    @Schema(description = "요청 버전 (선택 사항)", example = "v1")
    private String version;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "목적지 정보 DTO")
    public static class DestinationDto {

        @Schema(description = "목적지 카테고리 (예: 지역)", example = "강원도")
        private String category;

        @Schema(description = "목적지 이름 (예: 장소명)", example = "속초 해수욕장")
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "감정 DTO")
    public static class EmotionDto {

        @Schema(description = "감정 ID", example = "1")
        private Long tagId;

        @Schema(description = "감정 텍스트", example = "평온")
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "일정 정보 DTO")
    public static class ScheduleDto {

        @Schema(description = "여행 날짜 범위 리스트")
        private List<DateRangeDto> dateRanges;

        @Schema(description = "총 여행 일수", example = "3")
        private int totalDays;

        @Schema(description = "여행 날짜 범위 수", example = "1")
        private int rangeCount;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "여행 날짜 범위 DTO")
        public static class DateRangeDto {

            @Schema(description = "시작 날짜 (ISO 8601)", example = "2025-08-01T09:00:00.000Z")
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            private LocalDateTime startDate;

            @Schema(description = "종료 날짜 (ISO 8601)", example = "2025-08-03T18:00:00.000Z")
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            private LocalDateTime endDate;

            @Schema(description = "시작 날짜(문자열)", example = "2025-08-01")
            private String startDateFormatted;

            @Schema(description = "종료 날짜(문자열)", example = "2025-08-03")
            private String endDateFormatted;
        }
    }
}

package com.moodTrip.spring.domain.rooms.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {

    @NotNull
    private DestinationDto destination;

    @Size(min = 1)
    private List<EmotionDto> emotions;

    @NotNull
    private ScheduleDto schedule;

    @Min(1)
    private int maxParticipants;

    @NotBlank
    private String roomName;

    @NotBlank
    private String roomDescription;

    private String version;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DestinationDto {
        private String category; // 예: 지역명 나중에 지역 코드로 받아야됨
        private String name;     // 예: 장소명
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmotionDto {
        private Long id;
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleDto {

        private List<DateRangeDto> dateRanges;

        private int totalDays;

        private int rangeCount;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DateRangeDto {

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            private LocalDateTime startDate;

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            private LocalDateTime endDate;

            private String startDateFormatted;
            private String endDateFormatted;
        }
    }
}
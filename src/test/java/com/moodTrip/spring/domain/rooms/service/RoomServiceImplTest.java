package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RoomServiceImplTest {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    private RoomRequest baseRoomRequest;
    private RoomRequest baseRoomRequest2;

    private Member mockMember;

    private Member createTestMember(String idSuffix) {
        return memberRepository.save(Member.builder()
                .memberId("testUser" + idSuffix)
                .memberPw("pass" + idSuffix)
                .memberPhone("010-0000-000" + idSuffix)
                .nickname("테스터" + idSuffix)
                .memberAuth("U")
                .isWithdraw(false)
                .email("test" + idSuffix + "@example.com")
                .rptCnt(0L)
                .rptRcvdCnt(0L)
                .build());
    }

    @BeforeEach
    void setUp() {
        mockMember = createTestMember("01");

        // DB에 저장하고, 저장된 PK를 반영
        memberRepository.save(mockMember);

        baseRoomRequest = RoomRequest.builder()
                .roomName("기본 방 제목")
                .roomDescription("기본 방 설명")
                .maxParticipants(5)
                .destination(RoomRequest.DestinationDto.builder()
                        .category("강원도")
                        .name("속초 해수욕장")
                        .build())
                .emotions(List.of(
                        RoomRequest.EmotionDto.builder()
                                .tagId(1L)
                                .text("평온")
                                .build()))
                .schedule(RoomRequest.ScheduleDto.builder()
                        .dateRanges(List.of(
                                RoomRequest.ScheduleDto.DateRangeDto.builder()
                                        .startDate(OffsetDateTime.of(2025, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                                        .endDate(OffsetDateTime.of(2025, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC))
                                        .startDateFormatted("2025-08-01")
                                        .endDateFormatted("2025-08-03")
                                        .build()
                        ))
                        .rangeCount(1)
                        .totalDays(3)
                        .build())
                .version("v1")
                .build();

        baseRoomRequest2 = RoomRequest.builder()
                .roomName("기본 방 제목2")
                .roomDescription("기본 방 설명2")
                .maxParticipants(3)
                .destination(RoomRequest.DestinationDto.builder()
                        .category("서울")
                        .name("경복궁")
                        .build())
                .emotions(List.of(
                        RoomRequest.EmotionDto.builder()
                                .tagId(2L)
                                .text("즐거움")
                                .build()))
                .schedule(RoomRequest.ScheduleDto.builder()
                        .dateRanges(List.of(
                                RoomRequest.ScheduleDto.DateRangeDto.builder()
                                        .startDate(OffsetDateTime.of(2025, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                                        .endDate(OffsetDateTime.of(2025, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC))
                                        .startDateFormatted("2025-08-02")
                                        .endDateFormatted("2025-08-06")
                                        .build()
                        ))
                        .rangeCount(1)
                        .totalDays(5)
                        .build())
                .version("v1")
                .build();
    }

    @Test
    @DisplayName("방 생성 테스트 : 정상적인 요청으로 방이 생성되어야 한다")
    void createRoom() {
        // given
        // when
        RoomResponse roomResponse = roomService.createRoom(baseRoomRequest, mockMember.getMemberPk());
        // then
        assertThat(roomResponse).isNotNull();
        assertThat(roomResponse.getRoomName()).isEqualTo("기본 방 제목");
        assertThat(roomResponse.getRoomDescription()).contains("기본 방 설명");
        assertThat(roomResponse.getMaxParticipants()).isEqualTo(5);

    }

    @Test
    @DisplayName("방 단건 조회 테스트 : 존재하는 roomId로 조회")
    void getRoomById_success() {
        // given
        RoomResponse created = roomService.createRoom(baseRoomRequest, mockMember.getMemberPk());

        // when
        RoomResponse response = roomService.getRoomById(created.getRoomId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomName()).isEqualTo("기본 방 제목");
        assertThat(response.getRoomDescription()).isEqualTo("기본 방 설명");
        assertThat(response.getMaxParticipants()).isEqualTo(5);
    }

    @Test
    @DisplayName("방 단건 조회 테스트 : 존재하지 않는 roomId로 조회하면 예외 발생")
    void getRoomById_fail() {
        // given
        Long notExistRoomId = 99999L;

        // when & then
        assertThatThrownBy(() -> roomService.getRoomById(notExistRoomId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 방입니다.");
    }

    @Test
    @DisplayName("방 전체 조회 테스트 : 저장한 방 리스트 확인")
    void getAllRooms() {
        // given
        roomService.createRoom(baseRoomRequest, mockMember.getMemberPk());
        roomService.createRoom(baseRoomRequest2, mockMember.getMemberPk());

        // when
        List<RoomResponse> rooms = roomService.getAllRooms();

        // then
        assertThat(rooms).isNotNull();
        assertThat(rooms.size()).isGreaterThanOrEqualTo(2);
        assertThat(rooms).extracting(RoomResponse::getRoomName)
                .contains("기본 방 제목", "기본 방 제목2");
    }

    @Test
    @DisplayName("방 삭제 테스트 : soft delete로 isDeleteRoom = true 설정")
    void deleteRoomById() {
        // given
        RoomResponse created = roomService.createRoom(baseRoomRequest, mockMember.getMemberPk());

        // when
        roomService.deleteRoomById(created.getRoomId());

        // then
        Room room = roomRepository.findById(created.getRoomId())
                .orElseThrow(() -> new RuntimeException("room not found"));

        assertThat(room.getIsDeleteRoom()).isTrue();
    }

    @Test
    @DisplayName("방 수정 테스트 : 기존 방 정보 수정")
    void updateRoom() {
        // given
        RoomResponse created = roomService.createRoom(baseRoomRequest, mockMember.getMemberPk());

        UpdateRoomRequest updateRequest = UpdateRoomRequest.builder()
                .roomName("수정된 제목")
                .roomDescription("수정된 설명")
                .maxParticipants(10)
                .build();

        // when
        RoomResponse updated = roomService.updateRoom(created.getRoomId(), updateRequest);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getRoomName()).isEqualTo("수정된 제목");
        assertThat(updated.getRoomDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getMaxParticipants()).isEqualTo(10);
    }
}
package com.moodTrip.spring.domain.mainpage.service;

import com.moodTrip.spring.domain.mainpage.dto.response.MainPageRoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MainPageServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private MainPageService mainPageService;

    private Room room1;
    private Room room2;
    private Room room3;

    @BeforeEach
    void setUp() {
        room1 = Room.builder()
                .roomId(1L)
                .roomName("방1")
                .viewCount(50)
                .isDeleteRoom(false)
                .build();
        ReflectionTestUtils.setField(room1, "createdAt", LocalDateTime.now().minusDays(1));

        room2 = Room.builder()
                .roomId(2L)
                .roomName("방2")
                .viewCount(100)
                .isDeleteRoom(false)
                .build();
        ReflectionTestUtils.setField(room2, "createdAt", LocalDateTime.now().minusDays(2));

        room3 = Room.builder()
                .roomId(3L)
                .roomName("방3")
                .viewCount(30)
                .isDeleteRoom(false)
                .build();
        ReflectionTestUtils.setField(room3, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("인기 방 6개 조회 - 정상 반환")
    void getPopularRooms_Success() {
        // given
        given(roomRepository.findTop6ByIsDeleteRoomFalseOrderByViewCountDescCreatedAtDesc())
                .willReturn(List.of(room1, room2, room3));

        // when
        List<MainPageRoomResponse> result = mainPageService.getPopularRooms();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRoomName()).isEqualTo("방1"); // DTO 변환 확인
    }

    @Test
    @DisplayName("인기 방 조회 중 예외 발생 시 빈 리스트 반환")
    void getPopularRooms_Exception() {
        // given
        given(roomRepository.findTop6ByIsDeleteRoomFalseOrderByViewCountDescCreatedAtDesc())
                .willThrow(new RuntimeException("DB 오류"));

        // when
        List<MainPageRoomResponse> result = mainPageService.getPopularRooms();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("최신 방 6개 조회 - 최신순 정렬 확인")
    void getLatestRooms_Success() {
        // given
        given(roomRepository.findByIsDeleteRoomFalse())
                .willReturn(List.of(room1, room2, room3));

        // when
        List<MainPageRoomResponse> result = mainPageService.getLatestRooms();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRoomName()).isEqualTo("방3"); // 가장 최신 createdAt
        assertThat(result.get(1).getRoomName()).isEqualTo("방1");
        assertThat(result.get(2).getRoomName()).isEqualTo("방2");
    }

    @Test
    @DisplayName("최신 방 조회 중 예외 발생 시 빈 리스트 반환")
    void getLatestRooms_Exception() {
        // given
        given(roomRepository.findByIsDeleteRoomFalse())
                .willThrow(new RuntimeException("DB 오류"));

        // when
        List<MainPageRoomResponse> result = mainPageService.getLatestRooms();

        // then
        assertThat(result).isEmpty();
    }
}

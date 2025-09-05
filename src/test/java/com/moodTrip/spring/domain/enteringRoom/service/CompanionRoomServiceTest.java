package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionRoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @InjectMocks
    private CompanionRoomService companionRoomService;

    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        room1 = Room.builder()
                .roomId(1L)
                .roomName("서울 여행 방")
                .roomDescription("서울 투어")
                .destinationCategory("서울특별시")
                .destinationName("서울")
                .roomMaxCount(4)
                .roomCurrentCount(2)
                .isDeleteRoom(false)
                .viewCount(10)
                .build();
        ReflectionTestUtils.setField(room1, "travelStartDate", LocalDate.now().plusDays(5));

        room2 = Room.builder()
                .roomId(2L)
                .roomName("부산 바다 방")
                .roomDescription("부산 해운대 투어")
                .destinationCategory("부산광역시")
                .destinationName("부산")
                .roomMaxCount(6)
                .roomCurrentCount(5)
                .isDeleteRoom(false)
                .viewCount(3)
                .build();
        ReflectionTestUtils.setField(room2, "travelStartDate", LocalDate.now().plusDays(10));
    }

    @Test
    @DisplayName("전체 방 목록 조회 - 성공")
    void getAllRooms_Success() {
        // given
        when(roomRepository.findByIsDeleteRoomFalse()).thenReturn(List.of(room1, room2));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        List<CompanionRoomListResponse> result = companionRoomService.getAllRooms();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("서울 여행 방");
        assertThat(result.get(1).getTitle()).isEqualTo("부산 바다 방");
    }

    @Test
    @DisplayName("방 검색 - 키워드로 필터링")
    void searchRooms_ByKeyword() {
        // given
        when(roomRepository.findByIsDeleteRoomFalse()).thenReturn(List.of(room1, room2));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        List<CompanionRoomListResponse> result = companionRoomService.searchRooms("서울");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("서울");
    }

    @Test
    @DisplayName("방 검색 - 키워드 없으면 전체 반환")
    void searchRooms_EmptyKeyword() {
        // given
        when(roomRepository.findByIsDeleteRoomFalse()).thenReturn(List.of(room1, room2));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        List<CompanionRoomListResponse> result = companionRoomService.searchRooms("");

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("지역별 방 조회 - 서울 필터")
    void getRoomsByRegion_Seoul() {
        // given
        when(roomRepository.findByIsDeleteRoomFalse()).thenReturn(List.of(room1, room2));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        List<CompanionRoomListResponse> result = companionRoomService.getRoomsByRegion("서울");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocation()).isEqualTo("서울");
    }

    @Test
    @DisplayName("최대 인원별 방 조회 - 4인 방만 필터")
    void getRoomsByMaxParticipants_Four() {
        // given
        when(roomRepository.findByIsDeleteRoomFalse()).thenReturn(List.of(room1, room2));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        List<CompanionRoomListResponse> result = companionRoomService.getRoomsByMaxParticipants("4");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaxParticipants()).isEqualTo(4);
    }

    @Test
    @DisplayName("방 상세 조회 - 조회수 증가 확인")
    void getRoomDetailWithViewCount_Success() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(any(Room.class))).thenReturn(2L);

        // when
        CompanionRoomListResponse response = companionRoomService.getRoomDetailWithViewCount(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getViewCount()).isGreaterThan(10); // 조회수 1 증가 확인
    }

    @Test
    @DisplayName("방 상세 조회 - 존재하지 않는 방 예외")
    void getRoomDetailWithViewCount_NotFound() {
        // given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companionRoomService.getRoomDetailWithViewCount(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("방 상세 조회 중 오류가 발생했습니다."); // ✅ 수정
    }
}

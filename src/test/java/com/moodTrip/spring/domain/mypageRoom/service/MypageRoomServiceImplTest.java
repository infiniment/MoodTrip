package com.moodTrip.spring.domain.mypageRoom.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 임시로 lenient 모드 사용
class MypageRoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private MypageRoomServiceImpl mypageRoomService;

    private Member testMember;
    private Member otherMember;
    private Room testRoom;
    private Room deletedRoom;
    private RoomMember leaderRoomMember;
    private RoomMember memberRoomMember;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        // 테스트용 Member 객체 생성
        testMember = Member.builder()
                .memberPk(1L)
                .memberId("testUser")
                .nickname("테스트유저")
                .memberPhone("010-1234-5678")
                .email("test@example.com")
                .memberAuth("U")
                .isWithdraw(false)
                .build();

        otherMember = Member.builder()
                .memberPk(2L)
                .memberId("otherUser")
                .nickname("다른유저")
                .memberPhone("010-9876-5432")
                .email("other@example.com")
                .memberAuth("U")
                .isWithdraw(false)
                .build();

        // Mock Room 객체 생성 (Mockito 사용)
        testRoom = Mockito.mock(Room.class);
        when(testRoom.getRoomId()).thenReturn(1L);
        when(testRoom.getRoomName()).thenReturn("서울 여행 방");
        when(testRoom.getRoomDescription()).thenReturn("서울 맛집 투어");
        when(testRoom.getRoomMaxCount()).thenReturn(4);
        when(testRoom.getRoomCurrentCount()).thenReturn(3);
        when(testRoom.getTravelStartDate()).thenReturn(LocalDate.now().plusDays(7));
        when(testRoom.getTravelEndDate()).thenReturn(LocalDate.now().plusDays(10));
        when(testRoom.getDestinationCategory()).thenReturn("관광지");
        when(testRoom.getDestinationName()).thenReturn("서울");
        when(testRoom.getCreator()).thenReturn(testMember);
        when(testRoom.getIsDeleteRoom()).thenReturn(false);
        when(testRoom.getViewCount()).thenReturn(10);
        when(testRoom.getEmotionRooms()).thenReturn(new ArrayList<>()); // 핵심: 빈 리스트 반환
        when(testRoom.getAttraction()).thenReturn(null); // attraction이 null이면 기본 이미지 사용
        when(testRoom.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(5));

        // Mock 삭제된 방
        deletedRoom = Mockito.mock(Room.class);
        when(deletedRoom.getRoomId()).thenReturn(2L);
        when(deletedRoom.getRoomName()).thenReturn("부산 여행 방");
        when(deletedRoom.getRoomDescription()).thenReturn("부산 바다 투어");
        when(deletedRoom.getIsDeleteRoom()).thenReturn(true);
        when(deletedRoom.getEmotionRooms()).thenReturn(new ArrayList<>());
        when(deletedRoom.getAttraction()).thenReturn(null);

        // RoomMember 객체들 생성
        memberRoomMember = RoomMember.builder()
                .memberRoomId(2L)
                .member(testMember)
                .room(testRoom)
                .joinedAt(LocalDateTime.now().minusDays(3))
                .role("MEMBER")
                .isActive(true)
                .build();

        leaderRoomMember = RoomMember.builder()
                .memberRoomId(1L)
                .member(testMember)
                .room(testRoom)
                .joinedAt(LocalDateTime.now().minusDays(5))
                .role("LEADER")
                .isActive(true)
                .build();

        // 테스트용 Schedule
        testSchedule = new Schedule();
        // Schedule 객체 초기화 (실제 필드에 맞게 수정)
    }

    @Test
    @DisplayName("내가 참여한 방 목록 조회 - 성공 (삭제되지 않은 방만)")
    void getMyJoinedRooms_Success_OnlyActiveRooms() {
        // Given
        RoomMember deletedRoomMember = RoomMember.builder()
                .memberRoomId(3L)
                .member(testMember)
                .room(deletedRoom)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .role("MEMBER")
                .isActive(true)
                .build();

        List<RoomMember> roomMembers = Arrays.asList(memberRoomMember, deletedRoomMember);
        when(roomMemberRepository.findByMemberAndIsActiveTrue(testMember)).thenReturn(roomMembers);

        // When
        List<JoinedRoomResponse> result = mypageRoomService.getMyJoinedRooms(testMember);

        // Then
        assertThat(result).hasSize(1); // 삭제되지 않은 방만 반환
        assertThat(result.get(0).getRoomId()).isEqualTo(1L);
        assertThat(result.get(0).getRoomName()).isEqualTo("서울 여행 방");

        verify(roomMemberRepository, times(1)).findByMemberAndIsActiveTrue(testMember);
    }

    @Test
    @DisplayName("내가 참여한 방 목록 조회 - 빈 결과")
    void getMyJoinedRooms_EmptyResult() {
        // Given
        when(roomMemberRepository.findByMemberAndIsActiveTrue(testMember)).thenReturn(Arrays.asList());

        // When
        List<JoinedRoomResponse> result = mypageRoomService.getMyJoinedRooms(testMember);

        // Then
        assertThat(result).isEmpty();
        verify(roomMemberRepository, times(1)).findByMemberAndIsActiveTrue(testMember);
    }

    @Test
    @DisplayName("내가 만든 방 목록 조회 - 성공 (최신순 정렬)")
    void getMyCreatedRooms_Success_SortedByLatest() {
        // Given
        List<Room> createdRooms = Arrays.asList(testRoom);
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(testMember)).thenReturn(createdRooms);

        // When
        List<CreatedRoomResponse> result = mypageRoomService.getMyCreatedRooms(testMember);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomId()).isEqualTo(1L);
        assertThat(result.get(0).getRoomName()).isEqualTo("서울 여행 방");

        verify(roomRepository, times(1)).findByCreatorAndIsDeleteRoomFalse(testMember);
    }

    @Test
    @DisplayName("방 삭제 - 성공 (방장이 삭제, 스케줄도 함께 삭제)")
    void deleteRoom_Success_WithSchedules() throws AccessDeniedException {
        // Given
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.of(leaderRoomMember));
        when(scheduleRepository.findByRoom(testRoom)).thenReturn(schedules);

        // When
        mypageRoomService.deleteRoom(1L, testMember);

        // Then
        verify(roomRepository, times(1)).findById(1L);
        verify(roomMemberRepository, times(1)).findByMemberAndRoom(testMember, testRoom);
        verify(scheduleRepository, times(1)).findByRoom(testRoom);
        verify(scheduleRepository, times(1)).deleteAll(schedules);
        verify(testRoom, times(1)).setIsDeleteRoom(true);
        verify(roomRepository, times(1)).save(testRoom);
    }

    @Test
    @DisplayName("방 삭제 - 성공 (스케줄 없는 경우)")
    void deleteRoom_Success_NoSchedules() throws AccessDeniedException {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.of(leaderRoomMember));
        when(scheduleRepository.findByRoom(testRoom)).thenReturn(Arrays.asList());

        // When
        mypageRoomService.deleteRoom(1L, testMember);

        // Then
        verify(scheduleRepository, never()).deleteAll(any());
        verify(testRoom, times(1)).setIsDeleteRoom(true);
        verify(roomRepository, times(1)).save(testRoom);
    }

    @Test
    @DisplayName("방 삭제 - 실패 (존재하지 않는 방)")
    void deleteRoom_Fail_RoomNotFound() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mypageRoomService.deleteRoom(999L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("해당 방이 존재하지 않습니다.");
        verify(roomRepository, times(1)).findById(999L);
        verify(roomMemberRepository, never()).findByMemberAndRoom(any(), any());
    }

    @Test
    @DisplayName("방 삭제 - 실패 (방에 참여하지 않은 사용자)")
    void deleteRoom_Fail_NotParticipant() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(otherMember, testRoom)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mypageRoomService.deleteRoom(1L, otherMember);
        });

        assertThat(exception.getMessage()).isEqualTo("이 방에 참여하지 않은 사용자입니다.");
        verify(roomRepository, times(1)).findById(1L);
        verify(roomMemberRepository, times(1)).findByMemberAndRoom(otherMember, testRoom);
    }

    @Test
    @DisplayName("방 삭제 - 실패 (방장이 아닌 사용자)")
    void deleteRoom_Fail_NotLeader() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.of(memberRoomMember));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            mypageRoomService.deleteRoom(1L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("방장만 방을 삭제할 수 있습니다.");
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("방 나가기 - 성공 (인원 수 업데이트)")
    void leaveRoom_Success_UpdateCurrentCount() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.of(memberRoomMember));
        when(roomMemberRepository.countByRoomAndIsActiveTrue(testRoom)).thenReturn(2L); // 3명에서 2명으로

        // When
        mypageRoomService.leaveRoom(1L, testMember);

        // Then
        assertThat(memberRoomMember.getIsActive()).isFalse();
        verify(roomRepository, times(1)).findById(1L);
        verify(roomMemberRepository, times(1)).findByMemberAndRoom(testMember, testRoom);
        verify(roomMemberRepository, times(1)).save(memberRoomMember);
        verify(roomMemberRepository, times(1)).countByRoomAndIsActiveTrue(testRoom);
        verify(testRoom, times(1)).setRoomCurrentCount(2);
        verify(roomRepository, times(1)).save(testRoom);
    }

    @Test
    @DisplayName("방 나가기 - 실패 (존재하지 않는 방)")
    void leaveRoom_Fail_RoomNotFound() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mypageRoomService.leaveRoom(999L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("해당 방이 존재하지 않습니다.");
        verify(roomRepository, times(1)).findById(999L);
        verify(roomMemberRepository, never()).findByMemberAndRoom(any(), any());
    }

    @Test
    @DisplayName("방 나가기 - 실패 (참여하지 않은 방)")
    void leaveRoom_Fail_NotParticipant() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mypageRoomService.leaveRoom(1L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("이 방에 참여하지 않은 사용자입니다.");
        verify(roomMemberRepository, never()).save(any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("방 나가기 - 실패 (방장은 나갈 수 없음)")
    void leaveRoom_Fail_LeaderCannotLeave() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMemberRepository.findByMemberAndRoom(testMember, testRoom)).thenReturn(Optional.of(leaderRoomMember));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            mypageRoomService.leaveRoom(1L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("방장은 방을 나갈 수 없습니다. 방 삭제를 사용해주세요.");
        verify(roomMemberRepository, never()).save(any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("방 나가기 - RuntimeException 처리")
    void leaveRoom_RuntimeException() {
        // Given
        when(roomRepository.findById(1L)).thenThrow(new RuntimeException("DB 오류"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mypageRoomService.leaveRoom(1L, testMember);
        });

        assertThat(exception.getMessage()).isEqualTo("방 나가기 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("내가 참여한 방 목록 - 최근 참여순 정렬 확인")
    void getMyJoinedRooms_SortByJoinedAtDesc() {
        // Given
        Room anotherRoom = Mockito.mock(Room.class);
        when(anotherRoom.getRoomId()).thenReturn(3L);
        when(anotherRoom.getRoomName()).thenReturn("제주도 여행 방");
        when(anotherRoom.getRoomDescription()).thenReturn("제주도 힐링 투어");
        when(anotherRoom.getRoomMaxCount()).thenReturn(4);
        when(anotherRoom.getRoomCurrentCount()).thenReturn(2);
        when(anotherRoom.getTravelStartDate()).thenReturn(LocalDate.now().plusDays(14));
        when(anotherRoom.getTravelEndDate()).thenReturn(LocalDate.now().plusDays(17));
        when(anotherRoom.getDestinationCategory()).thenReturn("관광지");
        when(anotherRoom.getDestinationName()).thenReturn("제주도");
        when(anotherRoom.getCreator()).thenReturn(testMember);
        when(anotherRoom.getIsDeleteRoom()).thenReturn(false);
        when(anotherRoom.getViewCount()).thenReturn(5);
        when(anotherRoom.getEmotionRooms()).thenReturn(new ArrayList<>());
        when(anotherRoom.getAttraction()).thenReturn(null);

        RoomMember oldRoomMember = RoomMember.builder()
                .memberRoomId(3L)
                .member(testMember)
                .room(testRoom)
                .joinedAt(LocalDateTime.now().minusDays(10))
                .role("MEMBER")
                .isActive(true)
                .build();

        RoomMember recentRoomMember = RoomMember.builder()
                .memberRoomId(4L)
                .member(testMember)
                .room(anotherRoom)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .role("MEMBER")
                .isActive(true)
                .build();

        List<RoomMember> roomMembers = Arrays.asList(oldRoomMember, recentRoomMember);
        when(roomMemberRepository.findByMemberAndIsActiveTrue(testMember)).thenReturn(roomMembers);

        // When
        List<JoinedRoomResponse> result = mypageRoomService.getMyJoinedRooms(testMember);

        // Then
        assertThat(result).hasSize(2);
        // 최근 참여한 방이 먼저 와야 함
        assertThat(result.get(0).getJoinedAt()).isAfter(result.get(1).getJoinedAt());
    }
}
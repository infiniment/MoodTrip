package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplUnitTest {

    @InjectMocks
    private RoomServiceImpl roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member mockMember;
    private Room mockRoom;

    @BeforeEach
    void setup() {
        mockMember = Member.builder()
                .memberPk(1L)
                .memberId("testUser")
                .nickname("테스터")
                .memberPw("pw")
                .isWithdraw(false)
                .build();

        mockRoom = Room.builder()
                .roomId(10L)
                .roomName("모크 방")
                .roomDescription("테스트용")
                .roomMaxCount(5)
                .roomCurrentCount(1)
                .build();
    }

    @Test
    @DisplayName("joinRoom: 처음 참여하면 정상 저장됨")
    void joinRoom_success() {
        // given
        when(roomMemberRepository.findByMemberAndRoom(mockMember, mockRoom)).thenReturn(Optional.empty());

        // when
        roomService.joinRoom(mockMember, mockRoom, "LEADER");

        // then
        verify(roomMemberRepository, times(1)).save(any(RoomMember.class));
    }

    @Test
    @DisplayName("joinRoom: 이미 참여했으면 예외 발생")
    void joinRoom_duplicate() {
        // given
        when(roomMemberRepository.findByMemberAndRoom(mockMember, mockRoom))
                .thenReturn(Optional.of(RoomMember.builder().member(mockMember).room(mockRoom).build()));

        // when & then
        assertThatThrownBy(() -> roomService.joinRoom(mockMember, mockRoom, "LEADER"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 해당 방에 참여한 회원입니다.");
    }

    @Test
    @DisplayName("leaveRoom: 참여자가 나가면 isActive false로 저장됨")
    void leaveRoom_success() {
        // given
        RoomMember roomMember = RoomMember.builder()
                .member(mockMember)
                .room(mockRoom)
                .isActive(true)
                .build();

        when(roomMemberRepository.findByMemberAndRoom(mockMember, mockRoom))
                .thenReturn(Optional.of(roomMember));

        // when
        roomService.leaveRoom(mockMember, mockRoom);

        // then
        assertThat(roomMember.getIsActive()).isFalse();
        verify(roomMemberRepository).save(roomMember);
    }

    @Test
    @DisplayName("isMemberInRoom: 참여 여부 확인")
    void isMemberInRoom() {
        // given
        when(roomMemberRepository.findByMemberAndRoom(mockMember, mockRoom))
                .thenReturn(Optional.of(mock(RoomMember.class)));

        // when
        boolean result = roomService.isMemberInRoom(mockMember, mockRoom);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getActiveMembers: isActive=true만 조회됨")
    void getActiveMembers() {
        // given
        RoomMember activeMember = RoomMember.builder()
                .member(mockMember)
                .room(mockRoom)
                .role("LEADER")
                .isActive(true)
                .build();

        when(roomMemberRepository.findByRoomAndIsActiveTrue(mockRoom))
                .thenReturn(List.of(activeMember));

        // when
        var result = roomService.getActiveMembers(mockRoom);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("테스터");
    }
}

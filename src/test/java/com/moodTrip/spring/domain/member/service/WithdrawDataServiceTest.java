package com.moodTrip.spring.domain.member.service;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.member.service.WithdrawDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawDataServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    RoomRepository roomRepository;
    @Mock
    RoomMemberRepository roomMemberRepository;
    @Mock
    JoinRepository joinRepository;

    @InjectMocks
    WithdrawDataService withdrawDataService;

    Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberPk(1L)
                .memberId("testuser")
                .isWithdraw(false)
                .build();
    }

    @Test
    void processCompleteWithdraw_success() {
        when(joinRepository.findByApplicant(member)).thenReturn(List.of());
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(member)).thenReturn(List.of());
        when(roomMemberRepository.findByMemberAndIsActiveTrue(member)).thenReturn(List.of());

        WithdrawResponse response = withdrawDataService.processCompleteWithdraw(member);

        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("탈퇴가 완료");
        assertThat(member.getIsWithdraw()).isTrue();

        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void processCompleteWithdraw_callsDeleteJoinRequestsAndLeaveAllRooms() {
        EnteringRoom enteringRoom = mock(EnteringRoom.class);
        when(joinRepository.findByApplicant(member)).thenReturn(List.of(enteringRoom));

        // room도 mock 객체로 변경
        Room room = mock(Room.class);
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(member)).thenReturn(List.of(room));
        when(joinRepository.findByRoom(room)).thenReturn(List.of(enteringRoom));

        RoomMember roomMember = mock(RoomMember.class);
        when(roomMember.getRoom()).thenReturn(room);
        when(roomMemberRepository.findByMemberAndIsActiveTrue(member)).thenReturn(List.of(roomMember));

        // room에 대해 mock 메서드 stubbing
        when(room.getCreator()).thenReturn(Member.builder().memberPk(2L).build());
        when(room.getRoomCurrentCount()).thenReturn(5);

        WithdrawResponse response = withdrawDataService.processCompleteWithdraw(member);

        verify(joinRepository, atLeastOnce()).deleteAll(anyList());
        verify(roomMemberRepository).delete(any(RoomMember.class));
        verify(room).setRoomCurrentCount(4);
        verify(roomRepository).save(room);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(member.getIsWithdraw()).isTrue();
    }


    @Test
    void reactivateAccount_success() {
        Member withdrawn = Member.builder()
                .memberId(member.getMemberId())
                .isWithdraw(true)
                .build();

        when(memberRepository.findByMemberIdAndIsWithdrawTrue(member.getMemberId())).thenReturn(Optional.of(withdrawn));
        when(memberRepository.save(withdrawn)).thenAnswer(invocation -> invocation.getArgument(0));

        Member reactivated = withdrawDataService.reactivateAccount(member.getMemberId());

        assertThat(reactivated).isNotNull();
        assertThat(reactivated.getIsWithdraw()).isFalse();
    }

    @Test
    void canReactivate_returnsTrue() {
        when(memberRepository.existsByMemberIdAndIsWithdrawTrue(member.getMemberId())).thenReturn(true);

        boolean canReactivate = withdrawDataService.canReactivate(member.getMemberId());

        assertThat(canReactivate).isTrue();
    }
}

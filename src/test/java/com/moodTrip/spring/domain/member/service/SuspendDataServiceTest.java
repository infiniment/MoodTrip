package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspendDataServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomMemberRepository roomMemberRepository;
    @Mock JoinRepository joinRepository;

    @InjectMocks
    SuspendDataService suspendDataService;

    Member member;    // 정지 대상
    Member otherUser; // 새 방장 후보

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberPk(1L)
                .memberId("user1")
                .nickname("나")
                .status(Member.MemberStatus.ACTIVE)
                .isWithdraw(false)
                .build();

        otherUser = Member.builder()
                .memberPk(2L)
                .memberId("other")
                .nickname("다른유저")
                .build();
    }

    @Test
    void suspendMember_success() {
        // ==== 여기서만 필요한 stub 생성 ====

        // 남이 만든 방 (내가 참여 중 → 내 참여 삭제 + 인원수 -1)
        Room othersRoom = mock(Room.class);
        when(othersRoom.getCreator()).thenReturn(otherUser);
        when(othersRoom.getRoomName()).thenReturn("남의방");
        when(othersRoom.getRoomCurrentCount()).thenReturn(4);
        when(othersRoom.getRoomMaxCount()).thenReturn(10);

        RoomMember myParticipation = mock(RoomMember.class);
        when(myParticipation.getRoom()).thenReturn(othersRoom);

        // 내가 만든 방 1: 다른 멤버 있음 → 방장 이양
        Room myRoomWithOthers = mock(Room.class);
        when(myRoomWithOthers.getCreator()).thenReturn(member);
        when(myRoomWithOthers.getRoomName()).thenReturn("내가만든방(참여자있음)");

        RoomMember otherMemberInMyRoom = mock(RoomMember.class);
        when(otherMemberInMyRoom.getMember()).thenReturn(otherUser);

        RoomMember newLeaderRm = mock(RoomMember.class);

        // 내가 만든 방 2: 빈 방 → 방 삭제
        Room myEmptyRoom = mock(Room.class);
        when(myEmptyRoom.getRoomName()).thenReturn("내가만든방(빈방)");

        // 입장신청 1건 존재 → 삭제 경로
        when(joinRepository.findByApplicant(member))
                .thenReturn(List.of(mock(EnteringRoom.class)));

        // 내가 만든 방 목록 (참여자 있는 방 + 빈 방)
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(member))
                .thenReturn(List.of(myRoomWithOthers, myEmptyRoom));

        // 내가 참여 중인 방 목록 (남의 방 1개)
        when(roomMemberRepository.findByMemberAndIsActiveTrue(member))
                .thenReturn(List.of(myParticipation));

        // 이양 대상 방의 다른 참여자 목록(1명 존재)
        when(roomMemberRepository.findByRoomAndIsActiveTrue(myRoomWithOthers))
                .thenReturn(List.of(otherMemberInMyRoom));

        // (서비스 oldOwner 패치 기준) 새 오너의 RoomMember만 찾으면 됨
        when(roomMemberRepository.findByMemberAndRoom(eq(otherUser), same(myRoomWithOthers)))
                .thenReturn(Optional.of(newLeaderRm));

        // 빈 방의 남은 멤버 없음
        when(roomMemberRepository.findByRoom(myEmptyRoom)).thenReturn(List.of());

        // ==== 실행 ====
        suspendDataService.suspendMember(member);

        // ==== 검증 ====
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.SUSPENDED);

        // 입장신청 삭제
        verify(joinRepository, times(1)).deleteAll(anyList());

        // 남의 방 참여 해제 + 인원수 감소
        verify(roomMemberRepository, times(1)).delete(any(RoomMember.class));
        verify(othersRoom, times(1)).setRoomCurrentCount(3);
        verify(roomRepository, atLeastOnce()).save(othersRoom);

        // 방장 이양: 새 리더 ROLE 저장 + 방 저장
        verify(roomMemberRepository, times(1))
                .findByMemberAndRoom(eq(otherUser), same(myRoomWithOthers));
        verify(roomMemberRepository, times(1)).save(newLeaderRm);
        verify(roomRepository, atLeastOnce()).save(myRoomWithOthers);

        // 빈 방 삭제
        verify(roomMemberRepository, times(1)).findByRoom(myEmptyRoom);
        verify(roomRepository, times(1)).delete(myEmptyRoom);

        // 최종 member 저장
        verify(memberRepository, atLeastOnce()).save(member);
    }

    @Test
    void reactivateMember_success() {
        // 실행
        suspendDataService.reactivateMember(member);

        // 검증: 상태/저장 호출만 보면 됨 (불필요 stubbing 없음)
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        verify(memberRepository, times(1)).save(member);
    }
}

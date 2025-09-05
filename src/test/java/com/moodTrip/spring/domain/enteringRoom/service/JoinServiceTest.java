package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.request.JoinRequest;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinResponse;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinServiceTest {

    @Mock private JoinRepository joinRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomMemberRepository roomMemberRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks
    private JoinService joinService;

    private Member applicant;
    private Member creator;
    private Room room;
    private Profile profile;
    private JoinRequest request;

    @BeforeEach
    void setUp() {
        applicant = Member.builder()
                .memberPk(1L)
                .nickname("신청자")
                .build();

        creator = Member.builder()
                .memberPk(2L)
                .nickname("방장")
                .build();

        room = Room.builder()
                .roomId(100L)
                .roomName("테스트 방")
                .creator(creator)
                .roomMaxCount(5)
                .roomCurrentCount(1)
                .isDeleteRoom(false)
                .travelStartDate(LocalDate.now().plusDays(10))
                .build();

        profile = Profile.builder()
                .profileId(1L)
                .member(applicant)
                .profileBio("여행 좋아함")
                .build();

        request = JoinRequest.builder()
                .message("같이 가고 싶습니다!")
                .build();
    }

    @Test
    @DisplayName("방 입장 신청 성공")
    void applyToRoom_Success() {
        // given
        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoomAndIsActiveTrue(room)).willReturn(1L);
        given(joinRepository.existsByApplicantAndRoom(applicant, room)).willReturn(false);
        given(roomMemberRepository.findByMemberAndRoom(applicant, room)).willReturn(Optional.empty());
        given(profileRepository.findByMember(applicant)).willReturn(Optional.of(profile));
        given(joinRepository.save(any(EnteringRoom.class))).willAnswer(invocation -> {
            EnteringRoom saved = invocation.getArgument(0);
            saved.setEnteringRoomId(1L);
            return saved;
        });

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getJoinRequestId()).isEqualTo(1L);
        assertThat(response.getRoomId()).isEqualTo(100L);
        assertThat(response.getApplicantNickname()).isEqualTo("신청자");
        assertThat(response.getMessage()).contains("자기소개: 여행 좋아함");
        assertThat(response.getMessage()).contains("신청 내용: 같이 가고 싶습니다!");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        verify(joinRepository).save(any(EnteringRoom.class));
    }

    @Test
    @DisplayName("삭제된 방 신청 시 실패")
    void applyToRoom_DeletedRoom() {
        // given
        room.setIsDeleteRoom(true);

        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResultMessage()).isEqualTo("삭제된 방에는 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("자기 방 신청 시 실패")
    void applyToRoom_SelfRoom() {
        // given
        room.setCreator(applicant);

        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResultMessage()).isEqualTo("자신이 만든 방에는 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("정원 초과된 방 신청 시 실패")
    void applyToRoom_FullRoom() {
        // given
        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoomAndIsActiveTrue(room)).willReturn(5L);

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResultMessage()).contains("이미 방이 가득 찼습니다.");
    }

    @Test
    @DisplayName("이미 신청한 방일 경우 실패")
    void applyToRoom_AlreadyApplied() {
        // given
        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoomAndIsActiveTrue(room)).willReturn(1L);
        given(joinRepository.existsByApplicantAndRoom(applicant, room)).willReturn(true);

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResultMessage()).isEqualTo("이미 해당 방에 신청하셨습니다.");
    }

    @Test
    @DisplayName("이미 참여중인 방일 경우 실패")
    void applyToRoom_AlreadyParticipating() {
        // given
        given(securityUtil.getCurrentMember()).willReturn(applicant);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoomAndIsActiveTrue(room)).willReturn(1L);
        given(joinRepository.existsByApplicantAndRoom(applicant, room)).willReturn(false);

        // RoomMember mock
        RoomMember roomMember = RoomMember.builder()
                .isActive(true)
                .build();
        given(roomMemberRepository.findByMemberAndRoom(applicant, room))
                .willReturn(Optional.of(roomMember));

        // when
        JoinResponse response = joinService.applyToRoom(100L, request);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResultMessage()).isEqualTo("이미 참여중인 방입니다.");
    }

}

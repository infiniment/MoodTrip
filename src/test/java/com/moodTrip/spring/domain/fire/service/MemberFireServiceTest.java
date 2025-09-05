package com.moodTrip.spring.domain.fire.service;

import com.moodTrip.spring.domain.fire.dto.request.MemberFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.MemberFireResponse;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberFireServiceTest {

    @Mock
    private MemberFireRepository memberFireRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MemberFireService memberFireService;

    private Member reporter;
    private Member reported;
    private Room room;

    @BeforeEach
    void setUp() {
        reporter = Member.builder()
                .memberPk(1L)
                .nickname("신고자")
                .build();

        reported = Member.builder()
                .memberPk(2L)
                .nickname("신고당한유저")
                .build();

        room = Room.builder()
                .roomId(100L)
                .roomName("테스트 방")
                .build();
    }

    @Test
    @DisplayName("멤버 신고 성공")
    void reportMember_Success() {
        // given
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고당한유저")
                .reportReason("harassment")
                .reportMessage("욕설을 반복합니다")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(memberRepository.findByNickname("신고당한유저")).willReturn(Optional.of(reported));
        given(memberFireRepository.findByFireReporterAndReportedMemberAndTargetRoom(reporter, reported, room))
                .willReturn(Optional.empty());
        given(memberFireRepository.save(any(MemberFire.class)))
                .willAnswer(invocation -> {
                    MemberFire fire = invocation.getArgument(0);
                    fire.setFireId(1L);
                    return fire;
                });

        // when
        MemberFireResponse response = memberFireService.reportMember(100L, request);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getFireId()).isEqualTo(1L);
        assertThat(response.getReportedNickname()).isEqualTo("신고당한유저");
        assertThat(response.getRoomId()).isEqualTo(100L);

        verify(memberFireRepository).save(any(MemberFire.class));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자 신고 시 실패")
    void reportMember_NotLoggedIn() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고당한유저")
                .reportReason("spam")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(null);

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("로그인이 필요합니다.");

        verify(memberFireRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 방 신고 시 실패")
    void reportMember_RoomNotFound() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고당한유저")
                .reportReason("spam")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.empty());

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("존재하지 않는 방입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 멤버 신고 시 실패")
    void reportMember_ReportedMemberNotFound() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("없는유저")
                .reportReason("spam")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(memberRepository.findByNickname("없는유저")).willReturn(Optional.empty());

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("해당 닉네임의 멤버를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("자기 자신을 신고 시 실패")
    void reportMember_SelfReport() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고자")
                .reportReason("spam")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(memberRepository.findByNickname("신고자")).willReturn(Optional.of(reporter));

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("자신을 신고할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 신고한 멤버 신고 시 실패")
    void reportMember_AlreadyReported() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고당한유저")
                .reportReason("spam")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(memberRepository.findByNickname("신고당한유저")).willReturn(Optional.of(reported));
        given(memberFireRepository.findByFireReporterAndReportedMemberAndTargetRoom(reporter, reported, room))
                .willReturn(Optional.of(new MemberFire()));

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("이미 신고한 멤버입니다.");
    }

    @Test
    @DisplayName("유효하지 않은 신고 사유")
    void reportMember_InvalidReason() {
        MemberFireRequest request = MemberFireRequest.builder()
                .reportedNickname("신고당한유저")
                .reportReason("invalid_reason")
                .build();

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(memberRepository.findByNickname("신고당한유저")).willReturn(Optional.of(reported));

        MemberFireResponse response = memberFireService.reportMember(100L, request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("유효하지 않은 신고 사유입니다.");
    }
}

package com.moodTrip.spring.domain.fire.service;

import com.moodTrip.spring.domain.fire.dto.request.RoomFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.RoomFireResponse;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.fire.repository.RoomFireRepository;
import com.moodTrip.spring.domain.member.entity.Member;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomFireServiceTest {

    @Mock
    private RoomFireRepository fireRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private RoomFireService roomFireService;

    private Member reporter;
    private Member roomCreator;
    private Room room;
    private RoomFireRequest validRequest;

    @BeforeEach
    void setUp() {
        reporter = Member.builder()
                .memberPk(1L)
                .nickname("신고자")
                .build();

        roomCreator = Member.builder()
                .memberPk(2L)
                .nickname("방장")
                .build();

        room = Room.builder()
                .roomId(100L)
                .roomName("테스트 방")
                .creator(roomCreator)
                .isDeleteRoom(false)
                .build();

        validRequest = RoomFireRequest.builder()
                .reportReason("spam")
                .reportMessage("스팸 도배")
                .build();
    }

    @Test
    @DisplayName("방 신고 성공")
    void fireRoom_Success() {
        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(fireRepository.findByFireReporterAndFiredRoom(reporter, room)).willReturn(Optional.empty());
        given(fireRepository.save(any(RoomFire.class))).willAnswer(invocation -> {
            RoomFire fire = invocation.getArgument(0);
            fire.setFireId(1L);
            return fire;
        });

        RoomFireResponse response = roomFireService.fireRoom(100L, validRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getRoomId()).isEqualTo(100L);
        assertThat(response.getRoomTitle()).isEqualTo("테스트 방");
        assertThat(response.getFireReason()).isEqualTo("스팸/광고");

        verify(fireRepository).save(any(RoomFire.class));
    }

    @Test
    @DisplayName("자기 방 신고 시 실패")
    void fireRoom_SelfReport() {
        room.setCreator(reporter);

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));

        RoomFireResponse response = roomFireService.fireRoom(100L, validRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("자신이 만든 방은 신고할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 신고한 방 신고 시 실패")
    void fireRoom_AlreadyReported() {
        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));
        given(fireRepository.findByFireReporterAndFiredRoom(reporter, room))
                .willReturn(Optional.of(new RoomFire()));

        RoomFireResponse response = roomFireService.fireRoom(100L, validRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("이미 신고하신 방입니다.");
    }

    @Test
    @DisplayName("삭제된 방 신고 시 실패")
    void fireRoom_DeletedRoom() {
        room.setIsDeleteRoom(true);

        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.of(room));

        RoomFireResponse response = roomFireService.fireRoom(100L, validRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("삭제된 방은 신고할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 방 신고 시 실패")
    void fireRoom_RoomNotFound() {
        given(securityUtil.getCurrentMember()).willReturn(reporter);
        given(roomRepository.findById(100L)).willReturn(Optional.empty());

        RoomFireResponse response = roomFireService.fireRoom(100L, validRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("존재하지 않는 방입니다.");
    }

    @Test
    @DisplayName("유효하지 않은 신고 사유")
    void fireRoom_InvalidReason() {
        RoomFireRequest invalidRequest = RoomFireRequest.builder()
                .reportReason("invalid_reason")
                .reportMessage("테스트")
                .build();

        // ⚠️ securityUtil 스텁도 제거
        RoomFireResponse response = roomFireService.fireRoom(100L, invalidRequest);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("유효하지 않은 신고 사유");
    }
}

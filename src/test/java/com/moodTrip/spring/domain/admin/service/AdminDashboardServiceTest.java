package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.fire.repository.RoomFireRepository;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private AttractionRepository attractionRepository;
    @Mock private MemberFireRepository memberFireRepository;
    @Mock private RoomFireRepository roomFireRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("총 회원 수: memberRepository.count() 반환")
    void getMemberCount() {
        // Given
        when(memberRepository.count()).thenReturn(123L);

        // When
        long count = adminDashboardService.getMemberCount();

        // Then
        assertThat(count).isEqualTo(123L);
        verify(memberRepository, times(1)).count();
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("매칭 성사 수: today(KST)로 조회 후 결과 반환")
    void getMatchingCount() {
        // Given
        when(roomRepository.countByIsDeleteRoomFalseAndTravelEndDateBefore(any(LocalDate.class)))
                .thenReturn(42L);

        // When
        long count = adminDashboardService.getMatchingCount();

        // Then
        assertThat(count).isEqualTo(42L);

        // today(KST)가 전달되었는지 캡쳐해서 확인
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(roomRepository, times(1))
                .countByIsDeleteRoomFalseAndTravelEndDateBefore(captor.capture());

        LocalDate passedDate = captor.getValue();
        LocalDate todayKST = LocalDate.now(ZoneId.of("Asia/Seoul"));
        assertThat(passedDate).isEqualTo(todayKST);

        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    @DisplayName("등록 관광지 수: attractionRepository.count() 반환")
    void getAttractionCount() {
        // Given
        when(attractionRepository.count()).thenReturn(777L);

        // When
        long count = adminDashboardService.getAttractionCount();

        // Then
        assertThat(count).isEqualTo(777L);
        verify(attractionRepository, times(1)).count();
        verifyNoMoreInteractions(attractionRepository);
    }

    @Test
    @DisplayName("미처리 신고 수: Member PENDING + Room PENDING 합산")
    void getUnresolvedReportCount() {
        // Given
        when(memberFireRepository.countByFireStatus(MemberFire.FireStatus.PENDING)).thenReturn(3L);
        when(roomFireRepository.countByFireStatus(RoomFire.FireStatus.PENDING)).thenReturn(7L);

        // When
        long count = adminDashboardService.getUnresolvedReportCount();

        // Then
        assertThat(count).isEqualTo(10L);
        verify(memberFireRepository, times(1)).countByFireStatus(MemberFire.FireStatus.PENDING);
        verify(roomFireRepository, times(1)).countByFireStatus(RoomFire.FireStatus.PENDING);
        verifyNoMoreInteractions(memberFireRepository, roomFireRepository);
    }
}

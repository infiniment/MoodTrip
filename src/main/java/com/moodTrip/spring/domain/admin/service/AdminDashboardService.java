package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.fire.repository.RoomFireRepository;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final AttractionRepository attractionRepository;
    private final MemberFireRepository memberFireRepository;
    private final RoomFireRepository roomFireRepository;

    /** 총 회원 수 */
    public long getMemberCount() {
        return memberRepository.count();
    }

    /** 매칭 성사 수 = 여행이 끝난 방 수(삭제되지 않은 방만) */
    public long getMatchingCount() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return roomRepository.countByIsDeleteRoomFalseAndTravelEndDateBefore(today);
    }

    /** 등록 관광지 수 */
    public long getAttractionCount() {
        return attractionRepository.count();
    }

    /** 미처리 신고 수(PENDING) */
    public long getUnresolvedReportCount() {
        long memberPending = memberFireRepository.countByFireStatus(MemberFire.FireStatus.PENDING);
        long roomPending   = roomFireRepository.countByFireStatus(RoomFire.FireStatus.PENDING);
        return memberPending + roomPending;
    }
}

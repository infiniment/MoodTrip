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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFireService {

    private final MemberFireRepository memberFireRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public MemberFireResponse reportMember(Long roomId, MemberFireRequest request) {
        request.validate();

        Member reporter = securityUtil.getCurrentMember();
        if (reporter == null) {
            return MemberFireResponse.failure("로그인이 필요합니다.");
        }

        Room room = roomRepository.findById(roomId)
                .orElse(null);
        if (room == null) {
            return MemberFireResponse.failure("존재하지 않는 방입니다.");
        }

        Member reportedMember = memberRepository.findByNickname(request.getReportedNickname())
                .orElse(null);
        if (reportedMember == null) {
            return MemberFireResponse.failure("해당 닉네임의 멤버를 찾을 수 없습니다.");
        }

        if (reporter.getMemberPk().equals(reportedMember.getMemberPk())) {
            return MemberFireResponse.failure("자신을 신고할 수 없습니다.");
        }

        if (memberFireRepository.findByFireReporterAndReportedMemberAndTargetRoom(reporter, reportedMember, room).isPresent()) {
            return MemberFireResponse.failure("이미 신고한 멤버입니다.");
        }

        MemberFire.FireReason fireReason;
        try {
            fireReason = MemberFire.FireReason.fromString(request.getCleanedReportReason());
        } catch (IllegalArgumentException e) {
            return MemberFireResponse.failure("유효하지 않은 신고 사유입니다.");
        }

        MemberFire fire = MemberFire.builder()
                .fireReporter(reporter)
                .reportedMember(reportedMember)
                .targetRoom(room)
                .fireReason(fireReason)
                .fireMessage(request.getCleanedReportMessage())
                .fireStatus(MemberFire.FireStatus.PENDING)
                .build();

        MemberFire saved = memberFireRepository.save(fire);

        return MemberFireResponse.success(saved);
    }
}

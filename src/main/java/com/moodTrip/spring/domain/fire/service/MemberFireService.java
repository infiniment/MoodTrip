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
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

        Member reportedMember = memberRepository.findByNickname(request.getReportedNickname())
                .orElseThrow(() -> new RuntimeException("해당 닉네임의 멤버를 찾을 수 없습니다."));

        // 자기 자신 신고 금지
        if (reporter.getMemberPk().equals(reportedMember.getMemberPk())) {
            throw new RuntimeException("자신을 신고할 수 없습니다.");
        }

        // 중복 신고 체크
        memberFireRepository.findByFireReporterAndReportedMemberAndTargetRoom(reporter, reportedMember, room)
                .ifPresent(f -> { throw new RuntimeException("이미 신고한 멤버입니다."); });

        MemberFire.FireReason fireReason = MemberFire.FireReason.fromString(request.getCleanedReportReason());

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

package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.request.ReportActionDto;
import com.moodTrip.spring.domain.admin.dto.response.ReportDetailDto;
import com.moodTrip.spring.domain.admin.dto.response.ReportDto;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.fire.repository.RoomFireRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminReportService {

    private final RoomFireRepository roomFireRepository;
    private final MemberFireRepository memberFireRepository;
    private final MemberRepository memberRepository;

    // =======================
    // ====== 신고 등록 =======
    // =======================

    /** 회원 신고 접수 */
    @Transactional
    public MemberFire createMemberReport(Member reporter, Member target, Room room, String message, MemberFire.FireReason reason) {
        MemberFire mf = new MemberFire();
        mf.setFireReporter(reporter);
        mf.setReportedMember(target);
        mf.setTargetRoom(room);
        mf.setFireMessage(message);
        mf.setFireReason(reason);
        mf.setFireStatus(MemberFire.FireStatus.PENDING);


        // 신고자, 피신고자 카운트 업데이트
        if (reporter != null) {
            reporter.setRptCnt(safeLong(reporter.getRptCnt()) + 1);
            memberRepository.save(reporter);
        }
        if (target != null) {
            target.setRptRcvdCnt(safeLong(target.getRptRcvdCnt()) + 1);
            memberRepository.save(target);
        }

        return memberFireRepository.save(mf);
    }

    /** 방 신고 접수 */
    @Transactional
    public RoomFire createRoomReport(Member reporter, Room room, String message, RoomFire.FireReason reason) {
        RoomFire rf = new RoomFire();
        rf.setFireReporter(reporter);
        rf.setFiredRoom(room);
        rf.setFireMessage(message);
        rf.setFireReason(reason);
        rf.setFireStatus(RoomFire.FireStatus.PENDING);

        // 신고자 카운트 업데이트
        if (reporter != null) {
            reporter.setRptCnt(safeLong(reporter.getRptCnt()) + 1);
            memberRepository.save(reporter);
        }
        // 방장 카운트 업데이트
        if (room != null && room.getCreator() != null) {
            Member owner = room.getCreator();
            owner.setRptRcvdCnt(safeLong(owner.getRptRcvdCnt()) + 1);
            memberRepository.save(owner);
        }

        return roomFireRepository.save(rf);
    }

    // =======================
    // ====== 조회 쿼리 =======
    // =======================

    @Transactional(readOnly = true)
    public List<ReportDto> getAllReports(String status) {
        List<ReportDto> list = new ArrayList<>();

        RoomFire.FireStatus roomStatus = parseRoomStatus(status);
        MemberFire.FireStatus memberStatus = parseMemberStatus(status);

        // 방 신고
        List<RoomFire> roomFires = (roomStatus == null)
                ? roomFireRepository.findAll()
                : roomFireRepository.findByFireStatus(roomStatus);
        for (RoomFire rf : roomFires) list.add(mapRoomFireToReportDto(rf));

        // 회원 신고
        List<MemberFire> memberFires = (memberStatus == null)
                ? memberFireRepository.findAll()
                : memberFireRepository.findByFireStatus(memberStatus);
        for (MemberFire mf : memberFires) list.add(mapMemberFireToReportDto(mf));

        // 최신순 정렬
        list.sort(Comparator.comparing(ReportDto::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
        return list;
    }

    @Transactional(readOnly = true)
    public ReportDetailDto getDetail(Long fireId, String type) {
        if ("ROOM".equalsIgnoreCase(type)) {
            RoomFire rf = roomFireRepository.findById(fireId).orElseThrow();
            return mapRoomFireToDetailDto(rf);
        } else if ("MEMBER".equalsIgnoreCase(type)) {
            MemberFire mf = memberFireRepository.findById(fireId).orElseThrow();
            return mapMemberFireToDetailDto(mf);
        }
        throw new IllegalArgumentException("Unknown report type: " + type);
    }

    // =======================
    // ====== 처리 액션 =======
    // =======================

    @Transactional
    public void applyAction(Long fireId, String type, ReportActionDto dto) {
        if (dto == null || dto.getActionType() == null) {
            throw new IllegalArgumentException("actionType 이 필요합니다.");
        }
        String at = dto.getActionType().toLowerCase();
        switch (at) {
            case "resolve" -> resolve(fireId, type, dto);
            case "reject"  -> dismiss(fireId, type, dto);
            default -> throw new IllegalArgumentException("지원하지 않는 actionType: " + dto.getActionType());
        }
    }

    /** 신고 처리(RESOLVED) */
    @Transactional
    public void resolve(Long fireId, String type, ReportActionDto dto) {
        if ("ROOM".equalsIgnoreCase(type)) {
            RoomFire rf = roomFireRepository.findById(fireId).orElseThrow();
            rf.setFireStatus(RoomFire.FireStatus.RESOLVED);
            if (dto != null) rf.setAdminMemo(safe(dto.getAdminMemo()));
            roomFireRepository.save(rf);
        } else if ("MEMBER".equalsIgnoreCase(type)) {
            MemberFire mf = memberFireRepository.findById(fireId).orElseThrow();
            mf.setFireStatus(MemberFire.FireStatus.RESOLVED);
            if (dto != null) mf.setAdminMemo(safe(dto.getAdminMemo()));
            memberFireRepository.save(mf);
        }
    }

//    /** 신고 기각(DISMISSED) */
//    @Transactional
//    public void dismiss(Long fireId, String type, ReportActionDto dto) {
//        if ("ROOM".equalsIgnoreCase(type)) {
//            RoomFire rf = roomFireRepository.findById(fireId).orElseThrow();
//
//            // 카운트 감소 (기각 시에도 신고 카운트는 차감)
//            Member reporter = rf.getFireReporter();
//            Room room = rf.getFiredRoom();
//
//            if (reporter != null) {
//                reporter.setRptCnt(Math.max(0, safeLong(reporter.getRptCnt()) - 1));
//                memberRepository.save(reporter);
//            }
//            if (room != null && room.getCreator() != null) {
//                Member owner = room.getCreator();
//                owner.setRptRcvdCnt(Math.max(0, safeLong(owner.getRptRcvdCnt()) - 1));
//                memberRepository.save(owner);
//            }
//
//            rf.setFireStatus(RoomFire.FireStatus.DISMISSED);
//            if (dto != null) rf.setAdminMemo(safe(dto.getAdminMemo()));
//            roomFireRepository.save(rf);
//
//        } else if ("MEMBER".equalsIgnoreCase(type)) {
//            MemberFire mf = memberFireRepository.findById(fireId).orElseThrow();
//
//            // 카운트 감소 (기각 시에도 신고 카운트는 차감)
//            Member reporter = mf.getFireReporter();
//            Member target = mf.getReportedMember();
//
//            if (reporter != null) {
//                reporter.setRptCnt(Math.max(0, safeLong(reporter.getRptCnt()) - 1));
//                memberRepository.save(reporter);
//            }
//            if (target != null) {
//                target.setRptRcvdCnt(Math.max(0, safeLong(target.getRptRcvdCnt()) - 1));
//                memberRepository.save(target);
//            }
//
//            mf.setFireStatus(MemberFire.FireStatus.DISMISSED);
//            if (dto != null) mf.setAdminMemo(safe(dto.getAdminMemo()));
//            memberFireRepository.save(mf);
//        }
//    }
@Transactional
public void dismiss(Long fireId, String type, ReportActionDto dto) {
    System.out.println("=== dismiss 호출됨: fireId=" + fireId + ", type=" + type);

    if ("MEMBER".equalsIgnoreCase(type)) {
        MemberFire mf = memberFireRepository.findById(fireId).orElseThrow();

        Member reporter = mf.getFireReporter();
        Member target = mf.getReportedMember();

        System.out.println("처리 전 - 신고자 카운트: " + reporter.getRptCnt());
        System.out.println("처리 전 - 피신고자 카운트: " + target.getRptRcvdCnt());

        if (reporter != null) {
            reporter.setRptCnt(Math.max(0, safeLong(reporter.getRptCnt()) - 1));
            memberRepository.save(reporter);
            System.out.println("처리 후 - 신고자 카운트: " + reporter.getRptCnt());
        }
        if (target != null) {
            target.setRptRcvdCnt(Math.max(0, safeLong(target.getRptRcvdCnt()) - 1));
            memberRepository.save(target);
            System.out.println("처리 후 - 피신고자 카운트: " + target.getRptRcvdCnt());
        }

        mf.setFireStatus(MemberFire.FireStatus.DISMISSED);
        if (dto != null) mf.setAdminMemo(safe(dto.getAdminMemo()));
        memberFireRepository.save(mf);

        System.out.println("=== dismiss 완료");
    }
}

    // =======================
    // ====== 매퍼 부분 =======
    // =======================

    private ReportDto mapRoomFireToReportDto(RoomFire rf) {
        Member reporter = rf.getFireReporter();
        Room room = rf.getFiredRoom();

        return ReportDto.builder()
                .reportId(rf.getFireId())
                .type("ROOM")
                .typeDisplay("매칭방")
                .targetSummary(room != null ? safe(room.getRoomName()) : "-")
                .reporterNickname(reporter != null ? safe(reporter.getNickname()) : "-")
                .reportedNickname(room != null ? safe(room.getRoomName()) : "-")
                .reason(rf.getFireReason() != null ? rf.getFireReason().getDescription() : null)
                .createdAt(rf.getCreatedAt())
                .statusClass(toStatusClass(rf.getFireStatus()))
                .statusDisplay(rf.getFireStatus().getDescription())
                .roomName(room != null ? safe(room.getRoomName()) : null)
                .roomId(room != null ? room.getRoomId() : null)
                .reportedMemberId(null)
                .build();
    }

    private ReportDto mapMemberFireToReportDto(MemberFire mf) {
        Member reporter = mf.getFireReporter();
        Member target = mf.getReportedMember();
        Room room = mf.getTargetRoom();

        return ReportDto.builder()
                .reportId(mf.getFireId())
                .type("MEMBER")
                .typeDisplay("회원")
                .targetSummary(room != null ? (safe(target.getNickname()) + " @ " + safe(room.getRoomName())) : safe(target.getNickname()))
                .reporterNickname(reporter != null ? safe(reporter.getNickname()) : "-")
                .reportedNickname(target != null ? safe(target.getNickname()) : "-")
                .reason(mf.getFireReason() != null ? mf.getFireReason().getDescription() : null)
                .createdAt(mf.getCreatedAt())
                .statusClass(toStatusClass(mf.getFireStatus()))
                .statusDisplay(mf.getFireStatus().getDescription())
                .roomName(room != null ? safe(room.getRoomName()) : null)
                .roomId(room != null ? room.getRoomId() : null)
                .reportedMemberId(target != null ? target.getMemberPk() : null)
                .build();
    }

    private ReportDetailDto mapRoomFireToDetailDto(RoomFire rf) {
        Member reporter = rf.getFireReporter();
        Room room = rf.getFiredRoom();
        Member creator = room != null ? room.getCreator() : null;

        return ReportDetailDto.builder()
                .reportId(rf.getFireId())
                .type("ROOM")
                .typeDisplay("매칭방")
                .targetSummary(room != null ? safe(room.getRoomName()) : "-")
                .reporterNickname(reporter != null ? safe(reporter.getNickname()) : "-")
                .reportedNickname(room != null ? safe(room.getRoomName()) : "-")
                .reason(rf.getFireReason() != null ? rf.getFireReason().getDescription() : null)
                .fireMessage(rf.getFireMessage())
                .createdAt(rf.getCreatedAt())
                .statusClass(toStatusClass(rf.getFireStatus()))
                .statusDisplay(rf.getFireStatus().getDescription())
                .adminMemo(rf.getAdminMemo())
                .roomName(room != null ? safe(room.getRoomName()) : null)
                .roomId(room != null ? room.getRoomId() : null)
                .roomCreatorNickname(creator != null ? safe(creator.getNickname()) : null)
                .roomCreatedAt(room != null ? room.getCreatedAt() : null)
                .isRoomDeleted(room != null ? room.getIsDeleteRoom() : null)
                .processedAt(null)
                .processedBy(null)
                .build();
    }

    private ReportDetailDto mapMemberFireToDetailDto(MemberFire mf) {
        Member reporter = mf.getFireReporter();
        Member target = mf.getReportedMember();
        Room room = mf.getTargetRoom();

        return ReportDetailDto.builder()
                .reportId(mf.getFireId())
                .type("MEMBER")
                .typeDisplay("회원")
                .targetSummary(room != null
                        ? (safe(target.getNickname()) + " @ " + safe(room.getRoomName()))
                        : safe(target.getNickname()))
                .reporterNickname(reporter != null ? safe(reporter.getNickname()) : "-")
                .reportedNickname(target != null ? safe(target.getNickname()) : "-")
                .reason(mf.getFireReason() != null ? mf.getFireReason().getDescription() : null)
                .fireMessage(mf.getFireMessage())
                .createdAt(mf.getCreatedAt())
                .statusClass(toStatusClass(mf.getFireStatus()))
                .statusDisplay(mf.getFireStatus().getDescription())
                .adminMemo(mf.getAdminMemo())
                .roomName(room != null ? safe(room.getRoomName()) : null)
                .roomId(room != null ? room.getRoomId() : null)
                .reportedMemberId(target != null ? target.getMemberPk() : null)
                .reporterMemberId(reporter != null ? reporter.getMemberPk() : null)
                .build();
    }

    // =======================
    // ====== 헬퍼 ============
    // =======================

    private RoomFire.FireStatus parseRoomStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return RoomFire.FireStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private MemberFire.FireStatus parseMemberStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return MemberFire.FireStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private String toStatusClass(Enum<?> status) {
        String n = status.name();
        return switch (n) {
            case "PENDING" -> "status pending";
            case "INVESTIGATING" -> "status active";
            case "RESOLVED" -> "status success";
            case "DISMISSED" -> "status suspended";
            default -> "status pending";
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static long safeLong(Long v) {
        return v == null ? 0L : v;
    }
}

package com.moodTrip.spring.domain.fire.service;

import com.moodTrip.spring.domain.fire.dto.request.FireRequest;
import com.moodTrip.spring.domain.fire.dto.response.FireResponse;
import com.moodTrip.spring.domain.fire.entity.Fire;
import com.moodTrip.spring.domain.fire.repository.FireRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용 (성능 최적화)
public class FireService {

    private final FireRepository fireRepository;
    private final RoomRepository roomRepository;
    private final SecurityUtil securityUtil;


    @Transactional  // 데이터 변경이 있으므로 쓰기 트랜잭션
    public FireResponse fireRoom(Long roomId, FireRequest fireRequest) {
        log.info("🔥 방 신고 요청 시작 - roomId: {}, 신고 사유: {}",
                roomId, fireRequest.getReportReason());

        try {
            // 요청 데이터 유효성 검사
            fireRequest.validate();
            log.info("✅ 요청 데이터 유효성 검사 통과");

            // 로그인한 사용자 정보 가져오기
            Member currentMember = securityUtil.getCurrentMember();
            log.info("👤 신고자: {}", currentMember.getNickname());

            // 신고 대상 방 조회
            Room targetRoom = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        log.error("❌ 존재하지 않는 방 - roomId: {}", roomId);
                        return new RuntimeException("존재하지 않는 방입니다.");
                    });

            log.info("🏠 신고 대상 방: {}", targetRoom.getRoomName());

            // 비즈니스 로직 검증
            validateFireRequest(currentMember, targetRoom);

            // Fire 엔티티 생성 및 저장
            Fire fire = createFire(currentMember, targetRoom, fireRequest);
            Fire savedFire = fireRepository.save(fire);

            log.info("✅ 방 신고 완료 - fireId: {}, 방: {}, 신고자: {}",
                    savedFire.getFireId(), targetRoom.getRoomName(), currentMember.getNickname());

            // 성공 응답 생성
            return FireResponse.success(savedFire);

        } catch (RuntimeException e) {
            log.error("❌ 방 신고 실패 - roomId: {}, 오류: {}", roomId, e.getMessage());

            // 방 정보가 있으면 포함해서 실패 응답 생성
            try {
                Room room = roomRepository.findById(roomId).orElse(null);
                if (room != null) {
                    return FireResponse.failure(e.getMessage(), roomId, room.getRoomName());
                }
            } catch (Exception ex) {
                log.warn("방 정보 조회 중 오류: {}", ex.getMessage());
            }

            return FireResponse.failure(e.getMessage());

        } catch (Exception e) {
            log.error("💥 예상치 못한 오류 발생 - roomId: {}", roomId, e);
            return FireResponse.failure("신고 처리 중 오류가 발생했습니다. 고객센터에 문의해주세요.");
        }
    }

    private void validateFireRequest(Member fireReporter, Room targetRoom) {
        log.info("🔍 비즈니스 로직 검증 시작");

        // 자기 방 신고 막기
        if (targetRoom.getCreator().getMemberPk().equals(fireReporter.getMemberPk())) {
            log.warn("❌ 자기 방 신고 시도 - 방장: {}, 신고자: {}",
                    targetRoom.getCreator().getNickname(), fireReporter.getNickname());
            throw new RuntimeException("자신이 만든 방은 신고할 수 없습니다.");
        }

        // 중복 신고 체크
        Optional<Fire> existingFire = fireRepository.findByFireReporterAndFiredRoom(fireReporter, targetRoom);
        if (existingFire.isPresent()) {
            log.warn("❌ 중복 신고 시도 - 방: {}, 신고자: {}",
                    targetRoom.getRoomName(), fireReporter.getNickname());
            throw new RuntimeException("이미 신고하신 방입니다.");
        }

        // 삭제된 방 신고 방지
        if (targetRoom.getIsDeleteRoom() != null && targetRoom.getIsDeleteRoom()) {
            log.warn("❌ 삭제된 방 신고 시도 - 방: {}", targetRoom.getRoomName());
            throw new RuntimeException("삭제된 방은 신고할 수 없습니다.");
        }

        log.info("✅ 비즈니스 로직 검증 통과");
    }

    // 신고 엔티티 생성
    private Fire createFire(Member fireReporter, Room targetRoom, FireRequest fireRequest) {
        log.info("🔥 Fire 엔티티 생성 시작");

        // 문자열 신고 사유를 ENUM으로 변환
        Fire.FireReason fireReason = Fire.FireReason.fromString(fireRequest.getCleanedReportReason());

        Fire fire = Fire.builder()
                .fireReporter(fireReporter)
                .firedRoom(targetRoom)
                .fireReason(fireReason)
                .fireMessage(fireRequest.getCleanedReportMessage())
                .fireStatus(Fire.FireStatus.PENDING)  // 기본값: 처리 대기
                .build();

        log.info("✅ Fire 엔티티 생성 완료 - 사유: {}", fireReason.getDescription());
        return fire;
    }
}
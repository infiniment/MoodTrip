package com.moodTrip.spring.domain.mypageRoom.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 마이페이지 방 관련 서비스 구현체
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 조회 메서드가 많으므로 기본을 읽기 전용으로 설정
public class MypageRoomServiceImpl implements MypageRoomService {

    // 기존 Repository들을 주입받아서 사용
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<JoinedRoomResponse> getMyJoinedRooms(Member member) {
        log.info("[마이페이지] 내가 입장한 방 목록 조회 시작 - 회원ID: {}, 닉네임: {}",
                member.getMemberId(), member.getNickname());

        try {
            // 현재 회원이 참여 중인 활성 방들 조회, 수민이가 만든 roomMemberRepository 활용
            List<RoomMember> activeRoomMembers = roomMemberRepository.findByMemberAndIsActiveTrue(member);

            log.info("📊 [마이페이지] 활성 참여 방 개수: {}", activeRoomMembers.size());

            // 삭제되지 않은 방들만 필터링하고 DTO로 변환
            List<JoinedRoomResponse> joinedRooms = activeRoomMembers.stream()
                    .filter(roomMember -> {
                        // 삭제되지 않은 방만 포함
                        boolean isNotDeleted = !roomMember.getRoom().getIsDeleteRoom();
                        if (!isNotDeleted) {
                            log.debug("❌ [마이페이지] 삭제된 방 제외 - 방ID: {}",
                                    roomMember.getRoom().getRoomId());
                        }
                        return isNotDeleted;
                    })
                    .map(roomMember -> {
                        // 마이페이지 전용 DTO로 변환
                        log.debug("✅ [마이페이지] 방 정보 변환 - 방ID: {}, 방제목: {}, 내역할: {}",
                                roomMember.getRoom().getRoomId(),
                                roomMember.getRoom().getRoomName(),
                                roomMember.getRole());
                        return JoinedRoomResponse.from(roomMember);
                    })
                    .sorted((r1, r2) -> r2.getJoinedAt().compareTo(r1.getJoinedAt())) // 최근 참여한 방부터
                    .collect(Collectors.toList());

            log.info("✅ [마이페이지] 내가 입장한 방 목록 조회 완료 - 회원ID: {}, 유효한 참여 방 개수: {}",
                    member.getMemberId(), joinedRooms.size());

            // 디버깅용 상세 로그 => 성공 시 지워두 댐
            if (log.isDebugEnabled()) {
                joinedRooms.forEach(room ->
                        log.debug("📄 [마이페이지] 방 정보: ID={}, 제목={}, 역할={}, 참여일={}",
                                room.getRoomId(), room.getRoomName(), room.getMyRole(), room.getJoinedAt())
                );
            }

            return joinedRooms;

        } catch (Exception e) {
            log.error("💥 [마이페이지] 내가 입장한 방 목록 조회 실패 - 회원ID: {}, 오류: {}",
                    member.getMemberId(), e.getMessage(), e);

            // 💡 실제 환경에서는 CustomException 사용 권장
            // throw new CustomException(MYPAGE_ROOM_RETRIEVE_FAILED);
            throw new RuntimeException("마이페이지에서 참여 중인 방 목록을 조회하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreatedRoomResponse> getMyCreatedRooms(Member member) {
        log.info("📦 [마이페이지] 내가 만든 방 목록 조회 시작 - 회원ID: {}, 닉네임: {}",
                member.getMemberId(), member.getNickname());

        List<Room> createdRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);

        log.info("✅ 생성한 방 개수: {}", createdRooms.size());

        return createdRooms.stream()
                .map(CreatedRoomResponse::from)
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))  // 최신순 정렬
                .collect(Collectors.toList());
    }

}
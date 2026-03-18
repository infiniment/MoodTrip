// 📁 src/main/java/com/moodTrip/spring/domain/enteringRoom/repository/EnteringRoomRepository.java
package com.moodTrip.spring.domain.enteringRoom.repository;

import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoinRepository extends JpaRepository<EnteringRoom, Long> {

    // 특정 회원이 특정 방에 신청했는지 확인 (중복 신청 방지)
    Optional<EnteringRoom> findByApplicantAndRoom(Member applicant, Room room);

    // 특정 회원이 특정 방에 신청했는지 존재 여부만 확인
    boolean existsByApplicantAndRoom(Member applicant, Room room);

    // 특정 방의 모든 신청 목록 조회 (방장이 볼 때 사용)
    List<EnteringRoom> findByRoom(Room room);

    // 특정 방의 특정 상태 신청들만 조회
    List<EnteringRoom> findByRoomAndStatus(Room room, EnteringRoom.EnteringStatus status);

    // 특정 회원이 신청한 모든 방 목록 (내가 신청한 방들)
    List<EnteringRoom> findByApplicant(Member applicant);

    // 특정 방의 대기중인 신청 개수
    @Query("SELECT COUNT(e) FROM EnteringRoom e WHERE e.room = :room AND e.status = 'PENDING'")
    Long countPendingByRoom(@Param("room") Room room);

    // 특정 방의 승인된 신청 개수 (실제 참여자 수)
    @Query("SELECT COUNT(e) FROM EnteringRoom e WHERE e.room = :room AND e.status = 'APPROVED'")
    Long countApprovedByRoom(@Param("room") Room room);

    @Query("SELECT er FROM EnteringRoom er " +
            "JOIN FETCH er.applicant m " +
            "LEFT JOIN FETCH m.profile " +
            "WHERE er.room.roomId = :roomId")
    List<EnteringRoom> findByRoomIdWithProfile(@Param("roomId") Long roomId);


    //특정 회원의 승인된 매칭 참여 횟수 계산
    Long countByApplicantAndStatus(Member applicant, EnteringRoom.EnteringStatus status);

    //특정 회원의 모든 매칭 신청 내역 조회 (관리자용)
    List<EnteringRoom> findByApplicantOrderByCreatedAtDesc(Member applicant);

    //특정 회원의 상태별 매칭 신청 내역 조회
    List<EnteringRoom> findByApplicantAndStatusOrderByCreatedAtDesc(
            Member applicant, EnteringRoom.EnteringStatus status);
}

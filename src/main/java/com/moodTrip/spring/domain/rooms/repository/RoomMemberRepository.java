package com.moodTrip.spring.domain.rooms.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    // 회원이 어떤 방에 참여하고 있는지 조회
    List<RoomMember> findByMember(Member member);

    // 방에 참여 중인 전체 인원 조회
    List<RoomMember> findByRoom(Room room);

    // 특정 회원이 특정 방에 참여 중인지 조회
    Optional<RoomMember> findByMemberAndRoom(Member member, Room room);

    // 방 ID와 회원 ID로 참여자 조회
    Optional<RoomMember> findByMember_MemberPkAndRoom_RoomId(Long memberPk, Long room);

    // 방의 활성화된 참여자만 조회
    @Query("SELECT rm FROM RoomMember rm JOIN FETCH rm.member WHERE rm.room = :room AND rm.isActive = true")
    List<RoomMember> findByRoomAndIsActiveTrue(@Param("room")Room room);

    //방 ID 기준으로 전체 참여자 수 조회
    Long countByRoom(Room room);

    // 회원 기준 활성 참여 방 조회
    List<RoomMember> findByMemberAndIsActiveTrue(Member member);

    // 활성 멤버 여부만 체크 (가장 추천)
    boolean existsByMember_MemberPkAndRoom_RoomIdAndIsActiveTrue(Long memberPk, Long roomId);

}

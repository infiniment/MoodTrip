package com.moodTrip.spring.domain.rooms.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIsDeleteRoomFalse(); // 삭제되지 않은 방만 조회

    List<Room> findByCreatorAndIsDeleteRoomFalse(Member creator);

    @EntityGraph(attributePaths = {"attraction"})
    Optional<Room> findWithAttractionByRoomId(Long roomId);

    // 상우가 만듦. 메인페이지에 조회수 높은 방 6개 조회
    @EntityGraph(attributePaths = {"attraction", "creator"})
    List<Room> findTop6ByIsDeleteRoomFalseOrderByViewCountDescCreatedAtDesc();

    // 🔹 모든 Room 가져올 때 creator, attraction 같이 fetch
    @Override
    @EntityGraph(attributePaths = {"creator", "attraction"})
    List<Room> findAll();

    @Override
    @EntityGraph(attributePaths = {"creator", "attraction"})
    Page<Room> findAll(Pageable pageable);

    long countByIsDeleteRoomFalseAndTravelEndDateBefore(java.time.LocalDate date);

    @EntityGraph(attributePaths = {"creator", "attraction"})
    @Query("""
            select r
            from Room r
            where :keyword is null
               or trim(:keyword) = ''
               or r.destinationName like concat('%', :keyword, '%')
               or r.roomName like concat('%', :keyword, '%')
            """)
    List<Room> searchByKeyword(@Param("keyword") String keyword);



}

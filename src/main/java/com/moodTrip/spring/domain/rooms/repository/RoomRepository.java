package com.moodTrip.spring.domain.rooms.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RoomRepository extends JpaRepository<Room, Long> {


    List<Room> findByCreatorAndIsDeleteRoomFalse(Member creator);
}

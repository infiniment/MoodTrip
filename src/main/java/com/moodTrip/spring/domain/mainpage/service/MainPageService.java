package com.moodTrip.spring.domain.mainpage.service;

import com.moodTrip.spring.domain.mainpage.dto.response.MainPageRoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용 서비스이므로 readOnly 설정
public class MainPageService {

    private final RoomRepository roomRepository;

    /**
     * 메인페이지용 인기 방 6개 조회
     * - 조회수 기준 내림차순으로 상위 6개
     * - 삭제되지 않은 방만 조회
     * - Attraction, Creator 정보를 함께 조회해서 N+1 문제 방지
     */
    public List<MainPageRoomResponse> getPopularRooms() {
        log.info("메인페이지 인기 방 6개 조회 시작");

        try {
            // Repository에서 조회수 기준 상위 6개 방 조회
            // findTop6ByIsDeleteRoomFalseOrderByViewCountDescCreatedAtDesc()
            // 이 메소드 하나로 모든 조건 처리됨 (삭제 안된 방 + 조회수 내림차순 + 상위 6개)
            List<Room> popularRooms = roomRepository.findTop6ByIsDeleteRoomFalseOrderByViewCountDescCreatedAtDesc();

            log.info("인기 방 {}개 조회 완료", popularRooms.size());

            // Room 엔티티를 MainPageRoomResponse DTO로 변환
            List<MainPageRoomResponse> responses = popularRooms.stream()
                    .map(MainPageRoomResponse::from) // 정적 메소드로 변환
                    .collect(Collectors.toList());

            // 개발용 로그: 조회된 방들의 정보 출력
            if (log.isDebugEnabled()) {
                responses.forEach(room ->
                        log.debug("조회된 방: {} (조회수: {}, 상태: {})",
                                room.getRoomName(), room.getViewCount(), room.getStatus())
                );
            }

            return responses;

        } catch (Exception e) {
            log.error("메인페이지 인기 방 조회 중 오류 발생", e);

            // 오류가 발생해도 빈 목록을 반환해서 메인페이지가 깨지지 않도록 함
            // 프론트엔드에서는 빈 목록을 받으면 "표시할 방이 없습니다" 메시지 출력
            return List.of();
        }
    }

    /**
     * 추가 메소드: 최신 방 6개 조회 (조회수가 모두 0인 경우를 대비)
     * 필요시 컨트롤러에서 호출 가능
     */
    public List<MainPageRoomResponse> getLatestRooms() {
        log.info("메인페이지 최신 방 6개 조회 시작");

        try {
            // 삭제되지 않은 방 중 최신 생성순 6개
            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();

            List<Room> latestRooms = allRooms.stream()
                    .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())) // 최신순 정렬
                    .limit(6) // 상위 6개만
                    .collect(Collectors.toList());

            return latestRooms.stream()
                    .map(MainPageRoomResponse::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("메인페이지 최신 방 조회 중 오류 발생", e);
            return List.of();
        }
    }
}
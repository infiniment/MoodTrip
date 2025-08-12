// 📁 src/main/java/com/moodTrip/spring/domain/enteringRoom/controller/JoinRequestManagementViewController.java
package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.RoomWithRequestsResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RequestStatsResponse;
import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class JoinRequestManagementViewController {

    private final JoinRequestManagementService joinRequestManagementService;

    /**
     * 방 입장 요청 관리 페이지
     * GET /mypage/join-requests
     */
    @GetMapping("/join-requests")
    public String joinRequestsPage(Model model) {
        log.info("방 입장 요청 관리 페이지 요청");

        try {
            // 1️⃣ 방장의 방 목록 + 신청 목록 조회
            List<RoomWithRequestsResponse> rooms = joinRequestManagementService.getMyRoomsWithRequests();

            // 2️⃣ 통계 데이터 조회
            RequestStatsResponse stats = joinRequestManagementService.getRequestStats();

            // 3️⃣ 전체 대기 중인 신청 수 계산 (알림 배지용)
            int totalPendingRequests = rooms.stream()
                    .mapToInt(room -> room.getPendingRequestsCount())
                    .sum();

            // 4️⃣ 모델에 데이터 추가
            model.addAttribute("rooms", rooms);
            model.addAttribute("stats", stats);
            model.addAttribute("totalPendingRequests", totalPendingRequests);
            model.addAttribute("hasRequests", totalPendingRequests > 0);

            log.info("방 입장 요청 관리 페이지 데이터 로드 완료 - {}개 방, {}건 대기",
                    rooms.size(), totalPendingRequests);

            return "mypage/join-requests";  // join-requests.html 템플릿

        } catch (Exception e) {
            log.error("방 입장 요청 관리 페이지 로드 실패", e);

            // 에러 발생 시 빈 데이터로 페이지 렌더링
            model.addAttribute("rooms", List.of());
            model.addAttribute("stats", RequestStatsResponse.of(0, 0, 0, 0));
            model.addAttribute("totalPendingRequests", 0);
            model.addAttribute("hasRequests", false);
            model.addAttribute("error", "요청 목록을 불러오는 중 오류가 발생했습니다.");

            return "mypage/join-requests";
        }
    }

    /**
     * 방 입장 요청 관리 페이지 (AJAX용 부분 렌더링)
     * GET /mypage/join-requests/partial
     */
    @GetMapping("/join-requests/partial")
    public String joinRequestsPartial(Model model) {
        log.info("방 입장 요청 관리 부분 렌더링 요청");

        try {
            // API와 동일한 데이터 로딩
            List<RoomWithRequestsResponse> rooms = joinRequestManagementService.getMyRoomsWithRequests();
            model.addAttribute("rooms", rooms);

            // 부분 템플릿 반환 (AJAX로 특정 영역만 업데이트용)
            return "mypage/join-requests :: main-wrapper";

        } catch (Exception e) {
            log.error("부분 렌더링 실패", e);
            model.addAttribute("rooms", List.of());
            model.addAttribute("error", "데이터를 불러올 수 없습니다.");
            return "mypage/join-requests :: main-wrapper";
        }
    }

    /**
     * 방 입장 요청 처리 완료 페이지 (리다이렉트용)
     * GET /mypage/join-requests/success
     */
    @GetMapping("/join-requests/success")
    public String joinRequestSuccessPage(
            @org.springframework.web.bind.annotation.RequestParam(value = "action", required = false) String action,
            @org.springframework.web.bind.annotation.RequestParam(value = "count", required = false, defaultValue = "1") Integer count,
            Model model
    ) {
        log.info("방 입장 요청 처리 완료 페이지 - action: {}, count: {}", action, count);

        // 성공 메시지 생성
        String message;
        if ("approve".equals(action)) {
            message = count == 1 ? "1건의 요청을 승인했습니다." : count + "건의 요청을 승인했습니다.";
        } else if ("reject".equals(action)) {
            message = count == 1 ? "1건의 요청을 거절했습니다." : count + "건의 요청을 거절했습니다.";
        } else {
            message = "요청 처리가 완료되었습니다.";
        }

        model.addAttribute("message", message);
        model.addAttribute("action", action);
        model.addAttribute("count", count);

        return "mypage/join-request-success";  // 처리 완료 페이지 템플릿
    }
}
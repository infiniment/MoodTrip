package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.ActionResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinRequestListResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RequestStatsResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RoomWithRequestsResponse;
import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Join Request Management API", description = "방 입장 요청 관리 REST API")
@Slf4j
@RestController
@RequestMapping("/api/v1/join-requests")
@RequiredArgsConstructor
public class JoinRequestManagementApiController {

    private final JoinRequestManagementService joinRequestManagementService;

    /**
     * 방장의 모든 방과 각 방의 신청 목록 조회
     * GET /api/v1/join-requests/rooms
     */
    @Operation(
            summary = "방장의 방 목록과 신청 목록 조회",
            description = "현재 로그인한 방장이 만든 모든 방과 각 방에 온 입장 신청 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomWithRequestsResponse>> getMyRoomsWithRequests() {
        log.info("방장의 방 목록 + 신청 목록 조회 API 호출");

        try {
            List<RoomWithRequestsResponse> rooms = joinRequestManagementService.getMyRoomsWithRequests();
            log.info("방 목록 조회 성공 - {}개 방", rooms.size());
            return ResponseEntity.ok(rooms);

        } catch (RuntimeException e) {
            log.error("방 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 방의 신청 목록만 조회
     * GET /api/v1/join-requests/rooms/{roomId}
     */
    @Operation(
            summary = "특정 방의 신청 목록 조회",
            description = "특정 방에 온 입장 신청 목록만 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "해당 방의 관리 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<JoinRequestListResponse>> getRoomRequests(
            @Parameter(description = "방 ID", example = "1", required = true)
            @PathVariable("roomId") Long roomId
    ) {
        log.info("특정 방 신청 목록 조회 API 호출 - roomId: {}", roomId);

        try {
            List<JoinRequestListResponse> requests = joinRequestManagementService.getRoomRequests(roomId);
            log.info("방 신청 목록 조회 성공 - {}건", requests.size());
            return ResponseEntity.ok(requests);

        } catch (RuntimeException e) {
            log.error("방 신청 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 개별 신청 승인
     * POST /api/v1/join-requests/{requestId}/approve
     */
    @Operation(
            summary = "개별 신청 승인",
            description = "특정 입장 신청을 승인합니다. 승인 시 해당 회원이 방에 정식으로 참여하게 됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청이거나 방 정원 초과"),
            @ApiResponse(responseCode = "403", description = "해당 방의 관리 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 신청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ActionResponse> approveRequest(
            @Parameter(description = "신청 ID", example = "1", required = true)
            @PathVariable("requestId") Long requestId
    ) {
        log.info("개별 신청 승인 API 호출 - requestId: {}", requestId);

        try {
            ActionResponse response = joinRequestManagementService.approveRequest(requestId);

            if (response.isSuccess()) {
                log.info("개별 신청 승인 성공 - requestId: {}", requestId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("개별 신청 승인 실패 - requestId: {}, 사유: {}", requestId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (RuntimeException e) {
            log.error("개별 신청 승인 오류: {}", e.getMessage());
            ActionResponse errorResponse = ActionResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            ActionResponse errorResponse = ActionResponse.failure("신청 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 개별 신청 거절
     * POST /api/v1/join-requests/{requestId}/reject
     */
    @Operation(
            summary = "개별 신청 거절",
            description = "특정 입장 신청을 거절합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "403", description = "해당 방의 관리 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 신청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ActionResponse> rejectRequest(
            @Parameter(description = "신청 ID", example = "1", required = true)
            @PathVariable("requestId") Long requestId
    ) {
        log.info("개별 신청 거절 API 호출 - requestId: {}", requestId);

        try {
            ActionResponse response = joinRequestManagementService.rejectRequest(requestId);

            if (response.isSuccess()) {
                log.info("개별 신청 거절 성공 - requestId: {}", requestId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("개별 신청 거절 실패 - requestId: {}, 사유: {}", requestId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (RuntimeException e) {
            log.error("개별 신청 거절 오류: {}", e.getMessage());
            ActionResponse errorResponse = ActionResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            ActionResponse errorResponse = ActionResponse.failure("신청 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 통계 데이터 조회
     * GET /api/v1/join-requests/stats
     */
    @Operation(
            summary = "신청 통계 데이터 조회",
            description = "방장의 방들에 대한 입장 신청 통계 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/stats")
    public ResponseEntity<RequestStatsResponse> getRequestStats() {
        log.info("신청 통계 데이터 조회 API 호출");

        try {
            RequestStatsResponse stats = joinRequestManagementService.getRequestStats();
            log.info("통계 데이터 조회 성공");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("통계 데이터 조회 오류", e);
            RequestStatsResponse errorStats = RequestStatsResponse.of(0, 0, 0, 0);
            return ResponseEntity.internalServerError().body(errorStats);
        }
    }
}
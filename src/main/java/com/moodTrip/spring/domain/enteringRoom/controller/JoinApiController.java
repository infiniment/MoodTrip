// 📁 src/main/java/com/moodTrip/spring/domain/enteringRoom/controller/JoinApiController.java
package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.request.JoinRequest;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinResponse;
import com.moodTrip.spring.domain.enteringRoom.service.JoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Join API", description = "방 입장 신청 관련 REST API")
@Slf4j
@RestController
@RequestMapping("/api/v1/companion-rooms")
@RequiredArgsConstructor
public class JoinApiController {

    private final JoinService joinService;

    /**
     * 방 입장 신청 API
     * POST /api/v1/companion-rooms/{room_id}/join-requests
     */
    @Operation(
            summary = "방 입장 신청",
            description = "특정 방에 입장 신청을 합니다. 로그인이 필요하며, 중복 신청은 불가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 완료 (성공/실패 여부는 응답 body의 success 필드 확인)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 등)"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{room_id}/join-requests")
    public ResponseEntity<JoinResponse> applyToRoom(
            @Parameter(description = "방 ID", example = "1", required = true)
            @PathVariable("room_id") Long roomId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "입장 신청 요청 데이터"
            )
            @RequestBody JoinRequest request
    ) {
        log.info("방 입장 신청 API 호출 - roomId: {}", roomId);

        try {
            // 🔥 Service 호출해서 신청 처리
            JoinResponse response = joinService.applyToRoom(roomId, request);

            // 성공/실패 관계없이 200 OK로 응답 (비즈니스 로직 결과는 response.success로 판단)
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("방 입장 신청 API 예상치 못한 오류", e);

            // 예상치 못한 시스템 오류만 500으로 처리
            JoinResponse errorResponse = JoinResponse.builder()
                    .joinRequestId(null)
                    .resultMessage("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                    .success(false)
                    .appliedAt(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
package com.moodTrip.spring.domain.fire.controller;

import com.moodTrip.spring.domain.fire.dto.request.RoomFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.RoomFireResponse;
import com.moodTrip.spring.domain.fire.service.RoomFireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/fires")
@RequiredArgsConstructor
public class RoomFireApiController {

    private final RoomFireService fireService;

    @PostMapping("/rooms/{roomId}")
    public ResponseEntity<RoomFireResponse> reportRoom(
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomFireRequest fireRequest
    ) {
        log.info("🔥 방 신고 API 호출됨");
        log.info("📋 요청 정보 - 방ID: {}, 신고사유: {}, 메시지길이: {}글자",
                roomId,
                fireRequest.getReportReason(),
                fireRequest.getReportMessage() != null ? fireRequest.getReportMessage().length() : 0);

        try {
            // FireService에서 신고 처리
            RoomFireResponse response = fireService.fireRoom(roomId, fireRequest);

            // 처리 결과에 따른 HTTP 응답 생성
            if (response.isSuccess()) {
                log.info("방 신고 성공 - 방ID: {}, FireID: {}, 신고자: 현재사용자",
                        roomId, response.getFireId());

                return ResponseEntity.ok(response);

            } else {
                log.warn("방 신고 실패 - 방ID: {}, 사유: {}",
                        roomId, response.getMessage());

                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 데이터 - 방ID: {}, 오류: {}", roomId, e.getMessage());

            RoomFireResponse errorResponse = RoomFireResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (RuntimeException e) {
            log.error("⚠비즈니스 로직 오류 - 방ID: {}, 오류: {}", roomId, e.getMessage());

            RoomFireResponse errorResponse = RoomFireResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("서버 내부 오류 발생 - 방ID: {}", roomId, e);

            RoomFireResponse errorResponse = RoomFireResponse.failure(
                    "신고 처리 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
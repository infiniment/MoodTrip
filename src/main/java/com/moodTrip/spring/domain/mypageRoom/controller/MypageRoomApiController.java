package com.moodTrip.spring.domain.mypageRoom.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.service.MypageRoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 마이페이지 방 관리 관련 api 컨트롤러
@Tag(name = "Mypage Room API", description = "마이페이지 방 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/mypage/rooms")
@RequiredArgsConstructor
public class MypageRoomApiController {

    private final MypageRoomService mypageRoomService;
    private final SecurityUtil securityUtil;

    @Operation(
            summary = "내가 입장한 방 목록 조회",
            description = "마이페이지에서 현재 로그인한 사용자가 참여 중인 모든 방의 목록을 조회합니다. " +
                    "삭제된 방은 제외되며, 최근 참여한 방부터 정렬됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "참여 중인 방 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/joined")
    public ResponseEntity<List<JoinedRoomResponse>> getMyJoinedRooms() {
        log.info("[마이페이지] 내가 입장한 방 목록 조회 API 호출됨");

        try {
            // 현재 로그인한 사용자 정보 가져오기
            Member currentMember = securityUtil.getCurrentMember();
            log.info("[마이페이지] 현재 사용자: ID={}, 닉네임={}",
                    currentMember.getMemberId(), currentMember.getNickname());

            // 서비스에서 내가 참여한 방 목록 조회
            List<JoinedRoomResponse> joinedRooms = mypageRoomService.getMyJoinedRooms(currentMember);

            // 성공 응답 반환
            log.info("✅ [마이페이지] 내가 입장한 방 목록 조회 API 성공 - 사용자: {}, 방 개수: {}",
                    currentMember.getNickname(), joinedRooms.size());

            return ResponseEntity.ok(joinedRooms);

        } catch (RuntimeException e) {
            log.error("❌ [마이페이지] 내가 입장한 방 목록 조회 API 실패 (비즈니스 로직 오류): {}", e.getMessage());
            return ResponseEntity.badRequest().build();  // 400 에러

        } catch (Exception e) {
            log.error("💥 [마이페이지] 내가 입장한 방 목록 조회 API 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().build();  // 500 에러
        }
    }

    @GetMapping("/created")
    public ResponseEntity<List<CreatedRoomResponse>> getMyCreatedRooms() {
        log.info("[마이페이지] 내가 만든 방 목록 API 요청");

        try {
            Member currentMember = securityUtil.getCurrentMember();

            log.info("현재 사용자: ID={}, 닉네임={}",
                    currentMember.getMemberId(), currentMember.getNickname());

            List<CreatedRoomResponse> createdRooms = mypageRoomService.getMyCreatedRooms(currentMember);

            log.info("내가 만든 방 응답 성공 - {}개", createdRooms.size());

            return ResponseEntity.ok(createdRooms);

        } catch (RuntimeException e) {
            log.warn("[마이페이지] 내가 만든 방 API 실패 (비즈니스 오류): {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("[마이페이지] 내가 만든 방 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
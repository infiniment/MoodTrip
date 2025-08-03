package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.common.util.SecurityUtil; // 🔥 새로 추가!
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Member API", description = "회원 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {
    // 회원 탈퇴 전용 컨트롤러
    private final MemberService memberService;
    private final SecurityUtil securityUtil; // 🔥 SecurityUtil 주입!

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 회원의 계정을 탈퇴 처리합니다. JWT 토큰 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 처리 성공"),
            @ApiResponse(responseCode = "400", description = "이미 탈퇴한 회원이거나 잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/me")
    public ResponseEntity<WithdrawResponse> withdrawMember() {
        log.info("🚀 회원 탈퇴 API 호출됨 - JWT 인증 사용");

        try {
            Member currentMember = securityUtil.getCurrentMember();

            log.info("📝 탈퇴 요청 회원 - ID: {}, 닉네임: {}, PK: {}",
                    currentMember.getMemberId(),
                    currentMember.getNickname(),
                    currentMember.getMemberPk());

            // 🔍 추가 검증: 이미 탈퇴한 회원인지 미리 체크
            if (currentMember.getIsWithdraw() != null && currentMember.getIsWithdraw()) {
                log.warn("❌ 이미 탈퇴한 회원의 탈퇴 요청 - 회원ID: {}", currentMember.getMemberId());

                WithdrawResponse errorResponse = WithdrawResponse.builder()
                        .memberId(currentMember.getMemberId())
                        .success(false)
                        .message("이미 탈퇴한 회원입니다.")
                        .withdrawnAt(LocalDateTime.now())
                        .build();

                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 탈퇴 처리 실행
            WithdrawResponse response = memberService.withdrawMember(currentMember);

            log.info("✅ 회원 탈퇴 성공 - 회원ID: {}, 처리시간: {}",
                    currentMember.getMemberId(), response.getWithdrawnAt());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ 회원 탈퇴 실패 (비즈니스 로직 오류): {}", e.getMessage());

            WithdrawResponse errorResponse = WithdrawResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .withdrawnAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("💥 회원 탈퇴 중 예상치 못한 오류", e);

            WithdrawResponse errorResponse = WithdrawResponse.builder()
                    .success(false)
                    .message("탈퇴 처리 중 오류가 발생했습니다. 고객센터에 문의해 주세요.")
                    .withdrawnAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.service.MemberService;
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

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // 회원 탈퇴하기 전용 컨트롤러

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 회원의 계정을 탈퇴 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 처리 성공"),
            @ApiResponse(responseCode = "400", description = "이미 탈퇴한 회원이거나 잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/me")
    public ResponseEntity<WithdrawResponse> withdrawMember() {
        log.info("🚀 회원 탈퇴 API 호출됨 - MemberApiController");

        try {
            // 🔥 실제로는 JWT 토큰이나 세션에서 현재 로그인한 회원 정보를 가져와야 합니다.
            // 지금은 테스트용으로 임시 회원 생성
            Member currentMember = memberRepository.findByMemberId("testuser123")
                    .orElseThrow(() -> new RuntimeException("회원 없음"));

            log.info("📝 탈퇴 요청 회원: {}", currentMember.getMemberId());

            // 탈퇴 처리
            WithdrawResponse response = memberService.withdrawMember(currentMember);

            log.info("✅ 회원 탈퇴 성공 - 회원ID: {}", currentMember.getMemberId());

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

    /**
     * 테스트용 회원 생성 메서드
     *
     * 실제 운영 환경에서는:
     * - JWT 토큰에서 회원 정보 추출
     * - 세션에서 현재 로그인한 회원 조회
     * - memberService.getCurrentMember() 호출
     */
    private Member createTestMember() {
        return Member.builder()
                .memberPk(1L)
                .memberId("testuser123")
                .nickname("테스트유저")
                .email("test@moodtrip.com")
                .memberPhone("010-1234-5678")
                .memberAuth("U")
                .isWithdraw(false)  // 아직 탈퇴하지 않은 상태
                .provider(null)     // 일반 회원가입
                .providerId(null)
                .rptCnt(0L)
                .rptRcvdCnt(0L)
                .build();
    }

}
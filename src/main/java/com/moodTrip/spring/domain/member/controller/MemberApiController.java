package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.response.LogoutResponse;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SecurityUtil securityUtil; // 🔥 새로 추가된 의존성!

    // 🔥 새로 추가된 로그아웃 API
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자를 완전히 로그아웃 처리합니다. " +
                    "JWT 토큰, 세션 쿠키를 모두 삭제하고 서버 세션을 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/logout")  // POST /api/v1/members/logout
    public ResponseEntity<LogoutResponse> logout(
            HttpServletRequest request,   // 🔥 세션 관리를 위해 추가
            HttpServletResponse response  // 🔥 쿠키 삭제를 위해 추가
    ) {
        log.info("🚪 로그아웃 API 호출됨 - 완전한 로그아웃 처리 시작");

        try {
            // 1️⃣ 현재 로그인된 사용자 확인 (JWT 토큰 검증)
            Member currentMember = securityUtil.getCurrentMember();

            log.info("👤 로그아웃 처리 대상 사용자: {}, 닉네임: {}",
                    currentMember.getMemberId(), currentMember.getNickname());

            // 2️⃣ 🔥 JWT 토큰 쿠키 삭제
            Cookie jwtCookie = new Cookie("jwtToken", null);
            jwtCookie.setMaxAge(0);          // 즉시 만료
            jwtCookie.setPath("/");          // 전체 경로에서 삭제
            jwtCookie.setHttpOnly(true);     // XSS 공격 방지
            jwtCookie.setSecure(false);      // 개발환경: false, 운영환경: true
            response.addCookie(jwtCookie);

            log.info("🗑️ JWT 토큰(jwtToken) 쿠키 삭제 완료");

            // 3️⃣ 🔥 JSESSIONID 쿠키 삭제
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setMaxAge(0);      // 즉시 만료
            sessionCookie.setPath("/");      // 전체 경로에서 삭제
            sessionCookie.setHttpOnly(true); // XSS 공격 방지
            sessionCookie.setSecure(false);  // 개발환경: false, 운영환경: true
            response.addCookie(sessionCookie);

            log.info("🗑️ 세션(JSESSIONID) 쿠키 삭제 완료");

            // 4️⃣ 🔥 서버 세션 무효화
            HttpSession session = request.getSession(false); // 기존 세션만 가져오기
            if (session != null) {
                String sessionId = session.getId();
                session.invalidate(); // 세션 무효화
                log.info("🧹 서버 세션 무효화 완료 - SessionID: {}", sessionId);
            } else {
                log.info("📭 무효화할 서버 세션이 없음");
            }

            // 5️⃣ 🔥 보안 컨텍스트 클리어 (현재 요청에서 인증 정보 완전 제거)
            SecurityContextHolder.clearContext();
            log.info("🧹 Spring Security 컨텍스트 클리어 완료");

            // 6️⃣ 개인화된 성공 응답 생성
            LogoutResponse logoutResponse = LogoutResponse.success(
                    currentMember.getNickname() + "님, 안전하게 로그아웃되었습니다. 다음에 또 만나요! 👋"
            );

            log.info("✅ 완전한 로그아웃 처리 성공 - 사용자: {}", currentMember.getMemberId());
            log.info("🎯 삭제된 쿠키: jwtToken, JSESSIONID");
            log.info("🎯 무효화된 세션: {}", session != null ? "완료" : "없음");

            return ResponseEntity.ok(logoutResponse);

        } catch (Exception e) {
            log.warn("❌ 로그아웃 처리 중 오류 발생: {}", e.getMessage());
            log.debug("🔍 오류 상세:", e);

            // 🔥 인증 실패여도 모든 쿠키는 정리해주기 (방어적 프로그래밍)
            try {
                log.info("🧹 오류 상황에서도 쿠키 정리 시작");

                // JWT 토큰 삭제
                Cookie jwtCookie = new Cookie("jwtToken", null);
                jwtCookie.setMaxAge(0);
                jwtCookie.setPath("/");
                response.addCookie(jwtCookie);

                // JSESSIONID 삭제
                Cookie sessionCookie = new Cookie("JSESSIONID", null);
                sessionCookie.setMaxAge(0);
                sessionCookie.setPath("/");
                response.addCookie(sessionCookie);

                // 세션 무효화
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                // 보안 컨텍스트 클리어
                SecurityContextHolder.clearContext();

                log.info("🗑️ 오류 상황에서도 모든 인증 정보 정리 완료");

            } catch (Exception cleanupError) {
                log.error("💥 쿠키 정리 중 추가 오류 발생: {}", cleanupError.getMessage());
            }

            // 인증 관련 오류인지 확인하여 적절한 응답 코드 반환
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("로그인") ||
                    errorMessage.contains("인증") ||
                    errorMessage.contains("token")) {

                return ResponseEntity.status(401)
                        .body(LogoutResponse.failure("인증되지 않은 사용자입니다. 쿠키는 정리되었습니다."));
            } else {
                return ResponseEntity.status(500)
                        .body(LogoutResponse.failure("로그아웃 처리 중 오류가 발생했습니다."));
            }
        }
    }

    // 🔥 기존 회원 탈퇴 API (그대로 유지)
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
            // 🔥 SecurityUtil로 현재 사용자 가져오기 (테스트 코드 대신)
            Member currentMember = securityUtil.getCurrentMember();

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
     * 🔥 테스트용 회원 생성 메서드 제거!
     *
     * 기존에 있던 createTestMember() 메서드는 더 이상 필요 없습니다.
     * SecurityUtil.getCurrentMember()로 실제 로그인된 회원 정보를 가져오니까요!
     */

}
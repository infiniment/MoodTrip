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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final SecurityUtil securityUtil;

    // 로그아웃 관련 api
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
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            // 현재 로그인된 사용자 확인 (JWT 토큰 검증)
            Member currentMember = securityUtil.getCurrentMember();

            // JWT 토큰 쿠키 삭제
            Cookie jwtCookie = new Cookie("jwtToken", null);
            jwtCookie.setMaxAge(0);          // 즉시 만료
            jwtCookie.setPath("/");          // 전체 경로에서 삭제
            jwtCookie.setHttpOnly(true);     // XSS 공격 방지
            jwtCookie.setSecure(false);      // 개발환경: false, 운영환경: true
            response.addCookie(jwtCookie);


            // JSESSIONID 쿠키 삭제
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setMaxAge(0);      // 즉시 만료
            sessionCookie.setPath("/");      // 전체 경로에서 삭제
            sessionCookie.setHttpOnly(true); // XSS 공격 방지
            sessionCookie.setSecure(false);  // 개발환경: false, 운영환경: true
            response.addCookie(sessionCookie);


            // 서버 세션 무효화
            HttpSession session = request.getSession(false); // 기존 세션만 가져오기
            if (session != null) {
                String sessionId = session.getId();
                session.invalidate(); // 세션 무효화
            } else {
                log.info("📭 무효화할 서버 세션이 없음");
            }

            // 보안 컨텍스트 클리어 (현재 요청에서 인증 정보 완전 제거)
            SecurityContextHolder.clearContext();

            // 개인화된 성공 응답 생성
            LogoutResponse logoutResponse = LogoutResponse.success(
                    currentMember.getNickname() + "님, 안전하게 로그아웃되었습니다. 다음에 또 만나요! 👋"
            );

            return ResponseEntity.ok(logoutResponse);

        } catch (Exception e) {

            try {

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


            } catch (Exception cleanupError) {
                log.error("💥 쿠키 정리 중 추가 오류 발생: {}", cleanupError.getMessage());
            }

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

    // 회원 탈퇴 로직
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
    public ResponseEntity<WithdrawResponse> withdrawMember(HttpServletRequest request, HttpServletResponse response) {

        try {
            Member currentMember = securityUtil.getCurrentMember();
            WithdrawResponse withdrawResponse = memberService.withdrawMember(currentMember);

            // 🔹 세션 무효화
            request.getSession().invalidate();

            // 🔹 SecurityContext 초기화
            SecurityContextHolder.clearContext();

            // 🔹 JWT 쿠키 삭제
            ResponseCookie jwtClear = ResponseCookie.from("jwtToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();

            // 🔹 JSESSIONID 쿠키 삭제
            ResponseCookie jsessionClear = ResponseCookie.from("JSESSIONID", "")
                    .path("/")
                    .maxAge(0)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, jwtClear.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, jsessionClear.toString());

            return ResponseEntity.ok(withdrawResponse);

        } catch (RuntimeException e) {
            WithdrawResponse errorResponse = WithdrawResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .withdrawnAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            WithdrawResponse errorResponse = WithdrawResponse.builder()
                    .success(false)
                    .message("탈퇴 처리 중 오류가 발생했습니다. 고객센터에 문의해 주세요.")
                    .withdrawnAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
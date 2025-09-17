package com.moodTrip.spring.global.security.filter;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccessFilter implements Filter {

    private final SecurityUtil securityUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // /admin으로 시작하는 URL만 체크
        if (requestURI.startsWith("/admin")) {
            try {
                Member currentMember = securityUtil.getCurrentMember();

                // 로그인되지 않은 경우
                if (currentMember == null) {
                    log.warn("관리자 페이지 미로그인 접근 시도 - URI: {}", requestURI);
                    handleUnauthorizedAccess(httpRequest, httpResponse, "login_required");
                    return;
                }

                // member_pk가 1이 아닌 경우
                if (!currentMember.getMemberPk().equals(1L)) {
                    log.warn("관리자 페이지 무권한 접근 시도 - 사용자: {}, memberPk: {}, URI: {}",
                            currentMember.getMemberId(), currentMember.getMemberPk(), requestURI);
                    handleUnauthorizedAccess(httpRequest, httpResponse, "unauthorized");
                    return;
                }

                log.debug("관리자 접근 승인 - 사용자: {}", currentMember.getMemberId());

            } catch (Exception e) {
                log.error("관리자 권한 체크 중 오류 발생 - URI: {}", requestURI, e);
                handleUnauthorizedAccess(httpRequest, httpResponse, "system_error");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 권한 없는 접근 처리
     */
    private void handleUnauthorizedAccess(HttpServletRequest request, HttpServletResponse response, String errorType)
            throws IOException {

        // AJAX 요청인 경우 JSON 오류 응답
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            String message = switch (errorType) {
                case "login_required" -> "로그인이 필요합니다.";
                case "unauthorized" -> "관리자만 접근 가능합니다.";
                case "system_error" -> "시스템 오류가 발생했습니다.";
                default -> "접근 권한이 없습니다.";
            };

            response.getWriter().write(String.format("{\"error\":\"%s\",\"redirect\":\"/login\"}", message));
            return;
        }

        // 일반 요청인 경우 리다이렉트
        switch (errorType) {
            case "login_required" -> response.sendRedirect("/login?error=admin_login_required");
            case "unauthorized" -> response.sendRedirect("/?error=admin_unauthorized");
            case "system_error" -> response.sendRedirect("/login?error=system_error");
            default -> response.sendRedirect("/login");
        }
    }
}
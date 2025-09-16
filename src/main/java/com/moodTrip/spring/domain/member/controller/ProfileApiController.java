package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.ChangePasswordForm;
import com.moodTrip.spring.domain.member.dto.request.IntroduceUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileImageUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.common.util.SecurityUtil; // 🔥 새로 추가!
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ REST API 전용 프로필 컨트롤러 - JWT 인증 적용
 *
 * 주요 변경사항:
 * - createTestMember() 제거 ❌
 * - SecurityUtil.getCurrentMember() 사용 ✅
 * - 실제 로그인한 사용자의 프로필 처리 ✅
 */
@Tag(name = "Profile API", description = "프로필 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileService profileService;
    private final MemberService memberService;
    private final SecurityUtil securityUtil; // 🔥 SecurityUtil 주입!

    private static final String DEFAULT_IMG = "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg";

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 회원의 프로필 정보를 조회합니다. JWT 토큰 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "회원 또는 프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        log.info("🔍 프로필 조회 API 호출 - JWT 인증 사용");

        try {
            // 🔥 변경: 더미 데이터 대신 실제 로그인한 사용자 정보 사용
            Member currentMember = securityUtil.getCurrentMember();
            log.info("📝 프로필 조회 요청 - 회원ID: {}, 닉네임: {}",
                    currentMember.getMemberId(), currentMember.getNickname());

            ProfileResponse profileResponse = profileService.getMyProfile(currentMember);

            log.info("✅ 프로필 조회 성공 - 회원ID: {}", currentMember.getMemberId());
            return ResponseEntity.ok(profileResponse);

        } catch (RuntimeException e) {
            log.error("❌ 프로필 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 401 또는 400 에러
        } catch (Exception e) {
            log.error("💥 예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build(); // 500 에러
        }
    }

    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인한 회원의 닉네임을 수정합니다. JWT 토큰 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 닉네임 형식"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "회원/프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/nickname")
    public ResponseEntity<ProfileResponse> updateNickname(
            @org.springframework.web.bind.annotation.RequestBody NicknameUpdateRequest request
    ) {
        log.info("🔄 닉네임 수정 API 호출 - 새닉네임: {}", request.getNickname());

        try {
            // 🔥 변경: 실제 로그인한 사용자 정보 사용
            Member currentMember = securityUtil.getCurrentMember();
            log.info("📝 닉네임 수정 요청 - 회원ID: {}, 기존닉네임: {}, 새닉네임: {}",
                    currentMember.getMemberId(), currentMember.getNickname(), request.getNickname());

            ProfileResponse updatedProfile = memberService.updateNickname(currentMember, request);

            log.info("✅ 닉네임 수정 성공 - 회원ID: {}", currentMember.getMemberId());
            return ResponseEntity.ok(updatedProfile);

        } catch (RuntimeException e) {
            log.error("❌ 닉네임 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 400 에러
        } catch (Exception e) {
            log.error("💥 예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build(); // 500 에러
        }
    }

    @Operation(
            summary = "자기소개 수정",
            description = "현재 로그인한 회원의 자기소개를 수정합니다. Profile이 없으면 새로 생성됩니다. JWT 토큰 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자기소개 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (자기소개 길이 초과 등)"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me/introduce")
    public ResponseEntity<ProfileResponse> updateIntroduce(
            @RequestBody(description = "자기소개 수정 요청 데이터")
            @org.springframework.web.bind.annotation.RequestBody IntroduceUpdateRequest request
    ) {
        log.info("🔄 자기소개 수정 API 호출 - 요청 내용: {}글자",
                request.getProfileBio() != null ? request.getProfileBio().length() : 0);

        try {
            // 🔥 변경: 실제 로그인한 사용자 정보 사용
            Member currentMember = securityUtil.getCurrentMember();
            log.info("📝 자기소개 수정 요청 - 회원ID: {}", currentMember.getMemberId());

            ProfileResponse updatedProfile = profileService.updateIntroduce(currentMember, request);

            log.info("✅ 자기소개 수정 성공 - 회원ID: {}", currentMember.getMemberId());
            return ResponseEntity.ok(updatedProfile);

        } catch (RuntimeException e) {
            log.error("❌ 자기소개 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 400 에러
        } catch (Exception e) {
            log.error("💥 예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build(); // 500 에러
        }
    }

    @Operation(
            summary = "프로필 사진 변경",
            description = "현재 로그인한 회원의 프로필 사진을 변경합니다. JWT 토큰 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 사진 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 이미지 URL 형식"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
            @ApiResponse(responseCode = "404", description = "회원/프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })

    @PatchMapping("/me/profileImage")
    public ResponseEntity<ProfileResponse> updateProfileImage(
            @RequestBody(description = "프로필 사진 변경 요청 데이터")
            @org.springframework.web.bind.annotation.RequestBody ProfileImageUpdateRequest request
    ) {
        log.info("🔄 프로필 사진 변경 API 호출 - 새이미지URL: {}", request.getProfileImage());

        try {
            // 🔥 변경: 실제 로그인한 사용자 정보 사용
            Member currentMember = securityUtil.getCurrentMember();
            log.info("📝 프로필 사진 변경 요청 - 회원ID: {}", currentMember.getMemberId());

            ProfileResponse updatedProfile = profileService.updateProfileImage(currentMember, request);

            log.info("✅ 프로필 사진 변경 성공 - 회원ID: {}", currentMember.getMemberId());
            return ResponseEntity.ok(updatedProfile);

        } catch (RuntimeException e) {
            log.error("❌ 프로필 사진 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 400 에러
        } catch (Exception e) {
            log.error("💥 예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build(); // 500 에러
        }
    }

    // 비밀번호 변경 폼을 보여주는 GET 핸들러
    @GetMapping("/change-password")
    public String changePasswordForm(@ModelAttribute("form") ChangePasswordForm form) {
        return "mypage/change-password"; // templates/mypage/change-password.html
    }


    /**
     * 단건 아바타 조회: GET /api/v1/profiles/avatar?nickname=유저
     * 응답 예: { "nickname": "유저1", "url": "/uploads/...png" }
     */
    @GetMapping("/avatar")
    public ResponseEntity<Map<String, Object>> getAvatar(@RequestParam String nickname) {
        try {
            var memberOpt = memberService.findByNickname(nickname);
            String url = DEFAULT_IMG;

            if (memberOpt.isPresent()) {
                var prof = profileService.getProfileByMemberId(memberOpt.get().getMemberPk());
                if (prof != null && prof.getProfileImage() != null && !prof.getProfileImage().isBlank()) {
                    url = prof.getProfileImage();
                }
            }
            return ResponseEntity.ok(Map.of("nickname", nickname, "url", url));
        } catch (Exception e) {
            log.warn("아바타 단건 조회 실패 - nickname={}", nickname, e);
            return ResponseEntity.ok(Map.of("nickname", nickname, "url", DEFAULT_IMG));
        }
    }

    /**
     * 배치 아바타 조회: POST /api/v1/profiles/avatars
     * 요청 바디: ["유저1","유저2",...]
     * 응답 예: { "유저1": "/uploads/a.png", "유저2": "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg" }
     */
    @PostMapping("/avatars")
    public ResponseEntity<Map<String, String>> getAvatars(@org.springframework.web.bind.annotation.RequestBody List<String> nicknames) {
        Map<String, String> out = new java.util.HashMap<>();
        if (nicknames == null || nicknames.isEmpty()) return ResponseEntity.ok(out);

        for (String nick : new java.util.HashSet<>(nicknames)) { // 중복 제거
            try {
                var memberOpt = memberService.findByNickname(nick);
                String url = DEFAULT_IMG;

                if (memberOpt.isPresent()) {
                    var prof = profileService.getProfileByMemberId(memberOpt.get().getMemberPk());
                    if (prof != null && prof.getProfileImage() != null && !prof.getProfileImage().isBlank()) {
                        url = prof.getProfileImage();
                    }
                }
                out.put(nick, url);
            } catch (Exception e) {
                log.warn("아바타 배치 조회 실패 - nickname={}", nick, e);
                out.put(nick, DEFAULT_IMG);
            }
        }
        return ResponseEntity.ok(out);
    }



}
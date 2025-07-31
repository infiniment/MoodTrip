package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile API", description = "프로필 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 회원의 프로필 정보를 조회합니다. (테스트용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        log.info("프로필 조회 API 호출 (테스트용)");
        try {
            Member testMember = createTestMember();
            ProfileResponse profileResponse = profileService.getMyProfile(testMember);
            return ResponseEntity.ok(profileResponse);
        } catch (RuntimeException e) {
            log.error("프로필 조회 API 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 회원의 프로필 정보를 수정합니다. (테스트용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "회원 또는 프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestBody(description = "프로필 수정 요청 데이터")
            @org.springframework.web.bind.annotation.RequestBody ProfileUpdateRequest request
    ) {
        log.info("프로필 수정 API 호출 (테스트용)");
        try {
            Member testMember = createTestMember();
            ProfileResponse updatedProfile = profileService.updateMyProfile(testMember, request);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            log.error("프로필 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인한 회원의 닉네임만 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @PatchMapping("/nickname")  // ✅ PATCH 방식 사용!
    public ResponseEntity<ProfileResponse> updateNickname(
            @RequestBody NicknameUpdateRequest request
    ) {
        log.info("닉네임 수정 API 호출 - 새닉네임: {}", request.getNickname());

        try {
            // 임시로 테스트 회원 생성 (나중에 JWT로 실제 로그인 회원 가져올 예정)
            Member testMember = createTestMember();

            ProfileResponse updatedProfile = profileService.updateNickname(testMember, request);

            return ResponseEntity.ok(updatedProfile);

        } catch (RuntimeException e) {
            log.error("닉네임 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();  // 400 에러
        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ResponseEntity.internalServerError().build();  // 500 에러
        }
    }

    private Member createTestMember() {
        return Member.builder()
                .memberPk(1L)
                .memberId("testuser123")
                .memberName("테스트유저")
                .email("test@moodtrip.com")
                .memberPhone("010-1234-5678")
                .memberAuth("U")
                .isWithdraw(false)
                .build();
    }
}

package com.moodTrip.spring.domain.member.dto.response;

import com.moodTrip.spring.domain.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAdminDto {

    private Long memberPk;
    private String memberId;
    private String nickname;
    private String email;
    private String memberPhone;
    private LocalDateTime createdAt;        // 가입일
    private LocalDateTime lastLoginAt;      // 최근 로그인

    @Builder.Default
    private String status = "ACTIVE";       // 회원 상태 (ACTIVE, SUSPENDED, WITHDRAWN)

    @Builder.Default
    private Boolean isWithdraw = false;     // 탈퇴 여부

    @Builder.Default
    private Long rptRcvdCnt = 0L;           // 신고받은 횟수

    @Builder.Default
    private Long rptCnt = 0L;               // 신고한 횟수

    private String provider;                // 소셜 로그인 제공자

    // 추가 통계 정보
    @Builder.Default
    private Long matchingParticipationCount = 0L; // 매칭 참여 횟수

    @Builder.Default
    private Long reviewCount = 0L;          // 리뷰 작성 횟수 (추후 구현시 사용)

    // Entity -> DTO 변환 생성자
    public static MemberAdminDto fromEntity(Member member) {
        return MemberAdminDto.builder()
                .memberPk(member.getMemberPk())
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .memberPhone(member.getMemberPhone())
                .createdAt(member.getCreatedAt())
                .lastLoginAt(member.getLastLoginAt())
                .status(member.getStatus() != null ? member.getStatus().toString() : "ACTIVE")
                .isWithdraw(member.getIsWithdraw() != null ? member.getIsWithdraw() : false)
                .rptRcvdCnt(member.getRptRcvdCnt() != null ? member.getRptRcvdCnt() : 0L)
                .rptCnt(member.getRptCnt() != null ? member.getRptCnt() : 0L)
                .provider(member.getProvider())
                .build();
    }

    // 상태 표시용 메서드 (null-safe)
    public String getStatusDisplay() {
        if (Boolean.TRUE.equals(isWithdraw)) {
            return "탈퇴";
        }
        String s = (status == null) ? "ACTIVE" : status;
        switch (s) {
            case "ACTIVE":
                return "활성";
            case "SUSPENDED":
                return "정지";
            case "WITHDRAWN":
                return "탈퇴";
            default:
                return "활성";
        }
    }

    // 상태 CSS 클래스용 메서드 (null-safe)
    public String getStatusClass() {
        if (Boolean.TRUE.equals(isWithdraw)) {
            return "status suspended";
        }
        String s = (status == null) ? "ACTIVE" : status;
        switch (s) {
            case "ACTIVE":
                return "status active";
            case "SUSPENDED":
            case "WITHDRAWN":
                return "status suspended";
            default:
                return "status active";
        }
    }
}

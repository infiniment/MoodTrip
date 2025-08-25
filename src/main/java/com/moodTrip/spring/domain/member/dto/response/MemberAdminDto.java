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
    private String status;                  // 회원 상태 (ACTIVE, SUSPENDED, WITHDRAWN)
    private Boolean isWithdraw;             // 탈퇴 여부
    private Long rptRcvdCnt;               // 신고받은 횟수
    private Long rptCnt;                   // 신고한 횟수
    private String provider;               // 소셜 로그인 제공자

    // 추가 통계 정보
    private Long matchingParticipationCount; // 매칭 참여 횟수
    private Long reviewCount;               // 리뷰 작성 횟수 (추후 구현시 사용)

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
                .isWithdraw(member.getIsWithdraw())
                .rptRcvdCnt(member.getRptRcvdCnt() != null ? member.getRptRcvdCnt() : 0L)
                .rptCnt(member.getRptCnt() != null ? member.getRptCnt() : 0L)
                .provider(member.getProvider())
                .build();
    }

    // 상태 표시용 메서드
    public String getStatusDisplay() {
        if (isWithdraw != null && isWithdraw) {
            return "탈퇴";
        }

        switch (status) {
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

    // 상태 CSS 클래스용 메서드
    public String getStatusClass() {
        if (isWithdraw != null && isWithdraw) {
            return "status suspended";
        }

        switch (status) {
            case "ACTIVE":
                return "status active";
            case "SUSPENDED":
                return "status suspended";
            case "WITHDRAWN":
                return "status suspended";
            default:
                return "status active";
        }
    }
}
    package com.moodTrip.spring.domain.member.entity;

    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;

    //member 테이블과 연동

    @Entity
    @Table(name = "member") // 테이블 이름 명시 (안 적으면 클래스명 그대로 생성)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Member {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "member_pk")
        private Long memberPk;

        @Column(name = "member_id", nullable = false, length = 50)
        private String memberId;

        @Column(name = "member_pw", nullable = false, length = 100)
        private String memberPw;

        @Column(name = "member_phone", nullable = false, length = 20)
        private String memberPhone; // 전화번호

        @Column(name = "member_name", nullable = false, length = 30)
        private String memberName; // 이름

        @Column(name = "member_auth", nullable = false, length = 1)
        private String memberAuth; // 관리자분류 (ex: 'A','U' 등)

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt; // 등록일자

        @Column(name = "email", nullable = false, length = 100)
        private String email;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt; // 수정일자

        @Column(name = "is_withdraw", nullable = false)
        private Boolean isWithdraw; // 탈퇴여부

        @Column(name = "rpt_cnt")
        private Long rptCnt; // 신고횟수 (BIGINT)

        @Column(name = "rpt_rcvd_cnt")
        private Long rptRcvdCnt; // 신고 받은 횟수 (BIGINT)

        @Column(name = "social_type", length = 30)
        private String socialType; // 회원분류 (소셜로그인 구분 등)
    }
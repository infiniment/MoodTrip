package com.moodTrip.spring.domain.member.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity; // BaseEntity import 추가
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity { // BaseEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_pk")
    private Long memberPk;

    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId;

    @Column(name = "member_pw", nullable = false, length = 100)
    private String memberPw;

    @Column(name = "member_phone", nullable = false, length = 20)
    private String memberPhone;

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Column(name = "member_auth", nullable = false, length = 1)
    private String memberAuth;

    @Column(name = "email", nullable = true, length = 100)
    private String email;

    @Column(name = "is_withdraw", nullable = false)
    private Boolean isWithdraw;

    @Column(name = "rpt_cnt")
    private Long rptCnt;

    @Column(name = "rpt_rcvd_cnt")
    private Long rptRcvdCnt;

    @Column(name = "provider", length = 20)
    private String provider; // KAKAO, GOOGLE 등. 폼 회원이면 null

    @Column(name = "provider_id", length = 100)
    private String providerId; // 소셜 플랫폼 내 고유 ID. 폼 회원이면 null

    // 주의!
    // createdAt, updatedAt은 BaseEntity에서 상속받으므로 선언 필요 없음!
}
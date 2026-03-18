package com.moodTrip.spring.domain.member.entity;

import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.global.common.entity.BaseEntity; // BaseEntity import 추가
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Table(name = "member")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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

    //수연 추가 어드민에서 마지막 로그인 기록, 회원 상태 관리하기 위해 필요.
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;  // 최근 로그인

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;  // 회원 상태

    public enum MemberStatus {
        ACTIVE,      // 활성
        SUSPENDED,   // 정지
        WITHDRAWN    // 탈퇴
    }

    // r상우가 추가. 마이페이지에서 프로필 이미지 가져오기 위해 필요.
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @Builder.Default
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Room> createdRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<RoomMember> roomMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "applicant", fetch = FetchType.LAZY)
    private List<EnteringRoom> enteringRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<UserAttraction> userAttractions = new ArrayList<>();

    // 내가 신고한 멤버 신고 목록 (rpt_cnt와 연관)
    @Builder.Default
    @OneToMany(mappedBy = "fireReporter", fetch = FetchType.LAZY)
    private List<MemberFire> reportedMemberFires = new ArrayList<>();

    // 내가 당한 멤버 신고 목록 (rpt_rcvd_cnt와 연관)
    @Builder.Default
    @OneToMany(mappedBy = "reportedMember", fetch = FetchType.LAZY)
    private List<MemberFire> receivedMemberFires = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "fireReporter", fetch = FetchType.LAZY)
    private List<RoomFire> roomFires = new ArrayList<>();

    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile != null && profile.getMember() != this) {
            profile.setMember(this);
        }
    }

}

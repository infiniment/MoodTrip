package com.moodTrip.spring.domain.member.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "m_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    // Member와 일대일 관계 설정
    @OneToOne(fetch = FetchType.LAZY)  // 지연 로딩으로 성능 최적화
    @JoinColumn(name = "member_pk")
    private Member member;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "profile_bio")
    private String profileBio;
}

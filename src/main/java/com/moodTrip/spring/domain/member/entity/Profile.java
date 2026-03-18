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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_pk", nullable = false, unique = true)
    private Member member;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "profile_bio")
    private String profileBio;

    public void setMember(Member member) {
        this.member = member;
        if (member != null && member.getProfile() != this) {
            member.setProfile(this);
        }
    }
}

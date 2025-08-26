package com.moodTrip.spring.domain.attraction.entity;

import com.moodTrip.spring.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// ERD의 'UNIQUE (userid, attractionid)' 제약조건을 클래스 레벨에서 설정합니다.
@Table(name = "Muserattraction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_attraction_unique",
                        columnNames = {"memberpk", "attractionid"}
                )
        })
public class UserAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlistid")
    private Long id;

    // '어떤 회원'이 찜했는지에 대한 정보
    // 다대일(ManyToOne) 관계: 한 명의 회원은 여러 개의 찜을 할 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberpk", nullable = false)
    private Member member;

    // '어떤 관광지'를 찜했는지에 대한 정보
    // 다대일(ManyToOne) 관계: 하나의 관광지는 여러 회원에게 찜을 당할 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attractionid", nullable = false)
    private Attraction attraction;

    // 생성자: Service 로직에서 찜 정보를 생성할 때 사용됩니다.
    public UserAttraction(Member member, Attraction attraction) {
        this.member = member;
        this.attraction = attraction;
    }
}

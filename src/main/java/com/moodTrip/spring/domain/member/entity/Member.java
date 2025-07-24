package com.moodTrip.spring.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private Long id; // PK (Auto Increment)

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;
}
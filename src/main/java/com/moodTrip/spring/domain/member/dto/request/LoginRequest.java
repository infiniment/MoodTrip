package com.moodTrip.spring.domain.member.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // getter/setter/toString/equals/hashCode 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
public class LoginRequest {
    private String memberId;
    private String memberPw;
}
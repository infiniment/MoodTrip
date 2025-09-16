package com.moodTrip.spring.domain.member.dto.request;


import lombok.Data;

// 회원가입 요청 시 클라이언트로부터 전달받는 데이터를 담는 DTO.

@Data

public class MemberRequest {

    private String userId;          // 엔티티 memberId
    private String email;             // 엔티티 email
    private String password;          // 엔티티 memberPw
    private String passwordConfirm;   // 회원가입 시 입력값 확인용, 엔티티 불필요
    private String memberPhone;       // 엔티티 memberPhone 추가 시 DTO에도 추가
    private String nickname;        // 엔티티 nickname 추가 시 DTO에도 추가


    private boolean terms;            // 회원 약관 동의
    private boolean marketing;        // 마케팅 수신 동의
    private boolean marketingInfo;    // 세부 마케팅 수신 동의(옵션)

}
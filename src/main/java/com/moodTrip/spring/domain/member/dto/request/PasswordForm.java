package com.moodTrip.spring.domain.member.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
public class PasswordForm {
    private String newPassword;
    private String confirmPassword;


}
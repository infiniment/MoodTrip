package com.moodTrip.spring.domain.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendTempPasswordMail(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("무드트립 임시 비밀번호 안내");
        message.setText(
                "임시 비밀번호는 아래와 같습니다.\n\n" +
                        tempPassword +
                        "\n\n로그인 후 반드시 비밀번호를 변경해 주세요."
        );
        mailSender.send(message);
    }
}

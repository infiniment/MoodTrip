package com.moodTrip.spring.domain.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("임시 비밀번호 메일 발송 시, 올바른 수신자, 제목, 내용을 포함하여 send 메서드를 호출한다")
    void sendTempPasswordMail_shouldConstructAndSendEmailCorrectly() {
        // given
        String toEmail = "test@example.com";
        String tempPassword = "tempPassword123";

        // ArgumentCaptor를 사용하여 send 메서드에 전달된 SimpleMailMessage 객체를 캡처
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // when
        mailService.sendTempPasswordMail(toEmail, tempPassword);

        // then
        // 1. mailSender의 send 메서드가 정확히 1번 호출되었는지 검증
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // 2. 캡처된 메시지 객체의 내용을 검증
        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getTo()).containsExactly(toEmail); // 수신자 확인
        assertThat(capturedMessage.getSubject()).isEqualTo("무드트립 임시 비밀번호 안내"); // 제목 확인
        assertThat(capturedMessage.getText()).contains(tempPassword); // 본문에 임시 비밀번호가 포함되었는지 확인
    }
}
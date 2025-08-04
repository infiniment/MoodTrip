package com.moodTrip.spring.domain.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    private String originalName;   // 업로드된 원래 이름
    private String storedName;     // 서버 저장용 UUID 파일명
    private String filePath;       // 경로 (예: /uploads/2025/07/)
    private Long fileSize;         // 바이트 크기
    private String contentType;    // MIME 타입 (image/png 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notification notification;
}
package com.moodTrip.spring.domain.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "attachment")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;  // 원본 파일명
    private String storedName;    // 저장된 경로 (URL)
    private Long fileSize;
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id",
            foreignKey = @ForeignKey(name = "fk_attachment_notification"))
    private Notification notification;
}
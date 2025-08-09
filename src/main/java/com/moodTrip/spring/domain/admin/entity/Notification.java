package com.moodTrip.spring.domain.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate registeredDate = LocalDate.now();

    private Integer viewCount = 0;

    private Boolean isImportant;

    private Boolean isVisible;

    private String classification;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();
}


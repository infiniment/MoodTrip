package com.moodTrip.spring.domain.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faq")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "center_id")
    private Long id;


    //private FaqCategory category;
    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}

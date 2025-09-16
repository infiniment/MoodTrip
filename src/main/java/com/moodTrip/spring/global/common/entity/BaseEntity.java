package com.moodTrip.spring.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor  // JPA와 Lombok 빌더를 위한 기본 생성자 생성
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // Auditing(자동 시간 기록) 활성화
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false) // 생성일은 수정되지 않도록
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
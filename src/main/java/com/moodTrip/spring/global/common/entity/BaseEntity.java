package com.moodTrip.spring.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA 엔티티들이 상속받을 수 있는 공통 매핑 정보
@EntityListeners(AuditingEntityListener.class) // Auditing(자동 시간 기록) 활성화
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false) // 생성일은 수정되지 않도록
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
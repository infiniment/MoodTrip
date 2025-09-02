package com.moodTrip.spring.domain.support.entity;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "faq_vote",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"faq_id", "member_pk"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 FAQ에 대한 투표인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faq_id", nullable = false)
    private Faq faq;

    // 어떤 회원이 투표했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10)
    private VoteType voteType;  // YES / NO

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum VoteType {
        YES, NO
    }
}

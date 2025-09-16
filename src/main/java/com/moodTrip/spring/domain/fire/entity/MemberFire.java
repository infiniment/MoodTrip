package com.moodTrip.spring.domain.fire.entity;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFire extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fireId;

    // 신고한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    private Member fireReporter;

    // 신고 당한 멤버
    @ManyToOne(fetch = FetchType.LAZY)
    private Member reportedMember;

    // 신고가 발생한 방
    @ManyToOne(fetch = FetchType.LAZY)
    private Room targetRoom;

    @Enumerated(EnumType.STRING)
    private FireReason fireReason;

    private String fireMessage;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FireStatus fireStatus = FireStatus.PENDING;

    private String adminMemo;

    public enum FireReason {
        SPAM("스팸/광고"),
        INAPPROPRIATE("부적절한 내용"),
        FRAUD("사기/허위정보"),
        HARASSMENT("괴롭힘/혐오발언"),
        OTHER("기타");

        private final String description;
        FireReason(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static FireReason fromString(String value) {
            for (FireReason reason : FireReason.values()) {
                if (reason.name().equalsIgnoreCase(value)) return reason;
            }
            throw new IllegalArgumentException("유효하지 않은 신고 사유: " + value);
        }
    }

    public enum FireStatus {
        PENDING("처리 대기"),
        INVESTIGATING("조사 중"),
        RESOLVED("처리 완료"),
        DISMISSED("기각");

        private final String description;
        FireStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}

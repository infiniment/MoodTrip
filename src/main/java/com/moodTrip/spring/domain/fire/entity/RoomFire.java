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
public class RoomFire extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fireId;

    // 신고한 사람
    // ManyToOne: 한 명의 회원이 여러 건의 Fire를 할 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    private Member fireReporter;

    // 신고 당한 방
    // 하나의 방이 여러번 신고당할 수도 잇으니까 manyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    private Room firedRoom;

    // 신고 사유 select 박스 중에 한개
    @Enumerated(EnumType.STRING)  // ENUM을 문자열로 저장 아래 확인
    private FireReason fireReason;

    // 신고 내용
    private String fireMessage;

    // 신고 처리 상태
    @Enumerated(EnumType.STRING)
    @Builder.Default  // 기본 값은 "대기"
    private FireStatus fireStatus = FireStatus.PENDING;

    // 관리자가 신고 처리할 때 남기는 메모
    private String adminMemo;

    // 프론트에 있는 select 옵션들
    public enum FireReason {
        SPAM("스팸/광고"),
        INAPPROPRIATE("부적절한 내용"),
        FRAUD("사기/허위정보"),
        HARASSMENT("괴롭힘/혐오발언"),
        OTHER("기타");

        private final String description;  // 한국어 설명

        FireReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        // js에서 오는 사유를 ENUM으로 반환
        public static FireReason fromString(String value) {
            for (FireReason reason : FireReason.values()) {
                if (reason.name().equalsIgnoreCase(value)) {
                    return reason;
                }
            }
            throw new IllegalArgumentException("유효하지 않은 Fire 사유입니다: " + value);
        }
    }

    // 관리자가 신고를 어떻게 처리할지
    public enum FireStatus {
        PENDING("처리 대기"),        // Fire가 들어왔지만 아직 처리하지 않음
        INVESTIGATING("조사 중"),    // 관리자가 조사하고 있음
        RESOLVED("처리 완료"),       // Fire에 대한 조치를 취함 (방 삭제, 경고 등)
        DISMISSED("기각");          // Fire 내용이 부적절하다고 판단, 조치 없음

        private final String description;

        FireStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
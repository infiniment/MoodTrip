//package com.moodTrip.spring.domain.Member.Entity;
//
//
//import com.moodTrip.spring.domain.member.entity.Member;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.*;
//
///**
// * 🧪 Entity 단위테스트
// *
// * 🎯 테스트 목표:
// * 1. Entity 생성 및 필드 설정 테스트
// * 2. Builder 패턴 테스트
// * 3. Setter/Getter 테스트
// * 4. 연관관계 테스트
// *
// * 📚 순수 단위테스트:
// * - JPA 없이 순수 자바 객체로만 테스트
// * - DB 연결 없음
// * - 매우 빠른 실행
// */
//@DisplayName("Entity 단위테스트")
//class EntityUnitTest {
//
//    // ========================================
//    // 🧪 1. Member 엔티티 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("Member 엔티티 Builder 패턴으로 생성")
//    void member_빌더패턴_생성() {
//        // Given & When
//        Member member = Member.builder()
//                .memberPk(1L)
//                .memberId("testuser123")
//                .memberPw("password123")
//                .memberPhone("010-1234-5678")
//                .nickname("테스트유저")
//                .memberAuth("U")
//                .email("test@example.com")
//                .isWithdraw(false)
//                .rptCnt(0L)
//                .rptRcvdCnt(0L)
//                .provider("NORMAL")
//                .build();
//
//        // Then
//        assertThat(member.getMemberPk()).isEqualTo(1L);
//        assertThat(member.getMemberId()).isEqualTo("testuser123");
//        assertThat(member.getMemberPw()).isEqualTo("password123");
//        assertThat(member.getMemberPhone()).isEqualTo("010-1234-5678");
//        assertThat(member.getMemberName()).isEqualTo("테스트유저");
//        assertThat(member.getMemberAuth()).isEqualTo("U");
//        assertThat(member.getEmail()).isEqualTo("test@example.com");
//        assertThat(member.getIsWithdraw()).isFalse();
//        assertThat(member.getRptCnt()).isEqualTo(0L);
//        assertThat(member.getRptRcvdCnt()).isEqualTo(0L);
//        assertThat(member.getSocialType()).isEqualTo("NORMAL");
//    }
//}

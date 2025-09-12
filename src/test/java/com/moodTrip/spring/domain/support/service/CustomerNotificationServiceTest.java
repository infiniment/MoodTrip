// src/test/java/com/moodTrip/spring/domain/support/service/CustomerNotificationServiceTest.java
package com.moodTrip.spring.domain.support.service;

import com.moodTrip.spring.domain.admin.entity.Attachment;
import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.admin.repository.NotificationRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.support.entity.NotificationHelpful;
import com.moodTrip.spring.domain.support.repository.NotificationHelpfulRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerNotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationHelpfulRepository notificationHelpfulRepository;

    @InjectMocks CustomerNotificationService service;

    Notification n1, n2;

    @BeforeEach
    void setUp() {
        // 공지 1
        n1 = new Notification();
        n1.setNoticeId(1L);
        n1.setTitle("title-1");
        n1.setContent("content-1");
        n1.setIsVisible(true);
        n1.setIsImportant(false);
        n1.setRegisteredDate(LocalDate.of(2025, 9, 1)); // LocalDate 가정
        n1.setViewCount(0); // Integer 가정
        n1.setAttachments(new ArrayList<>());

        // 공지 2
        n2 = new Notification();
        n2.setNoticeId(2L);
        n2.setTitle("title-2");
        n2.setContent("content-2");
        n2.setIsVisible(false);
        n2.setIsImportant(true);
        n2.setRegisteredDate(LocalDate.of(2025, 9, 2));
        n2.setViewCount(7);
        n2.setAttachments(new ArrayList<>());
    }

    // ---------- findAll ----------
    @Test
    @DisplayName("findAll(): 모든 공지를 DTO로 매핑한다")
    void findAll_ok() {
        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        List<NotificationResponse> list = service.findAll();

        assertThat(list).hasSize(2);
        NotificationResponse r1 = list.get(0);
        NotificationResponse r2 = list.get(1);

        assertThat(r1.getId()).isEqualTo(1L);
        assertThat(r1.getTitle()).isEqualTo("title-1");
        assertThat(r1.getContent()).isEqualTo("content-1");
        assertThat(r1.getIsVisible()).isTrue();
        assertThat(r1.getIsImportant()).isFalse();
        assertThat(r1.getRegisteredDate()).isEqualTo(LocalDateTime.of(2025, 9, 1, 0, 0));
        assertThat(r1.getViewCount()).isEqualTo(0);
        assertThat(r1.getAttachments()).isEmpty();

        assertThat(r2.getId()).isEqualTo(2L);
        assertThat(r2.getIsVisible()).isFalse();
        assertThat(r2.getIsImportant()).isTrue();
        assertThat(r2.getRegisteredDate()).isEqualTo(LocalDateTime.of(2025, 9, 2, 0, 0));
        assertThat(r2.getViewCount()).isEqualTo(7);
    }

    // ---------- findById ----------
    @Test
    @DisplayName("findById(): 공지 1건을 DTO로 매핑하고 첨부도 포함한다")
    void findById_ok() {
        Attachment at = new Attachment();
        at.setOriginalName("a.txt");
        at.setStoredName("/uploads/a.txt");
        at.setNotification(n1);
        n1.getAttachments().add(at);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));

        NotificationResponse r = service.findById(1L);

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getAttachments()).hasSize(1);
        assertThat(r.getAttachments().get(0).getStoredName()).isEqualTo("/uploads/a.txt");
    }

    @Test
    @DisplayName("findById(): 존재하지 않으면 RuntimeException")
    void findById_notFound() {
        when(notificationRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("공지사항을 찾을 수 없습니다");
    }

    // ---------- increaseViewCount ----------
    @Test
    @DisplayName("increaseViewCount(): 조회수를 +1 하고 저장한다")
    void increaseViewCount_ok() {
        n1.setViewCount(12);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.increaseViewCount(1L);

        assertThat(n1.getViewCount()).isEqualTo(13);
        verify(notificationRepository).save(n1);
    }

    @Test
    @DisplayName("increaseViewCount(): 존재하지 않으면 RuntimeException")
    void increaseViewCount_notFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.increaseViewCount(1L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---------- searchByTitleOrContent ----------
    @Test
    @DisplayName("searchByTitleOrContent(): 제목/내용 검색 결과를 첨부 없이 DTO로 매핑한다")
    void searchByTitleOrContent_ok() {
        when(notificationRepository.findByTitleContainingOrContentContaining("hi", "hi"))
                .thenReturn(List.of(n1));

        List<NotificationResponse> list = service.searchByTitleOrContent("hi");

        assertThat(list).hasSize(1);
        NotificationResponse r = list.get(0);
        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getTitle()).isEqualTo("title-1");
        assertThat(r.getAttachments()).isNull(); // 검색용 변환은 attachments를 세팅하지 않음
    }

    // ---------- toggleHelpful ----------
    @Test
    @DisplayName("toggleHelpful(): 기존 추천이 있으면 삭제하고 false 반환")
    void toggleHelpful_existingDeletes() {
        Member m = new Member();
        NotificationHelpful existing = NotificationHelpful.builder()
                .notification(n1).member(m).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));
        when(notificationHelpfulRepository.findByNotificationAndMember(n1, m))
                .thenReturn(Optional.of(existing));

        boolean result = service.toggleHelpful(1L, m);

        assertThat(result).isFalse();
        verify(notificationHelpfulRepository).delete(existing);
        verify(notificationHelpfulRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleHelpful(): 기존 추천이 없으면 저장하고 true 반환")
    void toggleHelpful_savesWhenAbsent() {
        Member m = new Member();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));
        when(notificationHelpfulRepository.findByNotificationAndMember(n1, m))
                .thenReturn(Optional.empty());

        boolean result = service.toggleHelpful(1L, m);

        assertThat(result).isTrue();
        ArgumentCaptor<NotificationHelpful> cap = ArgumentCaptor.forClass(NotificationHelpful.class);
        verify(notificationHelpfulRepository).save(cap.capture());
        assertThat(cap.getValue().getNotification()).isSameAs(n1);
        assertThat(cap.getValue().getMember()).isSameAs(m);
    }

    @Test
    @DisplayName("toggleHelpful(): 공지 없으면 RuntimeException")
    void toggleHelpful_notFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.toggleHelpful(99L, new Member()))
                .isInstanceOf(RuntimeException.class);
    }

    // ---------- helpfulCount ----------
    @Test
    @DisplayName("helpfulCount(): 공지의 추천 수를 반환한다")
    void helpfulCount_ok() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(n2));
        when(notificationHelpfulRepository.countByNotification(n2)).thenReturn(42L);

        long count = service.helpfulCount(2L);

        assertThat(count).isEqualTo(42L);
    }

    @Test
    @DisplayName("helpfulCount(): 공지 없으면 RuntimeException")
    void helpfulCount_notFound() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.helpfulCount(2L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---------- isHelpfulByUser ----------
    @Test
    @DisplayName("isHelpfulByUser(): 유저가 추천했는지 여부를 반환한다")
    void isHelpfulByUser_ok() {
        Member m = new Member();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));
        when(notificationHelpfulRepository.existsByNotificationAndMember(n1, m)).thenReturn(true);

        boolean result = service.isHelpfulByUser(1L, m);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isHelpfulByUser(): 공지 없으면 RuntimeException")
    void isHelpfulByUser_notFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.isHelpfulByUser(1L, new Member()))
                .isInstanceOf(RuntimeException.class);
    }
}

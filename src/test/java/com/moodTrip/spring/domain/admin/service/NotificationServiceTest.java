// src/test/java/com/moodTrip/spring/domain/admin/service/NotificationServiceTest.java
package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.entity.Attachment;
import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.admin.repository.NotificationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock FileService fileService;

    @InjectMocks NotificationService notificationService;

    Notification existing;

    @BeforeEach
    void setUp() {
        existing = new Notification();
        existing.setNoticeId(1L);
        existing.setTitle("old title");
        existing.setContent("old content");
        existing.setClassification("GENERAL");
        existing.setIsImportant(false);
        existing.setIsVisible(true);
        existing.setAttachments(new ArrayList<>());
    }

    // ========== save ==========
    @Test
    @DisplayName("save(): 파일이 있으면 FileService로 저장하고 Attachment가 생성된다")
    void save_withFiles() throws IOException {
        // given
        MockMultipartFile f1 = new MockMultipartFile("files", "a.txt", "text/plain", "hi".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("files", "b.png", "image/png", new byte[]{1,2,3});
        when(fileService.saveFile(f1)).thenReturn("/uploads/uuid-a.txt");
        when(fileService.saveFile(f2)).thenReturn("/uploads/uuid-b.png");

        // save 시 id 세팅해 반환
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setNoticeId(99L);
            return n;
        });

        // when
        Long id = notificationService.save(
                "title", "content", "NOTICE", true, true, List.of(f1, f2));

        // then
        assertThat(id).isEqualTo(99L);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getContent()).isEqualTo("content");
        assertThat(saved.getClassification()).isEqualTo("NOTICE");
        assertThat(saved.getIsImportant()).isTrue();
        assertThat(saved.getIsVisible()).isTrue();

        assertThat(saved.getAttachments()).hasSize(2);
        Attachment a1 = saved.getAttachments().get(0);
        Attachment a2 = saved.getAttachments().get(1);
        assertThat(a1.getOriginalName()).isEqualTo("a.txt");
        assertThat(a1.getStoredName()).isEqualTo("/uploads/uuid-a.txt");
        assertThat(a1.getContentType()).isEqualTo("text/plain");
        assertThat(a1.getFileSize()).isEqualTo(f1.getSize());
        assertThat(a1.getNotification()).isSameAs(saved);

        assertThat(a2.getOriginalName()).isEqualTo("b.png");
        assertThat(a2.getStoredName()).isEqualTo("/uploads/uuid-b.png");
        assertThat(a2.getContentType()).isEqualTo("image/png");
        assertThat(a2.getFileSize()).isEqualTo(f2.getSize());
        assertThat(a2.getNotification()).isSameAs(saved);

        verify(fileService, times(1)).saveFile(f1);
        verify(fileService, times(1)).saveFile(f2);
    }

    @Test
    @DisplayName("save(): 파일이 없으면 빈 첨부로 저장된다")
    void save_withoutFiles() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setNoticeId(100L);
            return n;
        });

        Long id = notificationService.save(
                "t", "c", "GENERAL", false, true, List.of());

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getAttachments()).isEmpty();
        verifyNoInteractions(fileService);
    }

    @Test
    @DisplayName("save(): 파일 저장 IOException 발생 시 RuntimeException으로 감싼다")
    void save_fileIOException_wrapped() throws IOException {
        MockMultipartFile f1 = new MockMultipartFile("files", "a.txt", "text/plain", "hi".getBytes());
        when(fileService.saveFile(f1)).thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> notificationService.save(
                "t","c","GENERAL",false,true,List.of(f1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 저장 실패")
                .hasCauseInstanceOf(IOException.class);
    }

    // ========== findByIdForAdmin ==========
    @Test
    @DisplayName("findByIdForAdmin(): 존재하면 DTO 매핑 반환")
    void findByIdForAdmin_ok() {
        Attachment at = new Attachment();
        at.setOriginalName("a.txt");
        at.setStoredName("/uploads/a.txt");
        at.setContentType("text/plain");
        at.setFileSize(2L);
        at.setNotification(existing);
        existing.getAttachments().add(at);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(existing));

        NotificationResponse res = notificationService.findByIdForAdmin(1L);

        assertThat(res.getNoticeId()).isEqualTo(1L);
        assertThat(res.getTitle()).isEqualTo("old title");
        assertThat(res.getAttachments()).hasSize(1);
        assertThat(res.getAttachments().get(0).getStoredName()).isEqualTo("/uploads/a.txt");
    }

    @Test
    @DisplayName("findByIdForAdmin(): 없으면 404 예외")
    void findByIdForAdmin_notFound() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.findByIdForAdmin(2L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== findAll ==========
    @Test
    @DisplayName("findAll(): 목록을 DTO로 매핑하되 attachments는 null로 둔다")
    void findAll_mapsWithoutAttachments() {
        Notification n1 = new Notification();
        n1.setNoticeId(10L);
        n1.setTitle("t1"); n1.setContent("c1");
        n1.setClassification("GENERAL");
        n1.setIsImportant(false); n1.setIsVisible(true);

        Notification n2 = new Notification();
        n2.setNoticeId(11L);
        n2.setTitle("t2"); n2.setContent("c2");
        n2.setClassification("NOTICE");
        n2.setIsImportant(true); n2.setIsVisible(false);

        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        List<NotificationResponse> list = notificationService.findAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getNoticeId()).isEqualTo(10L);
        assertThat(list.get(0).getAttachments()).isNull();
        assertThat(list.get(1).getNoticeId()).isEqualTo(11L);
        assertThat(list.get(1).getAttachments()).isNull();
    }

    // ========== update ==========
    @Test
    @DisplayName("update(): 기존 첨부를 삭제(FileService.deleteFile) 후 새 파일로 교체한다")
    void update_replaceFiles() throws IOException {
        // 기존 첨부
        Attachment old1 = new Attachment();
        old1.setStoredName("/uploads/old-1.txt");
        old1.setNotification(existing);
        existing.getAttachments().add(old1);

        Attachment old2 = new Attachment();
        old2.setStoredName("/uploads/old-2.txt");
        old2.setNotification(existing);
        existing.getAttachments().add(old2);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(existing));

        // 새 파일
        MockMultipartFile nf1 = new MockMultipartFile("files", "newA.txt", "text/plain", "A".getBytes());
        MockMultipartFile nf2 = new MockMultipartFile("files", "newB.png", "image/png", new byte[]{9});
        when(fileService.saveFile(nf1)).thenReturn("/uploads/newA.txt");
        when(fileService.saveFile(nf2)).thenReturn("/uploads/newB.png");

        // when
        notificationService.update(1L, "t2", "c2", "NOTICE", true, false, List.of(nf1, nf2));

        // then: 기존 파일 삭제 호출
        verify(fileService).deleteFile("/uploads/old-1.txt");
        verify(fileService).deleteFile("/uploads/old-2.txt");

        // 저장 호출 및 내용 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("t2");
        assertThat(saved.getContent()).isEqualTo("c2");
        assertThat(saved.getClassification()).isEqualTo("NOTICE");
        assertThat(saved.getIsImportant()).isTrue();
        assertThat(saved.getIsVisible()).isFalse();

        assertThat(saved.getAttachments()).hasSize(2);
        assertThat(saved.getAttachments())
                .extracting(Attachment::getStoredName)
                .containsExactlyInAnyOrder("/uploads/newA.txt", "/uploads/newB.png");
    }

    @Test
    @DisplayName("update(): 공지 존재하지 않으면 404")
    void update_notFound() {
        when(notificationRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.update(123L, "t","c","G", false,true, List.of()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== delete ==========
    @Test
    @DisplayName("delete(): 첨부 파일을 먼저 삭제하고 엔티티를 삭제한다")
    void delete_ok() {
        Attachment a1 = new Attachment(); a1.setStoredName("/uploads/x.txt"); a1.setNotification(existing);
        Attachment a2 = new Attachment(); a2.setStoredName("/uploads/y.png"); a2.setNotification(existing);
        existing.getAttachments().add(a1);
        existing.getAttachments().add(a2);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(existing));

        notificationService.delete(1L);

        verify(fileService).deleteFile("/uploads/x.txt");
        verify(fileService).deleteFile("/uploads/y.png");
        verify(notificationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete(): 공지 없으면 404")
    void delete_notFound() {
        when(notificationRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.delete(9L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}

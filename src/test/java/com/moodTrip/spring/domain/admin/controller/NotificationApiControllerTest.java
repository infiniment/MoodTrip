package com.moodTrip.spring.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.service.NotificationService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("NotificationApiController 단위 테스트")
class NotificationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SecurityUtil securityUtil;

    @MockBean
    private EmotionService emotionService;

    private NotificationResponse testNotification1;
    private NotificationResponse testNotification2;
    private List<NotificationResponse> testNotificationList;

    @BeforeEach
    void setUp() {
        testNotification1 = new NotificationResponse();
        testNotification1.setNoticeId(1L);
        testNotification1.setTitle("시스템 점검 안내");
        testNotification1.setContent("시스템 점검으로 인한 서비스 일시 중단 안내");
        testNotification1.setClassification("시스템");
        testNotification1.setIsImportant(true);
        testNotification1.setIsVisible(true);

        testNotification2 = new NotificationResponse();
        testNotification2.setNoticeId(2L);
        testNotification2.setTitle("신규 서비스 출시");
        testNotification2.setContent("새로운 여행 매칭 서비스가 출시되었습니다");
        testNotification2.setClassification("서비스");
        testNotification2.setIsImportant(false);
        testNotification2.setIsVisible(true);

        testNotificationList = Arrays.asList(testNotification1, testNotification2);
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (파일 없음)")
    void createNotification_Success_WithoutFiles() throws Exception {
        // Given
        given(notificationService.save(
                eq("새로운 공지사항"),
                eq("공지사항 내용입니다"),
                eq("일반"),
                eq(false),
                eq(true),
                eq(null)
        )).willReturn(3L);

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications")
                        .param("title", "새로운 공지사항")
                        .param("content", "공지사항 내용입니다")
                        .param("classification", "일반")
                        .param("isImportant", "false")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("3"));

        verify(notificationService, times(1)).save(
                "새로운 공지사항", "공지사항 내용입니다", "일반", false, true, null);
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (파일 포함)")
    void createNotification_Success_WithFiles() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.txt", "text/plain", "test content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.txt", "text/plain", "test content 2".getBytes());

        given(notificationService.save(
                eq("파일 포함 공지사항"),
                eq("파일이 포함된 공지사항입니다"),
                eq("중요"),
                eq(true),
                eq(true),
                anyList()
        )).willReturn(4L);

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications")
                        .file(file1)
                        .file(file2)
                        .param("title", "파일 포함 공지사항")
                        .param("content", "파일이 포함된 공지사항입니다")
                        .param("classification", "중요")
                        .param("isImportant", "true")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("4"));

        verify(notificationService, times(1)).save(
                eq("파일 포함 공지사항"), eq("파일이 포함된 공지사항입니다"),
                eq("중요"), eq(true), eq(true), anyList());
    }

    @Test
    @DisplayName("공지사항 생성 - 필수 파라미터 누락")
    void createNotification_MissingRequiredParams() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications")
                        .param("title", "제목만 있는 공지사항"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).save(anyString(), anyString(), anyString(),
                any(Boolean.class), any(Boolean.class), anyList());
    }

    @Test
    @DisplayName("공지사항 목록 조회 - 성공")
    void getNotificationList_Success() throws Exception {
        // Given
        given(notificationService.findAll()).willReturn(testNotificationList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].noticeId").value(1L))
                .andExpect(jsonPath("$[0].title").value("시스템 점검 안내"))
                .andExpect(jsonPath("$[0].classification").value("시스템"))
                .andExpect(jsonPath("$[0].isImportant").value(true))
                .andExpect(jsonPath("$[1].noticeId").value(2L))
                .andExpect(jsonPath("$[1].title").value("신규 서비스 출시"))
                .andExpect(jsonPath("$[1].classification").value("서비스"))
                .andExpect(jsonPath("$[1].isImportant").value(false));

        verify(notificationService, times(1)).findAll();
    }

    @Test
    @DisplayName("공지사항 목록 조회 - 빈 목록")
    void getNotificationList_EmptyList() throws Exception {
        // Given
        given(notificationService.findAll()).willReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(notificationService, times(1)).findAll();
    }

    @Test
    @DisplayName("공지사항 단건 조회 - 성공")
    void getNotification_Success() throws Exception {
        // Given
        given(notificationService.findByIdForAdmin(1L)).willReturn(testNotification1);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/notifications/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.noticeId").value(1L))
                .andExpect(jsonPath("$.title").value("시스템 점검 안내"))
                .andExpect(jsonPath("$.content").value("시스템 점검으로 인한 서비스 일시 중단 안내"))
                .andExpect(jsonPath("$.classification").value("시스템"))
                .andExpect(jsonPath("$.isImportant").value(true))
                .andExpect(jsonPath("$.isVisible").value(true));

        verify(notificationService, times(1)).findByIdForAdmin(1L);
    }

    @Test
    @DisplayName("공지사항 단건 조회 - 존재하지 않는 ID")
    void getNotification_NotFound() throws Exception {
        // Given
        given(notificationService.findByIdForAdmin(999L))
                .willThrow(new RuntimeException("Notification not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/notifications/999"))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).findByIdForAdmin(999L);
    }

    @Test
    @DisplayName("공지사항 수정 - 성공 (파일 없음)")
    void updateNotification_Success_WithoutFiles() throws Exception {
        // Given
        doNothing().when(notificationService).update(
                eq(1L),
                eq("수정된 공지사항 제목"),
                eq("수정된 공지사항 내용"),
                eq("일반"),
                eq(false),
                eq(true),
                eq(null)
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications/1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정된 공지사항 제목")
                        .param("content", "수정된 공지사항 내용")
                        .param("classification", "일반")
                        .param("isImportant", "false")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(notificationService, times(1)).update(
                1L, "수정된 공지사항 제목", "수정된 공지사항 내용", "일반", false, true, null);
    }

    @Test
    @DisplayName("공지사항 수정 - 성공 (파일 포함)")
    void updateNotification_Success_WithFiles() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "update.txt", "text/plain", "updated content".getBytes());

        doNothing().when(notificationService).update(
                eq(1L),
                eq("수정된 제목"),
                eq("수정된 내용"),
                eq("중요"),
                eq(true),
                eq(true),
                anyList()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications/1")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정된 제목")
                        .param("content", "수정된 내용")
                        .param("classification", "중요")
                        .param("isImportant", "true")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(notificationService, times(1)).update(
                eq(1L), eq("수정된 제목"), eq("수정된 내용"), eq("중요"),
                eq(true), eq(true), anyList());
    }

    @Test
    @DisplayName("공지사항 수정 - 존재하지 않는 ID")
    void updateNotification_NotFound() throws Exception {
        // Given: files는 null이 아니라 '빈 리스트'로 들어올 수 있으니 이렇게 매칭
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).update(
                        eq(999L),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Boolean.class),
                        any(Boolean.class),
                        argThat(list -> list == null || list.isEmpty()) // ★ 핵심
                );

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications/999")
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("classification", "일반")
                        .param("isImportant", "false")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isNotFound()); // ★ 404 기대
    }

    @Test
    @DisplayName("공지사항 삭제 - 성공")
    void deleteNotification_Success() throws Exception {
        // Given
        doNothing().when(notificationService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/notifications/1"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(notificationService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("공지사항 삭제 - 존재하지 않는 ID")
    void deleteNotification_NotFound() throws Exception {
        // Given
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).delete(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/notifications/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).delete(999L);
    }


    @Test
    @DisplayName("공지사항 생성 - 잘못된 Boolean 값")
    void createNotification_InvalidBooleanValue() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications")
                        .param("title", "테스트 제목")
                        .param("content", "테스트 내용")
                        .param("classification", "일반")
                        .param("isImportant", "invalid_boolean")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).save(anyString(), anyString(), anyString(),
                any(Boolean.class), any(Boolean.class), anyList());
    }

    @Test
    @DisplayName("공지사항 생성 - 큰 파일 업로드")
    void createNotification_LargeFile() throws Exception {
        // Given
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile largeFile = new MockMultipartFile("files", "large.txt", "text/plain", largeContent);

        given(notificationService.save(
                anyString(), anyString(), anyString(),
                any(Boolean.class), any(Boolean.class), anyList()
        )).willReturn(5L);

        // When & Then
        mockMvc.perform(multipart("/api/v1/admin/notifications")
                        .file(largeFile)
                        .param("title", "큰 파일 테스트")
                        .param("content", "큰 파일이 포함된 공지사항")
                        .param("classification", "테스트")
                        .param("isImportant", "false")
                        .param("isVisible", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
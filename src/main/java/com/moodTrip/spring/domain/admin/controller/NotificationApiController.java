package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;

    // 공지사항 생성
    @PostMapping
    public ResponseEntity<Long> createNotification(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("classification") String classification,
            @RequestParam("isImportant") Boolean isImportant,
            @RequestParam("isVisible") Boolean isVisible,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        Long noticeId = notificationService.save(title, content, classification, isImportant, isVisible, files);
        return ResponseEntity.ok(noticeId);
    }
    // 공지사항 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationList() {
        List<NotificationResponse> notifications = notificationService.findAll();
        return ResponseEntity.ok(notifications);
    }


    // 공지사항 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long noticeId) {
        NotificationResponse notification = notificationService.findByIdForAdmin(noticeId);
        return ResponseEntity.ok(notification);
    }

    // 공지사항 수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<Void> updateNotification(
            @PathVariable Long noticeId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("classification") String classification,
            @RequestParam("isImportant") Boolean isImportant,
            @RequestParam("isVisible") Boolean isVisible,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        notificationService.update(noticeId, title, content, classification, isImportant, isVisible, files);
        return ResponseEntity.ok().build();
    }

    // 공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long noticeId) {
        notificationService.delete(noticeId);
        return ResponseEntity.ok().build();
    }
}
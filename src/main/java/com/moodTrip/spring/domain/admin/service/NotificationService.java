package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.entity.Attachment;
import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.admin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FileService fileService;  // FileService 사용

    public Long save(String title, String content, String classification,
                     Boolean isImportant, Boolean isVisible,
                     List<MultipartFile> files) {

        Notification n = new Notification();
        n.setTitle(title);
        n.setContent(content);
        n.setClassification(classification);
        n.setIsImportant(isImportant);
        n.setIsVisible(isVisible);

        // 파일 처리 - FileService 사용
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // FileService를 사용해서 파일 저장
                        String savedPath = fileService.saveFile(file);

                        // Attachment 엔티티 생성
                        Attachment attachment = new Attachment();
                        attachment.setOriginalName(file.getOriginalFilename());
                        attachment.setStoredName(savedPath);  // URL 경로 저장
                        attachment.setFileSize(file.getSize());
                        attachment.setContentType(file.getContentType());
                        attachment.setNotification(n);

                        n.getAttachments().add(attachment);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        Notification saved = notificationRepository.save(n);
        return saved.getNoticeId();
    }

    @Transactional(readOnly = true)
    public NotificationResponse findByIdForAdmin(Long noticeId) {
        Notification notification = notificationRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        NotificationResponse response = new NotificationResponse();
        response.setNoticeId(notification.getNoticeId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setClassification(notification.getClassification());
        response.setIsImportant(notification.getIsImportant());
        response.setIsVisible(notification.getIsVisible());
        response.setRegisteredDate(notification.getRegisteredDate());
        response.setViewCount(notification.getViewCount());
        response.setAttachments(notification.getAttachments());

        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(notification -> {
                    NotificationResponse response = new NotificationResponse();
                    response.setNoticeId(notification.getNoticeId());
                    response.setTitle(notification.getTitle());
                    response.setContent(notification.getContent());
                    response.setClassification(notification.getClassification());
                    response.setIsImportant(notification.getIsImportant());
                    response.setIsVisible(notification.getIsVisible());
                    response.setRegisteredDate(notification.getRegisteredDate());
                    response.setViewCount(notification.getViewCount());
                    response.setAttachments(null);  // Lazy Loading 방지
                    return response;
                })
                .collect(Collectors.toList());
    }

    public void update(Long noticeId, String title, String content, String classification,
                       Boolean isImportant, Boolean isVisible, List<MultipartFile> files) {
        Notification notification = notificationRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notification.setTitle(title);
        notification.setContent(content);
        notification.setClassification(classification);
        notification.setIsImportant(isImportant);
        notification.setIsVisible(isVisible);

        // 파일 업데이트 로직 추가
        if (files != null && !files.isEmpty()) {
            // 기존 파일 삭제 (선택사항)
            for (Attachment oldAttachment : notification.getAttachments()) {
                fileService.deleteFile(oldAttachment.getStoredName());
            }
            notification.getAttachments().clear();

            // 새 파일 추가
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String savedPath = fileService.saveFile(file);

                        Attachment attachment = new Attachment();
                        attachment.setOriginalName(file.getOriginalFilename());
                        attachment.setStoredName(savedPath);
                        attachment.setFileSize(file.getSize());
                        attachment.setContentType(file.getContentType());
                        attachment.setNotification(notification);

                        notification.getAttachments().add(attachment);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        notificationRepository.save(notification);
    }

    public void delete(Long noticeId) {
        Notification notification = notificationRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        // 파일 삭제
        for (Attachment attachment : notification.getAttachments()) {
            fileService.deleteFile(attachment.getStoredName());
        }

        notificationRepository.deleteById(noticeId);
    }
}
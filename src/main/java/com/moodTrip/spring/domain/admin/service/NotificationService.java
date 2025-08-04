package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.entity.Attachment;
import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.admin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Value("${file.upload.path:./uploads}")
    private String uploadRootPath;

    public Long save(String title, String content, String classification,
                     Boolean isImportant, Boolean isVisible,
                     List<MultipartFile> files) {

        Notification n = new Notification();
        n.setTitle(title);
        n.setContent(content);
        n.setClassification(classification);
        n.setIsImportant(isImportant);
        n.setIsVisible(isVisible);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                try {
                    String originalName = file.getOriginalFilename();
                    String uuid = UUID.randomUUID().toString();
                    String storedName = uuid + "_" + originalName;

                    File dir = new File(uploadRootPath);
                    if (!dir.exists()) dir.mkdirs();

                    String fullPath = uploadRootPath + "/" + storedName;
                    file.transferTo(new File(fullPath));

                    Attachment attachment = new Attachment();
                    attachment.setOriginalName(originalName);
                    attachment.setStoredName(storedName);
                    attachment.setFilePath(uploadRootPath);
                    attachment.setFileSize(file.getSize());
                    attachment.setContentType(file.getContentType());
                    attachment.setNotification(n);

                    n.getAttachments().add(attachment);
                } catch (IOException e) {
                    throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
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

        return new NotificationResponse(
                notification.getNoticeId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getClassification(),
                notification.getIsImportant(),
                notification.getIsVisible(),
                null
                //notification.getAttachments()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(notification -> new NotificationResponse(
                        notification.getNoticeId(),
                        notification.getTitle(),
                        notification.getContent(),
                        notification.getClassification(),
                        notification.getIsImportant(),
                        notification.getIsVisible(),
                        notification.getAttachments()
                ))
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

        // TODO: 파일 업데이트 로직 추가

        notificationRepository.save(notification);
    }

    public void delete(Long noticeId) {
        notificationRepository.deleteById(noticeId);
    }
}
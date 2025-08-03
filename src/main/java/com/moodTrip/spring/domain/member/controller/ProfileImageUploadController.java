package com.moodTrip.spring.domain.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles/me/profileImage")
public class ProfileImageUploadController {

    // 예: 저장 경로 → 프로젝트 루트/uploads (운영에서는 외부 경로 또는 S3 등 사용)
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("프로필 이미지 업로드 요청 받음: {}", file.getOriginalFilename());

        // 1️⃣ 파일 유효성 검사
        if (file.isEmpty()) {
            log.warn("파일이 비어 있음");
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어 있습니다."));
        }

        try {
            log.info("파일 업로드 요청 받음: {}", file.getOriginalFilename());

            // 1️⃣ 파일 이름 준비
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID() + extension;

            // 2️⃣ 절대 경로로 uploads 디렉토리 설정
            String baseDir = "C:/moodTrip/MoodTrip";
            Path uploadPath = Paths.get(baseDir, "uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 3️⃣ URL은 정적으로 열어주는 경로로 설정 (브라우저 접근 경로)
            String fileUrl = "/uploads/" + filename;

            log.info("업로드 완료 → {}", fileUrl);
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl));

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "파일 업로드 실패"));
        }
    }

    // 파일 확장자 추출 유틸
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : "";
    }
}

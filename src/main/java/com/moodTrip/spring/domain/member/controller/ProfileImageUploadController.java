package com.moodTrip.spring.domain.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles/me/profileImage")
public class ProfileImageUploadController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("프로필 이미지 업로드 요청 받음: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("파일이 비어 있음");
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어 있습니다."));
        }

        try {
            log.info("파일 업로드 시작: {}", file.getOriginalFilename());

            // 🔥 간단하게: 프로젝트 루트에 uploads 폴더 생성
            Path uploadPath = createUploadsDirectory();

            // 파일 이름 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID() + extension;

            // 파일 경로 생성
            Path filePath = uploadPath.resolve(filename);
            log.info("🛠 저장 시도할 파일 경로: {}", filePath.toAbsolutePath());

            try {
                file.transferTo(filePath.toFile());
                log.info("✅ 실제 파일 저장 완료: {}", filePath.toAbsolutePath());
            } catch (IOException e) {
                log.error("❌ 파일 저장 실패", e);
                return ResponseEntity.internalServerError().body(Map.of(
                        "error", "파일 저장 실패: " + e.getMessage()
                ));
            }

            // 웹 접근 URL 생성
            String fileUrl = "/uploads/" + filename;

            log.info("업로드 완료 → 경로: {}, URL: {}", filePath.toAbsolutePath(), fileUrl);

            return ResponseEntity.ok(Map.of(
                    "imageUrl", fileUrl,
                    "message", "이미지 업로드 성공"
            ));

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "파일 업로드 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔥 간단한 uploads 폴더 생성
     * 프로젝트 루트 디렉토리에 uploads 폴더를 만듭니다
     */
    private Path createUploadsDirectory() throws Exception {
        // 현재 프로젝트 루트 경로 가져오기
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, UPLOAD_DIR);

        log.info("📁 업로드 경로: {}", uploadPath.toAbsolutePath());

        // uploads 폴더가 없으면 생성
        if (!Files.exists(uploadPath)) {
            log.info("📁 uploads 폴더가 없어서 생성합니다...");

            try {
                Files.createDirectories(uploadPath);
                log.info("✅ uploads 폴더 생성 성공: {}", uploadPath.toAbsolutePath());
            } catch (Exception e) {
                log.error("❌ uploads 폴더 생성 실패", e);
                throw new Exception("uploads 폴더를 생성할 수 없습니다: " + e.getMessage());
            }
        } else {
            log.info("✅ uploads 폴더가 이미 존재합니다");
        }

        return uploadPath;
    }

    // 파일 확장자 추출 유틸
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : "";
    }
}
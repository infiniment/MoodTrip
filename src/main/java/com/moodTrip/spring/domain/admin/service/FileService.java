package com.moodTrip.spring.domain.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    public String saveFile(MultipartFile file) throws IOException {
        // 업로드 폴더 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일명 중복 방지를 위해 UUID 사용
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path filePath = Paths.get(uploadPath, savedFileName);
        Files.write(filePath, file.getBytes());

        // 저장된 파일 경로 반환 (DB에 저장할 값)
        return "/uploads/" + savedFileName;
    }

//    public void deleteFile(String filePath) {
//        try {
//            Path path = Paths.get("." + filePath);
//            Files.deleteIfExists(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // FileService.java
    public void deleteFile(String filePath) {
        try {
            Path path;
            // "/uploads/xxx.ext" 형태면 uploadPath로 매핑
            final String prefix = "/uploads/";
            if (filePath != null && filePath.startsWith(prefix)) {
                String filename = filePath.substring(prefix.length());
                path = Paths.get(uploadPath, filename);
            } else {
                // 그 외(절대/상대 경로)는 그대로 사용
                path = Paths.get(filePath);
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
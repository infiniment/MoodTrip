// src/test/java/com/moodTrip/spring/domain/admin/service/FileServiceTest.java
package com.moodTrip.spring.domain.admin.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    FileService fileService;
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("upload-test");
        fileService = new FileService();
        // uploadPath 주입 (리플렉션 이용)
        try {
            var field = FileService.class.getDeclaredField("uploadPath");
            field.setAccessible(true);
            field.set(fileService, tempDir.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempDir);
    }

    @Test
    @DisplayName("saveFile()은 업로드 경로에 UUID 파일을 저장하고 /uploads/ 경로를 반환한다")
    void saveFile_ok() throws IOException {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "Hello World".getBytes()
        );

        // when
        String returnedPath = fileService.saveFile(multipartFile);

        // then
        assertThat(returnedPath).startsWith("/uploads/");
        // 실제 저장된 파일 존재 확인
        String savedFileName = returnedPath.replace("/uploads/", "");
        Path savedPath = tempDir.resolve(savedFileName);
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(Files.readString(savedPath)).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("saveFile()은 확장자가 있는 파일만 처리한다")
    void saveFile_withExtension() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "data.json", "application/json", "{}".getBytes()
        );

        String returnedPath = fileService.saveFile(multipartFile);

        assertThat(returnedPath).endsWith(".json");
    }

    @Test
    @DisplayName("deleteFile(): saveFile로 저장한 파일 경로(/uploads/...)를 넘기면 실제 파일이 삭제된다")
    void deleteFile_existing() throws IOException {
        // 1) 저장
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "toDelete.txt", "text/plain", "delete me".getBytes()
        );
        String dbPath = fileService.saveFile(multipartFile); // "/uploads/{uuid}.txt"

        // 실제 저장 위치 확인
        String savedFileName = dbPath.replace("/uploads/", "");
        Path savedPath = tempDir.resolve(savedFileName);
        assertThat(Files.exists(savedPath)).isTrue();

        // 2) 삭제
        fileService.deleteFile(dbPath);

        // 3) 삭제 확인
        assertThat(Files.exists(savedPath)).isFalse();
    }

    @Test
    @DisplayName("deleteFile(): 존재하지 않는 파일이어도 예외 없이 동작")
    void deleteFile_nonExisting() {
        String dbPath = "/uploads/not-exist.txt";
        assertThatCode(() -> fileService.deleteFile(dbPath))
                .doesNotThrowAnyException();
    }


}

package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.service.AttractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupSyncRunner implements ApplicationRunner {

    private final AttractionService service;

    @Value("${moodtrip.sync.on-start:false}")
    private boolean onStart;

    @Value("${moodtrip.sync.page-size:500}")
    private int pageSize;

    @Value("${moodtrip.sync.pause-millis:0}")
    private long pauseMillis;

    @Value("${moodtrip.sync.areas:ALL}")
    private String areasProp;

    private static final List<Integer> ALL_AREA_CODES = List.of(
            1,  // 서울
            2,  // 인천
            3,  // 대전
            4,  // 대구
            5,  // 광주
            6,  // 부산
            7,  // 울산
            8,  // 세종
            31, // 경기
            32, // 강원
            33, // 충북
            34, // 충남
            35, // 경북
            36, // 경남
            37, // 전북
            38, // 전남
            39  // 제주
    );

    @Override
    public void run(ApplicationArguments args) {
        if (!onStart) {
            log.info("Startup sync disabled (moodtrip.sync.on-start=false)");
            return;
        }

        List<Integer> targetAreas = resolveAreas(areasProp);
        log.info("Startup sync begin: ONLY contentTypeId 12/14, pageSize={}, pauseMillis={}, areas={}",
                pageSize, pauseMillis, targetAreas);

        int totalCreated = 0;
        for (int area : targetAreas) {
            try {
                int created = service.syncAreaBasedListOnly12And14(area, null, pageSize, pauseMillis);
                totalCreated += created;
            } catch (Exception e) {
                log.error("Startup sync failed for areaCode={}", area, e);
            }

        }
        log.info("Startup sync done. totalCreated={}", totalCreated);
    }

    private List<Integer> resolveAreas(String prop) {
        if (prop == null || prop.isBlank() || "ALL".equalsIgnoreCase(prop.trim())) {
            return ALL_AREA_CODES;
        }
        try {
            return Arrays.stream(prop.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (Exception ignore) {
            // 포맷이 잘못되면 전체로 폴백
            return ALL_AREA_CODES;
        }
    }
}

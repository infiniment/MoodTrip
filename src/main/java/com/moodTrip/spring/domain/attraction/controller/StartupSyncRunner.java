package com.moodTrip.spring.domain.attraction.config;

import com.moodTrip.spring.domain.attraction.service.AttractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupSyncRunner implements ApplicationRunner {

    private final AttractionService service;

    @Value("${moodtrip.sync.on-start:false}")
    private boolean onStart;

    @Value("${moodtrip.sync.exclude-lodging:true}")
    private boolean excludeLodging;

    @Value("${moodtrip.sync.content-type-id:0}")
    private int contentTypeId;

    @Value("${moodtrip.sync.page-size:500}")
    private int pageSize;

    @Value("${moodtrip.sync.pause-millis:0}")
    private long pauseMillis;

    @Override
    public void run(ApplicationArguments args) {
        if (!onStart) {
            log.info("Startup sync disabled (moodtrip.sync.on-start=false)");
            return;
        }
        Integer contentType = (contentTypeId >= 12 && contentTypeId <= 99) ? contentTypeId : null;
        Set<Integer> excludes = new HashSet<>();
        if (excludeLodging) excludes.add(12);
        log.info("Startup sync begin: contentTypeId={}, pageSize={}, pauseMillis={}, excludes={}",
                contentType, pageSize, pauseMillis, excludes);

        int totalCreated = 0;
        for (int area = 1; area <= 99; area++) {
            try {
                totalCreated += service.syncAreaBasedListExcluding(
                        area, null, contentType, pageSize, pauseMillis, excludes
                );
            } catch (Exception e) {
                log.warn("Startup sync failed for areaCode={}: {}", area, e.toString());
            }
        }
        log.info("Startup sync done. totalCreated={}", totalCreated);
    }
}

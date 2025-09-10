package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.attraction.repository.AttractionIntroRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class IntroDebugController {

    private final AttractionService attractionService;
    private final AttractionIntroRepository introRepository;

    /** 상세 페이지 진입 시 동기화 흐름을 그대로 태운 뒤, DB에 저장된 intro 필드들을 JSON으로 보여준다. */
    @GetMapping("/intro/{contentId}")
    public ResponseEntity<?> showIntroSnapshot(@PathVariable Long contentId) {
        // ✅ 오버로드(1개) 호출로 동기화 트리거
        try {
            attractionService.getDetailResponse(contentId);
        } catch (Throwable ignore) {
            // 상세 조립 실패여도 intro 저장은 되었을 수 있으므로 조용히 진행
        }

        AttractionIntro i = introRepository.findById(contentId).orElse(null);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("contentId", contentId);
        out.put("exists", i != null);

        if (i != null) {
            out.put("infocenter", i.getInfocenter());
            out.put("overview", i.getOverview());
            out.put("usetime", i.getUsetime());
            out.put("restdate", i.getRestdate());
            out.put("parking", i.getParking());
            out.put("expagerange", i.getExpagerange());
            out.put("agelimit", i.getAgelimit());

            Map<String, Object> a11y = new LinkedHashMap<>();
            a11y.put("wheelchair", i.getWheelchair());
            a11y.put("elevator", i.getElevator());
            a11y.put("braileblock", i.getBraileblock());
            a11y.put("exit_access", i.getExit()); // 엔티티는 exit, 컬럼은 exit_access
            a11y.put("guidesystem", i.getGuidesystem());
            a11y.put("signguide", i.getSignguide());
            a11y.put("videoguide", i.getVideoguide());
            a11y.put("audioguide", i.getAudioguide());
            a11y.put("bigprint", i.getBigprint());
            a11y.put("brailepromotion", i.getBrailepromotion());
            a11y.put("helpdog", i.getHelpdog());
            a11y.put("infantsfamilyetc", i.getInfantsfamilyetc());
            a11y.put("hearingroom", i.getHearingroom());
            a11y.put("hearinghandicapetc", i.getHearinghandicapetc());
            a11y.put("blindhandicapetc", i.getBlindhandicapetc());
            a11y.put("handicapetc", i.getHandicapetc());
            out.put("a11y", a11y);

            long a11yFilled = Stream.of(
                    i.getWheelchair(), i.getElevator(), i.getBraileblock(), i.getExit(),
                    i.getGuidesystem(), i.getSignguide(), i.getVideoguide(), i.getAudioguide(),
                    i.getBigprint(), i.getBrailepromotion(), i.getHelpdog(), i.getInfantsfamilyetc(),
                    i.getHearingroom(), i.getHearinghandicapetc(), i.getBlindhandicapetc(), i.getHandicapetc()
            ).filter(StringUtils::hasText).count();
            out.put("a11yFilledCount", a11yFilled);

            out.put("syncedAt", i.getSyncedAt() == null ? null :
                    i.getSyncedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.put("hasRawJson", i.getRawJson() != null && !i.getRawJson().isBlank());
        }

        return ResponseEntity.ok(out);
    }

    /** 원하면 raw_json도 바로 확인 가능 */
    @GetMapping("/intro/{contentId}/raw")
    public ResponseEntity<?> showIntroRaw(@PathVariable Long contentId) {
        AttractionIntro i = introRepository.findById(contentId).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("contentId", contentId);
        out.put("raw_json", i == null ? null : i.getRawJson());
        return ResponseEntity.ok(out);
    }
}

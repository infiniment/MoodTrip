package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/attraction-emotions")
public class AttractionEmotionController {

    private final AttractionEmotionService attractionEmotionService;

    public AttractionEmotionController(AttractionEmotionService attractionEmotionService) {
        this.attractionEmotionService = attractionEmotionService;
    }

    // 관리자 페이지 첫 화면 - 모든 관광지와 감정 리스트 전달하여 폼 출력
    @GetMapping
    public String showMappingPage(Model model) {
        // 전체 관광지 조회 대신 상위 10개만 조회하도록 수정
        // List<Attraction> attractions = attractionEmotionService.getAllAttractions();
        List<Attraction> attractions = attractionEmotionService.getTop10Attractions();
        List<Emotion> emotions = attractionEmotionService.getAllEmotions();

        Map<Long, List<Long>> attractionToEmotionIds = new HashMap<>();
        Map<Long, Map<Long, BigDecimal>> attractionToEmotionWeights = new HashMap<>();

        for (Attraction attraction : attractions) {
            List<AttractionEmotion> activeEmotions = attractionEmotionService.getActiveEmotionsByAttraction(attraction.getId());

            List<Long> emotionIds = activeEmotions.stream()
                    .map(mapping -> mapping.getEmotion().getTagId().longValue())
                    .collect(Collectors.toList());

            Map<Long, BigDecimal> emotionWeightMap = activeEmotions.stream()
                    .collect(Collectors.toMap(
                            mapping -> mapping.getEmotion().getTagId().longValue(),
                            AttractionEmotion::getWeight
                    ));

            attractionToEmotionIds.put(attraction.getId(), emotionIds);
            attractionToEmotionWeights.put(attraction.getId(), emotionWeightMap);
        }

        model.addAttribute("attractions", attractions);
        model.addAttribute("emotions", emotions);
        model.addAttribute("attractionToEmotionIds", attractionToEmotionIds);
        model.addAttribute("attractionToEmotionWeights", attractionToEmotionWeights);

        return "admin/attraction-emotion-mapping";
    }



    // 한 관광지의 감정 매핑 업데이트 요청 처리
    @PostMapping("/update/{attractionId}")
    public String updateAttractionEmotion(@PathVariable Long attractionId,
                                          @RequestParam(name = "emotionId", required = false) List<Long> emotionIds,
                                          @RequestParam(name = "weight", required = false) List<String> weights) {
        if (emotionIds == null || weights == null || emotionIds.size() != weights.size()) {
            emotionIds = new ArrayList<>();
            weights = new ArrayList<>();
        }

        List<EmotionWeightDto> dtoList = new ArrayList<>();
        for (int i = 0; i < emotionIds.size(); i++) {
            try {
                BigDecimal weightValue = new BigDecimal(weights.get(i));
                dtoList.add(new EmotionWeightDto(emotionIds.get(i), weightValue));
            } catch (NumberFormatException e) {
                dtoList.add(new EmotionWeightDto(emotionIds.get(i), BigDecimal.ZERO));
            }
        }

        attractionEmotionService.updateAttractionEmotions(attractionId, dtoList);

        return "redirect:/admin/attraction-emotions";
    }


}

package com.moodTrip.spring.domain.emotion.controller;
import com.moodTrip.spring.domain.attraction.service.AttractionServiceImpl;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.emotion.service.EmotionService; // EmotionService import 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; // Map 사용을 위해 추가

@Controller
@RequestMapping("/admin/attraction-emotions")
public class AttractionEmotionController {

    @Autowired
    private AttractionEmotionService attractionEmotionService;

    @Autowired
    private AttractionServiceImpl AttractionServiceImpl; // AttractionService 주입

    @Autowired
    private  EmotionService emotionService; // EmotionService 주입

    // 매핑 페이지를 보여주는 GET 요청
    @GetMapping
    public String showMappingPage(Model model) {
        // 모든 관광지 목록을 가져와 모델에 추가


        model.addAttribute("attractions", AttractionServiceImpl.findInitialAttractions(5));


        //model.addAttribute("attractions", AttractionServiceImpl.getAllAttractions());

        // 모든 감정 태그 목록을 가져와 모델에 추가
        model.addAttribute("emotions", emotionService.getAllEmotions());

        // 각 관광지별로 매핑된 감정 태그 ID 목록을 가져와 모델에 추가 (UI 체크박스 초기화용)
        // 이 메서드는 AttractionEmotionService에 구현되어 있어야 합니다.
        Map<Long, List<Long>> attractionToEmotionIds = attractionEmotionService.getAttractionToEmotionIdsMap();
        model.addAttribute("attractionToEmotionIds", attractionToEmotionIds);

        // 각 관광지별, 감정 태그별 가중치 맵을 가져와 모델에 추가 (UI 가중치 입력란 초기화용)
        // 이 메서드 또한 AttractionEmotionService에 구현되어 있어야 합니다.
        Map<Long, Map<Long, BigDecimal>> attractionToEmotionWeights = attractionEmotionService.getAttractionToEmotionWeightsMap();
        model.addAttribute("attractionToEmotionWeights", attractionToEmotionWeights);

        return "admin/attraction-emotion-mapping";   // Thymeleaf 템플릿 이름
    }

    // 한 관광지의 감정 매핑 업데이트 요청 처리
    @PostMapping("/update/{attractionId}")
    @ResponseBody // 이 어노테이션이 있어야 메서드의 반환 값이 HTTP 응답 본문에 직접 쓰여집니다 (JSON).
    public ResponseEntity<String> updateAttractionEmotion(@PathVariable Long attractionId,
                                                          @RequestBody List<EmotionWeightDto> emotionWeights) {
        try {
            // 서비스 계층으로 업데이트 요청 전달
            attractionEmotionService.updateAttractionEmotions(attractionId, emotionWeights);
            // 성공 시 200 OK와 메시지 반환
            return ResponseEntity.ok("{\"message\": \"Attraction emotions updated successfully\"}");
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 attractionId 또는 emotionId 등의 경우 400 Bad Request와 메시지 반환
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\\\"}");
        } catch (Exception e) {
            // 기타 서버 오류 발생 시 500 Internal Server Error와 메시지 반환
            // 실제 서비스에서는 e.getMessage() 대신 일반적인 오류 메시지를 반환하고,
            // 자세한 오류 내용은 서버 로그에 기록하는 것이 좋습니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Internal server error: " + e.getMessage() + "\\\"}");
        }
    }
}

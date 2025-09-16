package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로깅을 위한 import 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList; // ArrayList import 추가
import java.util.List;

@Slf4j // 로그 사용을 위한 어노테이션
@Controller
@RequiredArgsConstructor
public class EmotionViewController {

    private final EmotionService emotionService;
    private final AttractionService attractionService;

    @GetMapping("/emotion-search")
    public String emotionSearch(
            @RequestParam(name = "tagId", required = false) Integer tagId,
            @RequestParam(name = "sort", required = false, defaultValue = "recommended") String sort,
            Model model) {

        // 항상 필요한 감정 카테고리 목록은 미리 추가
        model.addAttribute("emotionCategories", emotionService.getEmotionCategories());

        // [수정 1] attractions 리스트를 비어있는 상태로 즉시 초기화
        List<AttractionCardDTO> attractions = new ArrayList<>();
        String title = "여행지"; // 기본 제목 설정

        try {
            // 'sort' 파라미터 값에 따라 분기 처리
            if ("popular".equals(sort)) {
                attractions = attractionService.findPopularAttractions(12);
                title = "인기 여행지";
            } else {
                if (tagId != null) {
                    attractions = attractionService.findAttractionsByEmotionTag(tagId, 12);
                    title = "여행지 검색 결과";
                } else {
                    attractions = attractionService.findInitialAttractions(12);
                    title = "추천 여행지";
                }
            }
        } catch (Exception e) {
            // [수정 2] 서비스 로직에서 예외 발생 시 로그를 남기고, 사용자에게는 빈 화면이나 오류 메시지를 보여주도록 처리
            log.error("여행지 검색 중 오류가 발생했습니다. tagId: {}, sort: {}", tagId, sort, e);
            // 예외가 발생했으므로 attractions는 위에서 초기화한 빈 리스트(ArrayList)가 그대로 사용됩니다.
            title = "검색 중 오류가 발생했습니다";
        }

        // [수정 3] 만약의 경우를 대비해 null 체크 추가 (필수는 아니지만 방어적인 코딩)
        if (attractions == null) {
            attractions = new ArrayList<>();
        }

        // 최종 결과를 모델에 담기 (이 부분은 기존과 동일)
        model.addAttribute("attractions", attractions);
        model.addAttribute("resultsCount", attractions.size());
        model.addAttribute("resultsTitle", title);
        model.addAttribute("currentSort", sort);

        // 템플릿 렌더링
        return "emotion-search/emotion-search";
    }

}

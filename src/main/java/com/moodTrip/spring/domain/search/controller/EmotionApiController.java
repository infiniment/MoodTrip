package com.moodTrip.spring.domain.search.controller;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotions")
public class EmotionApiController {

    private final EmotionRepository emotionRepository;

    public EmotionApiController(EmotionRepository emotionRepository) {
        this.emotionRepository = emotionRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listByCategory(@RequestParam Long categoryId) {
        List<Emotion> list = emotionRepository.findByCategoryId(categoryId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (Emotion e : list) {
            res.add(Map.of("tagId", e.getTagId(), "tagName", e.getTagName()));
        }
        return res;
    }
}

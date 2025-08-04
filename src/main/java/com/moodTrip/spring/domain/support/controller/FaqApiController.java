package com.moodTrip.spring.domain.support.controller;

import com.moodTrip.spring.domain.support.dto.response.FaqResponse;
import com.moodTrip.spring.domain.support.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqApiController {

    private final FaqService faqService;

    @GetMapping
    public List<FaqResponse> getFaqs() {
        return faqService.getAllFaqs();
    }
}

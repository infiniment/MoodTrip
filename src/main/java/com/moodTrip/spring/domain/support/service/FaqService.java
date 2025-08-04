package com.moodTrip.spring.domain.support.service;

import com.moodTrip.spring.domain.support.dto.response.FaqResponse;
import com.moodTrip.spring.domain.support.entity.Faq;
import com.moodTrip.spring.domain.support.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqResponse> getAllFaqs() {
        return faqRepository.findAll()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }
}

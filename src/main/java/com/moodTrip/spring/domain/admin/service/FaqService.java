package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public List<Faq> findAll() {
        return faqRepository.findAll();
    }

    public Faq findById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));
    }

    @Transactional
    public Faq save(Faq faq) {
        if (faq.getId() == null) {
            faq.setCreatedAt(LocalDateTime.now());
        }
        faq.setModifiedAt(LocalDateTime.now());
        return faqRepository.save(faq);
    }

    @Transactional
    public void delete(Long id) {
        faqRepository.deleteById(id);
    }

    public List<Faq> findByCategory(String category) {
        return faqRepository.findByCategory(category);
    }

//    public List<Faq> searchByTitleOrContent(String query) {
//        return faqRepository.findByTitleContainingOrContentContaining(query, query);
//    }
    //테스트
    public List<Faq> searchByTitleOrContent(String query) {
        return faqRepository.searchByQuery(query);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setViewCount(faq.getViewCount() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void increaseHelpful(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setHelpful(faq.getHelpful() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void increaseNotHelpful(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setNotHelpful(faq.getNotHelpful() + 1);
        faqRepository.save(faq);
    }
}
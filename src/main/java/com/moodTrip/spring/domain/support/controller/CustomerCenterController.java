package com.moodTrip.spring.domain.support.controller;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.service.FaqService;
import com.moodTrip.spring.domain.support.dto.response.FaqResponse;
import com.moodTrip.spring.domain.support.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.support.service.CustomerNotificationService;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer-center")
public class CustomerCenterController {

    private final CustomerNotificationService customerNotificationService;
    private final FaqService faqService;

    // 메인 고객센터 페이지
    @GetMapping
    public String customerCenter(Model model) {
        List<NotificationResponse> notices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)  // 공개된 공지만
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))  // 최신순
                .limit(5)  // 상위 5개
                .collect(Collectors.toList());

        List<Faq> faqs = faqService.findAll().stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("notices", notices);
        model.addAttribute("faqs", faqs);
        return "customer-center/customer-center";
    }

    // 공지사항 목록
    @GetMapping("/announcement")
    public String announcementPage(Model model,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        // 페이징 처리된 공지사항 목록
        List<NotificationResponse> notices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))
                .collect(Collectors.toList());

        // 페이징 계산
        int totalItems = notices.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);

        List<NotificationResponse> pageNotices = notices.subList(start, end);

        model.addAttribute("notices", pageNotices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);

        return "customer-center/announcement";
    }

    // 공지사항 상세
    @GetMapping("/announcement-detail")
    public String announcementDetailPage(@RequestParam Long id, Model model) {
        NotificationResponse notice = customerNotificationService.findById(id);


        // 조회수 증가
        customerNotificationService.increaseViewCount(id);

        // 이전글/다음글 찾기
        List<NotificationResponse> allNotices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))
                .collect(Collectors.toList());

        NotificationResponse prevNotice = null;
        NotificationResponse nextNotice = null;

        for (int i = 0; i < allNotices.size(); i++) {
            if (allNotices.get(i).getId().equals(id)) {
                if (i > 0) nextNotice = allNotices.get(i - 1);
                if (i < allNotices.size() - 1) prevNotice = allNotices.get(i + 1);
                break;
            }
        }

        model.addAttribute("notice", notice);
        model.addAttribute("prevNotice", prevNotice);
        model.addAttribute("nextNotice", nextNotice);

        return "customer-center/announcement-detail";
    }

    // FAQ 목록
    @GetMapping("/faq")
    public String faqPage(Model model,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String category) {

        List<Faq> allFaqs;
        if (category != null && !category.isEmpty()) {
            allFaqs = faqService.findByCategory(category);
        } else {
            allFaqs = faqService.findAll();
        }

        // 페이징 계산
        int totalItems = allFaqs.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);

        List<Faq> pagedFaqs = allFaqs.subList(start, end);

        model.addAttribute("faqs", pagedFaqs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedCategory", category);

        return "customer-center/faq";
    }

    // FAQ 상세 페이지
    @GetMapping("/faq-detail")
    public String faqDetailPage(@RequestParam Long id, Model model) {
        Faq faq = faqService.findById(id);

        // 조회수 증가
        faqService.increaseViewCount(id);

        // 도움됨 퍼센트 계산
        int totalVotes = faq.getHelpful() + faq.getNotHelpful();
        int helpfulPercentage = totalVotes > 0 ? (faq.getHelpful() * 100) / totalVotes : 0;

        // 같은 카테고리의 관련 FAQ 찾기
        List<Faq> relatedFaqs = faqService.findByCategory(faq.getCategory())
                .stream()
                .filter(f -> !f.getId().equals(id))
                .limit(3)
                .collect(Collectors.toList());

        // 이전/다음 FAQ 찾기
        List<Faq> allFaqs = faqService.findAll();
        Faq prevFaq = null;
        Faq nextFaq = null;

        for (int i = 0; i < allFaqs.size(); i++) {
            if (allFaqs.get(i).getId().equals(id)) {
                if (i > 0) prevFaq = allFaqs.get(i - 1);
                if (i < allFaqs.size() - 1) nextFaq = allFaqs.get(i + 1);
                break;
            }
        }

        model.addAttribute("faq", faq);
        model.addAttribute("helpfulPercentage", helpfulPercentage);
        model.addAttribute("relatedFaqs", relatedFaqs);
        model.addAttribute("prevFaq", prevFaq);
        model.addAttribute("nextFaq", nextFaq);

        return "customer-center/faq-detail";
    }


    @PostMapping("/faq/helpful/{id}")
    @ResponseBody
    public ResponseEntity<Void> increaseHelpful(@PathVariable Long id) {
        faqService.increaseHelpful(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/faq/not-helpful/{id}")
    @ResponseBody
    public ResponseEntity<Void> increaseNotHelpful(@PathVariable Long id) {
        faqService.increaseNotHelpful(id);
        return ResponseEntity.ok().build();
    }

    // FAQ 데이터 API
    @GetMapping("/faq/data")
    @ResponseBody
    public List<FaqResponse> getFaqData(@RequestParam(required = false) String category) {
        List<Faq> faqs;
        if (category != null) {
            faqs = faqService.findByCategory(category);
        } else {
            faqs = faqService.findAll().stream().limit(5).collect(Collectors.toList());
        }

        return faqs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private FaqResponse convertToResponse(Faq faq) {
        FaqResponse response = new FaqResponse();
        response.setId(faq.getId());
        response.setCategory(faq.getCategory());
        response.setTitle(faq.getTitle());
        response.setContent(faq.getContent());
        response.setViewCount(faq.getViewCount());
        response.setHelpful(faq.getHelpful());
        response.setNotHelpful(faq.getNotHelpful());
        response.setCreatedAt(faq.getCreatedAt());
        response.setModifiedAt(faq.getModifiedAt());
        return response;
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("query", query);
        return "customer-center/customer-center-search";
    }
    @GetMapping("/search/api")
    @ResponseBody
    public Map<String, Object> searchAll(@RequestParam String query) {
        // 직접 서비스 호출로 수정
        List<Faq> faqs = faqService.searchByTitleOrContent(query);
        List<FaqResponse> faqResults = faqs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        List<NotificationResponse> noticeResults = customerNotificationService.searchByTitleOrContent(query);

        Map<String, Object> results = new HashMap<>();
        results.put("faq", faqResults);
        results.put("notice", noticeResults);
        results.put("totalCount", faqResults.size() + noticeResults.size());

        return results;
    }
}
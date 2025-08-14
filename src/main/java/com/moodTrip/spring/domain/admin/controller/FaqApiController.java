package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/faq")
public class FaqApiController {

    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<List<Faq>> getAllFaqs() {
        return ResponseEntity.ok(faqService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createFaq(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("받은 데이터: " + request);

            Faq faq = new Faq();
            faq.setTitle((String) request.get("title"));
            faq.setContent((String) request.get("content"));
            faq.setCategory((String) request.get("category"));

            Faq saved = faqService.save(faq);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faq> updateFaq(@PathVariable Long id, @RequestBody Faq faq) {
        faq.setId(id);
        return ResponseEntity.ok(faqService.save(faq));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.ok().build();
    }
}
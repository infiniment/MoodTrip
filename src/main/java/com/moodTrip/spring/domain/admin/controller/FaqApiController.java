package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<Faq> updateFaq(@PathVariable("id") Long id, @RequestBody Faq faq) {
        try {
            faq.setId(id);
            return ResponseEntity.ok(faqService.save(faq));
        } catch (RuntimeException e) { // ex) "FAQ not found with id: 999"
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable("id") Long id) {
        try {
            faqService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) { // 서비스에서 없는 ID 처리 시 동일 변환
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Faq> getFaqById(@PathVariable("id") Long id) {
        try {
            Faq faq = faqService.findById(id); // 여기서 RuntimeException 발생 가능
            return ResponseEntity.ok(faq);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

}

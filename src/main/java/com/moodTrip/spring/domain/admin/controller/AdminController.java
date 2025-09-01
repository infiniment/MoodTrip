package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.dto.response.AttractionAdminDto;
import com.moodTrip.spring.domain.admin.service.FaqService;
import com.moodTrip.spring.domain.admin.service.NotificationService;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.global.common.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.member.dto.response.MemberAdminDto;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;
    private final FaqService faqService;
    private final MemberService memberService;
    private final AttractionService attractionService;

    @GetMapping
    public String adminPage(Model model) {


        // 공지사항 목록 가져오기
        model.addAttribute("notices", new ArrayList<>());

        // FAQ 목록 가져오기
        model.addAttribute("faqs", faqService.findAll());

        //회원 목록 추가
        model.addAttribute("members", memberService.getAllMembersForAdmin());

        model.addAttribute("attractions", attractionService.getAttractionsForAdmin("", 0, 10).getContent());


        return "admin/admin";
    }


    // 회원 상세 정보 조회 (AJAX용)
    @GetMapping("/members/{memberPk}")
    @ResponseBody
    public ResponseEntity<MemberAdminDto> getMemberDetail(@PathVariable Long memberPk) {
        try {
            MemberAdminDto memberDetail = memberService.getMemberDetailForAdmin(memberPk);
            return ResponseEntity.ok(memberDetail);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 회원 상태 변경 (정지/활성화)
    @PutMapping("/members/{memberPk}/status")
    @ResponseBody
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable Long memberPk,
            @RequestParam String status) {
        try {
            memberService.updateMemberStatus(memberPk, status);
            return ResponseEntity.ok("회원 상태가 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("상태 변경 중 오류가 발생했습니다.");
        }
    }

    // 회원 강제 탈퇴 처리
    @PutMapping("/members/{memberPk}/withdraw")
    @ResponseBody
    public ResponseEntity<String> withdrawMember(@PathVariable Long memberPk) {
        try {
            memberService.withdrawMember(memberPk);
            return ResponseEntity.ok("회원이 탈퇴 처리되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    // 회원 검색 (AJAX용)
    @GetMapping("/members/search")
    @ResponseBody
    public ResponseEntity<java.util.List<MemberAdminDto>> searchMembers(@RequestParam String keyword) {
        try {
            java.util.List<MemberAdminDto> searchResult = memberService.searchMembersForAdmin(keyword);
            return ResponseEntity.ok(searchResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/attractions")
    @ResponseBody
    public ResponseEntity<PageResult<AttractionAdminDto>> getAttractions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search) {
        var result = attractionService.getAttractionsForAdmin(search, page, size);
        return ResponseEntity.ok(PageResult.of(result));
    }

    // 관광지 상세 조회
    @GetMapping("/attractions/{attractionId}")
    @ResponseBody
    public ResponseEntity<AttractionAdminDto> getAttractionDetail(@PathVariable Long attractionId) {
        // 일단 기본 응답
        return ResponseEntity.ok(new AttractionAdminDto());
    }


}

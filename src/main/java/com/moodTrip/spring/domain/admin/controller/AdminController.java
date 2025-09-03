package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.dto.request.ReportActionDto;
import com.moodTrip.spring.domain.admin.dto.response.*;
import com.moodTrip.spring.domain.admin.service.*;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.exception.CustomException;
import com.moodTrip.spring.global.common.util.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.member.dto.response.MemberAdminDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;


import static com.moodTrip.spring.global.common.code.status.ErrorStatus.ROOM_NOT_FOUND;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;
    private final FaqService faqService;
    private final MemberService memberService;
    private final AttractionService attractionService;
    private final AdminReportService adminReportService;
    private final AdminMatchingService adminMatchingService;
    private final AdminDashboardService adminDashboardService;
    private final RoomService roomService;
    private final RoomRepository roomRepository;

    @GetMapping
    public String adminPage(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        // 공지사항 목록
        model.addAttribute("notices", new ArrayList<>());

        // FAQ 목록
        model.addAttribute("faqs", faqService.findAll());

        // 회원 목록 (페이징 처리된 데이터 사용)
        Page<MemberAdminDto> memberPage = memberService.getAllMembersForAdmin(page, size);
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("page", memberPage);   // 페이지 정보 추가
        model.addAttribute("memberSize", size);        // 사이즈 정보 추가

        // 관광지 목록
        model.addAttribute("attractions", attractionService.getAttractionsForAdmin("", 0, 10).getContent());

        // 신고 목록 (페이징)
        var all = adminReportService.getAllReports(null); // 상태 필터 없음
        int total = all.size();
        int from = Math.min(page * size, total);
        int to   = Math.min(from + size, total);

        Page<ReportDto> reportPage = new PageImpl<>(
                all.subList(from, to),
                PageRequest.of(page, size),
                total
        );

        model.addAttribute("reports", reportPage.getContent()); // 테이블에 뿌릴 리스트
        model.addAttribute("page", reportPage);                 // 페이지네이션 정보
        model.addAttribute("size", size);                       // 페이지네이션 링크에 size 그대로 씀

        // 매칭 목록 (타임리프 카드에 뿌림)
        model.addAttribute("matchings", adminMatchingService.getAllMatchings());

        // 대시보드 카운트 정보
        model.addAttribute("memberCount", adminDashboardService.getMemberCount());
        model.addAttribute("matchingCount", adminDashboardService.getMatchingCount());
        model.addAttribute("attractionCount", adminDashboardService.getAttractionCount());
        model.addAttribute("unresolvedReportCount", adminDashboardService.getUnresolvedReportCount());

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

    @GetMapping("/members")
    public String membersPage(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Page<MemberAdminDto> memberPage = memberService.getAllMembersForAdmin(page, size);
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("page", memberPage);
        model.addAttribute("size", size);
        return "admin/admin";
    }

    @GetMapping("/members/export")
    public void exportMembers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=members.xlsx");

        List<MemberAdminDto> members = memberService.getAllMembersForExport();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("회원목록");

            // 헤더
            Row header = sheet.createRow(0);
            String[] columns = {"회원 ID", "닉네임", "이메일", "전화번호", "가입일", "최근 로그인", "상태", "매칭 참여 횟수", "신고 받은 횟수", "신고한 횟수"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            // 데이터
            int rowIdx = 1;
            for (MemberAdminDto member : members) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(member.getMemberId());
                row.createCell(1).setCellValue(member.getNickname());
                row.createCell(2).setCellValue(member.getEmail() != null ? member.getEmail() : "-");
                row.createCell(3).setCellValue(member.getMemberPhone() != null ? member.getMemberPhone() : "-");
                row.createCell(4).setCellValue(member.getCreatedAt().toString());
                row.createCell(5).setCellValue(member.getLastLoginAt() != null ? member.getLastLoginAt().toString() : "로그인 기록 없음");
                row.createCell(6).setCellValue(member.getStatusDisplay());
                row.createCell(7).setCellValue(member.getMatchingParticipationCount());
                row.createCell(8).setCellValue(member.getRptRcvdCnt());
                row.createCell(9).setCellValue(member.getRptCnt());
            }

            workbook.write(response.getOutputStream());
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

    // 신고 목록 (PageResult.of(Page) 컨벤션으로 통일)
    @GetMapping("/reports")
    @ResponseBody
    public ResponseEntity<PageResult<ReportDto>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        // 서비스는 List<ReportDto> 반환 → 여기서 PageImpl로 감싸서 PageResult.of(...) 사용
        var all = adminReportService.getAllReports(status); // List<ReportDto>
        int total = all.size();
        int from = Math.min(page * size, total);
        int to   = Math.min(from + size, total);

        Page<ReportDto> result = new PageImpl<>(
                all.subList(from, to),
                PageRequest.of(page, size),
                total
        );
        return ResponseEntity.ok(PageResult.of(result));
    }

    // 신고 상세 (type 필요: ROOM | MEMBER)
    @GetMapping("/reports/{reportId}")
    @ResponseBody
    public ResponseEntity<ReportDetailDto> getReportDetail(
            @PathVariable Long reportId,
            @RequestParam String type) {
        var detail = adminReportService.getDetail(reportId, type);
        return ResponseEntity.ok(detail);
    }

    // 신고 처리 완료(RESOLVED)
    @PutMapping("/reports/{reportId}/resolve")
    @ResponseBody
    public ResponseEntity<String> resolveReport(
            @PathVariable Long reportId,
            @RequestParam String type,
            @RequestBody(required = false) ReportActionDto actionDto) {
        adminReportService.resolve(reportId, type, actionDto);
        return ResponseEntity.ok("신고가 처리되었습니다.");
    }

    // 신고 거부(DISMISSED)
    @PutMapping("/reports/{reportId}/reject")
    @ResponseBody
    public ResponseEntity<String> rejectReport(
            @PathVariable Long reportId,
            @RequestParam String type,
            @RequestBody(required = false) ReportActionDto actionDto) {
        adminReportService.dismiss(reportId, type, actionDto);
        return ResponseEntity.ok("신고가 기각되었습니다.");
    }

    // (선택) 신고 삭제 — 실제로는 삭제보다 상태 변경을 권장
    @DeleteMapping("/reports/{reportId}")
    @ResponseBody
    public ResponseEntity<String> deleteReport(
            @PathVariable Long reportId,
            @RequestParam String type) {
        // 필요시 실제 삭제 로직 추가 (권장: 상태 DISMISSED로)
        // adminReportService.delete(fireId, type);  // 아직 없으니 주석
        return ResponseEntity.status(501).body("삭제는 미지원입니다. 기각 처리(end-point: /reject)를 사용하세요.");
    }

    @GetMapping("/matchings")
    @ResponseBody
    public ResponseEntity<PageResult<AdminMatchingDto>> getMatchings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        var result = adminMatchingService.getMatchings(page, size);
        return ResponseEntity.ok(PageResult.of(result));
    }
    @GetMapping("/admin/rooms/{roomId}/members")
    @ResponseBody
    public ResponseEntity<List<RoomMemberResponse>> getRoomMembers(@PathVariable Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));
        List<RoomMemberResponse> members = roomService.getActiveMembers(room);
        return ResponseEntity.ok(members);
    }

    @PutMapping("/matchings/{roomId}/terminate")
    @ResponseBody
    public ResponseEntity<String> terminateMatching(@PathVariable Long roomId) {
        try {
            adminMatchingService.terminateMatching(roomId);
            return ResponseEntity.ok("매칭이 강제 종료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("강제 종료 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/matchings/{roomId}/restore")
    @ResponseBody
    public ResponseEntity<String> restoreMatching(@PathVariable Long roomId) {
        try {
            adminMatchingService.restoreMatching(roomId);
            return ResponseEntity.ok("매칭이 복구되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("복구 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/members/json")
    @ResponseBody
    public Page<MemberAdminDto> getMembersJson(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {
        return memberService.getAllMembersForAdmin(page, size);
    }

    @GetMapping("/matchings/json")
    @ResponseBody
    public PageResult<AdminMatchingDto> getMatchingsJson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AdminMatchingDto> matchingPage = adminMatchingService.getMatchings(page, size);
        return PageResult.of(matchingPage); // 관광지/회원과 동일 포맷
    }

    @GetMapping("/reports/json")
    @ResponseBody
    public ResponseEntity<PageResult<ReportDto>> getReportsJson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status // 'PENDING'|'RESOLVED'|'DISMISSED' 또는 '대기'|'처리완료'|'거부'|null
    ) {
        // 한글도 들어올 수 있으므로 영문 enum으로 정규화
        String normalized = normalizeStatus(status);

        // 서비스: 최신순 정렬된 List<ReportDto> 반환
        List<ReportDto> all = adminReportService.getAllReports(normalized);

        int total = all.size();
        int from  = Math.min(page * size, total);
        int to    = Math.min(from + size, total);

        Page<ReportDto> result = new PageImpl<>(
                all.subList(from, to),
                PageRequest.of(page, size),
                total
        );
        return ResponseEntity.ok(PageResult.of(result));
    }

    private String normalizeStatus(String s) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        // 영문 그대로면 통과
        switch (s.toUpperCase()) {
            case "PENDING":
            case "RESOLVED":
            case "DISMISSED": return s.toUpperCase();
        }
        // 한글 → 영문 enum 매핑
        return switch (s) {
            case "대기"    -> "PENDING";
            case "처리완료" -> "RESOLVED";
            case "거부"    -> "DISMISSED";
            default        -> null;
        };
    }



}

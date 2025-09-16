package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.dto.response.ReportDto;
import com.moodTrip.spring.domain.admin.service.*;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.member.dto.response.MemberAdminDto;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    // Controller 인스턴스를 직접 만들고 주입
    @InjectMocks
    private AdminController adminController;

    // 의존성은 Mockito @Mock
    @Mock NotificationService notificationService;
    @Mock FaqService faqService;
    @Mock MemberService memberService;
    @Mock AttractionService attractionService;
    @Mock AdminReportService adminReportService;
    @Mock AdminMatchingService adminMatchingService;
    @Mock AdminDashboardService adminDashboardService;
    @Mock RoomService roomService;
    @Mock RoomRepository roomRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mvc = MockMvcBuilders.standaloneSetup(adminController)
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(om))
                .build();
    }

    // ===== Helpers =====
    private MemberAdminDto mockMember(long id) {
        MemberAdminDto dto = new MemberAdminDto();
        dto.setMemberPk(id);
        dto.setMemberId("user" + id);                 // (중요) String 타입으로 채움
        dto.setNickname("user" + id);
        dto.setEmail("user"+id+"@test.com");
        dto.setMemberPhone("010-0000-000"+id);
        dto.setCreatedAt(LocalDateTime.of(2025,9,1,12,0));
        dto.setLastLoginAt(LocalDateTime.of(2025,9,2,12,0));
        dto.setStatus("ACTIVE");
        dto.setIsWithdraw(false);
        dto.setMatchingParticipationCount(3L);
        dto.setRptRcvdCnt(1L);
        dto.setRptCnt(0L);
        return dto;
    }

    private ReportDto mockReport(long id, String status) {
        ReportDto dto = new ReportDto();
        dto.setReportId(id);
        // dto.setStatus(status); // 필드 없으면 생략
        dto.setCreatedAt(LocalDateTime.of(2025,9,1,10,0));
        return dto;
    }

    // ===== Tests =====
    @Test
    @DisplayName("GET /admin/members/json - 페이지네이션 JSON 응답")
    void getMembersJson() throws Exception {
        mvc.perform(get("/admin/members/json")
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].memberPk").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }


    @Test
    @DisplayName("GET /admin/reports/json - 한글 상태값 정규화 확인 (대기→PENDING)")
    void getReportsJson_koreanStatusNormalization() throws Exception {
        List<ReportDto> all = List.of(
                mockReport(10, "PENDING"),
                mockReport(11, "PENDING")
        );
        when(adminReportService.getAllReports("PENDING")).thenReturn(all);

        mvc.perform(get("/admin/reports/json")
                        .param("status", "대기")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].reportId").value(10))
                .andExpect(jsonPath("$.content[1].reportId").value(11));

        verify(adminReportService).getAllReports("PENDING");
    }

//    @Test
//    void getReportsJson_noStatus() throws Exception {
////        // fixture
////        List<ReportDto> fixture = List.of(
////                new ReportDto(1L, /* ... */),
////                new ReportDto(2L, /* ... */)
////        );
//
//        // ✅ null 인자 매칭 (anyString()은 null 매치를 못함)
//        given(adminReportService.getAllReports(isNull())).willReturn(fixture);
//
//        mvc.perform(get("/admin/reports/json")
//                        .param("page","0")
//                        .param("size","10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(2))
//                .andExpect(jsonPath("$.totalElements").value(2))
//                .andExpect(jsonPath("$.number").value(0))
//                .andExpect(jsonPath("$.size").value(10))
//                .andExpect(jsonPath("$.first").value(true))
//                .andExpect(jsonPath("$.last").value(true));
//    }


    @Test
    @DisplayName("GET /admin/members/{id} - 상세조회 성공")
    void getMemberDetail_ok() throws Exception {
        MemberAdminDto dto = mockMember(7);
        when(memberService.getMemberDetailForAdmin(7L)).thenReturn(dto);

        mvc.perform(get("/admin/members/{memberPk}", 7L))
                .andExpect(status().isOk())
                // memberId는 String이므로 "user7" 기대
                .andExpect(jsonPath("$.memberId").value("user7"))
                .andExpect(jsonPath("$.nickname").value("user7"));
    }

    @Test
    @DisplayName("GET /admin/members/{id} - 상세조회 실패 시 404")
    void getMemberDetail_notFound() throws Exception {
        when(memberService.getMemberDetailForAdmin(99L)).thenThrow(new RuntimeException("not found"));

        mvc.perform(get("/admin/members/{memberPk}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /admin/members/{id}/status - 상태 변경 OK")
    void updateMemberStatus_ok() throws Exception {
        doNothing().when(memberService).updateMemberStatus(5L, "SUSPENDED");

        mvc.perform(put("/admin/members/{memberPk}/status", 5)
                        .param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 상태가 변경되었습니다."));
    }

    @Test
    @DisplayName("PUT /admin/members/{id}/withdraw - 강제 탈퇴 OK")
    void withdrawMember_ok() throws Exception {
        doNothing().when(memberService).withdrawMember(6L);

        mvc.perform(put("/admin/members/{memberPk}/withdraw", 6))
                .andExpect(status().isOk())
                .andExpect(content().string("회원이 탈퇴 처리되었습니다."));
    }

    @Test
    @DisplayName("PUT /admin/matchings/{roomId}/terminate|restore - 매칭 종료/복구 OK")
    void matchingTerminateRestore_ok() throws Exception {
        doNothing().when(adminMatchingService).terminateMatching(100L);
        doNothing().when(adminMatchingService).restoreMatching(100L);

        mvc.perform(put("/admin/matchings/{roomId}/terminate", 100))
                .andExpect(status().isOk())
                .andExpect(content().string("매칭이 강제 종료되었습니다."));

        mvc.perform(put("/admin/matchings/{roomId}/restore", 100))
                .andExpect(status().isOk())
                .andExpect(content().string("매칭이 복구되었습니다."));
    }

    @Test
    @DisplayName("GET /admin/members/export - 엑셀 헤더/데이터 검증")
    void exportMembers_excel() throws Exception {
        List<MemberAdminDto> exportList = List.of(mockMember(1), mockMember(2));
        when(memberService.getAllMembersForExport()).thenReturn(exportList);

        byte[] bytes = mvc.perform(get("/admin/members/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("members.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();

        try (var wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var sheet = wb.getSheetAt(0);
            var header = sheet.getRow(0);
            String[] expected = {"회원 ID", "닉네임", "이메일", "전화번호", "가입일", "최근 로그인", "상태", "매칭 참여 횟수", "신고 받은 횟수", "신고한 횟수"};
            for (int i=0;i<expected.length;i++) {
                Assertions.assertEquals(expected[i], header.getCell(i).getStringCellValue());
            }
            var row1 = sheet.getRow(1);
            // memberId는 문자열("user1")로 써졌으므로 String 셀로 확인
            Assertions.assertEquals("user1", row1.getCell(0).getStringCellValue());
            Assertions.assertEquals("user1", row1.getCell(1).getStringCellValue());
        }
    }

    @Nested
    @DisplayName("페이지 경계(subList) 안전성")
    class PaginationBounds {
        @Test
        @DisplayName("총 3건, page=1,size=10 요청 시 빈 목록이어야 함")
        void reportsBounds() throws Exception {
            List<ReportDto> all = List.of(mockReport(1,"PENDING"), mockReport(2,"PENDING"), mockReport(3,"PENDING"));
            when(adminReportService.getAllReports(null)).thenReturn(all);

            mvc.perform(get("/admin/reports/json")
                            .param("page","1")
                            .param("size","10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(3));
        }
    }
}

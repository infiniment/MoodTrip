// src/test/java/com/moodTrip/spring/domain/support/controller/CustomerCenterControllerTest.java
package com.moodTrip.spring.domain.support.controller;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.service.FaqService;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.dto.response.FaqResponse;
import com.moodTrip.spring.domain.support.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.support.service.CustomerNotificationService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCenterControllerTest {

    @Mock CustomerNotificationService customerNotificationService;
    @Mock FaqService faqService;

    @InjectMocks CustomerCenterController controller;

    List<NotificationResponse> notifications;
    List<Faq> faqs;

    @BeforeEach
    void setUp() {
        notifications = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> {
                    NotificationResponse r = new NotificationResponse();
                    // 컨트롤러에서 prev/next 계산 시 getId() 사용 → id도 세팅
                    r.setId((long) i);
                    r.setId((long) i);
                    r.setTitle("N" + i);
                    r.setContent("C" + i);
                    r.setIsVisible(i % 2 == 0); // 짝수만 공개
                    r.setRegisteredDate(LocalDateTime.now().minusDays(i));
                    r.setClassification("GENERAL");
                    r.setIsImportant(false);
                    r.setViewCount(0);
                    return r;
                })
                .collect(Collectors.toList());

        faqs = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> {
                    Faq f = new Faq();
                    f.setId((long) i);
                    f.setTitle("FAQ " + i);
                    f.setContent("FAQ-C " + i);
                    f.setCategory(i % 2 == 0 ? "A" : "B");
                    f.setViewCount(0);
                    f.setHelpful(0);
                    f.setNotHelpful(0);
                    f.setCreatedAt(LocalDateTime.now().minusDays(i));
                    f.setModifiedAt(LocalDateTime.now().minusDays(i));
                    return f;
                })
                .collect(Collectors.toList());
    }

    // ------------------- GET /customer-center -------------------
    @Test
    @DisplayName("메인 고객센터 페이지: 공개 공지 최신순 상위 5 + FAQ 상위 5, 뷰명 반환")
    void customerCenter_ok() {
        when(customerNotificationService.findAll()).thenReturn(notifications);
        when(faqService.findAll()).thenReturn(faqs);

        Model model = new ExtendedModelMap();
        String view = controller.customerCenter(model);

        assertThat(view).isEqualTo("customer-center/customer-center");

        @SuppressWarnings("unchecked")
        List<NotificationResponse> notices = (List<NotificationResponse>) model.getAttribute("notices");
        @SuppressWarnings("unchecked")
        List<Faq> faqTop = (List<Faq>) model.getAttribute("faqs");

        // 공개(true)인 공지만, 최신순, 5개
        List<NotificationResponse> expected = notifications.stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted(Comparator.comparing(NotificationResponse::getRegisteredDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        assertThat(notices).containsExactlyElementsOf(expected);
        assertThat(faqTop).hasSize(5);
    }

    // ------------------- GET /customer-center/announcement (paging) -------------------
    @Test
    @DisplayName("공지 목록: 페이지네이션 계산 및 뷰 모델 세팅")
    void announcementPage_ok() {
        when(customerNotificationService.findAll()).thenReturn(notifications);

        Model model = new ExtendedModelMap();
        String view = controller.announcementPage(model, 2, 4); // page=2, size=4

        assertThat(view).isEqualTo("customer-center/announcement");

        @SuppressWarnings("unchecked")
        List<NotificationResponse> pageNotices = (List<NotificationResponse>) model.getAttribute("notices");
        int currentPage = (int) model.getAttribute("currentPage");
        int totalPages = (int) model.getAttribute("totalPages");
        int totalItems = (int) model.getAttribute("totalItems");

        List<NotificationResponse> allVisible = notifications.stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted(Comparator.comparing(NotificationResponse::getRegisteredDate).reversed())
                .collect(Collectors.toList());

        assertThat(totalItems).isEqualTo(allVisible.size());
        assertThat(totalPages).isEqualTo((int) Math.ceil(totalItems / 4.0));
        assertThat(currentPage).isEqualTo(2);
        assertThat(pageNotices).hasSize(Math.min(4, totalItems - 4)); // 2페이지 아이템 수
    }

    // ------------------- GET /customer-center/announcement-detail -------------------
    @Test
    @DisplayName("공지 상세: 조회수 증가, helpful 정보, prev/next 계산")
    void announcementDetail_ok() {
        Long id = 6L; // 공개 공지로 가정
        NotificationResponse target = notifications.stream().filter(n -> n.getId().equals(id)).findFirst().orElseThrow();
        when(customerNotificationService.findById(id)).thenReturn(target);
        when(customerNotificationService.findAll()).thenReturn(notifications);
        when(customerNotificationService.helpfulCount(id)).thenReturn(3L);
        // user null → userHelpful=false
        Model model = new ExtendedModelMap();

        String view = controller.announcementDetailPage(id, null, model);

        assertThat(view).isEqualTo("customer-center/announcement-detail");
        verify(customerNotificationService).increaseViewCount(id);

        NotificationResponse notice = (NotificationResponse) model.getAttribute("notice");
        Boolean userHelpful = (Boolean) model.getAttribute("userHelpful");
        Long helpfulCount = (Long) model.getAttribute("helpfulCount");

        assertThat(notice.getId()).isEqualTo(id);
        assertThat(userHelpful).isFalse();
        assertThat(helpfulCount).isEqualTo(3L);

        // prev/next 존재성만 간단 검증
        assertThat(model.getAttribute("prevNotice")).isNotNull();
        assertThat(model.getAttribute("nextNotice")).isNotNull();
    }

    // ------------------- GET /customer-center/faq (paging + category) -------------------
    @Test
    @DisplayName("FAQ 목록: 카테고리 필터 null이면 전체, 있으면 해당 카테고리 + 페이징")
    void faqPage_ok() {
        when(faqService.findAll()).thenReturn(faqs);
        when(faqService.findByCategory("A")).thenReturn(faqs.stream().filter(f -> "A".equals(f.getCategory())).collect(Collectors.toList()));

        // 전체
        Model m1 = new ExtendedModelMap();
        String v1 = controller.faqPage(m1, 1, 7, null);
        assertThat(v1).isEqualTo("customer-center/faq");
        @SuppressWarnings("unchecked")
        List<Faq> p1 = (List<Faq>) m1.getAttribute("faqs");
        assertThat(p1).hasSize(7);

        // 카테고리 A
        Model m2 = new ExtendedModelMap();
        String v2 = controller.faqPage(m2, 2, 5, "A");
        assertThat(v2).isEqualTo("customer-center/faq");
        @SuppressWarnings("unchecked")
        List<Faq> p2 = (List<Faq>) m2.getAttribute("faqs");
        int totalA = (int) faqs.stream().filter(f -> "A".equals(f.getCategory())).count();
        int expectedSize = Math.min(5, totalA - 5); // 2페이지
        assertThat(p2).hasSize(Math.max(0, expectedSize));
        assertThat(m2.getAttribute("selectedCategory")).isEqualTo("A");
    }

    // ------------------- GET /customer-center/faq-detail -------------------
    @Test
    @DisplayName("FAQ 상세: 조회수 + 도움됨 퍼센트 + 사용자 투표 + prev/next/related")
    void faqDetail_ok() {
        Long id = 5L;
        Faq target = faqs.stream().filter(f -> f.getId().equals(id)).findFirst().orElseThrow();

        when(faqService.findById(id)).thenReturn(target);
        when(faqService.helpfulPercentage(id)).thenReturn(80);
        when(faqService.getUserVote(eq(id), any(Member.class))).thenReturn(null); // 투표 안 함
        when(faqService.findByCategory(target.getCategory())).thenReturn(
                faqs.stream().filter(f -> f.getCategory().equals(target.getCategory())).collect(Collectors.toList())
        );
        when(faqService.findAll()).thenReturn(faqs);

        Member mem = new Member();
        MyUserDetails user = new MyUserDetails(mem);

        Model model = new ExtendedModelMap();
        String view = controller.faqDetailPage(id, user, model);

        assertThat(view).isEqualTo("customer-center/faq-detail");
        verify(faqService).increaseViewCount(id);

        assertThat(model.getAttribute("faq")).isSameAs(target);
        assertThat((int) model.getAttribute("helpfulPercentage")).isEqualTo(80);
        assertThat(model.getAttribute("userVote")).isNull(); // 투표한 적 없음
        assertThat(model.getAttribute("relatedFaqs")).isNotNull();
        assertThat(model.getAttribute("prevFaq")).isNotNull();
        assertThat(model.getAttribute("nextFaq")).isNotNull();
    }

    // ------------------- POST /customer-center/announcement/helpful/{id} -------------------
    @Test
    @DisplayName("공지 도움됨 토글: 미로그인 401, 로그인 시 active/count 응답")
    void announcementHelpful_toggle() {
        // 미로그인
        ResponseEntity<Map<String, Object>> unauth = controller.announcementHelpful(1L, null);
        assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 로그인
        Member m = new Member();
        MyUserDetails user = new MyUserDetails(m);
        when(customerNotificationService.toggleHelpful(1L, m)).thenReturn(true);
        when(customerNotificationService.helpfulCount(1L)).thenReturn(10L);

        ResponseEntity<Map<String, Object>> ok = controller.announcementHelpful(1L, user);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody()).isNotNull();
        assertThat(ok.getBody().get("active")).isEqualTo(true);
        assertThat(ok.getBody().get("count")).isEqualTo(10L);
    }

    // ------------------- POST /customer-center/faq/helpful|not-helpful/{id} -------------------
    @Test
    @DisplayName("FAQ 도움됨/도움안됨: 미로그인 401, 로그인 시 200")
    void faqHelpful_toggle() {
        // helpful
        ResponseEntity<?> r1 = controller.faqHelpful(2L, null);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        Member m = new Member();
        MyUserDetails user = new MyUserDetails(m);

        ResponseEntity<?> r2 = controller.faqHelpful(2L, user);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(faqService).voteHelpful(2L, m, true);

        // not helpful
        ResponseEntity<?> r3 = controller.notHelpful(3L, null);
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<?> r4 = controller.notHelpful(3L, user);
        assertThat(r4.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(faqService).voteHelpful(3L, m, false);
    }

    // ------------------- GET /customer-center/faq/data -------------------
    @Test
    @DisplayName("FAQ 데이터 API: category 없으면 전체 상위5, 있으면 해당 카테고리 전체")
    void getFaqData_ok() {
        when(faqService.findAll()).thenReturn(faqs);
        when(faqService.findByCategory("B")).thenReturn(
                faqs.stream().filter(f -> "B".equals(f.getCategory())).collect(Collectors.toList())
        );

        // category null
        List<FaqResponse> top5 = controller.getFaqData(null);
        assertThat(top5).hasSize(5);

        // category B
        List<FaqResponse> onlyB = controller.getFaqData("B");
        int countB = (int) faqs.stream().filter(f -> "B".equals(f.getCategory())).count();
        assertThat(onlyB).hasSize(countB);
        assertThat(onlyB).allMatch(fr -> "B".equals(fr.getCategory()));
    }

    // ------------------- GET /customer-center/search/api -------------------
    @Test
    @DisplayName("통합 검색 API: FAQ/공지 검색 결과와 totalCount 반환")
    void searchAll_ok() {
        String q = "hello";
        when(faqService.searchByTitleOrContent(q)).thenReturn(faqs.subList(0, 3));
        List<NotificationResponse> foundNotices = notifications.subList(0, 2);
        when(customerNotificationService.searchByTitleOrContent(q)).thenReturn(foundNotices);

        Map<String, Object> result = controller.searchAll(q);

        @SuppressWarnings("unchecked")
        List<?> faqList = (List<?>) result.get("faq");
        @SuppressWarnings("unchecked")
        List<?> noticeList = (List<?>) result.get("notice");

        assertThat(faqList).hasSize(3);
        assertThat(noticeList).hasSize(2);
        assertThat(result.get("totalCount")).isEqualTo(5);
    }

    // ------------------- GET /customer-center/search (view only) -------------------
    @Test
    @DisplayName("검색 페이지 뷰: query 모델에 세팅하고 뷰명 반환")
    void searchPage_ok() {
        Model model = new ExtendedModelMap();
        String view = controller.searchPage("nvda", model);

        assertThat(view).isEqualTo("customer-center/customer-center-search");
        assertThat(model.getAttribute("query")).isEqualTo("nvda");
    }
}

package com.moodTrip.spring.domain.attraction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.moodTrip.spring.global.common.util.SecurityUtil; // ✅ SecurityUtil 임포트

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.moodTrip.spring.domain.emotion.service.EmotionService;

@WebMvcTest(AttractionController.class) // AttractionController를 테스트 대상으로 지정
@WithMockUser // Spring Security가 적용된 API 테스트를 위해 가짜 사용자 인증 추가
class AttractionControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션

    @MockitoBean
    private EmotionService emotionService;

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환

    @MockitoBean // Controller가 의존하는 Service를 가짜(Mock) 객체로 대체
    private AttractionService attractionService;

    @MockitoBean
    private SecurityUtil securityUtil;


    @BeforeEach
    void setUp() {
        // attractionService.create() 메소드는 어떤 AttractionInsertRequest 객체를 받든지
        // 항상 아래에 정의된 AttractionResponse 객체를 반환하도록 설정합니다.
        AttractionResponse mockResponse = AttractionResponse.builder()
                .contentId(12345L)
                .title("기본 응답 관광지")
                .addr1("기본 주소")
                .build();

        given(attractionService.create(any(AttractionInsertRequest.class))).willReturn(mockResponse);
    }

    @Test
    @DisplayName("새로운 관광지를 성공적으로 생성한다")
    void create_Success() throws Exception {
        // given
        AttractionInsertRequest request = new AttractionInsertRequest();
        request.setTitle("새로운 관광지");
        request.setAddr1("서울시 테스트구 테스트동");

        AttractionResponse response = AttractionResponse.builder()
                .contentId(12345L)
                .title("새로운 관광지")
                .addr1("서울시 테스트구 테스트동")
                .build();

        // AttractionController의 `service` 필드와 `attractionService` 필드가 동일한 MockBean을 참조하도록 명시적 설정
        given(attractionService.create(any(AttractionInsertRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/attractions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/attractions/content/12345"))
                .andExpect(jsonPath("$.contentId").value(12345))
                .andExpect(jsonPath("$.title").value("새로운 관광지"));
    }

    @Test
    @DisplayName("필수 값이 없는 관광지 생성 요청 시, (현재 로직상) 201 Created를 반환한다")
    void create_With_InvalidInput_ShouldReturnCreated_AsValidationIsMissing() throws Exception {
        // given
        // title이 null인 요청 데이터
        AttractionInsertRequest invalidRequest = new AttractionInsertRequest();
        invalidRequest.setAddr1("주소만 있음");

        // setUp()에서 정의한 기본 Mock 행동이 사용됩니다.
        // 따라서 service.create()는 null이 아닌 mockResponse를 반환합니다.

        // when & then
        // DTO에 유효성 검증이 없으므로, 서버는 이 요청을 정상으로 처리하고 201을 반환하게 됩니다.
        // NullPointerException은 더 이상 발생하지 않습니다.
        mockMvc.perform(post("/api/attractions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/attractions/content/12345"))
                .andExpect(jsonPath("$.title").value("기본 응답 관광지"));
    }


    @Test
    @DisplayName("키워드로 관광지 목록을 페이징하여 조회한다")
    void searchPaged_Success() throws Exception {
        // given
        String keyword = "바다";
        int page = 0;
        int size = 10;

        Attraction attraction = Attraction.builder().title("해운대 바다").contentId(1L).build();
        PageImpl<Attraction> pageResult = new PageImpl<>(List.of(attraction), PageRequest.of(page, size), 1);

        // 서비스의 검색 메소드가 호출되면 위에서 만든 Page 객체를 반환하도록 설정
        given(attractionService.searchKeywordPrefTitleStarts(anyString(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/attractions/search-paged")
                        .param("q", keyword)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1)) // 전체 아이템 수 검증
                .andExpect(jsonPath("$.content[0].title").value("해운대 바다")); // 첫 번째 아이템의 제목 검증
    }

    @Test
    @DisplayName("Content ID로 관광지 상세 정보를 조회한다")
    void getDetail_Success() throws Exception {
        // given
        long contentId = 12345L;
        AttractionDetailResponse detailResponse = AttractionDetailResponse.builder()
                .contentId(contentId)
                .title("상세 정보 테스트 관광지")
                .overview("이것은 상세한 설명입니다.")
                .build();

        given(attractionService.getDetailResponse(contentId)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/attractions/content/{contentId}/detail", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(contentId))
                .andExpect(jsonPath("$.title").value("상세 정보 테스트 관광지"))
                .andExpect(jsonPath("$.overview").value("이것은 상세한 설명입니다."));
    }

    @Test
    @DisplayName("Content ID로 관광지의 감정 태그 목록을 조회한다")
    void getEmotionTags_Success() throws Exception {
        // given
        long contentId = 12345L;
        List<String> tags = List.of("행복", "설렘", "신남");

        given(attractionService.getEmotionTagNames(contentId)).willReturn(tags);

        // when & then
        mockMvc.perform(get("/api/attractions/content/{contentId}/emotion-tags", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // 결과가 배열인지 확인
                .andExpect(jsonPath("$.length()").value(3)) // 배열의 크기 확인
                .andExpect(jsonPath("$[0]").value("행복"))
                .andExpect(jsonPath("$[1]").value("설렘"));
    }
}

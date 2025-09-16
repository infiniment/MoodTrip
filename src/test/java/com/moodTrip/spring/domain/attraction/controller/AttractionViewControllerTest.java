package com.moodTrip.spring.domain.attraction.controller; // 컨트롤러와 같은 패키지에 위치

import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AttractionViewController에 대한 웹 계층 테스트.
 * 이 컨트롤러는 단순 뷰 반환 로직만 가지므로,
 * 서비스 Mocking 없이 요청/응답 및 뷰 이름 검증에 초점을 맞춥니다.
 */
@WebMvcTest(AttractionViewController.class) // 1. 테스트할 컨트롤러 클래스를 지정
@WithMockUser // 2. Spring Security 필터를 통과하기 위한 Mock 사용자 설정
class AttractionViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 3. @WebMvcTest 실행 시 로드되는 전역 Advice들의 의존성 해결을 위한 Mock Bean
    // 이 테스트에서 직접 사용되지는 않지만, 컨텍스트 로딩을 위해 반드시 필요합니다.
    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private EmotionService emotionService;

    @Test
    @DisplayName("GET /regions 요청 시 지역별 관광지 페이지를 정상적으로 반환한다")
    void regionPage_ReturnsCorrectViewAndModel() throws Exception {
        // when & then
        mockMvc.perform(get("/regions"))
                // HTTP 응답 상태가 200 OK 인지 검증
                .andExpect(status().isOk())
                // 반환된 뷰의 이름이 'region-tourist-attractions/region-page'인지 검증
                .andExpect(view().name("region-tourist-attractions/region-page"))
                // 모델에 'initialAttractions' 속성이 존재하는지 검증
                .andExpect(model().attributeExists("initialAttractions"))
                // 'initialAttractions' 속성의 값이 빈 리스트인지 검증
                .andExpect(model().attribute("initialAttractions", is(Collections.emptyList())));
    }
}

// src/test/java/com/moodTrip/spring/domain/member/controller/SuspendedPageControllerTest.java
package com.moodTrip.spring.domain.member.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// JUnit5 + Mockito
@ExtendWith(MockitoExtension.class)
class SuspendedPageControllerTest {

    private SuspendedPageController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new SuspendedPageController();

        // 뷰 리졸버 설정(standalone에서 view().name() 검증 안정화용)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("메서드 직접 호출 시 올바른 뷰 이름 반환")
    void suspendedPage_returnsCorrectViewName() {
        String viewName = controller.suspendedPage();
        assertThat(viewName).isEqualTo("login/suspended");
    }

    @Test
    @DisplayName("GET /suspended → 200 OK & 뷰 이름 login/suspended")
    void getSuspended_returnsOkAndView() throws Exception {
        mockMvc.perform(get("/suspended"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/suspended"));
    }

    @Test
    @DisplayName("잘못된 경로는 404")
    void wrongPath_returns404() throws Exception {
        mockMvc.perform(get("/suspendedd")) // 오타
                .andExpect(status().isNotFound());
    }
}

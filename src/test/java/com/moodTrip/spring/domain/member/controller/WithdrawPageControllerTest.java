package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.controller.WithdrawPageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WithdrawPageControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WithdrawPageController controller = new WithdrawPageController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void withdrawPage_returnsCorrectView() throws Exception {
        mockMvc.perform(get("/withdraw"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/withdraw"));
    }
}
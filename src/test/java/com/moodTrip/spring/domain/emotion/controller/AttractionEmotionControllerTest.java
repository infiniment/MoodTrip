package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.controller.AttractionEmotionController;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AttractionEmotionControllerTest {

    @Mock
    private AttractionEmotionService attractionEmotionService;

    @Mock
    private AttractionService attractionService;

    @Mock
    private EmotionService emotionService;

    @InjectMocks
    private AttractionEmotionController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void showMappingPage_shouldReturnViewWithModelAttributes() throws Exception {
        // 준비 (Arrange)
        Page<Attraction> attractionPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 1);

        // 행동 정의 (Stubbing) - int 타입과 매칭되도록 anyInt() 사용
        when(attractionService.findAttractions(anyInt(), anyInt())).thenReturn(attractionPage);
        when(emotionService.getAllEmotions()).thenReturn(Collections.emptyList());
        when(attractionEmotionService.getAttractionToEmotionIdsMap()).thenReturn(Collections.emptyMap());
        when(attractionEmotionService.getAttractionToEmotionWeightsMap()).thenReturn(Collections.emptyMap());

        // 실행 (Act) 및 검증 (Assert)
        mockMvc.perform(get("/admin/attraction-emotions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/attraction-emotion-mapping :: content"))
                .andExpect(model().attributeExists("attractions"));
    }


    @Test
    void search_shouldReturnViewWithSearchResults() throws Exception {
        List<Attraction> attractions = List.of(mock(Attraction.class));
        Page<Attraction> searchPage = new PageImpl<>(attractions, PageRequest.of(0,10), 1);

        when(attractionService.searchAttractions(anyString(), anyInt(), anyInt())).thenReturn(searchPage);
        when(emotionService.getAllEmotions()).thenReturn(List.of());
        when(attractionEmotionService.getAttractionToEmotionIdsMap()).thenReturn(Collections.emptyMap());
        when(attractionEmotionService.getAttractionToEmotionWeightsMap()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/admin/attraction-emotions/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/attraction-emotion-mapping :: content"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attributeExists("attractions"))
                .andExpect(model().attributeExists("emotions"));
    }

    @Test
    void updateAttractionEmotion_success() throws Exception {
        List<EmotionWeightDto> dtoList = List.of(new EmotionWeightDto(1L, BigDecimal.valueOf(0.5)));

        mockMvc.perform(post("/admin/attraction-emotions/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attraction emotions updated successfully"));
    }

    @Test
    void updateAttractionEmotion_badRequest() throws Exception {
        List<EmotionWeightDto> dtoList = List.of(new EmotionWeightDto(1L, BigDecimal.valueOf(0.5)));

        doThrow(new IllegalArgumentException("Invalid data"))
                .when(attractionEmotionService).updateAttractionEmotions(eq(1L), anyList());

        mockMvc.perform(post("/admin/attraction-emotions/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data"));
    }

    @Test
    void updateAttractionEmotion_internalServerError() throws Exception {
        List<EmotionWeightDto> dtoList = List.of(new EmotionWeightDto(1L, BigDecimal.valueOf(0.5)));

        doAnswer(invocation -> {
            System.out.println("Mocked updateAttractionEmotions throwing exception");
            throw new RuntimeException("DB error");
        }).when(attractionEmotionService).updateAttractionEmotions(eq(1L), anyList());

        mockMvc.perform(post("/admin/attraction-emotions/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}

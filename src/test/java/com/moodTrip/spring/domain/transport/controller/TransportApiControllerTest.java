package com.moodTrip.spring.domain.transport.controller;

import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;
import com.moodTrip.spring.domain.transport.service.ODsayService;
import com.moodTrip.spring.domain.transport.service.TransportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransportApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransportApiControllerIT {

    @Autowired MockMvc mockMvc;

    // 컨트롤러 의존 서비스
    @MockitoBean
    TransportService transportService;
    @MockitoBean ODsayService odsayService;

    // 전역 어드바이스가 요구하는 빈들(있으면 모킹해서 충돌 방지)
    @MockitoBean
    com.moodTrip.spring.global.common.util.SecurityUtil securityUtil;
    @MockitoBean
    com.moodTrip.spring.domain.emotion.service.EmotionService emotionService;

    @Test
    @DisplayName("GET /api/v1/transport/kakao/search : 쿼리 + (선택)x,y,radius 바인딩 및 서비스 호출")
    void kakao_search_ok() throws Exception {
        String query = "카페";
        Double x = 127.0;
        Double y = 37.5;
        Integer radius = 500;

        // Mockito mock 대신 '직렬화 가능한 실제 DTO 인스턴스'를 반환
        KakaoPlaceResponse dto = new KakaoPlaceResponse(); // no-args 생성자가 있어야 함
        when(transportService.searchPlace(query, x, y, radius)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/transport/kakao/search")
                        .param("query", query)
                        .param("x", String.valueOf(x))
                        .param("y", String.valueOf(y))
                        .param("radius", String.valueOf(radius)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(transportService).searchPlace(query, x, y, radius);
    }

    @Test
    @DisplayName("GET /api/v1/transport/kakao/geocode : 주소 파라미터 바인딩 및 서비스 호출")
    void kakao_geocode_ok() throws Exception {
        String address = "서울특별시 중구 세종대로 110";

        KakaoAddressSearchResponse dto = new KakaoAddressSearchResponse();
        when(transportService.geocode(address)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/transport/kakao/geocode")
                        .param("address", address))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(transportService).geocode(address);
    }

    @Test
    @DisplayName("GET /api/v1/transport/kakao/reverse-geocode : 좌표 파라미터 바인딩 및 서비스 호출")
    void kakao_reverse_ok() throws Exception {
        double x = 127.0, y = 37.5;

        KakaoCoord2AddressResponse dto = new KakaoCoord2AddressResponse();
        when(transportService.reverseGeocode(x, y)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/transport/kakao/reverse-geocode")
                        .param("x", String.valueOf(x))
                        .param("y", String.valueOf(y)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(transportService).reverseGeocode(x, y);
    }

    @Test
    @DisplayName("GET /api/v1/transport/routes : 환승 경로 조회 파라미터 바인딩 및 서비스 호출")
    void routes_ok() throws Exception {
        double sx = 127.0, sy = 37.5, ex = 126.98, ey = 37.57;

        when(odsayService.getTransitRoutes(sx, sy, ex, ey)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/transport/routes")
                        .param("sx", String.valueOf(sx))
                        .param("sy", String.valueOf(sy))
                        .param("ex", String.valueOf(ex))
                        .param("ey", String.valueOf(ey)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(odsayService).getTransitRoutes(sx, sy, ex, ey);
    }

    @Test
    @DisplayName("GET /api/v1/transport/kakao/search : 선택 파라미터 생략해도 동작")
    void kakao_search_without_optional_params_ok() throws Exception {
        String query = "편의점";

        KakaoPlaceResponse dto = new KakaoPlaceResponse();
        when(transportService.searchPlace(eq(query), isNull(), isNull(), isNull())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/transport/kakao/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(transportService).searchPlace(eq(query), isNull(), isNull(), isNull());
    }
}

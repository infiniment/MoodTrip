package com.moodTrip.spring.domain.attraction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionRegionResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.attraction.repository.*;
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttractionServiceImplTest {

    @Mock private AttractionRepository repository;
    @Mock private AttractionIntroRepository introRepository;
    @Mock private AttractionEmotionRepository attractionEmotionRepository;
    @Mock private UserAttractionRepository userAttractionRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private AttractionServiceImpl service;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Attraction sampleAttraction;
    private AttractionIntro sampleIntro;

    @BeforeEach
    void setUp() {
        sampleAttraction = Attraction.builder()
                .attractionId(1L)
                .contentId(123L)
                .title("테스트 관광지")
                .contentTypeId(12)
                .areaCode(11)
                .sigunguCode(1)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        sampleIntro = AttractionIntro.builder()
                .contentId(123L)
                .contentTypeId(12)
                .infocenter("123-4567")
                .usetime("09:00~18:00")
                .overview("테스트 개요")
                .build();
    }

    @Test
    @DisplayName("syncAreaBasedList: 외부 API JSON 파싱 성공")
    void testSyncAreaBasedList() {
        // given
        String fakeJson = """
            {
              "response": {
                "header": { "resultCode": "0000", "resultMsg": "OK" },
                "body": {
                  "totalCount": 1,
                  "items": {
                    "item": {
                      "contentid": "123",
                      "contenttypeid": "12",
                      "title": "테스트 관광지",
                      "areacode": "11",
                      "sigungucode": "1",
                      "createdtime": "20220101120000",
                      "modifiedtime": "20220102120000"
                    }
                  }
                }
              }
            }
            """;
        when(restTemplate.getForObject(any(), eq(String.class))).thenReturn(fakeJson);
        when(repository.findByContentId(123L)).thenReturn(Optional.of(sampleAttraction));
        when(introRepository.existsById(123L)).thenReturn(true);

        // when
        int created = service.syncAreaBasedList(11, 1, 12, 10, 0);

        // then
        assertThat(created).isEqualTo(0); // 이미 존재 → 신규 추가 아님
    }

    @Test
    @DisplayName("syncDetailIntro: 외부 API JSON 파싱 성공")
    void testSyncDetailIntro() {
        // given
        String fakeJson = """
            {
              "response": {
                "header": { "resultCode": "0000", "resultMsg": "OK" },
                "body": {
                  "items": {
                    "item": {
                      "contentid": "123",
                      "contenttypeid": "12",
                      "infocenter": "123-4567",
                      "usetime": "09:00~18:00",
                      "overview": "테스트 개요"
                    }
                  }
                }
              }
            }
            """;
        when(restTemplate.getForObject(any(), eq(String.class))).thenReturn(fakeJson);
        when(introRepository.findById(123L)).thenReturn(Optional.of(sampleIntro));

        // when
        int saved = service.syncDetailIntro(123L, 12);

        // then
        assertThat(saved).isEqualTo(1);
    }

    @Test
    @DisplayName("getDetailResponse: 상세 응답 조립 성공")
    void testGetDetailResponse() {
        // given
        when(repository.findByContentId(123L)).thenReturn(Optional.of(sampleAttraction));
        when(introRepository.findById(123L)).thenReturn(Optional.of(sampleIntro));

        // when
        AttractionDetailResponse resp = service.getDetailResponse(123L, 12);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.getContentId()).isEqualTo(123L);
        assertThat(resp.getTitle()).isEqualTo("테스트 관광지");
        assertThat(resp.getOverview()).isEqualTo("테스트 개요");
    }

    @Test
    @DisplayName("findAttractionsByEmotionIds: 감정 태그 기반 조회")
    void testFindAttractionsByEmotionIds() {
        // given
        when(repository.findAttractionsByEmotionIds(anyList())).thenReturn(List.of(sampleAttraction));

        // when
        var results = service.findAttractionsByEmotionIds(List.of(1, 2));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("테스트 관광지");
    }

    @Test
    @DisplayName("findPopularAttractions: 인기순 관광지 조회")
    void testFindPopularAttractions() {
        // given
        when(userAttractionRepository.findPopularAttractionIds(any())).thenReturn(List.of(1L));
        when(repository.findAllById(anyList())).thenReturn(List.of(sampleAttraction));

        // when
        var results = service.findPopularAttractions(5);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("테스트 관광지");
    }

    @Test
    @DisplayName("find: 지역별 조회")
    void testFind() {
        when(repository.findAllByAreaCode(11)).thenReturn(List.of(sampleAttraction));

        var results = service.find(11, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContentId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("searchAttractions: 키워드 검색")
    void testSearchAttractions() {
        Page<Attraction> page = new PageImpl<>(List.of(sampleAttraction));
        when(repository.findByTitleContainingIgnoreCase(eq("테스트"), any())).thenReturn(page);

        var results = service.searchAttractions("테스트", 0, 10);

        assertThat(results.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getRegionAttractions: 지역별 페이징 조회")
    void testGetRegionAttractions() {
        Page<Attraction> page = new PageImpl<>(List.of(sampleAttraction));
        when(repository.findByAreaCode(eq(11), any(Pageable.class))).thenReturn(page);

        AttractionRegionResponse resp = service.getRegionAttractions(11, null, 0, 10);

        assertThat(resp.getList()).hasSize(1);   // ✅ getList() 사용
        assertThat(resp.getList().get(0).getTitle()).isEqualTo("테스트 관광지");
    }

}

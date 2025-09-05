package com.moodTrip.spring.domain.emotion.service;


import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // ✅
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit5에서 Mockito를 사용하기 위한 확장
class AttractionEmotionServiceTest {

    @InjectMocks // 테스트 대상 클래스. @Mock으로 생성된 객체들이 여기에 주입됩니다.
    private AttractionEmotionService attractionEmotionService;

    // 의존성들을 Mock 객체로 생성
    @Mock
    private AttractionEmotionRepository attractionEmotionRepository;

    @Mock
    private EmotionRepository emotionRepository;

    @Mock
    private AttractionRepository attractionRepository;

    private Attraction testAttraction;
    private Emotion testEmotion1;
    private Emotion testEmotion2;

    @BeforeEach
    void setUp() {
        // given
        testAttraction = Attraction.builder()
                .title("테스트 관광지")
                .addr1("테스트 주소")
                .firstImage("image.jpg")
                .build();

        // ✅ ReflectionTestUtils를 사용하여 attractionId 필드에 값을 직접 주입합니다.
        // 이것이 가장 확실하고 안정적인 방법입니다.
        ReflectionTestUtils.setField(testAttraction, "attractionId", 1L);

        testEmotion1 = Emotion.builder().tagId(101).tagName("행복").build();
    }


    @Test
    @DisplayName("관광지 감정 가중치 목록을 성공적으로 갱신한다")
    void updateAttractionEmotions_Success() {
        // given
        Long attractionId = testAttraction.getAttractionId();

        // 시나리오: 기존 '행복' 감정은 가중치를 바꾸고, '설렘' 감정은 새로 추가
        List<EmotionWeightDto> emotionWeights = List.of(
                new EmotionWeightDto(101L, new BigDecimal("0.8")), // 기존 감정 (가중치 변경)
                new EmotionWeightDto(102L, new BigDecimal("0.5"))  // 새로운 감정
        );

        // 기존에 '행복' 감정만 활성화 상태였다고 가정
        AttractionEmotion existingMapping = AttractionEmotion.builder()
                .attraction(testAttraction)
                .emotion(testEmotion1)
                .weight(new BigDecimal("0.3"))
                .isActive(true)
                .build();

        // Repository Mocking 설정
        when(attractionEmotionRepository.findByAttraction_AttractionIdAndIsActiveTrue(attractionId))
                .thenReturn(List.of(existingMapping));
        when(attractionEmotionRepository.findByAttraction_AttractionIdAndEmotion_TagId(attractionId, 101L))
                .thenReturn(Optional.of(existingMapping));
        when(attractionEmotionRepository.findByAttraction_AttractionIdAndEmotion_TagId(attractionId, 102L))
                .thenReturn(Optional.empty()); // '설렘'은 기존에 없었음

        when(emotionRepository.findById(102L)).thenReturn(Optional.of(testEmotion2));
        when(attractionRepository.findById(attractionId)).thenReturn(Optional.of(testAttraction));

        // when
        attractionEmotionService.updateAttractionEmotions(attractionId, emotionWeights);

        // then
        // 1. 기존 매핑 비활성화를 위해 saveAll이 호출되었는지 검증
        verify(attractionEmotionRepository).saveAll(any());

        // 2. 새로운/수정된 매핑 저장을 위해 save가 2번 호출되었는지 검증
        ArgumentCaptor<AttractionEmotion> captor = ArgumentCaptor.forClass(AttractionEmotion.class);
        verify(attractionEmotionRepository, times(2)).save(captor.capture());

        List<AttractionEmotion> savedMappings = captor.getAllValues();
        assertThat(savedMappings).hasSize(2);

        // '행복' 감정 검증
        AttractionEmotion happyMapping = savedMappings.stream()
                .filter(m -> m.getEmotion().getTagId().equals(101))
                .findFirst().orElseThrow();
        assertThat(happyMapping.getWeight()).isEqualByComparingTo("0.8");
        assertThat(happyMapping.isActive()).isTrue();

        // '설렘' 감정 검증
        AttractionEmotion excitedMapping = savedMappings.stream()
                .filter(m -> m.getEmotion().getTagId().equals(102))
                .findFirst().orElseThrow();
        assertThat(excitedMapping.getWeight()).isEqualByComparingTo("0.5");
        assertThat(excitedMapping.isActive()).isTrue();
    }

    @Test
    @DisplayName("특정 contentId에 연결된 모든 감정 태그 이름을 반환한다")
    void findEmotionNamesByContentId_Success() {
        // given
        long contentId = 12345L;
        Attraction attractionWithContentId = Attraction.builder().contentId(contentId).build();
        List<AttractionEmotion> mappings = List.of(
                AttractionEmotion.builder().attraction(attractionWithContentId).emotion(testEmotion1).build(),
                AttractionEmotion.builder().attraction(attractionWithContentId).emotion(testEmotion2).build()
        );

        when(attractionEmotionRepository.findByAttraction_ContentIdAndIsActiveTrue(contentId))
                .thenReturn(mappings);

        // when
        List<String> emotionNames = attractionEmotionService.findEmotionNamesByContentId(contentId);

        // then
        assertThat(emotionNames).hasSize(2).containsExactlyInAnyOrder("행복", "설렘");
    }
//
//    @Test
//    @DisplayName("특정 감정 ID로 관련 관광지 목록을 조회한다")
//    void findAttractionsByEmotion_Success() {
//        // given
//        Long emotionId = 101L;
//
//        List<AttractionEmotion> mappings = List.of(
//                AttractionEmotion.builder().attraction(testAttraction).emotion(testEmotion1).build()
//        );
//
//        when(attractionEmotionRepository.findByEmotion_TagIdAndIsActiveTrue(emotionId.intValue()))
//                .thenReturn(mappings);
//
//        // when
//        List<AttractionResponse> attractions = attractionEmotionService.findAttractionsByEmotion(emotionId);
//
//        // then
//        assertThat(attractions).hasSize(1);
//
//        // 이제 testAttraction 객체의 ID가 확실히 1L이므로, DTO 변환 후에도 null이 되지 않습니다.
//        assertThat(attractions.get(0).getAttractionId()).isEqualTo(1L);
//        assertThat(attractions.get(0).getTitle()).isEqualTo("테스트 관광지");
//    }


    @Test
    @DisplayName("관련 관광지가 없는 감정 ID 조회 시 빈 리스트를 반환한다")
    void findAttractionsByEmotion_WhenNoAttractions_ShouldReturnEmptyList() {
        // given
        Long emotionId = 999L; // 존재하지 않거나, 매핑되지 않은 감정
        when(attractionEmotionRepository.findByEmotion_TagIdAndIsActiveTrue(emotionId.intValue()))
                .thenReturn(Collections.emptyList());

        // when
        var attractions = attractionEmotionService.findAttractionsByEmotion(emotionId);

        // then
        assertThat(attractions).isNotNull().isEmpty();
    }
}
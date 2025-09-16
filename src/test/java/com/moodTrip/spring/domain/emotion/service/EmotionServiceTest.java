package com.moodTrip.spring.domain.emotion.service;


import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import com.moodTrip.spring.domain.emotion.repository.EmotionCategoryRepository;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionServiceTest {

    @InjectMocks // 테스트 대상 서비스
    private EmotionService emotionService;

    @Mock // 서비스가 의존하는 Repository Mock 객체
    private EmotionCategoryRepository emotionCategoryRepository;

    @Mock
    private EmotionRepository emotionRepository;

    private EmotionCategory category1;
    private EmotionCategory category2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        Emotion happy = Emotion.builder().tagId(101).tagName("행복").build();
        Emotion excited = Emotion.builder().tagId(102).tagName("설렘").build();
        Emotion sad = Emotion.builder().tagId(201).tagName("슬픔").build();

        // ✅ 수정된 부분: Set.of() -> List.of()로 변경하여 타입 일치
        category1 = EmotionCategory.builder()
                .emotionCategoryId(1L)
                .emotionCategoryName("긍정")
                .emotions(List.of(happy, excited)) // Set -> List
                .build();

        // ✅ 수정된 부분: Set.of() -> List.of()로 변경하여 타입 일치
        category2 = EmotionCategory.builder()
                .emotionCategoryId(2L)
                .emotionCategoryName("부정")
                .emotions(List.of(sad)) // Set -> List
                .build();
    }

    @Test
    @DisplayName("모든 감정 카테고리와 하위 감정 목록을 중복 없이 DTO로 변환하여 반환한다")
    void getEmotionCategories_Success() {
        // given
        // Repository가 중복된 데이터를 포함한 리스트를 반환한다고 가정
        List<EmotionCategory> categoriesWithDuplicates = List.of(category1, category2, category1);

        when(emotionCategoryRepository.findAllWithEmotions()).thenReturn(categoriesWithDuplicates);

        // when
        List<EmotionCategoryDto> resultDtos = emotionService.getEmotionCategories();

        // then
        // 1. 서비스 로직에 의해 중복이 제거되어 최종 결과는 2개여야 한다.
        assertThat(resultDtos).hasSize(2);

        // 2. 첫 번째 카테고리('긍정')의 내용이 올바른지 확인
        EmotionCategoryDto positiveCategoryDto = resultDtos.stream()
                .filter(dto -> dto.getEmotionCategoryName().equals("긍정"))
                .findFirst().orElseThrow();

        assertThat(positiveCategoryDto.getEmotionCategoryId()).isEqualTo(1L);
        assertThat(positiveCategoryDto.getEmotions()).hasSize(2);
        assertThat(positiveCategoryDto.getEmotions())
                .extracting("tagName") // EmotionDto의 tagName 필드를 추출
                .containsExactlyInAnyOrder("행복", "설렘");

        // 3. 두 번째 카테고리('부정')의 내용이 올바른지 확인
        EmotionCategoryDto negativeCategoryDto = resultDtos.stream()
                .filter(dto -> dto.getEmotionCategoryName().equals("부정"))
                .findFirst().orElseThrow();
        assertThat(negativeCategoryDto.getEmotions()).hasSize(1);
        assertThat(negativeCategoryDto.getEmotions().get(0).getTagName()).isEqualTo("슬픔");
    }

    @Test
    @DisplayName("감정 카테고리가 없을 경우 빈 리스트를 반환한다")
    void getEmotionCategories_WhenEmpty_ShouldReturnEmptyList() {
        // given
        // Repository가 빈 리스트를 반환한다고 설정
        when(emotionCategoryRepository.findAllWithEmotions()).thenReturn(Collections.emptyList());

        // when
        List<EmotionCategoryDto> resultDtos = emotionService.getEmotionCategories();

        // then
        assertThat(resultDtos).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("모든 감정 엔티티 목록을 반환한다")
    void getAllEmotions_Success() {
        // given
        Emotion emotion1 = Emotion.builder().tagId(1).tagName("기쁨").build();
        Emotion emotion2 = Emotion.builder().tagId(2).tagName("분노").build();
        List<Emotion> allEmotions = List.of(emotion1, emotion2);

        when(emotionRepository.findAll()).thenReturn(allEmotions);

        // when
        List<Emotion> result = emotionService.getAllEmotions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(emotion1, emotion2);
    }
}
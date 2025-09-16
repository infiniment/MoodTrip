package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // ← 실제 MariaDB 사용
@ActiveProfiles("test")
class AttractionEmotionRepositoryTest {

    @Autowired AttractionEmotionRepository repo;
    @Autowired org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager em;

    @Test
    void findActiveEmotionNamesByContentId_sortsByWeightDesc() {
        // given
        Attraction attr = Attraction.builder()
                .contentId(9999L)
                .title("테스트")
                .areaCode(1)
                .sigunguCode(1)
                .contentTypeId(14)
                .build();
        em.persist(attr);

        Emotion e1 = Emotion.builder().tagName("힐링").build();
        Emotion e2 = Emotion.builder().tagName("여유").build();
        em.persist(e1); em.persist(e2);

        em.persist(AttractionEmotion.builder()
                .attraction(attr).emotion(e1)
                .weight(new BigDecimal("0.80")).isActive(true).build());

        em.persist(AttractionEmotion.builder()
                .attraction(attr).emotion(e2)
                .weight(new BigDecimal("0.90")).isActive(true).build());

        em.flush();

        // when
        List<String> names = repo.findActiveEmotionNamesByContentId(9999L);

        // then
        assertThat(names).containsExactly("여유","힐링"); // weight 내림차순
    }
}
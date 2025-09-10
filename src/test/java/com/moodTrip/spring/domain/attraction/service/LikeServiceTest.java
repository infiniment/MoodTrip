package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.repository.UserAttractionRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private UserAttractionRepository userAttractionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AttractionRepository attractionRepository;

    @InjectMocks
    private LikeService likeService;

    private Member member;
    private Attraction attraction;

    @BeforeEach
    void setUp() {
        member = new Member();
        attraction = new Attraction();
    }

    @Test
    @DisplayName("addLike: 이미 찜한 경우 저장하지 않는다")
    void addLike_alreadyExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attractionRepository.findById(100L)).thenReturn(Optional.of(attraction));
        when(userAttractionRepository.existsByMemberAndAttraction(member, attraction)).thenReturn(true);

        likeService.addLike(1L, 100L);

        verify(userAttractionRepository, never()).save(any(UserAttraction.class));
    }

    @Test
    @DisplayName("addLike: 처음 찜하는 경우 저장된다")
    void addLike_newLike() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attractionRepository.findById(100L)).thenReturn(Optional.of(attraction));
        when(userAttractionRepository.existsByMemberAndAttraction(member, attraction)).thenReturn(false);

        likeService.addLike(1L, 100L);

        verify(userAttractionRepository, times(1)).save(any(UserAttraction.class));
    }

    @Test
    @DisplayName("removeLike: 회원 또는 관광지가 없으면 예외 발생")
    void removeLike_memberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> likeService.removeLike(1L, 100L)
        );
    }

    @Test
    @DisplayName("removeLike: 정상적으로 찜이 삭제된다")
    void removeLike_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attractionRepository.findById(100L)).thenReturn(Optional.of(attraction));

        likeService.removeLike(1L, 100L);

        verify(userAttractionRepository, times(1))
                .deleteByMemberAndAttraction(member, attraction);
    }

    @Test
    @DisplayName("isLiked: 찜 여부 확인 - true")
    void isLiked_true() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attractionRepository.findById(100L)).thenReturn(Optional.of(attraction));
        when(userAttractionRepository.existsByMemberAndAttraction(member, attraction)).thenReturn(true);

        boolean result = likeService.isLiked(1L, 100L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isLiked: 찜 여부 확인 - false")
    void isLiked_false() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attractionRepository.findById(100L)).thenReturn(Optional.of(attraction));
        when(userAttractionRepository.existsByMemberAndAttraction(member, attraction)).thenReturn(false);

        boolean result = likeService.isLiked(1L, 100L);

        assertThat(result).isFalse();
    }
}

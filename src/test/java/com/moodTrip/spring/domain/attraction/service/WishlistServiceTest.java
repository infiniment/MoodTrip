package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.repository.WishlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("removeWishlist: memberPk와 attractionId로 삭제 메서드가 호출된다")
    void removeWishlist_callsRepositoryDelete() {
        // given
        Long memberPk = 1L;
        Long attractionId = 100L;

        // when
        wishlistService.removeWishlist(memberPk, attractionId);

        // then
        verify(wishlistRepository, times(1))
                .deleteByMember_MemberPkAndAttraction_AttractionId(memberPk, attractionId);
    }


}

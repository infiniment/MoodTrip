package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public void removeWishlist(Long memberPk, Long attractionId) {
        // memberpk와 attractionid로 찜 데이터를 찾아 삭제
        wishlistRepository.deleteByMember_MemberPkAndAttraction_AttractionId(memberPk, attractionId);
    }
}

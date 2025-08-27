package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final WishlistService wishlistService;

    // 현재 로그인한 사용자 정보를 가져오는 로직이 필요합니다.
    // 예: @AuthenticationPrincipal UserDetails userDetails

    @DeleteMapping("/{attractionId}")
    public ResponseEntity<String> removeWishlist(@PathVariable Long attractionId) {
        ResponseEntity<String> result;
        // 1. 현재 로그인한 사용자의 memberpk를 가져옵니다.
        //    (Spring Security 등 인증 프레임워크를 통해 얻는 것을 권장)
        Long memberPk = getCurrentUserMemberPk(); // 예시 메서드

        if (memberPk == null) {
            result = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        } else {
            try {
                wishlistService.removeWishlist(memberPk, attractionId);
                result = ResponseEntity.ok("찜이 정상적으로 취소되었습니다.");
            } catch (Exception e) {
                // 예: 삭제할 데이터가 없는 경우 등 예외 처리
                result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("찜 취소 중 오류가 발생했습니다.");
            }
        }

        return result;
    }

    private Long getCurrentUserMemberPk() {
        // SecurityContextHolder 등에서 사용자 정보를 조회하여 반환하는 로직
        // 예시로 임의의 값을 반환합니다.
        return 1L;
    }
}
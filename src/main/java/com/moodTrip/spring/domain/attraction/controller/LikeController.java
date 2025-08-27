package com.moodTrip.spring.domain.attraction.controller;



import com.moodTrip.spring.domain.attraction.service.LikeService;
// CustomUserDetails 대신 MyUserDetails를 임포트합니다.
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    // 찜하기 (POST /api/likes/{attractionId})
    @PostMapping("/{attractionId}")
    public ResponseEntity<Void> addLike(
            @PathVariable Long attractionId,
            // 파라미터 타입을 CustomUserDetails에서 MyUserDetails로 변경합니다.
            @AuthenticationPrincipal MyUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        // userDetails.getMember()를 호출하여 Member 엔티티를 가져옵니다.
        Long memberPk = userDetails.getMember().getMemberPk();

        likeService.addLike(memberPk, attractionId);
        return ResponseEntity.ok().build();
    }

    // 찜 취소하기 (DELETE /api/likes/{attractionId})
    @DeleteMapping("/{attractionId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long attractionId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        if (userDetails == null) {
            // 로그인하지 않은 사용자는 접근 불가 (401 Unauthorized)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 현재 로그인한 사용자의 PK와 삭제할 관광지 ID를 서비스로 전달
        Long memberPk = userDetails.getMember().getMemberPk();
        likeService.removeLike(memberPk, attractionId);

        return ResponseEntity.ok().build(); // 성공 시 200 OK 응답
    }





}

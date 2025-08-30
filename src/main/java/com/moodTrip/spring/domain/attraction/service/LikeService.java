package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.repository.UserAttractionRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성해줍니다.
public class LikeService {

    private final UserAttractionRepository userAttractionRepository;
    private final MemberRepository memberRepository;       // 회원 정보를 가져오기 위함
    private final AttractionRepository attractionRepository; // 관광지 정보를 가져오기 위함

    @Transactional
    public void addLike(Long memberPk, Long attractionId) {
        // 현재 로그인한 회원과 찜할 관광지 정보를 찾습니다.
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new IllegalArgumentException("관광지를 찾을 수 없습니다."));

        // 이미 찜한 상태인지 확인하고, 중복 찜을 방지합니다.
        if (userAttractionRepository.existsByMemberAndAttraction(member, attraction)) {
            // 이미 찜한 경우 아무 작업도 하지 않거나, 예외를 발생시킬 수 있습니다.
            return;
        }

        // UserAttraction 객체를 생성하고 저장합니다.
        UserAttraction userAttraction = new UserAttraction(member, attraction);
        userAttractionRepository.save(userAttraction);
    }

    @Transactional
    public void removeLike(Long memberPk, Long attractionId) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new IllegalArgumentException("관광지를 찾을 수 없습니다."));

        // 찜 기록을 삭제합니다.
        userAttractionRepository.deleteByMemberAndAttraction(member, attraction);
    }
}
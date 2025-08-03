package com.moodTrip.spring.global.security.jwt;

import com.moodTrip.spring.domain.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username = memberId로 가정
        return memberRepository.findByMemberId(username)
                .map(MyUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 없습니다: " + username));
    }

}
package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final WithdrawDataService withdrawDataService;

    // 회원가입 등록
    public void register(MemberRequest request) {

        Member reactivatedMember = handleReregistration(request.getUserId());
        if (reactivatedMember != null) {
            // 기존 탈퇴 계정 복구
            updateReactivatedMemberInfo(reactivatedMember, request);
            log.info("기존 계정 복구 완료 - 회원ID: {}", reactivatedMember.getMemberId());
            return;
        }

        // 엔티티 변환 및 저장
        if (!request.isTerms()) {
            throw new IllegalArgumentException("이용약관에 동의해야 회원가입이 가능합니다.");
        }
        // 비밀번호 중복
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 아이디 중복 체크
        if (memberRepository.existsByMemberId(request.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 엔티티 객체로 변환
        Member member = Member.builder()
                .memberId(request.getUserId())
                .memberPw(encodedPassword)
                .email(request.getEmail())
                .memberAuth("U")                        // 기본 권한 값 예시
                .nickname(request.getNickname()) // 우선 아이디로만 설정
                .memberPhone(request.getMemberPhone())           // 필수면 기본값 또는 폼 추가 필요
                .isWithdraw(false)
                .build();
        try {
            memberRepository.save(member);
            log.info("회원가입 성공 - {}", member.getMemberId());
        } catch (Exception e) {
            log.error("회원가입 저장 중 오류 발생", e);
            throw e;  // 예외 다시 던지기 (필요 시)
        }
    }

    // 소셜 로그인 회원가입
    public void save(Member member) {
        memberRepository.save(member);
    }

    //소셜 아이디ㅣ 유효성 검사
    public boolean existsByProviderAndProviderId(String provider, String providerId) {
        return memberRepository.existsByProviderAndProviderId(provider, providerId);
    }

    // 이메일로 회원 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElse(null);
    }

    // provider와 providerId로 회원찾기(소셜 로그인)
    public Member findByProviderAndProviderId(String provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));
    }

    //새 비밀번호
    public void updatePassword(Member member, String encodedPassword) {
        member.setMemberPw(encodedPassword);
        memberRepository.save(member);
        log.info("회원({}) 비밀번호가 DB에 저장되었습니다", member.getEmail());
    }


    // 닉네임 수정 로직
    @Transactional
    public ProfileResponse updateNickname(Member member, NicknameUpdateRequest request) {

        // 로그 남기기
        log.info("닉네임 수정 요청 - 회원ID: {}, 기존닉네임: {}, 새닉네임: {}",
                member.getMemberId(), member.getNickname(), request.getNickname());

        // 유효성 검사 추가
        String newNickname = request.getNickname();
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new RuntimeException("닉네임은 필수 입력 항목입니다.");
        }

        newNickname = newNickname.trim();

        if (newNickname.length() > 30) {
            throw new RuntimeException("닉네임은 30자 이내로 입력해주세요.");
        }

        // 최소 길이 체크 추가
        if (newNickname.length() < 2) {
            throw new RuntimeException("닉네임은 2자 이상 입력해주세요.");
        }

        // 한글, 영문, 숫자만 허용
        if (!newNickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new RuntimeException("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
        }

        // 해당 회원의 프로필 찾기
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - 회원ID: {}", member.getMemberId());
                    return new RuntimeException("프로필을 찾을 수 없습니다.");
                });

        Member memberToUpdate = profile.getMember();
        memberToUpdate.setNickname(newNickname);

        log.info("닉네임 수정 성공 - 회원ID: {}, 새닉네임: {}",
                member.getMemberId(), newNickname);

        return ProfileResponse.from(profile);
    }


    public Optional<Member> findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }

    @Transactional
    public WithdrawResponse withdrawMember(Member member) {
        log.info("회원 탈퇴 요청 - 회원ID: {}", member.getMemberId());

        // 이미 탈퇴한 회원인지 확인
        if (member.getIsWithdraw()) {
            log.error("이미 탈퇴한 회원 - 회원ID: {}", member.getMemberId());
            throw new RuntimeException("이미 탈퇴 처리된 회원입니다.");
        }

        // WithdrawDataService를 통한 하이브리드 탈퇴 처리
        try {
            WithdrawResponse response = withdrawDataService.processCompleteWithdraw(member);
            log.info("회원 탈퇴 완료 - 회원ID: {}", member.getMemberId());
            return response;

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 실패 - 회원ID: {}", member.getMemberId(), e);
            throw new RuntimeException("탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 새로 추가: 재가입 처리 메서드
     * - 회원가입 시 기존 탈퇴 계정이 있는지 확인하고 복구
     */
    public Member handleReregistration(String memberId) {
        log.info("재가입 처리 확인 - 회원ID: {}", memberId);

        if (withdrawDataService.canReactivate(memberId)) {
            log.info("기존 탈퇴 계정 발견 - 복구 진행 - 회원ID: {}", memberId);
            return withdrawDataService.reactivateAccount(memberId);
        } else {
            log.info("신규 가입자 - 회원ID: {}", memberId);
            return null;  // 새로 가입해야 함
        }
    }

    public Member handleSocialReregistration(String provider, String providerId) {
        log.info("소셜 재가입 처리 확인 - Provider: {}, ProviderId: {}", provider, providerId);

        if (withdrawDataService.canReactivateSocial(provider, providerId)) {
            log.info("기존 탈퇴 소셜 계정 발견 - 복구 진행");
            return withdrawDataService.reactivateSocialAccount(provider, providerId);
        } else {
            log.info("신규 소셜 가입자");
            return null;  // 새로 가입해야 함
        }
    }

    /**
     * 복구된 계정 정보 업데이트
     */
    private void updateReactivatedMemberInfo(Member reactivatedMember, MemberRequest request) {
        log.info("복구된 계정 정보 업데이트 시작 - 회원ID: {}", reactivatedMember.getMemberId());

        // 새 비밀번호로 업데이트
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        reactivatedMember.setMemberPw(encodedPassword);

        // 새 이메일로 업데이트 (이메일이 바뀔 수 있음)
        if (!reactivatedMember.getEmail().equals(request.getEmail())) {
            if (memberRepository.existsByEmailAndIsWithdrawFalse(request.getEmail())) {
                throw new IllegalArgumentException("해당 이메일은 다른 계정에서 사용 중입니다.");
            }
            reactivatedMember.setEmail(request.getEmail());
        }

        memberRepository.save(reactivatedMember);
        log.info("복구된 계정 정보 업데이트 완료 - 회원ID: {}", reactivatedMember.getMemberId());
    }
    // 상우가 일반 로그인에서 회원 탈퇴 후 다시 재로그인할려는 경우 사용
    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }
}
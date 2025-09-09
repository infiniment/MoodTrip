package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.MemberAdminDto;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final JoinRepository enteringRoomRepository;
    private final WithdrawDataService withdrawDataService;
    private final RoomMemberRepository roomMemberRepository;
    private final MemberFireRepository memberFireRepository;

    // 회원가입 등록 - Profile 생성 추가
    @Transactional
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
                .memberAuth("U")
                .nickname(request.getNickname())
                .memberPhone(request.getMemberPhone())
                .isWithdraw(false)
                .build();

        try {
            // Member 먼저 저장
            Member savedMember = memberRepository.save(member);
            log.info("회원가입 성공 - {}", savedMember.getMemberId());

            // Profile도 함께 생성
            Profile profile = Profile.builder()
                    .member(savedMember)
                    .profileImage("/image/fix/moodtrip.png") // 기본 프로필 이미지
                    .profileBio("안녕하세요~")  // 기본값
                    .build();

            profileRepository.save(profile);
            log.info("프로필 생성 완료 - 회원ID: {}", savedMember.getMemberId());

        } catch (Exception e) {
            log.error("회원가입 저장 중 오류 발생", e);
            throw e;
        }
    }

    // 소셜 로그인 회원가입 - Profile 생성 추가
    @Transactional
    public void save(Member member) {
        try {
            // Member 먼저 저장
            Member savedMember = memberRepository.save(member);
            log.info("소셜 회원가입 성공 - {}", savedMember.getMemberId());

            // Profile도 함께 생성
            Profile profile = Profile.builder()
                    .member(savedMember)
                    .profileImage("/image/fix/moodtrip.png") // 기본 프로필 이미지
                    .profileBio("안녕하세요~")  // 기본값
                    .build();

            profileRepository.save(profile);
            log.info("소셜 회원 프로필 생성 완료 - 회원ID: {}", savedMember.getMemberId());

        } catch (Exception e) {
            log.error("소셜 회원가입 저장 중 오류 발생", e);
            throw e;
        }
    }

    //소셜 아이디 유효성 검사
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

    // 닉네임 수정 로직 - 안전장치 추가
    @Transactional
    public ProfileResponse updateNickname(Member member, NicknameUpdateRequest request) {

        log.info("닉네임 수정 요청 - 회원ID: {}, 기존닉네임: {}, 새닉네임: {}",
                member.getMemberId(), member.getNickname(), request.getNickname());

        // 유효성 검사
        String newNickname = request.getNickname();
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new RuntimeException("닉네임은 필수 입력 항목입니다.");
        }

        newNickname = newNickname.trim();

        if (newNickname.length() > 30) {
            throw new RuntimeException("닉네임은 30자 이내로 입력해주세요.");
        }

        if (newNickname.length() < 2) {
            throw new RuntimeException("닉네임은 2자 이상 입력해주세요.");
        }

        if (!newNickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new RuntimeException("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
        }

        // Profile 찾기 또는 생성
        Profile profile = profileRepository.findByMember(member)
                .orElseGet(() -> {
                    log.warn("프로필이 없어서 새로 생성 - 회원ID: {}", member.getMemberId());
                    Profile newProfile = Profile.builder()
                            .member(member)
                            .profileImage("/image/fix/moodtrip.png") // 기본 프로필 이미지
                            .profileBio("안녕하세요~")
                            .build();
                    return profileRepository.save(newProfile);
                });

        // 닉네임 수정
        Member memberToUpdate = profile.getMember();
        memberToUpdate.setNickname(newNickname);
        memberRepository.save(memberToUpdate);  // Member 저장도 명시적으로 호출

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

        if (member.getIsWithdraw()) {
            log.error("이미 탈퇴한 회원 - 회원ID: {}", member.getMemberId());
            throw new RuntimeException("이미 탈퇴 처리된 회원입니다.");
        }

        try {
            WithdrawResponse response = withdrawDataService.processCompleteWithdraw(member);
            log.info("회원 탈퇴 완료 - 회원ID: {}", member.getMemberId());
            return response;

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 실패 - 회원ID: {}", member.getMemberId(), e);
            throw new RuntimeException("탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public Member handleReregistration(String memberId) {
        log.info("재가입 처리 확인 - 회원ID: {}", memberId);

        if (withdrawDataService.canReactivate(memberId)) {
            log.info("기존 탈퇴 계정 발견 - 복구 진행 - 회원ID: {}", memberId);
            return withdrawDataService.reactivateAccount(memberId);
        } else {
            log.info("신규 가입자 - 회원ID: {}", memberId);
            return null;
        }
    }

    public Member handleSocialReregistration(String provider, String providerId) {
        log.info("소셜 재가입 처리 확인 - Provider: {}, ProviderId: {}", provider, providerId);

        if (withdrawDataService.canReactivateSocial(provider, providerId)) {
            log.info("기존 탈퇴 소셜 계정 발견 - 복구 진행");
            return withdrawDataService.reactivateSocialAccount(provider, providerId);
        } else {
            log.info("신규 소셜 가입자");
            return null;
        }
    }

    // 복구된 계정 정보 업데이트 - Profile 확인 추가
    @Transactional
    protected void updateReactivatedMemberInfo(Member reactivatedMember, MemberRequest request) {
        log.info("복구된 계정 정보 업데이트 시작 - 회원ID: {}", reactivatedMember.getMemberId());

        // 새 비밀번호로 업데이트
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        reactivatedMember.setMemberPw(encodedPassword);

        // 새 이메일로 업데이트
        if (!reactivatedMember.getEmail().equals(request.getEmail())) {
            if (memberRepository.existsByEmailAndIsWithdrawFalse(request.getEmail())) {
                throw new IllegalArgumentException("해당 이메일은 다른 계정에서 사용 중입니다.");
            }
            reactivatedMember.setEmail(request.getEmail());
        }

        memberRepository.save(reactivatedMember);

        // 복구된 계정의 Profile도 확인하고 없으면 생성
        if (!profileRepository.existsByMember(reactivatedMember)) {
            Profile profile = Profile.builder()
                    .member(reactivatedMember)
                    .profileImage("/image/fix/moodtrip.png") // 기본 프로필 이미지
                    .profileBio("안녕하세요~")
                    .build();
            profileRepository.save(profile);
            log.info("복구된 계정의 프로필 생성 완료 - 회원ID: {}", reactivatedMember.getMemberId());
        }

        log.info("복구된 계정 정보 업데이트 완료 - 회원ID: {}", reactivatedMember.getMemberId());
    }

    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<MemberAdminDto> getAllMembersForAdmin() {
        List<Member> members = memberRepository.findAllByOrderByCreatedAtDesc();

        return members.stream()
                .map(member -> {
                    MemberAdminDto dto = MemberAdminDto.fromEntity(member);
                    long participationCount = roomMemberRepository.countByMemberAndIsActiveTrue(member);
                    dto.setMatchingParticipationCount((long) participationCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberAdminDto getMemberDetailForAdmin(Long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        MemberAdminDto dto = MemberAdminDto.fromEntity(member);

        long participationCount = roomMemberRepository.countByMemberAndIsActiveTrue(member);
        dto.setMatchingParticipationCount(participationCount);

        return dto;
    }

    //회원 상태 변경 (정지/활성화)
    public void updateMemberStatus(Long memberPk, String status) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        try {
            Member.MemberStatus memberStatus = Member.MemberStatus.valueOf(status.toUpperCase());
            member.setStatus(memberStatus);
            memberRepository.save(member);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 상태값입니다.");
        }
    }

    //회원 강제 탈퇴 처리
    public void withdrawMember(Long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        member.setIsWithdraw(true);
        member.setStatus(Member.MemberStatus.WITHDRAWN);
        memberRepository.save(member);
    }

    //관리자용 회원 검색
    @Transactional(readOnly = true)
    public List<MemberAdminDto> searchMembersForAdmin(String keyword) {
        List<Member> members;

        if (keyword == null || keyword.trim().isEmpty()) {
            members = memberRepository.findAllByOrderByCreatedAtDesc();
        } else {
            members = memberRepository.findByMemberIdContainingOrNicknameContainingOrEmailContaining(
                    keyword, keyword, keyword
            );
        }

        return members.stream()
                .map(member -> {
                    MemberAdminDto dto = MemberAdminDto.fromEntity(member);
                    Long participationCount = enteringRoomRepository.countByApplicantAndStatus(
                            member, EnteringRoom.EnteringStatus.APPROVED
                    );
                    dto.setMatchingParticipationCount(participationCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 페이징 조회
    @Transactional(readOnly = true)
    public Page<MemberAdminDto> getAllMembersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Member> memberPage = memberRepository.findAll(pageable);

        return memberPage.map(this::mapToAdminDto);
    }

    private MemberAdminDto mapToAdminDto(Member member) {
        Long participationCount = roomMemberRepository.countByMemberAndIsActiveTrue(member);
        String statusName = (member.getStatus() != null) ? member.getStatus().name() : Member.MemberStatus.ACTIVE.name();
        return MemberAdminDto.builder()
                .memberPk(member.getMemberPk())
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .status(statusName)
                .isWithdraw(member.getIsWithdraw())
                .rptRcvdCnt(member.getRptRcvdCnt() == null ? 0 : member.getRptRcvdCnt())
                .matchingParticipationCount(participationCount == null ? 0 : participationCount)
                .build();
    }

    @Transactional
    public void updateReportCounts(Long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        long reportedCount = memberFireRepository.countByFireReporter(member);
        member.setRptCnt(reportedCount);

        long receivedCount = memberFireRepository.countByReportedMember(member);
        member.setRptRcvdCnt(receivedCount);

        memberRepository.save(member);
    }

    @Transactional
    public void incrementReportCount(Long reporterId, Long reportedMemberId) {
        updateReportCounts(reporterId);
        updateReportCounts(reportedMemberId);
    }

    @Transactional
    public void decrementReportCount(Long reporterId, Long reportedMemberId) {
        updateReportCounts(reporterId);
        updateReportCounts(reportedMemberId);
    }

    @Transactional(readOnly = true)
    public List<MemberAdminDto> getAllMembersForExport() {
        List<Member> members = memberRepository.findAllByOrderByCreatedAtDesc();

        return members.stream()
                .map(member -> {
                    MemberAdminDto dto = MemberAdminDto.fromEntity(member);

                    long participationCount = roomMemberRepository.countByMemberAndIsActiveTrue(member);
                    dto.setMatchingParticipationCount(participationCount);

                    long reportedCount = memberFireRepository.countByFireReporter(member);
                    long receivedCount = memberFireRepository.countByReportedMember(member);
                    dto.setRptCnt(reportedCount);
                    dto.setRptRcvdCnt(receivedCount);

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
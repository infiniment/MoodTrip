package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.repository.FaqRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.entity.FaqVote;
import com.moodTrip.spring.domain.support.repository.FaqVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;
    private final FaqVoteRepository faqVoteRepository;


    public List<Faq> findAll() {
        return faqRepository.findAll();
    }

    public Faq findById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));
    }

    @Transactional
    public Faq save(Faq faq) {
        if (faq.getId() == null) {
            faq.setCreatedAt(LocalDateTime.now());
        }
        faq.setModifiedAt(LocalDateTime.now());
        return faqRepository.save(faq);
    }

    @Transactional
    public void delete(Long id) {
        faqRepository.deleteById(id);
    }

    public List<Faq> findByCategory(String category) {
        return faqRepository.findByCategory(category);
    }

//    public List<Faq> searchByTitleOrContent(String query) {
//        return faqRepository.findByTitleContainingOrContentContaining(query, query);
//    }
    //테스트
    public List<Faq> searchByTitleOrContent(String query) {
        return faqRepository.searchByQuery(query);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setViewCount(faq.getViewCount() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void increaseHelpful(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setHelpful(faq.getHelpful() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void increaseNotHelpful(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ를 찾을 수 없습니다."));

        faq.setNotHelpful(faq.getNotHelpful() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void voteHelpful(Long faqId, Member member, boolean isHelpful) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));

        FaqVote.VoteType desired = isHelpful ? FaqVote.VoteType.YES : FaqVote.VoteType.NO;

        Optional<FaqVote> existing = faqVoteRepository.findByFaqAndMember(faq, member);

        if (existing.isPresent()) {
            FaqVote vote = existing.get();
            // 동일 투표면 그대로 OK 반환(중복 저장 안 함)
            if (vote.getVoteType() == desired) return;

            // 반대쪽에서 누르면 타입만 변경 → 고정 키( faq_id, member_pk ) 그대로라 제약 위반 없음
            vote.setVoteType(desired);
            faqVoteRepository.save(vote);
            return;
        }

        // 처음 투표면 새로 저장
        FaqVote vote = FaqVote.builder()
                .faq(faq)
                .member(member)
                .voteType(desired)
                .build();
        faqVoteRepository.save(vote);
    }

    @Transactional(readOnly = true)
    public int helpfulPercentage(Long faqId) {
        Faq faq = faqRepository.findById(faqId).orElseThrow();
        long yes = faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.YES);
        long no  = faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.NO);
        long total = yes + no;
        return total == 0 ? 0 : (int)(yes * 100 / total);
    }

    @Transactional(readOnly = true)
    public FaqVote.VoteType getUserVote(Long faqId, Member member) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        return faqVoteRepository.findByFaqAndMember(faq, member)
                .map(FaqVote::getVoteType)
                .orElse(null);
    }
}
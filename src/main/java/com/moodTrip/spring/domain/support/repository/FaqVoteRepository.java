package com.moodTrip.spring.domain.support.repository;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.entity.FaqVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaqVoteRepository extends JpaRepository<FaqVote, Long> {
    Optional<FaqVote> findByFaqAndMember(Faq faq, Member member);
    long countByFaqAndVoteType(Faq faq, FaqVote.VoteType voteType);
}

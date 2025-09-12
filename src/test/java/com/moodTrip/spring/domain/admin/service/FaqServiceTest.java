// src/test/java/com/moodTrip/spring/domain/admin/service/FaqServiceTest.java
package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.repository.FaqRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.entity.FaqVote;
import com.moodTrip.spring.domain.support.repository.FaqVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @Mock FaqRepository faqRepository;
    @Mock FaqVoteRepository faqVoteRepository;

    @InjectMocks FaqService faqService;

    Faq faq;
    Member member;

    @BeforeEach
    void setUp() {
        faq = new Faq();
        faq.setId(1L);
        faq.setTitle("T");
        faq.setContent("C");
        faq.setCategory("CAT");
        faq.setViewCount(0);
        faq.setHelpful(0);
        faq.setNotHelpful(0);

        member = new Member();
        // 필요 시 식별자 세팅 (엔티티에 맞게 수정)
        try {
            var idField = member.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, 7L);
        } catch (Exception ignore) {}
    }

    // ---------- CRUD & 조회 ----------
    @Test
    @DisplayName("findAll()은 레포지토리 위임 결과를 그대로 반환한다")
    void findAll() {
        when(faqRepository.findAll()).thenReturn(List.of(faq));

        var list = faqService.findAll();

        assertThat(list).containsExactly(faq);
        verify(faqRepository).findAll();
    }

    @Test
    @DisplayName("findById() 성공")
    void findById_ok() {
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));

        var found = faqService.findById(1L);

        assertThat(found).isSameAs(faq);
        verify(faqRepository).findById(1L);
    }

    @Test
    @DisplayName("findById() 실패 시 RuntimeException")
    void findById_notFound() {
        when(faqRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FAQ");
    }

    @Test
    @DisplayName("save() 새 엔티티면 createdAt/modifiedAt 세팅되고 저장된다")
    void save_new_setsTimestamps() {
        Faq newFaq = new Faq();
        newFaq.setTitle("N");
        // save가 그대로 객체를 반환하도록 설정
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        Faq saved = faqService.save(newFaq);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getModifiedAt()).isNotNull();
        verify(faqRepository).save(saved);
    }

    @Test
    @DisplayName("save() 기존 엔티티면 modifiedAt만 갱신된다")
    void save_existing_updatesModifiedAt() {
        faq.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = faq.getModifiedAt();
        Faq saved = faqService.save(faq);

        assertThat(saved.getCreatedAt()).isEqualTo(faq.getCreatedAt());
        assertThat(saved.getModifiedAt()).isNotNull();
        verify(faqRepository).save(saved);
    }

    @Test
    @DisplayName("delete()는 deleteById 호출")
    void delete_ok() {
        faqService.delete(1L);
        verify(faqRepository).deleteById(1L);
    }

    @Test
    @DisplayName("findByCategory() 위임")
    void findByCategory_ok() {
        when(faqRepository.findByCategory("CAT")).thenReturn(List.of(faq));

        var list = faqService.findByCategory("CAT");

        assertThat(list).containsExactly(faq);
        verify(faqRepository).findByCategory("CAT");
    }

    @Test
    @DisplayName("searchByTitleOrContent()는 repo.searchByQuery 위임")
    void search_ok() {
        when(faqRepository.searchByQuery("q")).thenReturn(List.of(faq));

        var list = faqService.searchByTitleOrContent("q");

        assertThat(list).containsExactly(faq);
        verify(faqRepository).searchByQuery("q");
    }

    // ---------- 카운트 증가 ----------
    @Test
    @DisplayName("increaseViewCount()는 조회수를 +1하고 저장한다")
    void increaseViewCount_ok() {
        faq.setViewCount(10);
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        faqService.increaseViewCount(1L);

        assertThat(faq.getViewCount()).isEqualTo(11);
        verify(faqRepository).save(faq);
    }

    @Test
    @DisplayName("increaseHelpful()는 helpful을 +1하고 저장한다")
    void increaseHelpful_ok() {
        faq.setHelpful(5);
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        faqService.increaseHelpful(1L);

        assertThat(faq.getHelpful()).isEqualTo(6);
        verify(faqRepository).save(faq);
    }

    @Test
    @DisplayName("increaseNotHelpful()는 notHelpful을 +1하고 저장한다")
    void increaseNotHelpful_ok() {
        faq.setNotHelpful(2);
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        faqService.increaseNotHelpful(1L);

        assertThat(faq.getNotHelpful()).isEqualTo(3);
        verify(faqRepository).save(faq);
    }

    // ---------- 투표 로직 ----------
    @Nested
    class VoteHelpful {

        @Test
        @DisplayName("처음 투표면 새 레코드 생성")
        void firstVote_creates() {
            when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
            when(faqVoteRepository.findByFaqAndMember(faq, member)).thenReturn(Optional.empty());

            faqService.voteHelpful(1L, member, true);

            ArgumentCaptor<FaqVote> captor = ArgumentCaptor.forClass(FaqVote.class);
            verify(faqVoteRepository).save(captor.capture());
            FaqVote saved = captor.getValue();

            assertThat(saved.getFaq()).isEqualTo(faq);
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.getVoteType()).isEqualTo(FaqVote.VoteType.YES);
        }

        @Test
        @DisplayName("같은 타입으로 재투표하면 아무 변화 없음")
        void sameVote_noop() {
            when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
            FaqVote existing = FaqVote.builder().faq(faq).member(member).voteType(FaqVote.VoteType.NO).build();
            when(faqVoteRepository.findByFaqAndMember(faq, member)).thenReturn(Optional.of(existing));

            faqService.voteHelpful(1L, member, false); // NO로 또 투표

            verify(faqVoteRepository, never()).save(any(FaqVote.class));
        }

        @Test
        @DisplayName("반대 타입으로 투표하면 기존 레코드의 타입만 변경")
        void oppositeVote_switchesType() {
            when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
            FaqVote existing = FaqVote.builder().faq(faq).member(member).voteType(FaqVote.VoteType.NO).build();
            when(faqVoteRepository.findByFaqAndMember(faq, member)).thenReturn(Optional.of(existing));

            faqService.voteHelpful(1L, member, true); // YES로 변경

            assertThat(existing.getVoteType()).isEqualTo(FaqVote.VoteType.YES);
            verify(faqVoteRepository).save(existing);
        }
    }

    // ---------- 통계/조회 ----------
    @Test
    @DisplayName("helpfulPercentage(): 투표가 없으면 0, 있으면 floor(yes/total*100)")
    void helpfulPercentage_ok() {
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));

        // case 1: 0표
        when(faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.YES)).thenReturn(0L);
        when(faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.NO)).thenReturn(0L);

        assertThat(faqService.helpfulPercentage(1L)).isEqualTo(0);

        // case 2: yes=3, no=1 -> 75%
        when(faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.YES)).thenReturn(3L);
        when(faqVoteRepository.countByFaqAndVoteType(faq, FaqVote.VoteType.NO)).thenReturn(1L);

        assertThat(faqService.helpfulPercentage(1L)).isEqualTo(75);
    }

    @Test
    @DisplayName("getUserVote(): 유저 투표 없으면 null, 있으면 해당 타입")
    void getUserVote_ok() {
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));

        when(faqVoteRepository.findByFaqAndMember(faq, member)).thenReturn(Optional.empty());
        assertThat(faqService.getUserVote(1L, member)).isNull();

        FaqVote existing = FaqVote.builder().faq(faq).member(member).voteType(FaqVote.VoteType.YES).build();
        when(faqVoteRepository.findByFaqAndMember(faq, member)).thenReturn(Optional.of(existing));
        assertThat(faqService.getUserVote(1L, member)).isEqualTo(FaqVote.VoteType.YES);
    }
}

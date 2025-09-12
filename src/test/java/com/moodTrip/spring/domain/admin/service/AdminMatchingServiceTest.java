package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.response.AdminMatchingDto;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMatchingServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private ScheduleRepository scheduleRepository;

    @InjectMocks
    private AdminMatchingService service;

    // ===== helper builders =====
    private Member member(String nickname) {
        Member m = new Member();
        m.setNickname(nickname);
        return m;
    }

    private RoomMember activeMember(Member m, boolean active) {
        RoomMember rm = new RoomMember();
        rm.setMember(m);
        rm.setIsActive(active);
        return rm;
    }

    private Attraction attraction(int areaCode, String title) {
        Attraction a = new Attraction();
        a.setAreaCode(areaCode);
        a.setTitle(title);
        return a;
    }

    private Room room(
            Long id, String name,
            Member creator,
            int current, int max,
            LocalDate start, LocalDate end,
            boolean deleted,
            Attraction attr,
            List<RoomMember> participants
    ) {
        Room r = new Room();
        r.setRoomId(id);
        r.setRoomName(name);
        r.setCreator(creator);
        r.setRoomCurrentCount(current);
        r.setRoomMaxCount(max);
        r.setTravelStartDate(start);
        r.setTravelEndDate(end);
        r.setIsDeleteRoom(deleted);
        r.setAttraction(attr);
        r.setRoomMembers(participants);
        // createdAt 정렬 검증용(값만 세팅)
        r.setCreatedAt(start != null ? start.atStartOfDay() : LocalDate.now().atStartOfDay());
        return r;
    }

    @Test
    @DisplayName("getAllMatchings: 엔티티를 DTO로 매핑한다(기간/상태/참가자 필터링 포함)")
    void getAllMatchings_mapsToDto() {
        // Given
        Member creator = member("크리에이터");
        List<RoomMember> members = new ArrayList<>();
        members.add(activeMember(member("참여자1"), true));
        members.add(activeMember(member("참여자2"), false)); // 비활성 → 제외

        Room room = room(
                10L, "방이름", creator,
                2, 4,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12), // 3일(포함)
                false,
                null, // attraction == null → regionName "-"
                members
        );

        when(roomRepository.findAll()).thenReturn(List.of(room));

        // When
        List<AdminMatchingDto> list = service.getAllMatchings();

        // Then
        assertThat(list).hasSize(1);
        AdminMatchingDto dto = list.get(0);
        assertThat(dto.getRoomId()).isEqualTo(10L);
        assertThat(dto.getRoomName()).isEqualTo("방이름");
        assertThat(dto.getCreatorNickname()).isEqualTo("크리에이터");
        assertThat(dto.getCurrentCount()).isEqualTo(2);
        assertThat(dto.getMaxCount()).isEqualTo(4);
        assertThat(dto.getTravelStartDate()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(dto.getTravelEndDate()).isEqualTo(LocalDate.of(2025, 1, 12));
        assertThat(dto.getTravelPeriod()).isEqualTo(3); // between + 1
        assertThat(dto.getRegionName()).isEqualTo("-"); // attraction null 처리
        assertThat(dto.getAttractionTitle()).isEqualTo("-");
        assertThat(dto.getStatus()).isIn("진행중", "완료"); // 함수 로직상 today 기준
        assertThat(dto.getParticipants()).containsExactly("참여자1");

        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getMatchings(page,size): Pageable(정렬: createdAt DESC) 전달 및 매핑")
    void getMatchings_paginationAndSort() {
        // Given
        Room r1 = room(1L, "A", member("m1"), 1, 3,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2),
                false, attraction(1, "타이틀1"), List.of());

        Page<Room> page = new PageImpl<>(List.of(r1));
        when(roomRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        Page<AdminMatchingDto> result = service.getMatchings(2, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRoomId()).isEqualTo(1L);

        // Pageable 검증
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(roomRepository).findAll(captor.capture());
        Pageable used = captor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(2);
        assertThat(used.getPageSize()).isEqualTo(20);
        Sort.Order order = used.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("terminateMatching: 스케줄 삭제 후 방 삭제 플래그 true 저장")
    void terminateMatching_success() {
        // Given
        Room room = room(5L, "방", member("u"), 1, 2,
                LocalDate.now().minusDays(3), LocalDate.now().minusDays(1),
                false, null, List.of());

        List<Schedule> schedules = List.of(new Schedule(), new Schedule());

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(scheduleRepository.findByRoom(room)).thenReturn(schedules);

        // When
        service.terminateMatching(5L);

        // Then
        verify(scheduleRepository, times(1)).deleteAll(schedules);
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getIsDeleteRoom()).isTrue();
    }

    @Test
    @DisplayName("terminateMatching: 방이 없으면 IllegalArgumentException")
    void terminateMatching_notFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.terminateMatching(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 방이 존재하지 않습니다.");
        verify(roomRepository, never()).save(any());
        verify(scheduleRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("restoreMatching: 삭제 플래그 false 저장")
    void restoreMatching_success() {
        Room room = room(7L, "방", member("u"), 1, 2,
                null, null,
                true, null, List.of());

        when(roomRepository.findById(7L)).thenReturn(Optional.of(room));

        service.restoreMatching(7L);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getIsDeleteRoom()).isFalse();
    }

    @Test
    @DisplayName("restoreMatching: 방이 없으면 IllegalArgumentException")
    void restoreMatching_notFound() {
        when(roomRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.restoreMatching(123L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 방이 존재하지 않습니다.");
        verify(roomRepository, never()).save(any());
    }
}

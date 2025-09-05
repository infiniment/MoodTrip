package com.moodTrip.spring.domain.schedule.service;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.moodTrip.spring.global.common.code.status.ErrorStatus.ROOM_NOT_FOUND;
import static com.moodTrip.spring.global.common.code.status.ErrorStatus.SCHEDULE_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock ScheduleRepository scheduleRepository;
    @Mock RoomRepository roomRepository;

    @InjectMocks ScheduleServiceImpl service;

    @Test
    @DisplayName("getSchedulesByRoomId: 엔티티 -> DTO 매핑")
    void getSchedulesByRoomId_ok() {
        Schedule s1 = new Schedule();
        setField(s1, 1L, 10L, "제목1", "설명1", LocalDateTime.of(2025, 9, 10, 9, 10));
        Schedule s2 = new Schedule();
        setField(s2, 2L, 10L, "제목2", "설명2",LocalDateTime.of(2025, 9, 11, 9, 10));

        when(scheduleRepository.findByRoom_RoomId(10L)).thenReturn(List.of(s1, s2));

        List<ScheduleResponse> result = service.getSchedulesByRoomId(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScheduleId()).isEqualTo(1L);
        assertThat(result.get(0).getRoomId()).isEqualTo(10L);
        assertThat(result.get(0).getScheduleTitle()).isEqualTo("제목1");
    }

    @Test
    @DisplayName("createSchedule: 방 존재 시 저장하고 DTO 반환")
    void createSchedule_ok() {
        long roomId = 99L;
        Room room = new Room();
        room.setRoomId(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        ScheduleRequest req = new ScheduleRequest();
        req.setScheduleTitle("새 일정");
        req.setScheduleDescription("설명");
        req.setTravelStartDate(LocalDateTime.of(2025, 9, 12, 0, 0));

        // save 에 넘어오는 Schedule 캡처해서 검증
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(inv -> {
            Schedule s = inv.getArgument(0);
            // 보통 JPA가 세팅하는 ID를 테스트에선 수동으로 세팅
            s.setScheduleId(1L);
            return s;
        });

        ScheduleResponse resp = service.createSchedule(roomId, req);

        verify(roomRepository).findById(roomId);
        verify(scheduleRepository).save(captor.capture());

        Schedule saved = captor.getValue();
        assertThat(saved.getRoom()).isSameAs(room);
        assertThat(saved.getScheduleTitle()).isEqualTo("새 일정");
        assertThat(saved.getScheduleDescription()).isEqualTo("설명");
        assertThat(saved.getTravelStartDate())
                .isEqualTo(LocalDateTime.of(2025, 9, 12, 0, 0));
        assertThat(saved.getStartedSchedule()).isNotNull();
        assertThat(saved.getUpdatedSchedule()).isNotNull();

        assertThat(resp.getScheduleId()).isEqualTo(1L);
        assertThat(resp.getRoomId()).isEqualTo(roomId);
        assertThat(resp.getScheduleTitle()).isEqualTo("새 일정");
    }

    @Test
    @DisplayName("createSchedule: 방 없으면 ROOM_NOT_FOUND 예외")
    void createSchedule_roomNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        ScheduleRequest req = new ScheduleRequest();
        assertThatThrownBy(() -> service.createSchedule(1L, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorStatus")
                .isEqualTo(ROOM_NOT_FOUND);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSchedule: 일부 필드만 변경 가능, updatedSchedule 갱신")
    void updateSchedule_ok_partialUpdate() {
        Schedule existing = new Schedule();
        setField(existing, 7L, 55L, "old", "old-desc",
                LocalDateTime.of(2025, 9, 10, 9, 10));
        LocalDateTime before = LocalDateTime.of(2025, 1, 1, 0, 0);
        existing.setUpdatedSchedule(before);

        when(scheduleRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(inv -> inv.getArgument(0));

        ScheduleRequest req = new ScheduleRequest();
        req.setScheduleTitle("new");
        req.setTravelStartDate(LocalDateTime.of(2025, 9, 12, 0, 0));

        ScheduleResponse resp = service.updateSchedule(7L, req);

        verify(scheduleRepository).save(existing);
        assertThat(existing.getScheduleTitle()).isEqualTo("new");
        assertThat(existing.getScheduleDescription()).isEqualTo("old-desc");
        // ✅ 바뀐 시간이 이전 값보다 이후인지 확인 (정확한 now 값은 비교하지 않음)
        assertThat(existing.getUpdatedSchedule()).isAfter(before);

        assertThat(resp.getScheduleId()).isEqualTo(7L);
        assertThat(resp.getRoomId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("updateSchedule: 스케줄 없으면 SCHEDULE_NOT_FOUND 예외")
    void updateSchedule_notFound() {
        when(scheduleRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateSchedule(7L, new ScheduleRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorStatus")
                .isEqualTo(SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteSchedule: ID로 삭제")
    void deleteSchedule_ok() {
        service.deleteSchedule(9L);
        verify(scheduleRepository).deleteById(9L);
    }

    @Test
    @DisplayName("getRoomIdByScheduleId: 스케줄에서 roomId 추출")
    void getRoomIdByScheduleId_ok() {
        Schedule s = new Schedule();
        setField(s, 100L, 777L, "t", "d", LocalDateTime.now());

        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(s));

        Long roomId = service.getRoomIdByScheduleId(100L);
        assertThat(roomId).isEqualTo(777L);
    }

    @Test
    @DisplayName("getRoomIdByScheduleId: 스케줄 없으면 예외")
    void getRoomIdByScheduleId_notFound() {
        when(scheduleRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRoomIdByScheduleId(100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorStatus")
                .isEqualTo(SCHEDULE_NOT_FOUND);
    }

    // ===== 테스트 편의: 엔티티 필드 세팅 헬퍼 =====
    private static void setField(Schedule s, Long scheduleId, Long roomId,
                                 String title, String desc, LocalDateTime travelStart) {
        Room r = new Room();
        r.setRoomId(roomId);
        s.setScheduleId(scheduleId);
        s.setRoom(r);
        s.setScheduleTitle(title);
        s.setScheduleDescription(desc);
        s.setTravelStartDate(travelStart);
        s.setStartedSchedule(LocalDateTime.now());
        s.setUpdatedSchedule(LocalDateTime.now());
    }
}

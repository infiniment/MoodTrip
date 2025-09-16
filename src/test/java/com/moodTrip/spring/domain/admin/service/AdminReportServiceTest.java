package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.request.ReportActionDto;
import com.moodTrip.spring.domain.admin.dto.response.ReportDetailDto;
import com.moodTrip.spring.domain.admin.dto.response.ReportDto;
import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.fire.repository.MemberFireRepository;
import com.moodTrip.spring.domain.fire.repository.RoomFireRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReportService 단위 테스트")
class AdminReportServiceTest {

    @Mock
    private RoomFireRepository roomFireRepository;

    @Mock
    private MemberFireRepository memberFireRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminReportService adminReportService;

    private Member testReporter;
    private Member testTarget;
    private Room testRoom;
    private MemberFire testMemberFire;
    private RoomFire testRoomFire;

    @BeforeEach
    void setUp() {
        testReporter = createMember(1L, "reporter", 0L, 0L);
        testTarget = createMember(2L, "target", 0L, 0L);
        testRoom = createRoom(1L, "테스트 방", testTarget);

        testMemberFire = createMemberFire(1L, testReporter, testTarget, testRoom,
                MemberFire.FireReason.HARASSMENT, "부적절한 발언", MemberFire.FireStatus.PENDING);

        testRoomFire = createRoomFire(1L, testReporter, testRoom,
                RoomFire.FireReason.INAPPROPRIATE, "부적절한 방", RoomFire.FireStatus.PENDING);
    }

    private Member createMember(Long id, String nickname, Long rptCnt, Long rptRcvdCnt) {
        Member member = new Member();
        member.setMemberPk(id);
        member.setNickname(nickname);
        member.setRptCnt(rptCnt);
        member.setRptRcvdCnt(rptRcvdCnt);
        return member;
    }

    private Room createRoom(Long id, String name, Member creator) {
        Room room = new Room();
        room.setRoomId(id);
        room.setRoomName(name);
        room.setCreator(creator);
        room.setCreatedAt(LocalDateTime.now());
        room.setIsDeleteRoom(false);
        return room;
    }

    private MemberFire createMemberFire(Long fireId, Member reporter, Member target, Room room,
                                        MemberFire.FireReason reason, String message, MemberFire.FireStatus status) {
        MemberFire fire = MemberFire.builder()
                .fireId(fireId)
                .fireReporter(reporter)
                .reportedMember(target)
                .targetRoom(room)
                .fireReason(reason)
                .fireMessage(message)
                .fireStatus(status)
                .build();
        fire.setCreatedAt(LocalDateTime.now());
        return fire;
    }

    private RoomFire createRoomFire(Long fireId, Member reporter, Room room,
                                    RoomFire.FireReason reason, String message, RoomFire.FireStatus status) {
        RoomFire fire = RoomFire.builder()
                .fireId(fireId)
                .fireReporter(reporter)
                .firedRoom(room)
                .fireReason(reason)
                .fireMessage(message)
                .fireStatus(status)
                .build();
        fire.setCreatedAt(LocalDateTime.now());
        return fire;
    }

    @Test
    @DisplayName("회원 신고 접수 - 성공")
    void createMemberReport_Success() {
        // Given
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);
        given(memberRepository.save(any(Member.class))).willReturn(testReporter);

        // When
        MemberFire result = adminReportService.createMemberReport(
                testReporter, testTarget, testRoom, "부적절한 발언", MemberFire.FireReason.HARASSMENT);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFireReporter()).isEqualTo(testReporter);
        assertThat(result.getReportedMember()).isEqualTo(testTarget);
        assertThat(result.getFireStatus()).isEqualTo(MemberFire.FireStatus.PENDING);

        verify(memberFireRepository).save(any(MemberFire.class));
        verify(memberRepository, times(2)).save(any(Member.class)); // reporter, target 각각 저장
        assertThat(testReporter.getRptCnt()).isEqualTo(1L);
        assertThat(testTarget.getRptRcvdCnt()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원 신고 접수 - null 멤버들 처리")
    void createMemberReport_WithNullMembers() {
        // Given
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);

        // When
        MemberFire result = adminReportService.createMemberReport(
                null, null, testRoom, "신고 메시지", MemberFire.FireReason.SPAM);

        // Then
        assertThat(result).isNotNull();
        verify(memberFireRepository).save(any(MemberFire.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("방 신고 접수 - 성공")
    void createRoomReport_Success() {
        // Given
        given(roomFireRepository.save(any(RoomFire.class))).willReturn(testRoomFire);
        given(memberRepository.save(any(Member.class))).willReturn(testReporter);

        // When
        RoomFire result = adminReportService.createRoomReport(
                testReporter, testRoom, "부적절한 방", RoomFire.FireReason.INAPPROPRIATE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFireReporter()).isEqualTo(testReporter);
        assertThat(result.getFiredRoom()).isEqualTo(testRoom);
        assertThat(result.getFireStatus()).isEqualTo(RoomFire.FireStatus.PENDING);

        verify(roomFireRepository).save(any(RoomFire.class));
        verify(memberRepository, times(2)).save(any(Member.class)); // reporter, room creator
        assertThat(testReporter.getRptCnt()).isEqualTo(1L);
        assertThat(testRoom.getCreator().getRptRcvdCnt()).isEqualTo(1L);
    }

    @Test
    @DisplayName("방 신고 접수 - null 값들 처리")
    void createRoomReport_WithNullValues() {
        // Given
        Room roomWithoutCreator = new Room();
        roomWithoutCreator.setRoomId(2L);
        roomWithoutCreator.setRoomName("방장 없는 방");
        roomWithoutCreator.setCreator(null);

        given(roomFireRepository.save(any(RoomFire.class))).willReturn(testRoomFire);

        // When
        RoomFire result = adminReportService.createRoomReport(
                null, roomWithoutCreator, "신고 메시지", RoomFire.FireReason.SPAM);

        // Then
        assertThat(result).isNotNull();
        verify(roomFireRepository).save(any(RoomFire.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("전체 신고 목록 조회 - 상태 필터 없음")
    void getAllReports_WithoutStatusFilter() {
        // Given
        List<RoomFire> roomFires = Arrays.asList(testRoomFire);
        List<MemberFire> memberFires = Arrays.asList(testMemberFire);

        given(roomFireRepository.findAll()).willReturn(roomFires);
        given(memberFireRepository.findAll()).willReturn(memberFires);

        // When
        List<ReportDto> result = adminReportService.getAllReports(null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(dto -> "ROOM".equals(dto.getType()));
        assertThat(result).anyMatch(dto -> "MEMBER".equals(dto.getType()));

        verify(roomFireRepository).findAll();
        verify(memberFireRepository).findAll();
    }

    @Test
    @DisplayName("전체 신고 목록 조회 - PENDING 상태 필터")
    void getAllReports_WithPendingStatus() {
        // Given
        List<RoomFire> roomFires = Arrays.asList(testRoomFire);
        List<MemberFire> memberFires = Arrays.asList(testMemberFire);

        given(roomFireRepository.findByFireStatus(RoomFire.FireStatus.PENDING)).willReturn(roomFires);
        given(memberFireRepository.findByFireStatus(MemberFire.FireStatus.PENDING)).willReturn(memberFires);

        // When
        List<ReportDto> result = adminReportService.getAllReports("PENDING");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> "처리 대기".equals(dto.getStatusDisplay()));

        verify(roomFireRepository).findByFireStatus(RoomFire.FireStatus.PENDING);
        verify(memberFireRepository).findByFireStatus(MemberFire.FireStatus.PENDING);
    }

    @Test
    @DisplayName("전체 신고 목록 조회 - 잘못된 상태 값")
    void getAllReports_WithInvalidStatus() {
        // Given
        given(roomFireRepository.findAll()).willReturn(Arrays.asList(testRoomFire));
        given(memberFireRepository.findAll()).willReturn(Arrays.asList(testMemberFire));

        // When
        List<ReportDto> result = adminReportService.getAllReports("INVALID_STATUS");

        // Then
        assertThat(result).hasSize(2);
        verify(roomFireRepository).findAll();
        verify(memberFireRepository).findAll();
    }

    @Test
    @DisplayName("방 신고 상세 조회 - 성공")
    void getDetail_RoomReport_Success() {
        // Given
        given(roomFireRepository.findById(1L)).willReturn(Optional.of(testRoomFire));

        // When
        ReportDetailDto result = adminReportService.getDetail(1L, "ROOM");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("ROOM");
        assertThat(result.getTypeDisplay()).isEqualTo("매칭방");
        assertThat(result.getRoomName()).isEqualTo("테스트 방");

        verify(roomFireRepository).findById(1L);
    }

    @Test
    @DisplayName("회원 신고 상세 조회 - 성공")
    void getDetail_MemberReport_Success() {
        // Given
        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));

        // When
        ReportDetailDto result = adminReportService.getDetail(1L, "MEMBER");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("MEMBER");
        assertThat(result.getTypeDisplay()).isEqualTo("회원");
        assertThat(result.getReportedNickname()).isEqualTo("target");

        verify(memberFireRepository).findById(1L);
    }

    @Test
    @DisplayName("신고 상세 조회 - 잘못된 타입")
    void getDetail_InvalidType() {
        // When & Then
        assertThatThrownBy(() -> adminReportService.getDetail(1L, "INVALID_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown report type");
    }

    @Test
    @DisplayName("신고 처리 완료 - 방 신고")
    void resolve_RoomReport_Success() {
        // Given
        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setAdminMemo("처리 완료");

        given(roomFireRepository.findById(1L)).willReturn(Optional.of(testRoomFire));
        given(roomFireRepository.save(any(RoomFire.class))).willReturn(testRoomFire);

        // When
        adminReportService.resolve(1L, "ROOM", actionDto);

        // Then
        verify(roomFireRepository).findById(1L);
        verify(roomFireRepository).save(argThat(rf ->
                rf.getFireStatus() == RoomFire.FireStatus.RESOLVED &&
                        "처리 완료".equals(rf.getAdminMemo())
        ));
    }

    @Test
    @DisplayName("신고 처리 완료 - 회원 신고")
    void resolve_MemberReport_Success() {
        // Given
        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setAdminMemo("경고 처리");

        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);

        // When
        adminReportService.resolve(1L, "MEMBER", actionDto);

        // Then
        verify(memberFireRepository).findById(1L);
        verify(memberFireRepository).save(argThat(mf ->
                mf.getFireStatus() == MemberFire.FireStatus.RESOLVED &&
                        "경고 처리".equals(mf.getAdminMemo())
        ));
    }

    @Test
    @DisplayName("신고 기각 - 회원 신고")
    void dismiss_MemberReport_Success() {
        // Given
        testReporter.setRptCnt(1L);
        testTarget.setRptRcvdCnt(1L);

        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setAdminMemo("기각 사유");

        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);
        given(memberRepository.save(any(Member.class))).willReturn(testReporter);

        // When
        adminReportService.dismiss(1L, "MEMBER", actionDto);

        // Then
        verify(memberFireRepository).findById(1L);
        verify(memberFireRepository).save(argThat(mf ->
                mf.getFireStatus() == MemberFire.FireStatus.DISMISSED &&
                        "기각 사유".equals(mf.getAdminMemo())
        ));
        verify(memberRepository, times(2)).save(any(Member.class));

        assertThat(testReporter.getRptCnt()).isEqualTo(0L);
        assertThat(testTarget.getRptRcvdCnt()).isEqualTo(0L);
    }

    @Test
    @DisplayName("신고 기각 - 카운트가 0보다 작아지지 않음")
    void dismiss_CountNotBelowZero() {
        // Given
        testReporter.setRptCnt(0L);
        testTarget.setRptRcvdCnt(0L);

        ReportActionDto actionDto = new ReportActionDto();

        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);

        // When
        adminReportService.dismiss(1L, "MEMBER", actionDto);

        // Then
        assertThat(testReporter.getRptCnt()).isEqualTo(0L);
        assertThat(testTarget.getRptRcvdCnt()).isEqualTo(0L);
    }

    @Test
    @DisplayName("신고 액션 적용 - resolve 타입")
    void applyAction_ResolveType() {
        // Given
        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setActionType("resolve");
        actionDto.setAdminMemo("처리 완료");

        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);

        // When
        adminReportService.applyAction(1L, "MEMBER", actionDto);

        // Then
        verify(memberFireRepository).save(argThat(mf ->
                mf.getFireStatus() == MemberFire.FireStatus.RESOLVED
        ));
    }

    @Test
    @DisplayName("신고 액션 적용 - reject 타입")
    void applyAction_RejectType() {
        // Given
        testReporter.setRptCnt(1L);
        testTarget.setRptRcvdCnt(1L);

        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setActionType("reject");

        given(memberFireRepository.findById(1L)).willReturn(Optional.of(testMemberFire));
        given(memberFireRepository.save(any(MemberFire.class))).willReturn(testMemberFire);

        // When
        adminReportService.applyAction(1L, "MEMBER", actionDto);

        // Then
        verify(memberFireRepository).save(argThat(mf ->
                mf.getFireStatus() == MemberFire.FireStatus.DISMISSED
        ));
    }

    @Test
    @DisplayName("신고 액션 적용 - null actionType")
    void applyAction_NullActionType() {
        // Given
        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setActionType(null);

        // When & Then
        assertThatThrownBy(() -> adminReportService.applyAction(1L, "MEMBER", actionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actionType 이 필요합니다");
    }

    @Test
    @DisplayName("신고 액션 적용 - 지원하지 않는 actionType")
    void applyAction_UnsupportedActionType() {
        // Given
        ReportActionDto actionDto = new ReportActionDto();
        actionDto.setActionType("unsupported");

        // When & Then
        assertThatThrownBy(() -> adminReportService.applyAction(1L, "MEMBER", actionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 actionType");
    }

    @Test
    @DisplayName("존재하지 않는 방 신고 조회")
    void getDetail_RoomNotFound() {
        // Given
        given(roomFireRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminReportService.getDetail(999L, "ROOM"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원 신고 조회")
    void getDetail_MemberNotFound() {
        // Given
        given(memberFireRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminReportService.getDetail(999L, "MEMBER"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("신고 목록 정렬 확인 - 최신순")
    void getAllReports_SortedByCreatedAtDesc() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        RoomFire olderRoomFire = createRoomFire(2L, testReporter, testRoom,
                RoomFire.FireReason.SPAM, "오래된 방 신고", RoomFire.FireStatus.PENDING);
        olderRoomFire.setCreatedAt(now.minusHours(2));

        RoomFire newerRoomFire = createRoomFire(3L, testReporter, testRoom,
                RoomFire.FireReason.FRAUD, "최근 방 신고", RoomFire.FireStatus.PENDING);
        newerRoomFire.setCreatedAt(now.minusHours(1));

        given(roomFireRepository.findAll()).willReturn(Arrays.asList(olderRoomFire, newerRoomFire));
        given(memberFireRepository.findAll()).willReturn(Arrays.asList());

        // When
        List<ReportDto> result = adminReportService.getAllReports(null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReportId()).isEqualTo(3L); // 최신 것이 첫 번째
        assertThat(result.get(1).getReportId()).isEqualTo(2L); // 오래된 것이 두 번째
    }
}
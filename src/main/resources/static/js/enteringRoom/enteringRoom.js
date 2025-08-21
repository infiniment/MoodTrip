// 서버 API 기본 URL
const API_BASE_URL = '/api/v1/companion-rooms/search';
const JOIN_API_BASE_URL = '/api/v1/companion-rooms';
const FIRE_API_BASE_URL = '/api/v1/fires';

// 방 데이터 (서버에서 가져온 데이터로 저장)
let roomsData = [];
let filteredRooms = [];

// 현재 상태 변수들
const currentDate = new Date('2025-07-02');
let currentFilter = 'all';
let currentPage = 1;
let currentPeopleFilter = 'all';
let currentRegionFilter = 'all';
let currentDetailRoomId = null;
let currentReportRoomId = null;

// 🔥 기존 변수들 아래에 추가
let currentPageNew = 1;
const itemsPerPageNew = 5; // 방 카드는 5개씩 표시
let totalItemsNew = 0;
let totalPagesNew = 0;
let allRoomCards = []; // 모든 방 카드를 저장
let currentPageRooms = []; // 현재 페이지의 방들

// 페이징 캐시 시스템
let roomCardsCache = null;
let lastCacheTimeNew = 0;
const CACHE_DURATION_NEW = 5000; // 5초 캐시

// 서버에서 방 목록 가져오기
async function fetchRoomsFromServer(params = {}) {
    console.log('🔍 서버에서 방 목록 가져오는 중...', params);

    // URL 파라미터 생성
    const urlParams = new URLSearchParams();
    if (params.search) urlParams.append('search', params.search);
    if (params.region) urlParams.append('region', params.region);
    if (params.maxParticipants) urlParams.append('maxParticipants', params.maxParticipants);
    if (params.urgent) urlParams.append('urgent', params.urgent);

    const url = `${API_BASE_URL}${urlParams.toString() ? '?' + urlParams.toString() : ''}`;
    console.log('📡 API 호출 URL:', url);

    const response = await fetch(url);
    const data = await response.json();

    console.log('✅ 서버에서 받은 데이터:', data);
    return data;
}

// 🔥 기존 함수에서 이 부분만 수정
async function loadRoomsData(params = {}) {
    // 🔥 추가: 캐시 무효화
    roomCardsCache = null;

    // 서버에서 데이터 가져오기 (기존 코드 유지)
    const data = await fetchRoomsFromServer(params);

    // 전역 변수 업데이트 (기존 코드 유지)
    roomsData = data;
    filteredRooms = [...data];

    // 화면 업데이트 (기존 코드 유지)
    renderRooms(); // 이미 수정된 함수 호출
    updateResultsCount();

    // 🔥 제거: updatePagination(); <- 이 줄 삭제
    // 🔥 제거: 기존 페이징 관련 코드들 모두 삭제

    console.log('✅ 방 목록 로드 완료:', data.length + '개');
}

// DOM 로드 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 페이지 로드 - 초기화 시작');

    initializeEventListeners();
    loadRoomsData(); // 기본 로드
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 검색 기능
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');

    if (searchButton && searchInput) {
        searchButton.addEventListener('click', handleSearch);
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }

    // 필터 탭
    const filterTabs = document.querySelectorAll('.filter-tab');
    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const filter = this.getAttribute('data-filter');
            setActiveFilter(filter);
            applyFilters();
        });
    });

    // 지역별 서브 필터
    const regionFilterTabs = document.querySelectorAll('.region-filter-tab');
    regionFilterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const regionFilter = this.getAttribute('data-region');
            setActiveRegionFilter(regionFilter);
            applyFilters();
        });
    });

    // 인원별 서브 필터
    const peopleFilterTabs = document.querySelectorAll('.people-filter-tab');
    peopleFilterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const peopleFilter = this.getAttribute('data-people');
            setActivePeopleFilter(peopleFilter);
            applyFilters();
        });
    });

    // 마감 임박 체크박스
    const urgentOnly = document.getElementById('urgentOnly');
    if (urgentOnly) {
        urgentOnly.addEventListener('change', applyFilters);
    }

    // 신고 사유 변경 감지
    const reportReason = document.getElementById('reportReason');
    if (reportReason) {
        reportReason.addEventListener('change', validateReportForm);
        validateReportForm();
    }

    console.log('🔥 모든 이벤트 리스너 등록 완료');
}

// 검색 처리 (서버 API 호출)
async function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    const searchTerm = searchInput.value.trim();
    console.log('🔍 검색 요청:', searchTerm);

    // 서버에서 검색 결과 가져오기
    await loadRoomsData({ search: searchTerm });
    currentPageNew = 1;
}

// 필터 적용 (서버 API 호출)
async function applyFilters() {
    const urgentOnly = document.getElementById('urgentOnly');
    const urgentChecked = urgentOnly ? urgentOnly.checked : false;

    const params = {};

    // 지역 필터
    if (currentFilter === 'nearby' && currentRegionFilter !== 'all') {
        params.region = currentRegionFilter;
    }

    // 인원 필터
    if (currentFilter === 'popular' && currentPeopleFilter !== 'all') {
        params.maxParticipants = currentPeopleFilter;
    }

    // 마감 임박 필터
    if (urgentChecked) {
        params.urgent = true;
    }

    console.log('🔄 필터 적용:', params);

    // 서버에서 필터링된 데이터 가져오기
    await loadRoomsData(params);

    currentPageNew = 1; // 새로운 변수 사용
}

// 방 상세보기 함수
function viewRoomDetail(roomId) {
    console.log('방 상세보기 요청 - roomId:', roomId);

    fetch(`/entering-room/${roomId}/modal-data`)
        .then(response => response.json())
        .then(roomData => {
            console.log('받은 방 데이터:', roomData); // 디버깅용 로그 추가

            // ✅ 이미지 세팅
            const imgEl = document.getElementById('detailRoomImage');
            if (roomData.image) {
                imgEl.src = roomData.image;
            } else {
                imgEl.src = '/image/fix/moodtrip.png'; // fallback
            }
            imgEl.alt = roomData.title || '방 이미지';

            // 🔥 나머지 필드 세팅
            document.getElementById('detailRoomTitle').textContent = roomData.title;
            const locationText = roomData.category || roomData.location;
            document.getElementById('detailRoomLocation').textContent = locationText;
            document.getElementById('detailRoomDate').textContent = roomData.date;
            document.getElementById('detailRoomParticipants').textContent =
                `${roomData.currentParticipants}/${roomData.maxParticipants}명`;
            document.getElementById('detailRoomViews').textContent = roomData.views;
            document.getElementById('detailRoomPeriod').textContent = roomData.createdDate;
            document.getElementById('detailRoomDesc').textContent = roomData.description;

            // 🔥 새로 추가: 감정 태그 처리
            const tagsContainer = document.getElementById('detailRoomTags');
            console.log('감정 데이터:', roomData.emotions); // 디버깅용 로그

            if (roomData.emotions && roomData.emotions.length > 0) {
                // 감정 태그들을 HTML로 생성
                const emotionTagsHtml = roomData.emotions.map(emotion =>
                    `<span class="detail-emotion-tag"> #${emotion} </span>`
                ).join('');
                tagsContainer.innerHTML = emotionTagsHtml;
                console.log('감정 태그 생성 완료:', emotionTagsHtml); // 디버깅용 로그
            } else {
                // 감정 태그가 없을 때
                tagsContainer.innerHTML = '<span class="no-tags">등록된 감정 태그가 없습니다.</span>';
                console.log('감정 태그 없음'); // 디버깅용 로그
            }

            // 모달 표시
            document.getElementById('detailModal').style.display = 'flex';
        })
        .catch(error => {
            console.error('방 상세 정보 조회 실패:', error);
            alert('방 정보를 불러올 수 없습니다.');
        });
}


// 상세보기 모달 열기
function openDetailModal(room) {
    const modal = document.getElementById('detailModal');
    if (!modal) return;

    // 모달 내용 업데이트
    const elements = {
        detailRoomImage: document.getElementById('detailRoomImage'),
        detailRoomStatus: document.getElementById('detailRoomStatus'),
        detailRoomTitle: document.getElementById('detailRoomTitle'),
        detailRoomLocation: document.getElementById('detailRoomLocation'),
        detailRoomDate: document.getElementById('detailRoomDate'),
        detailRoomParticipants: document.getElementById('detailRoomParticipants'),
        detailRoomViews: document.getElementById('detailRoomViews'),
        detailRoomPeriod: document.getElementById('detailRoomPeriod'),
        detailRoomDesc: document.getElementById('detailRoomDesc'),
        detailRoomTags: document.getElementById('detailRoomTags')
    };

    if (elements.detailRoomImage) {
        elements.detailRoomImage.src = room.image || '/image/fix/moodtrip.png';
        elements.detailRoomImage.alt = room.title;
    }
    if (elements.detailRoomStatus) {
        elements.detailRoomStatus.textContent = room.status;
        elements.detailRoomStatus.className = `detail-room-status ${room.urgent ? 'urgent' : ''}`;
    }
    if (elements.detailRoomTitle) elements.detailRoomTitle.textContent = room.title;
    if (elements.detailRoomLocation) elements.detailRoomLocation.textContent = room.location;
    if (elements.detailRoomDate) elements.detailRoomDate.textContent = room.date;
    if (elements.detailRoomParticipants) elements.detailRoomParticipants.textContent = `${room.currentParticipants} / ${room.maxParticipants}명`;
    if (elements.detailRoomViews) elements.detailRoomViews.textContent = room.views;
    if (elements.detailRoomPeriod) elements.detailRoomPeriod.textContent = room.createdDate;
    if (elements.detailRoomDesc) elements.detailRoomDesc.textContent = room.description;

    // 태그 업데이트
    if (elements.detailRoomTags) {
        const tags = room.tags || room.emotions || [];
        elements.detailRoomTags.innerHTML = tags.map(tag => `<span class="tag"># ${tag}</span>`).join('');
    }

    // 모달 표시
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
}

// 상세보기 모달 닫기
function closeDetailModal() {
    const modal = document.getElementById('detailModal');
    if (modal) {
        modal.style.display = 'none';
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = 'auto';
        currentDetailRoomId = null;
    }
}

// 모달에서 입장 신청
function applyFromModal() {
    if (currentDetailRoomId) {
        const room = roomsData.find(r => r.id === currentDetailRoomId);
        if (room) {
            closeDetailModal();
            openApplicationModal(room);
        }
    }
}

// 활성 필터 설정
function setActiveFilter(filter) {
    currentFilter = filter;

    // UI 업데이트
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-filter="${filter}"]`);
    if (activeTab) activeTab.classList.add('active');

    // 지역별 필터 섹션 표시/숨김
    const regionFilterSection = document.getElementById('regionFilterSection');
    if (regionFilterSection) {
        if (filter === 'nearby') {
            regionFilterSection.style.display = 'block';
        } else {
            regionFilterSection.style.display = 'none';
            currentRegionFilter = 'all';
            setActiveRegionFilter('all');
        }
    }

    // 인원별 필터 섹션 표시/숨김
    const peopleFilterSection = document.getElementById('peopleFilterSection');
    if (peopleFilterSection) {
        if (filter === 'popular') {
            peopleFilterSection.style.display = 'block';
        } else {
            peopleFilterSection.style.display = 'none';
            currentPeopleFilter = 'all';
            setActivePeopleFilter('all');
        }
    }
}

// 활성 지역별 필터 설정
function setActiveRegionFilter(regionFilter) {
    currentRegionFilter = regionFilter;

    document.querySelectorAll('.region-filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-region="${regionFilter}"]`);
    if (activeTab) activeTab.classList.add('active');
}

// 활성 인원별 필터 설정
function setActivePeopleFilter(peopleFilter) {
    currentPeopleFilter = peopleFilter;

    document.querySelectorAll('.people-filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-people="${peopleFilter}"]`);
    if (activeTab) activeTab.classList.add('active');
}

// 🔥 기존 코드를 이렇게 수정
function renderRooms() {
    const roomList = document.getElementById('roomList');
    if (!roomList) return;

    if (filteredRooms.length === 0) {
        roomList.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #64748b;">
                <h3>검색 결과가 없습니다</h3>
                <p>다른 키워드로 검색해보세요.</p>
            </div>
        `;
        hidePaginationControlsNew();
        return;
    }

    console.log('renderRooms 시작 - 총 방 개수:', filteredRooms.length);

    // 전체 방 카드 렌더링
    renderAllRoomCards();

    // DOM이 완전히 렌더링된 후 페이징 초기화
    // requestAnimationFrame을 두 번 사용해서 확실히 DOM 업데이트 완료 후 실행
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            console.log('페이징 초기화 시작');
            initializeRoomPaginationNew();
        });
    });
}

// 결과 개수 업데이트
function updateResultsCount() {
    const resultsCount = document.getElementById('resultsCount');
    if (resultsCount) {
        resultsCount.textContent = filteredRooms.length.toLocaleString();
    }
}

// 방 입장 신청하기
function applyRoom(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (room) {
        openApplicationModal(room);
    }
}

// 신청 모달 열기
function openApplicationModal(room) {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;

    const modalRoomTitle = document.getElementById('modalRoomTitle');
    const modalRoomMeta = document.getElementById('modalRoomMeta');

    if (modalRoomTitle) modalRoomTitle.textContent = room.title;
    if (modalRoomMeta) modalRoomMeta.textContent = `${room.location} | ${room.currentParticipants}/${room.maxParticipants}명 | ${room.createdDate}`;

    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    modal.setAttribute('data-room-id', room.id);
    document.body.style.overflow = 'hidden';
}

// 신청 모달 닫기
function closeApplicationModal() {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;

    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = 'auto';

    // 폼 초기화
    const applicationMessage = document.getElementById('applicationMessage');
    if (applicationMessage) applicationMessage.value = '';
}

// 신청 제출 함수 - 실제 API 호출
async function submitApplication() {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;

    const roomId = parseInt(modal.getAttribute('data-room-id'));
    const messageElement = document.getElementById('applicationMessage');

    if (!messageElement) return;

    const message = messageElement.value.trim();

    if (!message) {
        alert('신청 메시지를 입력해주세요.');
        return;
    }

    const room = roomsData.find(r => r.id === roomId);
    if (!room) return;

    // 버튼 비활성화 (중복 클릭 방지)
    const submitButton = modal.querySelector('.btn-primary');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '신청 중...';
    }

    try {
        console.log('🚀 방 입장 신청 API 호출 시작 - roomId:', roomId);

        // 실제 API 호출
        const response = await fetch(`${JOIN_API_BASE_URL}/${roomId}/join-requests`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: message
            })
        });

        const result = await response.json();

        console.log('✅ 방 입장 신청 API 응답:', result);

        if (response.ok && result.success) {
            // 성공 시
            alert(`"${room.title}" 방에 입장 신청이 완료되었습니다!\n${result.resultMessage}`);
            closeApplicationModal();

            // 방 목록 새로고침 (참여자 수 업데이트 등)
            await loadRoomsData();

        } else {
            // 실패 시 (비즈니스 로직 오류)
            alert(result.resultMessage || '입장 신청 중 오류가 발생했습니다.');
        }

    } catch (error) {
        console.error('❌ 방 입장 신청 API 오류:', error);
        alert('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');

    } finally {
        // 버튼 복구
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = '신청하기';
        }
    }
}

// 방 신고 관련 함수들 (API 연동)

// 방 신고하기 (카드에서만)
function reportRoomFromCard(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (room) {
        openReportModal(room);
    }
}

// 신고 모달 열기
function openReportModal(room) {
    const modal = document.getElementById('reportModal');
    if (!modal) return;

    currentReportRoomId = room.id;

    const reportRoomTitle = document.getElementById('reportRoomTitle');
    const reportRoomMeta = document.getElementById('reportRoomMeta');

    if (reportRoomTitle) reportRoomTitle.textContent = room.title;
    if (reportRoomMeta) reportRoomMeta.textContent = `${room.location} | ${room.currentParticipants}/${room.maxParticipants}명`;

    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
}

// 신고 모달 닫기
function closeReportModal() {
    const modal = document.getElementById('reportModal');
    if (!modal) return;

    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = 'auto';
    currentReportRoomId = null;

    // 폼 초기화
    const reportReason = document.getElementById('reportReason');
    const reportMessage = document.getElementById('reportMessage');

    if (reportReason) reportReason.value = '';
    if (reportMessage) reportMessage.value = '';
}

// 신고 제출 함수 - 실제 API 호출
async function submitReport() {
    const reasonElement = document.getElementById('reportReason');
    const messageElement = document.getElementById('reportMessage');

    if (!reasonElement) {
        alert('신고 사유를 선택해주세요.');
        return;
    }

    const reason = reasonElement.value;
    const message = messageElement ? messageElement.value.trim() : '';

    // 유효성 검사
    if (!reason || reason === '') {
        alert('신고 사유를 선택해주세요.');
        return;
    }

    const room = roomsData.find(r => r.id === currentReportRoomId);
    if (!room) {
        alert('방 정보를 찾을 수 없습니다.');
        return;
    }

    // 버튼 비활성화 (중복 클릭 방지)
    const modal = document.getElementById('reportModal');
    const submitButton = modal.querySelector('.btn-danger');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '신고 중...';
    }

    try {
        console.log('🚀 방 신고 API 호출 시작 - roomId:', currentReportRoomId);
        console.log('📋 신고 데이터:', {
            roomId: currentReportRoomId,
            reportReason: reason,
            reportMessage: message
        });

        // 실제 Fire API 호출
        const response = await fetch(`${FIRE_API_BASE_URL}/rooms/${currentReportRoomId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                reportReason: reason,    // "spam", "inappropriate" 등
                reportMessage: message   // 상세 신고 내용
            })
        });

        const result = await response.json();

        console.log('✅ 방 신고 API 응답:', result);

        if (response.ok && result.success) {
            // 신고 성공
            alert(`"${room.title}" 방 신고가 접수되었습니다.\n${result.message}`);
            closeReportModal();

            console.log('🔥 신고 완료 정보:', {
                fireId: result.fireId,
                roomTitle: result.roomTitle,
                fireReason: result.fireReason,
                firedAt: result.firedAt
            });

        } else {
            // 신고 실패 (비즈니스 로직 오류)
            alert(result.message || '신고 접수 중 오류가 발생했습니다.');

            console.warn('⚠️ 신고 실패:', result);
        }

    } catch (error) {
        console.error('❌ 방 신고 API 오류:', error);

        // 네트워크 오류인지 서버 오류인지 구분
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            alert('네트워크 연결을 확인해주세요.');
        } else {
            alert('신고 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }

    } finally {
        // 버튼 복구
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = '신고하기';
        }
    }
}

// 신고 사유 변경 시 유효성 체크 (선택사항)
function validateReportForm() {
    const reasonElement = document.getElementById('reportReason');
    const submitButton = document.querySelector('#reportModal .btn-danger');

    if (reasonElement && submitButton) {
        const reason = reasonElement.value;

        // 신고 사유가 선택되면 버튼 활성화
        if (reason && reason !== '') {
            submitButton.disabled = false;
            submitButton.style.opacity = '1';
        } else {
            submitButton.disabled = true;
            submitButton.style.opacity = '0.6';
        }
    }
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeDetailModal();
        closeApplicationModal();
        closeReportModal();
    }
});

// 모달 오버레이 클릭으로 닫기
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        closeDetailModal();
        closeApplicationModal();
        closeReportModal();
    }
});

// 헬퍼 함수들 (기존 유지)
function formatScheduleForDisplay(schedule) {
    if (!schedule || !schedule.dateRanges || schedule.dateRanges.length === 0) {
        return '일정 미정';
    }

    const totalDays = schedule.totalDays || 1;
    const nights = Math.max(0, totalDays - 1);
    return `${nights}박 ${totalDays}일`;
}

function convertPeopleToNumber(peopleText) {
    if (peopleText === '2명') return 2;
    if (peopleText === '4명') return 4;
    if (peopleText === '기타') return 6;
    return 2;
}

function formatDate(date) {
    const year = date.getFullYear().toString().slice(-2);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}/${month}/${day}`;
}

// 상세보기 페이지 관련 함수들 (기존 유지)
function closeDetailPage() {
    const detailPage = document.getElementById('detailPage');
    if (detailPage) {
        detailPage.style.display = 'none';
        document.body.style.overflow = 'auto';
        currentDetailRoomId = null;
    }
}

function applyFromDetailPage() {
    if (currentDetailRoomId) {
        closeDetailPage();
        applyRoom(currentDetailRoomId);
    }
}

function updateRoomStatusColors() {
    document.querySelectorAll('.room-status').forEach(status => {
        const text = status.textContent.trim();

        if (text === '모집완료' || text.includes('완료')) {
            status.classList.add('completed');
            status.style.background = '#dc2626 !important';
        }
    });
}

// 페이지 로드 시 상태 색상 업데이트
document.addEventListener('DOMContentLoaded', function() {
    updateRoomStatusColors();
    // 0.5초마다 계속 실행
    setInterval(updateRoomStatusColors, 500);
});

// 스토리지 이벤트 리스너
window.addEventListener('storage', function(e) {
    if (e.key === 'roomDataUpdate') {
        try {
            const updateData = JSON.parse(e.newValue);
            console.log('📢 다른 탭에서 방 데이터 변경 감지:', updateData);

            if (updateData.type === 'MEMBER_LEFT') {
                console.log(`🔄 방 ${updateData.roomTitle}에서 멤버 나가기 감지, 페이지 업데이트`);

                // 즉시 페이지 새로고침
                showNotification('info', '방 정보가 업데이트되었습니다.');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);

                // 특정 방만 업데이트 (선택사항)
                updateSpecificRoom(updateData.roomId);
            }
        } catch (error) {
            console.error('방 데이터 업데이트 처리 오류:', error);
        }
    }
});

// 특정 방만 업데이트하는 함수
function updateSpecificRoom(roomId) {
    const roomCard = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomCard) {
        // 해당 방의 인원 수 -1
        const participantsElement = roomCard.querySelector('.participants-count');
        if (participantsElement) {
            const currentText = participantsElement.textContent; // "2 / 4"
            const [current, max] = currentText.split(' / ').map(num => parseInt(num.trim()));
            const newCurrent = Math.max(0, current - 1);

            participantsElement.textContent = `${newCurrent} / ${max}`;

            // 상태 업데이트
            const statusElement = roomCard.querySelector('.room-status');
            if (newCurrent < max && statusElement.textContent.includes('완료')) {
                statusElement.textContent = '모집중';
                statusElement.classList.remove('completed');
                statusElement.style.background = '#10b981'; // 초록색으로 변경
            }

            console.log(`✅ 방 ${roomId} 인원 업데이트: ${current} → ${newCurrent}`);
        }
    }
}

// 알림 표시 함수 (showNotification이 없으면 추가)
function showNotification(type, message) {
    // 간단한 알림 구현
    const notification = document.createElement('div');
    notification.style.cssText = `
       position: fixed;
       top: 20px;
       right: 20px;
       background: ${type === 'info' ? '#3b82f6' : '#ef4444'};
       color: white;
       padding: 12px 20px;
       border-radius: 6px;
       z-index: 9999;
       font-size: 14px;
       box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
   `;
    notification.textContent = message;

    document.body.appendChild(notification);

    // 3초 후 제거
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 3000);
}

// 모든 방 카드 렌더링 함수 (감정 태그 추가)
function renderAllRoomCards() {
    const roomList = document.getElementById('roomList');
    if (!roomList) return;

    // HTML 생성 부분에 감정 태그 추가
    roomList.innerHTML = filteredRooms.map(room => {
        const isDateAdjustment = room.status === '날짜조율';
        const isCompleted = room.status === '모집완료';
        const isUrgent = room.urgent === true;
        const isRecruiting = room.status === '모집중';

        // 감정 태그 HTML 생성
        const emotionsHtml = (room.emotions && room.emotions.length > 0)
            ? `<div class="room-emotions">
         ${room.emotions.map(emotion => `<span class="emotion-tag"> #${emotion} </span>`).join('')}
       </div>`
            : '';


        return `
            <div class="room-card room-visible ${isUrgent ? 'urgent' : ''} ${isCompleted ? 'completed' : ''} ${isDateAdjustment ? 'date-adjustment' : ''} ${isRecruiting ? 'recruiting' : ''}" 
                 data-room-id="${room.id}" 
                 data-pagination="true">
                <div class="room-image">
                    <img src="${room.image || '/image/fix/moodtrip.png'}" alt="${room.title}" onerror="this.src='/image/fix/moodtrip.png'">
                    <div class="room-status ${isUrgent ? 'urgent' : ''} ${isCompleted ? 'completed' : ''} ${isDateAdjustment ? 'date-adjustment' : ''}">${room.status}</div>
                </div>
                <div class="room-content">
                    <div class="room-header">
                        <h3 class="room-title">${room.title}</h3>
                        <div class="room-meta">
                            <span class="room-location">${room.location}</span>
                            <span class="room-date">${room.date}</span>
                            <span class="room-views">${room.views}</span>
                        </div>
                    </div>
                    <div class="room-description">${room.description}</div>
                    
                    <div class="room-footer">
                        <div class="room-participants-and-emotions">
                            <div class="room-participants">
                                <span class="participants-label">인원현재</span>
                                <span class="participants-count">${room.currentParticipants} / ${room.maxParticipants}</span>
                            </div>
                            ${emotionsHtml}
                        </div>
                        
                        <div class="room-date-info">
                            <span class="created-date">${room.createdDate}</span>
                        </div>
                        <div class="room-actions">
                            <button class="btn-detail" onclick="viewRoomDetail(${room.id})" aria-label="방 상세보기">상세보기</button>
                            ${isCompleted ?
            '<button class="btn-apply" disabled>모집완료</button>' :
            `<button class="btn-apply" onclick="applyRoom(${room.id})" aria-label="방 입장 신청">입장 신청</button>`
        }
                            <button class="btn-report-card" onclick="reportRoomFromCard(${room.id})" aria-label="방 신고하기">신고</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// 🔥 페이징 초기화 함수
function initializeRoomPaginationNew() {
    console.log('📄 방 카드 페이징 시스템 초기화 시작');

    const roomList = document.getElementById('roomList');
    if (!roomList) {
        console.error('❌ #roomList를 찾을 수 없음');
        return;
    }

    collectAllRoomCardsNew();

    if (totalItemsNew > 0) {
        showRoomPageNew(1);
        createRoomPaginationControlsNew();
    }
}

// 기존 renderRooms() 함수를 이렇게 수정하세요
function renderRooms() {
    const roomList = document.getElementById('roomList');
    if (!roomList) return;

    if (filteredRooms.length === 0) {
        roomList.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #64748b;">
                <h3>검색 결과가 없습니다</h3>
                <p>다른 키워드로 검색해보세요.</p>
            </div>
        `;
        hidePaginationControlsNew();
        return;
    }

    console.log('renderRooms 시작 - 총 방 개수:', filteredRooms.length);

    // 전체 방 카드 렌더링
    renderAllRoomCards();

    // DOM이 완전히 렌더링된 후 페이징 초기화
    // requestAnimationFrame을 두 번 사용해서 확실히 DOM 업데이트 완료 후 실행
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            console.log('페이징 초기화 시작');
            initializeRoomPaginationNew();
        });
    });
}

// collectAllRoomCardsNew() 함수 수정
function collectAllRoomCardsNew() {
    const now = Date.now();

    // 캐시 확인
    if (roomCardsCache && (now - lastCacheTimeNew) < CACHE_DURATION_NEW) {
        allRoomCards = roomCardsCache.cards;
        totalItemsNew = roomCardsCache.totalItems;
        totalPagesNew = roomCardsCache.totalPages;
        console.log('캐시 사용 - 방:', totalItemsNew, '페이지:', totalPagesNew);
        return;
    }

    console.log('DOM에서 방 카드 수집 시작');

    // 더 구체적인 선택자 사용
    const allCards = document.querySelectorAll('.room-card[data-pagination="true"]');
    console.log('실제 DOM에서 찾은 방 카드 수:', allCards.length);

    // 혹시 위의 선택자로 못 찾으면 대안 선택자 사용
    if (allCards.length === 0) {
        const alternativeCards = document.querySelectorAll('.room-card');
        console.log('대안 선택자로 찾은 방 카드 수:', alternativeCards.length);

        if (alternativeCards.length > 0) {
            // 대안으로 찾은 카드들에 data 속성 추가
            alternativeCards.forEach(card => {
                card.setAttribute('data-pagination', 'true');
            });

            collectAllRoomCardsNew(); // 다시 호출
            return;
        }
    }

    allRoomCards = [];

    for (let i = 0; i < allCards.length; i++) {
        const card = allCards[i];
        allRoomCards.push({
            element: card,
            roomTitle: card.querySelector('.room-title')?.textContent || '',
            roomId: card.getAttribute('data-room-id') || '',
            index: i
        });
    }

    totalItemsNew = allRoomCards.length;
    totalPagesNew = Math.ceil(totalItemsNew / itemsPerPageNew);

    console.log('최종 계산된 값:');
    console.log('  - 총 방 카드:', totalItemsNew);
    console.log('  - 페이지당 카드:', itemsPerPageNew);
    console.log('  - 총 페이지:', totalPagesNew);
    console.log('  - 첫 번째 방 제목:', allRoomCards[0]?.roomTitle);

    // 캐시 저장
    roomCardsCache = {
        cards: [...allRoomCards],
        totalItems: totalItemsNew,
        totalPages: totalPagesNew
    };
    lastCacheTimeNew = now;
}


// showRoomPageNew() 함수도 더 명확하게 수정
function showRoomPageNew(pageNumber) {
    console.log(`페이지 ${pageNumber} 표시 시작 - 총 카드: ${allRoomCards.length}`);

    if (allRoomCards.length === 0) {
        console.log('카드가 없어서 페이징 중단');
        return;
    }

    currentPageNew = pageNumber;

    // 1단계: 모든 방 카드 숨기기
    let hiddenCount = 0;
    allRoomCards.forEach(cardData => {
        cardData.element.classList.add('room-hidden');
        cardData.element.classList.remove('room-visible', 'room-fade-in');
        hiddenCount++;
    });
    console.log(`${hiddenCount}개 카드 숨김 처리 완료`);

    // 2단계: 현재 페이지에 표시할 카드 계산
    const startIndex = (pageNumber - 1) * itemsPerPageNew;
    const endIndex = Math.min(startIndex + itemsPerPageNew, totalItemsNew);
    currentPageRooms = allRoomCards.slice(startIndex, endIndex);

    console.log(`페이지 ${pageNumber}: ${startIndex}~${endIndex-1} 인덱스 (${currentPageRooms.length}개 카드)`);

    // 3단계: 현재 페이지 카드들만 표시
    let shownCount = 0;
    currentPageRooms.forEach((cardData, index) => {
        const element = cardData.element;

        element.classList.remove('room-hidden');
        element.classList.add('room-visible');

        // 애니메이션 효과
        setTimeout(() => {
            element.classList.add('room-fade-in');
        }, index * 50);

        shownCount++;
        console.log(`카드 ${index + 1} 표시: ${cardData.roomTitle}`);
    });

    console.log(`총 ${shownCount}개 카드 표시 완료`);

    updateRoomPaginationButtonsNew();
}


// 🔥 페이징 컨트롤 생성 함수
function createRoomPaginationControlsNew() {
    const existingPagination = document.querySelector('.room-pagination-controls-new');
    if (existingPagination) existingPagination.remove();

    if (totalPagesNew <= 1) return;

    const roomList = document.getElementById('roomList');
    if (!roomList) return;

    const paginationHtml = `
        <div class="room-pagination-controls-new" style="
            display: flex; 
            justify-content: center; 
            align-items: center; 
            gap: 8px; 
            margin: 3rem auto 2rem; 
            padding: 1.5rem;
            border: 1px solid #e5e7eb;
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        ">
            <button class="room-prev-btn-new" onclick="goToPrevRoomPageNew()" style="
                padding: 10px 20px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 600;
            ">← 이전</button>
            
            <div class="room-page-numbers-new" style="display: flex; gap: 6px;"></div>
            
            <button class="room-next-btn-new" onclick="goToNextRoomPageNew()" style="
                padding: 10px 20px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 600;
            ">다음 →</button>
        </div>
    `;

    roomList.insertAdjacentHTML('afterend', paginationHtml);
    createRoomPageNumbersNew();
}

// 🔥 페이지 번호 생성 함수
function createRoomPageNumbersNew() {
    const container = document.querySelector('.room-page-numbers-new');
    if (!container) return;

    container.innerHTML = '';

    const maxButtons = 5;
    let startPage = Math.max(1, currentPageNew - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPagesNew, startPage + maxButtons - 1);

    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPageNew;
        const button = document.createElement('button');
        button.textContent = i;
        button.onclick = () => goToRoomPageNew(i);
        button.style.cssText = `
            padding: 10px 15px;
            border: 1px solid ${isActive ? '#005792' : '#e5e7eb'};
            background: ${isActive ? '#005792' : 'white'};
            color: ${isActive ? 'white' : '#374151'};
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            min-width: 45px;
            font-weight: 600;
        `;
        container.appendChild(button);
    }
}

// 🔥 페이징 버튼 업데이트 함수
function updateRoomPaginationButtonsNew() {
    const prevBtn = document.querySelector('.room-prev-btn-new');
    const nextBtn = document.querySelector('.room-next-btn-new');

    if (prevBtn) {
        prevBtn.disabled = currentPageNew === 1;
        prevBtn.style.opacity = currentPageNew === 1 ? '0.5' : '1';
    }

    if (nextBtn) {
        nextBtn.disabled = currentPageNew === totalPagesNew;
        nextBtn.style.opacity = currentPageNew === totalPagesNew ? '0.5' : '1';
    }

    createRoomPageNumbersNew();
}

// 🔥 페이징 숨기기 함수
function hidePaginationControlsNew() {
    const pagination = document.querySelector('.room-pagination-controls-new');
    if (pagination) pagination.style.display = 'none';
}

// 🔥 네비게이션 함수들
function goToRoomPageNew(pageNumber) {
    if (pageNumber < 1 || pageNumber > totalPagesNew) return;
    showRoomPageNew(pageNumber);
}

function goToPrevRoomPageNew() {
    if (currentPageNew > 1) {
        goToRoomPageNew(currentPageNew - 1);
    }
}

function goToNextRoomPageNew() {
    if (currentPageNew < totalPagesNew) {
        goToRoomPageNew(currentPageNew + 1);
    }
}

// 🔥 전역 함수 노출
window.goToRoomPageNew = goToRoomPageNew;
window.goToPrevRoomPageNew = goToPrevRoomPageNew;
window.goToNextRoomPageNew = goToNextRoomPageNew;

// 모든 리소스 로드 후에도 실행
window.addEventListener('load', updateRoomStatusColors);
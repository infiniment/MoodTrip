// 🔥 서버 API 기본 URL
const API_BASE_URL = '/api/v1/companion-rooms/search';
const JOIN_API_BASE_URL = '/api/v1/companion-rooms'; // 🔥 방 입장 신청 API URL 추가

// 방 데이터 (서버에서 가져온 데이터로 저장)
let roomsData = [];
let filteredRooms = [];

// 현재 상태 변수들
const currentDate = new Date('2025-07-02');
let currentFilter = 'all';
let currentSort = 'default';
let currentPage = 1;
let currentPeopleFilter = 'all';
let currentRegionFilter = 'all';
let currentDetailRoomId = null;
let currentReportRoomId = null;

// 🚀 서버에서 방 목록 가져오기
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

// 🔄 데이터 로드 및 화면 업데이트
async function loadRoomsData(params = {}) {
    // 서버에서 데이터 가져오기
    const data = await fetchRoomsFromServer(params);

    // 전역 변수 업데이트
    roomsData = data;
    filteredRooms = [...data];

    // 화면 업데이트
    renderRooms();
    updateResultsCount();
    updatePagination();

    console.log('✅ 방 목록 로드 완료:', data.length + '개');
}

// 📊 통계 정보 가져오기
async function fetchRoomStats() {
    const response = await fetch(`${API_BASE_URL}/stats`);
    const stats = await response.json();

    console.log('📊 방 통계:', stats);
    return stats;
}

// DOM 로드 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 페이지 로드 - 초기화 시작');

    initializeEventListeners();
    loadRoomsData(); // 🔥 서버에서 데이터 로드
    loadUserCreatedRooms(); // localStorage 확인
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

    // 정렬 드롭다운
    const sortButton = document.getElementById('sortButton');
    const sortMenu = document.getElementById('sortMenu');
    const sortOptions = document.querySelectorAll('.sort-option');

    if (sortButton && sortMenu) {
        sortButton.addEventListener('click', function() {
            sortMenu.classList.toggle('show');
        });

        sortOptions.forEach(option => {
            option.addEventListener('click', function() {
                const sort = this.getAttribute('data-sort');
                setActiveSort(sort);
                applySorting();
                renderRooms();
                updatePagination();
                sortMenu.classList.remove('show');
            });
        });

        // 외부 클릭 시 드롭다운 닫기
        document.addEventListener('click', function(e) {
            if (!sortButton.contains(e.target) && !sortMenu.contains(e.target)) {
                sortMenu.classList.remove('show');
            }
        });
    }

    // 마감 임박 체크박스
    const urgentOnly = document.getElementById('urgentOnly');
    if (urgentOnly) {
        urgentOnly.addEventListener('change', applyFilters);
    }

    // 페이지네이션
    initializePagination();
}

// 🔍 검색 처리 (서버 API 호출)
async function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    const searchTerm = searchInput.value.trim();
    console.log('🔍 검색 요청:', searchTerm);

    // 서버에서 검색 결과 가져오기
    await loadRoomsData({ search: searchTerm });

    currentPage = 1;
    updatePagination();
}

// 🔄 필터 적용 (서버 API 호출)
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

    currentPage = 1;
    updatePagination();
}

// 🔧 수정된 방 상세보기 함수
async function viewRoomDetail(roomId) {
    try {
        console.log('🔍 방 상세보기 요청 - roomId:', roomId);
        currentDetailRoomId = roomId;

        // 🔥 올바른 엔드포인트로 수정 (조회수 증가 포함)
        const response = await fetch(`/entering-room/${roomId}/modal-data`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const room = await response.json();

        console.log('✅ 방 상세보기 데이터 (조회수 증가됨):', room);
        console.log('🔥 현재 조회수:', room.viewCount, '- 표시:', room.views);

        openDetailModal(room);

    } catch (error) {
        console.error('❌ 방 상세보기 실패:', error);
        alert('방 정보를 불러올 수 없습니다.');
    }
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

    // 태그 업데이트 (서버에서 emotions 필드로 올 수도 있음)
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

// 정렬 적용 (클라이언트 사이드)
function applySorting() {
    switch (currentSort) {
        case 'nearest':
            filteredRooms.sort((a, b) => {
                // 날짜 문자열을 Date 객체로 변환하여 비교
                const aDate = new Date(a.dateValue || a.date);
                const bDate = new Date(b.dateValue || b.date);
                const aDiff = Math.abs(aDate.getTime() - currentDate.getTime());
                const bDiff = Math.abs(bDate.getTime() - currentDate.getTime());
                return aDiff - bDiff;
            });
            break;
        case 'popular':
            filteredRooms.sort((a, b) => {
                return (b.viewCount || 0) - (a.viewCount || 0);
            });
            break;
        default:
            filteredRooms.sort((a, b) => {
                if (a.urgent !== b.urgent) {
                    return a.urgent ? 1 : -1;
                }
                return a.id - b.id;
            });
    }

    currentPage = 1;
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

// 활성 정렬 설정
function setActiveSort(sort) {
    currentSort = sort;

    document.querySelectorAll('.sort-option').forEach(option => {
        option.classList.remove('active');
    });
    const activeOption = document.querySelector(`[data-sort="${sort}"]`);
    if (activeOption) activeOption.classList.add('active');

    // 버튼 텍스트 업데이트
    const sortTexts = {
        'default': '기본 정렬 순',
        'nearest': '가까운 날짜 순',
        'popular': '인기순'
    };

    const sortButton = document.getElementById('sortButton');
    if (sortButton && sortButton.firstChild) {
        sortButton.firstChild.textContent = sortTexts[sort] + ' ';
    }
}

// 방 목록 렌더링
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
        return;
    }

    // 페이지네이션 적용
    const itemsPerPage = 5;
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageRooms = filteredRooms.slice(startIndex, endIndex);

    roomList.innerHTML = pageRooms.map(room => `
        <div class="room-card" data-room-id="${room.id}">
            <div class="room-image">
                <img src="${room.image || '/image/fix/moodtrip.png'}" alt="${room.title}" onerror="this.src='/image/fix/moodtrip.png'">
                <div class="room-status ${room.urgent ? 'urgent' : ''}">${room.status}</div>
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
                <div class="room-tags">
                    ${(room.tags || []).map(tag => `<span class="tag"># ${tag}</span>`).join('')}
                </div>
                <div class="room-footer">
                    <div class="room-participants">
                        <span class="participants-label">인원현재</span>
                        <span class="participants-count">${room.currentParticipants} / ${room.maxParticipants}</span>
                    </div>
                    <div class="room-date-info">
                        <span class="created-date">${room.createdDate}</span>
                    </div>
                    <div class="room-actions">
                        <button class="btn-detail" onclick="viewRoomDetail(${room.id})" aria-label="방 상세보기">상세보기</button>
                        <button class="btn-apply" onclick="applyRoom(${room.id})" aria-label="방 입장 신청">입장 신청</button>
                        <button class="btn-report-card" onclick="reportRoomFromCard(${room.id})" aria-label="방 신고하기">신고</button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// 결과 개수 업데이트
function updateResultsCount() {
    const resultsCount = document.getElementById('resultsCount');
    if (resultsCount) {
        resultsCount.textContent = filteredRooms.length.toLocaleString();
    }
}

// 페이지네이션 초기화
function initializePagination() {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                renderRooms();
                updatePagination();
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            const totalPages = Math.ceil(filteredRooms.length / 5);
            if (currentPage < totalPages) {
                currentPage++;
                renderRooms();
                updatePagination();
            }
        });
    }

    // 페이지 번호 클릭
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('page-btn') && e.target.getAttribute('data-page')) {
            const page = parseInt(e.target.getAttribute('data-page'));
            currentPage = page;
            renderRooms();
            updatePagination();
        }
    });
}

// 페이지네이션 업데이트
function updatePagination() {
    const totalPages = Math.ceil(filteredRooms.length / 5);
    const pagination = document.getElementById('pagination');

    if (!pagination) return;

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';

    // 페이지 버튼 업데이트
    const pageButtons = document.querySelectorAll('.page-btn[data-page]');
    pageButtons.forEach(btn => {
        const page = parseInt(btn.getAttribute('data-page'));
        btn.classList.toggle('active', page === currentPage);
    });

    // 이전/다음 버튼 상태
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (prevBtn) prevBtn.disabled = currentPage === 1;
    if (nextBtn) nextBtn.disabled = currentPage === totalPages;
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

// 🔥 수정된 신청 제출 함수 - 실제 API 호출
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

    // 🔥 버튼 비활성화 (중복 클릭 방지)
    const submitButton = modal.querySelector('.btn-primary');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '신청 중...';
    }

    try {
        console.log('🚀 방 입장 신청 API 호출 시작 - roomId:', roomId);

        // 🔥 실제 API 호출
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
            // 🎉 성공 시
            alert(`"${room.title}" 방에 입장 신청이 완료되었습니다!\n${result.resultMessage}`);
            closeApplicationModal();

            // 🔄 방 목록 새로고침 (참여자 수 업데이트 등)
            await loadRoomsData();

        } else {
            // ❌ 실패 시 (비즈니스 로직 오류)
            alert(result.resultMessage || '입장 신청 중 오류가 발생했습니다.');
        }

    } catch (error) {
        console.error('❌ 방 입장 신청 API 오류:', error);
        alert('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');

    } finally {
        // 🔄 버튼 복구
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = '신청하기';
        }
    }
}

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

// 신고 제출
function submitReport() {
    const reasonElement = document.getElementById('reportReason');
    const messageElement = document.getElementById('reportMessage');

    if (!reasonElement) return;

    const reason = reasonElement.value;
    const message = messageElement ? messageElement.value.trim() : '';

    if (!reason) {
        alert('신고 사유를 선택해주세요.');
        return;
    }

    const room = roomsData.find(r => r.id === currentReportRoomId);
    if (!room) return;

    // 🔥 TODO: 실제 서버에 신고 데이터 전송하는 API 구현 필요
    console.log('신고 데이터:', {
        roomId: currentReportRoomId,
        roomTitle: room.title,
        reason: reason,
        message: message,
        timestamp: new Date().toISOString()
    });

    alert('신고가 접수되었습니다.\n검토 후 적절한 조치를 취하겠습니다.');
    closeReportModal();
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

// localStorage에서 사용자 생성 방 데이터 불러오기 (기존 기능 유지)
function loadUserCreatedRooms() {
    try {
        const finalSubmission = localStorage.getItem('final_room_submission');
        if (finalSubmission) {
            const roomData = JSON.parse(finalSubmission);

            const newRoom = {
                id: roomsData.length + 1,
                title: roomData.roomName || '새로운 여행',
                location: roomData.destination?.name || '미정',
                date: formatScheduleForDisplay(roomData.schedule),
                views: '0명이 봄',
                viewCount: 0,
                description: roomData.roomIntro || '여행 소개가 없습니다.',
                tags: roomData.emotions?.map(e => typeof e === 'string' ? e : e.text) || ['여행'],
                currentParticipants: 1,
                maxParticipants: convertPeopleToNumber(roomData.people),
                createdDate: formatDate(new Date()),
                image: roomData.destination?.image || '/image/fix/moodtrip.png',
                urgent: false,
                status: '모집중'
            };

            if (!roomsData.find(room => room.id === newRoom.id)) {
                roomsData.unshift(newRoom);
                filteredRooms = [...roomsData];
                renderRooms();
                updateResultsCount();
                console.log('사용자 생성 방이 목록에 추가되었습니다:', newRoom);
            }
        }
    } catch (error) {
        console.error('사용자 생성 방 로드 중 오류:', error);
    }
}

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
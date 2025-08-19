let currentPage = 1;
const itemsPerPage = 3; // 방은 3개씩 표시 (카드가 크므로)
let totalItems = 0;
let totalPages = 0;
let allRoomSections = []; // 모든 방 섹션을 저장
let currentPageRooms = []; // 현재 페이지의 방들

// 🔥 서버 API 기본 URL
const API_BASE_URL = '/api/v1/join-requests';

document.addEventListener('DOMContentLoaded', function() {
    // 🔥 1순위: 페이징 즉시 초기화 (기존 DOM 데이터 활용)
    console.log('⚡ 페이지 로드 즉시 페이징 초기화');
    initializeRoomPagination();

    // 🔥 2순위: UI 초기화
    initializeFilters();
    initializeSearch();
    initializeRequestActions();
    initializeModals();

    // 🔥 3순위: 백그라운드에서 최신 데이터 로드 (페이징은 이미 동작 중)
    loadInitialData();
});

async function loadInitialData() {
    try {
        console.log('🚀 백그라운드 데이터 로드 시작');

        const [statsData, roomsData] = await Promise.all([
            fetchRequestStats(),
            fetchMyRoomsWithRequests()
        ]);

        updateStatsDisplay(statsData);

        // 🔥 데이터가 변경되었을 때만 다시 렌더링
        const currentRoomCount = document.querySelectorAll('.room-section').length;
        if (roomsData.length !== currentRoomCount) {
            renderRoomsData(roomsData);
            // 🔥 데이터 변경 시에만 페이징 재초기화
            setTimeout(() => {
                initializeRoomPagination();
            }, 100);
        }

        updateNotificationBadge();

        console.log('✅ 백그라운드 데이터 로드 완료');

    } catch (error) {
        console.error('❌ 백그라운드 데이터 로드 실패:', error);
        // 🔥 API 실패해도 기존 페이징은 계속 동작
        showToast('error', '최신 데이터를 불러오는 중 오류가 발생했습니다.');
    }
}

/**
 * 🔥 방장의 방 목록 + 신청 목록 조회 API
 */
async function fetchMyRoomsWithRequests() {
    const response = await fetch(`${API_BASE_URL}/rooms`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
}

/**
 * 🔥 통계 데이터 조회 API
 */
async function fetchRequestStats() {
    const response = await fetch(`${API_BASE_URL}/stats`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
}

/**
 * 필터 기능 초기화
 */
function initializeFilters() {
    const roomFilter = document.getElementById('room-filter');
    const priorityFilter = document.getElementById('priority-filter');

    if (roomFilter) roomFilter.addEventListener('change', applyFilters);
    if (priorityFilter) priorityFilter.addEventListener('change', applyFilters);
}

/**
 * 검색 기능 초기화
 */
function initializeSearch() {
    const searchInput = document.getElementById('search-requests');
    if (!searchInput) return;

    // 디바운스 적용 (300ms 지연)
    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            applyFilters();
        }, 300);
    });
}

/**
 * 개별 요청 액션 버튼 초기화
 */
function initializeRequestActions() {
    // 승인 버튼 이벤트
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-approve')) {
            const requestId = e.target.getAttribute('data-request-id');
            const roomId = e.target.getAttribute('data-room-id');
            handleApproveRequest(requestId, roomId, e.target);
        }
    });

    // 거절 버튼 이벤트
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-reject')) {
            const requestId = e.target.getAttribute('data-request-id');
            const roomId = e.target.getAttribute('data-room-id');
            handleRejectRequest(requestId, roomId, e.target);
        }
    });
}

function initializeModals() {
    const modal = document.getElementById('confirmModal');
    if (!modal) return;

    const closeBtn = modal.querySelector('.modal-close');
    const cancelBtn = modal.querySelector('.btn-cancel');

    if (closeBtn) closeBtn.addEventListener('click', hideModal);
    if (cancelBtn) cancelBtn.addEventListener('click', hideModal);

    // 오버레이 클릭으로 닫기
    modal.addEventListener('click', function(e) {
        if (e.target === modal) hideModal();
    });

    // ESC 키로 모달 닫기만 (페이징 키보드 기능 제거)
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            hideModal();
        }
    });
}

/**
 * 필터 적용
 */
function applyFilters() {
    const roomFilter = (document.getElementById('room-filter')?.value) || 'all';
    const priorityFilter = (document.getElementById('priority-filter')?.value) || 'all';
    const searchTerm = (document.getElementById('search-requests')?.value || '').toLowerCase();

    const sections = document.querySelectorAll('.room-section');
    let visibleRequests = 0;

    sections.forEach(section => {
        const roomTitle = section.querySelector('.room-title')?.textContent || '';
        const requests = section.querySelectorAll('.request-item-detailed');
        let sectionHasVisibleRequests = false;

        // 방 필터 적용
        if (roomFilter !== 'all' && !roomTitle.includes(roomFilter)) {
            section.classList.add('hidden');
            return;
        }

        // 각 요청 필터링
        requests.forEach(request => {
            const name = (request.querySelector('.request-name-large')?.textContent || '').toLowerCase();
            const message = (request.querySelector('.request-message')?.textContent || '').toLowerCase();
            const priority = request.querySelector('.priority-badge')?.classList.contains('priority-high') ? 'high' : 'normal';

            let visible = true;

            // 우선순위 필터
            if (priorityFilter !== 'all' && priority !== priorityFilter) visible = false;

            // 검색어 필터
            if (searchTerm && !name.includes(searchTerm) && !message.includes(searchTerm)) visible = false;

            if (visible) {
                request.classList.remove('hidden');
                sectionHasVisibleRequests = true;
                visibleRequests++;
            } else {
                request.classList.add('hidden');
            }
        });

        // 섹션 표시/숨김
        section.classList.toggle('hidden', !sectionHasVisibleRequests);
    });

    // 결과 없음 표시
    showNoResultsIfNeeded(visibleRequests === 0);
}

/**
 * 검색 결과 없음 표시
 */
function showNoResultsIfNeeded(show) {
    let noResultsDiv = document.getElementById('no-results');

    if (show && !noResultsDiv) {
        noResultsDiv = document.createElement('div');
        noResultsDiv.id = 'no-results';
        noResultsDiv.className = 'no-results';
        noResultsDiv.innerHTML = `
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
            </svg>
            <h3>검색 결과가 없습니다</h3>
            <p>다른 검색어나 필터를 시도해보세요.</p>
        `;
        document.querySelector('.main-wrapper')?.appendChild(noResultsDiv);
    } else if (!show && noResultsDiv) {
        noResultsDiv.remove();
    }
}

/**
 * 🔥 개별 요청 승인 처리 (실제 API 호출)
 */
function handleApproveRequest(requestId, roomId, buttonElement) {
    const requestItem = buttonElement.closest('.request-item-detailed');
    const userName = (requestItem.querySelector('.request-name-large')?.firstChild?.textContent || '').trim();

    showConfirmModal(
        '입장 승인',
        `${userName}님의 입장을 승인하시겠습니까?`,
        '',
        async () => {
            await processRequestApproval(parseInt(requestId), requestItem);
        }
    );
}

/**
 * 🔥 개별 요청 거절 처리 (실제 API 호출)
 */
function handleRejectRequest(requestId, roomId, buttonElement) {
    const requestItem = buttonElement.closest('.request-item-detailed');
    const userName = (requestItem.querySelector('.request-name-large')?.firstChild?.textContent || '').trim();

    showConfirmModal(
        '입장 거절',
        `${userName}님의 입장을 거절하시겠습니까?`,
        '',
        async () => {
            await processRequestRejection(parseInt(requestId), requestItem);
        }
    );
}

/**
 * 🔥 개별 요청 승인 처리 (실제 API 호출)
 */
async function processRequestApproval(requestId, requestItem) {
    setButtonLoading(requestItem, 'approve', true);

    try {
        console.log('🚀 개별 승인 API 호출 시작 - requestId:', requestId);

        const response = await fetch(`${API_BASE_URL}/${requestId}/approve`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();

        if (response.ok && result.success) {
            console.log('✅ 개별 승인 성공:', result);
            animateRequestRemoval(requestItem, 'approve');
            showToast('success', result.message);
            await refreshData();
        } else {
            console.error('❌ 개별 승인 실패:', result.message);
            showToast('error', result.message || '승인 처리에 실패했습니다.');
            setButtonLoading(requestItem, 'approve', false);
        }

    } catch (error) {
        console.error('❌ 개별 승인 API 오류:', error);
        showToast('error', '네트워크 오류가 발생했습니다. 다시 시도해주세요.');
        setButtonLoading(requestItem, 'approve', false);
    }
}

/**
 * 🔥 개별 요청 거절 처리 (실제 API 호출)
 */
async function processRequestRejection(requestId, requestItem) {
    setButtonLoading(requestItem, 'reject', true);

    try {
        console.log('🚀 개별 거절 API 호출 시작 - requestId:', requestId);

        const response = await fetch(`${API_BASE_URL}/${requestId}/reject`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();

        if (response.ok && result.success) {
            console.log('✅ 개별 거절 성공:', result);
            animateRequestRemoval(requestItem, 'reject');
            showToast('info', result.message);
            await refreshData();
        } else {
            console.error('❌ 개별 거절 실패:', result.message);
            showToast('error', result.message || '거절 처리에 실패했습니다.');
            setButtonLoading(requestItem, 'reject', false);
        }

    } catch (error) {
        console.error('❌ 개별 거절 API 오류:', error);
        showToast('error', '네트워크 오류가 발생했습니다. 다시 시도해주세요.');
        setButtonLoading(requestItem, 'reject', false);
    }
}

/**
 * 🔥 데이터 새로고침 (통계 + 대기 카운트 등)
 */
/**
 * 🔥 데이터 새로고침 (통계 + 대기 카운트 등) - 수정됨
 */
async function refreshData() {
    try {
        console.log('🔄 데이터 새로고침 시작');

        // 통계 데이터 새로고침
        const statsData = await fetchRequestStats();
        updateStatsDisplay(statsData);

        // 🔥 페이징 데이터 다시 수집 추가
        setTimeout(() => {
            collectAllRequestItems();
            if (currentPage > totalPages && totalPages > 0) {
                // 현재 페이지가 총 페이지보다 크면 마지막 페이지로 이동
                showPage(totalPages);
            } else {
                // 현재 페이지 다시 표시
                showPage(currentPage);
            }
            updatePaginationButtons();
        }, 100);

        // 알림 배지/대기 건수/빈 섹션 업데이트
        updateNotificationBadge();

        console.log('✅ 데이터 새로고침 완료');

    } catch (error) {
        console.error('❌ 데이터 새로고침 실패:', error);
    }
}

/**
 * 🔥 통계 데이터 UI 업데이트
 */
function updateStatsDisplay(statsData) {
    document.getElementById('total-requests').textContent = statsData.totalRequests || 0;
    document.getElementById('today-requests').textContent = statsData.todayRequests || 0;
    document.getElementById('urgent-requests').textContent = statsData.urgentRequests || 0;
}

/**
 * 🔥 방 목록 데이터 렌더링 (서버에서 받은 데이터로)
 */
function renderRoomsData(roomsData) {
    const mainWrapper = document.querySelector('.main-wrapper');
    if (!mainWrapper) return;

    // 기존 내용 제거
    mainWrapper.innerHTML = '';

    if (!roomsData || roomsData.length === 0) {
        mainWrapper.innerHTML = `
            <div class="empty-section">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="M12 6v6l4 2"></path>
                </svg>
                <h3>아직 만든 방이 없습니다</h3>
                <p>방을 만들면 입장 요청을 관리할 수 있습니다.</p>
            </div>
        `;
        return;
    }

    // 방별로 섹션 생성
    roomsData.forEach(room => {
        const roomSection = createRoomSection(room);
        mainWrapper.appendChild(roomSection);
    });
}

/**
 * 🔥 방 섹션 HTML 생성
 */
function createRoomSection(room) {
    const section = document.createElement('div');
    section.className = 'room-section';
    section.setAttribute('data-room-id', room.roomId);

    const waitingText = room.pendingRequestsCount > 0
        ? `${room.pendingRequestsCount}건 대기`
        : '요청 없음';

    section.innerHTML = `
        <div class="room-header">
            <div>
                <h3 class="room-title">${room.roomTitle}</h3>
                <div class="room-meta">여행 날짜: ${room.travelDate} | 현재 인원: ${room.currentParticipants}/${room.maxParticipants}명</div>
            </div>
            <div class="waiting-count ${room.pendingRequestsCount === 0 ? 'no-requests' : ''}">${waitingText}</div>
        </div>
        <div class="requests-container">
            ${room.joinRequests && room.joinRequests.length > 0
        ? room.joinRequests.map(request => createRequestItem(request)).join('')
        : createEmptySection()
    }
        </div>
    `;

    return section;
}

/**
 * 🔥 요청 아이템 HTML 생성 (체크박스/대량 기능 제거 버전)
 */
function createRequestItem(request) {
    const priorityClass = request.priority === 'HIGH' ? 'priority-high' : 'priority-normal';
    const priorityText = request.priority === 'HIGH' ? '높음' : '보통';

    return `
        <div class="request-item-detailed" data-request-id="${request.joinRequestId}">
            <div class="request-avatar-large">
                <img src="${request.applicantProfileImage}" alt="${request.applicantNickname}">
            </div>
            <div class="request-details">
                <div class="request-name-large">
                    ${request.applicantNickname}
                    <span class="priority-badge ${priorityClass}">${priorityText}</span>
                </div>
                <div class="request-message">${request.message}</div>
                <div class="request-meta-detailed">
                    <span>${request.appliedAt} 신청</span>
                    <span class="time-ago">${request.timeAgo}</span>
                </div>
            </div>
            <div class="request-actions-detailed">
                <button class="btn-approve" data-request-id="${request.joinRequestId}">승인</button>
                <button class="btn-reject" data-request-id="${request.joinRequestId}">거절</button>
            </div>
        </div>
    `;
}

/**
 * 빈 섹션 HTML 생성
 */
function createEmptySection() {
    return `
        <div class="empty-section">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 6v6l4 2"></path>
            </svg>
            <h4>아직 입장 요청이 없습니다</h4>
            <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
        </div>
    `;
}

/**
 * 버튼 로딩 상태 설정
 */
function setButtonLoading(requestItem, action, isLoading) {
    const buttons = requestItem.querySelectorAll('button');
    buttons.forEach(btn => {
        btn.disabled = isLoading;
        if (btn.classList.contains(`btn-${action}`)) {
            btn.textContent = isLoading ? '처리 중...' : (action === 'approve' ? '승인' : '거절');
        }
    });
}

/**
 * 단일 요청 제거 애니메이션
 */
function animateRequestRemoval(requestItem, action) {
    const direction = action === 'approve' ? '100%' : '-100%';

    requestItem.style.transform = `translateX(${direction})`;
    requestItem.style.opacity = '0';
    requestItem.style.transition = 'all 0.3s ease';

    setTimeout(() => {
        if (requestItem.parentNode) requestItem.remove();
    }, 300);
}

/**
 * 대기 건수 업데이트
 */
function updateWaitingCounts() {
    const sections = document.querySelectorAll('.room-section');

    sections.forEach(section => {
        const requests = section.querySelectorAll('.request-item-detailed:not(.hidden)');
        const waitingCount = section.querySelector('.waiting-count');

        if (waitingCount) {
            if (requests.length > 0) {
                waitingCount.textContent = `${requests.length}건 대기`;
                waitingCount.classList.remove('no-requests');
            } else {
                waitingCount.textContent = '요청 없음';
                waitingCount.classList.add('no-requests');
            }
        }
    });
}

/**
 * 알림 배지 업데이트
 */
function updateNotificationBadge() {
    const badge = document.getElementById('join-requests-badge');
    if (!badge) return; // 요소 없으면 종료

    const totalRequests = document.querySelectorAll('.request-item-detailed:not(.hidden)').length;
    if (totalRequests > 0) {
        badge.textContent = totalRequests;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

/**
 * 빈 섹션 확인 및 메시지 표시
 */
function checkEmptySections() {
    const sections = document.querySelectorAll('.room-section');

    sections.forEach(section => {
        const requests = section.querySelectorAll('.request-item-detailed');
        const container = section.querySelector('.requests-container');

        if (requests.length === 0 && container) {
            container.innerHTML = createEmptySection();
        }
    });
}

/**
 * 확인 모달 표시
 */
function showConfirmModal(title, message, details, onConfirm) {
    const modal = document.getElementById('confirmModal');
    const titleElement = document.getElementById('confirmTitle');
    const messageElement = document.getElementById('confirmMessage');
    const detailsElement = document.getElementById('confirmDetails');
    const confirmButton = document.getElementById('confirmAction');

    if (!modal) return;

    titleElement.textContent = title;
    messageElement.textContent = message;

    if (details) {
        detailsElement.textContent = `대상: ${details}`;
        detailsElement.style.display = 'block';
    } else {
        detailsElement.style.display = 'none';
    }

    // 기존 이벤트 리스너 제거 후 새로 추가
    const newConfirmButton = confirmButton.cloneNode(true);
    confirmButton.parentNode.replaceChild(newConfirmButton, confirmButton);

    newConfirmButton.addEventListener('click', function() {
        hideModal();
        if (onConfirm) onConfirm();
    });

    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

/**
 * 모달 숨기기
 */
function hideModal() {
    const modal = document.getElementById('confirmModal');
    if (!modal) return;
    modal.classList.remove('show');
    document.body.style.overflow = '';
}

/**
 * 토스트 알림 표시
 */
function showToast(type, message) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    // 3초 후 자동 제거
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (toast.parentNode) toast.remove();
        }, 300);
    }, 3000);
}

/**
 * 페이지 언로드 시 정리 작업
 */
window.addEventListener('beforeunload', function() {
    document.body.style.overflow = '';
});

/**
 * 키보드 접근성 개선 (체크박스 기능 제거로 단순화)
 */
document.addEventListener('keydown', function(e) {
    // 엔터키로 버튼 활성화
    if (e.key === 'Enter' && e.target.tagName === 'BUTTON') {
        e.target.click();
    }
});

// ==========================================
// 🔥 페이지 가시성 변경 시 데이터 새로고침
// ==========================================
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        console.log('🔄 페이지 활성화 - 데이터 새로고침');
        refreshData();
    }
});

// ==========================================
// 🎯 전역 에러 핸들러
// ==========================================
window.addEventListener('unhandledrejection', function(event) {
    console.error('처리되지 않은 Promise 오류:', event.reason);
    handleApiError(event.reason, '예상치 못한 오류가 발생했습니다.');
});

window.addEventListener('error', function(event) {
    console.error('전역 JavaScript 오류:', event.error);
    showToast('error', '페이지에서 오류가 발생했습니다.');
});

/**
 * 에러 처리 헬퍼 함수
 */
function handleApiError(error, defaultMessage = '오류가 발생했습니다.') {
    console.error('API 오류:', error);

    if (String(error?.message || '').includes('404')) {
        showToast('error', '요청하신 데이터를 찾을 수 없습니다.');
    } else if (String(error?.message || '').includes('403')) {
        showToast('error', '권한이 없습니다.');
    } else if (String(error?.message || '').includes('401')) {
        showToast('error', '로그인이 필요합니다.');
        // window.location.href = '/login';
    } else {
        showToast('error', defaultMessage);
    }
}
function initializeRoomPagination() {
    console.log('📄 방 단위 페이징 시스템 초기화 시작');

    // DOM이 준비되었는지 확인
    const mainWrapper = document.querySelector('.main-wrapper');
    if (!mainWrapper) {
        console.error('❌ .main-wrapper를 찾을 수 없음');
        return;
    }

    // 모든 방 섹션 수집
    collectAllRoomSections();

    console.log(`📊 수집된 방: ${totalItems}개 방, ${totalPages}페이지`);

    if (totalItems > 0) {
        console.log('✅ 방 페이징 시작');
        showRoomPage(1);
        createRoomPaginationControls();
    } else {
        console.log('⚠️ 표시할 방이 없음');
    }
}

/**
 * 모든 방 섹션 수집
 */
function collectAllRoomSections() {
    console.log('🔍 방 섹션 수집 시작');

    const allSections = document.querySelectorAll('.room-section');
    console.log(`📂 찾은 방 섹션 수: ${allSections.length}`);

    allRoomSections = [];

    allSections.forEach((section, index) => {
        const roomTitle = section.querySelector('.room-title')?.textContent || '';
        const roomId = section.getAttribute('data-room-id') || '';
        const requestCount = section.querySelectorAll('.request-item-detailed').length;

        const roomData = {
            element: section,
            roomTitle: roomTitle,
            roomId: roomId,
            requestCount: requestCount,
            index: index
        };

        allRoomSections.push(roomData);
        console.log(`  📂 방 ${index + 1}: "${roomTitle}" (${requestCount}개 요청)`);
    });

    totalItems = allRoomSections.length;
    totalPages = Math.ceil(totalItems / itemsPerPage);

    console.log(`✅ 방 수집 완료: ${totalItems}개 방, ${totalPages}페이지`);
}

/**
 * 특정 페이지의 방들만 표시
 */
function showRoomPage(pageNumber) {
    console.log(`📄 방 페이지 ${pageNumber} 표시 시작`);

    if (allRoomSections.length === 0) {
        console.log('⚠️ 표시할 방이 없음');
        return;
    }

    currentPage = pageNumber;

    // 1단계: 모든 방 섹션 숨기기
    allRoomSections.forEach(roomData => {
        roomData.element.style.display = 'none';
    });

    // 2단계: 현재 페이지에 해당하는 방들 계산
    const startIndex = (pageNumber - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, totalItems);
    currentPageRooms = allRoomSections.slice(startIndex, endIndex);

    console.log(`📄 페이지 ${pageNumber}: ${startIndex}-${endIndex-1} (${currentPageRooms.length}개 방)`);

    // 3단계: 현재 페이지의 방들만 표시
    currentPageRooms.forEach((roomData, index) => {
        roomData.element.style.display = 'block';

        // 🎨 부드러운 애니메이션 효과
        roomData.element.style.opacity = '0';
        roomData.element.style.transform = 'translateY(20px)';

        setTimeout(() => {
            roomData.element.style.transition = 'all 0.3s ease';
            roomData.element.style.opacity = '1';
            roomData.element.style.transform = 'translateY(0)';
        }, index * 150); // 방별로 순차적 애니메이션

        console.log(`  📂 표시: "${roomData.roomTitle}" (${roomData.requestCount}개 요청)`);
    });

    // 4단계: 페이징 컨트롤 업데이트
    updateRoomPaginationButtons();
    updateRoomPageInfo();

    // 5단계: 상단으로 부드러운 스크롤
    scrollToTopSmoothly();

    console.log('✅ 방 페이지 표시 완료');
}

/**
 * 방 페이징 네비게이션 컨트롤 생성
 */
function createRoomPaginationControls() {
    console.log('🎯 방 페이징 컨트롤 생성 시작');

    // 기존 페이징 컨트롤 제거
    const existingPagination = document.querySelector('.room-pagination-controls');
    if (existingPagination) {
        existingPagination.remove();
        console.log('🗑️ 기존 방 페이징 컨트롤 제거');
    }

    if (totalPages <= 1) {
        console.log('⚠️ 페이지가 1개 이하라 페이징 컨트롤 생성 안함');
        return;
    }

    const mainWrapper = document.querySelector('.main-wrapper');
    if (!mainWrapper) return;

    // 🔥 페이지 정보(room-page-info) 제거된 버전
    const paginationHtml = `
        <div class="room-pagination-controls" style="
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
            max-width: 800px;
        ">
            <button class="room-pagination-btn room-prev-btn" onclick="goToPrevRoomPage()" style="
                padding: 10px 20px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 5px;
            ">
                ← 이전
            </button>
            
            <div class="room-page-numbers" style="display: flex; gap: 6px; align-items: center;">
                <!-- 페이지 번호들 -->
            </div>
            
            <button class="room-pagination-btn room-next-btn" onclick="goToNextRoomPage()" style="
                padding: 10px 20px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 5px;
            ">
                다음 →
            </button>
        </div>
    `;

    // 메인 래퍼 다음에 추가
    mainWrapper.insertAdjacentHTML('afterend', paginationHtml);

    // 페이지 번호 생성
    createRoomPageNumbers();

    console.log('✅ 방 페이징 컨트롤 생성 완료');
}

/**
 * 방 페이지 번호 버튼들 생성
 */
function createRoomPageNumbers() {
    const pageNumbersContainer = document.querySelector('.room-page-numbers');
    if (!pageNumbersContainer) return;

    pageNumbersContainer.innerHTML = '';

    // 표시할 페이지 번호 범위 계산 (최대 5개 버튼)
    const maxButtons = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxButtons - 1);

    if (endPage - startPage + 1 < maxButtons) {
        startPage = Math.max(1, endPage - maxButtons + 1);
    }

    // 페이지 번호 버튼 생성
    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        const pageButton = document.createElement('button');
        pageButton.className = `room-page-number-btn ${isActive ? 'active' : ''}`;
        pageButton.textContent = i;
        pageButton.onclick = () => goToRoomPage(i);

        pageButton.style.cssText = `
            padding: 10px 15px;
            border: 1px solid ${isActive ? '#005792' : '#e5e7eb'};
            background: ${isActive ? '#005792' : 'white'};
            color: ${isActive ? 'white' : '#374151'};
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            min-width: 45px;
            font-weight: 600;
            box-shadow: ${isActive ? '0 2px 4px rgba(0, 87, 146, 0.3)' : '0 1px 2px rgba(0, 0, 0, 0.1)'};
        `;

        // 호버 효과
        if (!isActive) {
            pageButton.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f1f5f9';
                this.style.borderColor = '#005792';
                this.style.transform = 'translateY(-1px)';
                this.style.boxShadow = '0 2px 4px rgba(0, 87, 146, 0.2)';
            });

            pageButton.addEventListener('mouseleave', function() {
                this.style.backgroundColor = 'white';
                this.style.borderColor = '#e5e7eb';
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '0 1px 2px rgba(0, 0, 0, 0.1)';
            });
        }

        pageNumbersContainer.appendChild(pageButton);
    }
}

/**
 * 방 페이징 버튼 상태 업데이트
 */
function updateRoomPaginationButtons() {
    const prevBtn = document.querySelector('.room-prev-btn');
    const nextBtn = document.querySelector('.room-next-btn');

    if (prevBtn) {
        prevBtn.disabled = currentPage === 1;
        prevBtn.style.opacity = currentPage === 1 ? '0.5' : '1';
        prevBtn.style.cursor = currentPage === 1 ? 'not-allowed' : 'pointer';
        prevBtn.style.backgroundColor = currentPage === 1 ? '#f9fafb' : 'white';
    }

    if (nextBtn) {
        nextBtn.disabled = currentPage === totalPages;
        nextBtn.style.opacity = currentPage === totalPages ? '0.5' : '1';
        nextBtn.style.cursor = currentPage === totalPages ? 'not-allowed' : 'pointer';
        nextBtn.style.backgroundColor = currentPage === totalPages ? '#f9fafb' : 'white';
    }

    // 페이지 번호 버튼들 다시 생성
    createRoomPageNumbers();
}

/**
 * 방 페이지 정보 업데이트
 */
function updateRoomPageInfo() {
    const pageInfo = document.querySelector('.room-page-info');
    if (!pageInfo) return;

    const startItem = (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min(currentPage * itemsPerPage, totalItems);

    // 현재 페이지 방들의 총 요청 수 계산
    const totalRequestsOnPage = currentPageRooms.reduce((sum, room) => sum + room.requestCount, 0);

    pageInfo.innerHTML = `
        <div style="font-size: 1rem; margin-bottom: 8px;">
            📂 총 <strong style="color: #005792;">${totalItems}</strong>개 방 중 
            <strong style="color: #059669;">${startItem} - ${endItem}</strong>번째 방 표시
        </div>
        <div style="font-size: 0.85rem; color: #64748b;">
            현재 페이지: <strong>${totalRequestsOnPage}</strong>개 입장 요청 
            | 페이지 ${currentPage} / ${totalPages}
        </div>
        <div style="font-size: 0.8rem; color: #9ca3af; margin-top: 8px;">
            ← → 화살표키 또는 1-9 숫자키로 페이지 이동 가능
        </div>
    `;
}

/**
 * 부드러운 스크롤
 */
function scrollToTopSmoothly() {
    const header = document.querySelector('.page-header') || document.querySelector('header') || document.body;
    header.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

// ==========================================
// 🔥 방 페이징 네비게이션 함수들 (전역 함수로 노출)
// ==========================================

/**
 * 특정 방 페이지로 이동
 */
function goToRoomPage(pageNumber) {
    if (pageNumber < 1 || pageNumber > totalPages) return;

    console.log(`🎯 방 페이지 ${pageNumber}로 이동`);
    showRoomPage(pageNumber);
}

/**
 * 이전 방 페이지로 이동
 */
function goToPrevRoomPage() {
    if (currentPage > 1) {
        goToRoomPage(currentPage - 1);
    }
}

/**
 * 다음 방 페이지로 이동
 */
function goToNextRoomPage() {
    if (currentPage < totalPages) {
        goToRoomPage(currentPage + 1);
    }
}

// ==========================================
// 🔥 수정된 refreshData 함수 (방 페이징과 연동)
// ==========================================

async function refreshData() {
    try {
        console.log('🔄 데이터 새로고침 시작');

        // 통계 데이터 새로고침
        const statsData = await fetchRequestStats();
        updateStatsDisplay(statsData);

        // 🔥 올바른 함수 이름으로 수정
        setTimeout(() => {
            collectAllRoomSections();  // ✅ 방 섹션 수집 함수
            if (currentPage > totalPages && totalPages > 0) {
                showRoomPage(totalPages);  // ✅ 방 페이지 표시 함수
            } else {
                showRoomPage(currentPage);  // ✅ 현재 페이지 다시 표시
            }
            updateRoomPaginationButtons();  // ✅ 방 페이징 버튼 업데이트
        }, 100);

        // 알림 배지 업데이트
        updateNotificationBadge();

        console.log('✅ 방 데이터 새로고침 완료');

    } catch (error) {
        console.error('❌ 데이터 새로고침 실패:', error);
    }
}

// ==========================================
// 🔥 수정된 키보드 네비게이션 (방 페이징용)
// ==========================================

// 기존 initializeModals 함수에서 키보드 이벤트 부분을 다음과 같이 수정:

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        hideModal();
    }

    // 페이징 키보드 단축키
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        return; // 입력 필드에서는 단축키 비활성화
    }

    // 모달이 열려있으면 단축키 비활성화
    const visibleModals = document.querySelectorAll('.modal.show');
    if (visibleModals.length > 0) {
        return;
    }

    // 🔥 방 페이징 키보드 단축키
    if (e.key === 'ArrowLeft') {
        e.preventDefault();
        goToPrevRoomPage();
    } else if (e.key === 'ArrowRight') {
        e.preventDefault();
        goToNextRoomPage();
    }

    // 숫자 키로 직접 페이지 이동 (1-9)
    if (e.key >= '1' && e.key <= '9') {
        const pageNum = parseInt(e.key);
        if (pageNum <= totalPages) {
            e.preventDefault();
            goToRoomPage(pageNum);
        }
    }
});

// 🔥 전역 함수 노출
window.goToRoomPage = goToRoomPage;
window.goToPrevRoomPage = goToPrevRoomPage;
window.goToNextRoomPage = goToNextRoomPage;
window.initializeRoomPagination = initializeRoomPagination;

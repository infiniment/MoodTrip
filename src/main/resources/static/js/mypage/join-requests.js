// 🔥 서버 API 기본 URL
const API_BASE_URL = '/api/v1/join-requests';

// 방 입장 요청 관리
document.addEventListener('DOMContentLoaded', function() {
    // UI 초기화
    initializeFilters();
    initializeSearch();
    initializeRequestActions();
    initializeModals();

    // 🔥 페이지 로드 시 데이터 가져오기
    loadInitialData();
});

/**
 * 🔥 초기 데이터 로드 (백엔드 API 호출)
 */
async function loadInitialData() {
    try {
        console.log('🚀 초기 데이터 로드 시작');

        // 통계 데이터와 방 목록을 병렬로 가져오기
        const [statsData, roomsData] = await Promise.all([
            fetchRequestStats(),
            fetchMyRoomsWithRequests()
        ]);

        // UI 업데이트
        updateStatsDisplay(statsData);
        renderRoomsData(roomsData);
        updateNotificationBadge();

        console.log('✅ 초기 데이터 로드 완료');

    } catch (error) {
        console.error('❌ 초기 데이터 로드 실패:', error);
        showToast('error', '데이터를 불러오는 중 오류가 발생했습니다.');
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

/**
 * 모달 초기화
 */
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

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') hideModal();
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
async function refreshData() {
    try {
        console.log('🔄 데이터 새로고침 시작');

        // 통계 데이터 새로고침
        const statsData = await fetchRequestStats();
        updateStatsDisplay(statsData);

        // 알림 배지/대기 건수/빈 섹션 업데이트
        updateNotificationBadge();
        updateWaitingCounts();
        checkEmptySections();

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

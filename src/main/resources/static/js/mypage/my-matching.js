let currentPage = 1;
const itemsPerPage = 3; // 매칭 카드는 크므로 3개씩 표시
let totalItems = 0;
let totalPages = 0;
let currentTabData = []; // 현재 탭의 데이터를 저장
let activeTab = 'received'; // 현재 활성 탭

document.addEventListener('DOMContentLoaded', function() {
    initializeTabs();
    initializeButtons();
    initializeModals();
    checkAndDisableLeaderButtons();

    // 🔥 새로 추가: 페이징 초기화
    initializePagination();
    checkRoomNotification();
});

function initializePagination() {
    console.log('📄 매칭 정보 페이징 시스템 초기화 시작');

    // 현재 활성 탭 확인
    const activeTabElement = document.querySelector('.tab-content.active');
    if (!activeTabElement) return;

    // 탭 ID로 activeTab 설정
    activeTab = activeTabElement.id.includes('received') ? 'received' : 'created';

    // 현재 탭의 모든 매칭 아이템 가져오기
    const allItems = activeTabElement.querySelectorAll('.matching-item');
    currentTabData = Array.from(allItems);
    totalItems = currentTabData.length;
    totalPages = Math.ceil(totalItems / itemsPerPage);

    console.log(`📊 ${activeTab} 탭: 총 ${totalItems}개 방, ${totalPages}페이지`);

    if (totalItems > 0) {
        // 첫 페이지 표시
        showPage(1);

        // 페이징 네비게이션 생성
        createPaginationControls();

        // 키보드 네비게이션 활성화
        enableKeyboardNavigation();
    }
}

/**
 * 방 입장 승인/거절 알림 체크
 */
function checkRoomNotification() {
    // Thymeleaf에서 전달된 알림 데이터 확인
    if (window.notificationData) {
        console.log('알림 데이터 발견:', window.notificationData);
        showRoomStatusModal(window.notificationData);
        return;
    }

    // localStorage 방식으로도 체크 (다른 탭에서 승인/거절된 경우)
    const savedNotification = localStorage.getItem('roomStatusNotification');
    if (savedNotification) {
        try {
            const notificationData = JSON.parse(savedNotification);

            // 5분 이내의 알림만 표시
            const notificationTime = new Date(notificationData.timestamp);
            const now = new Date();
            const diffMinutes = (now - notificationTime) / (1000 * 60);

            if (diffMinutes <= 5) {
                showRoomStatusModal(notificationData);
            }

            // 표시 후 제거
            localStorage.removeItem('roomStatusNotification');
        } catch (error) {
            console.error('알림 데이터 파싱 오류:', error);
            localStorage.removeItem('roomStatusNotification');
        }
    }
}

/**
 * 방 상태 모달 표시
 */
function showRoomStatusModal(notificationData) {
    const modal = document.getElementById('roomStatusModal');
    if (!modal || !notificationData) return;

    const titleElement = modal.querySelector('#roomStatusTitle');
    const messageElement = modal.querySelector('#roomStatusMessage');
    const approvedIcon = modal.querySelector('#approvedIcon');
    const rejectedIcon = modal.querySelector('#rejectedIcon');

    // 알림 타입에 따라 모달 내용 설정
    if (notificationData.type === 'ROOM_APPROVED') {
        titleElement.textContent = '🎉 방 입장 승인';
        titleElement.style.color = '#10b981';
        messageElement.textContent = notificationData.message;
        approvedIcon.style.display = 'block';
        rejectedIcon.style.display = 'none';

        // 모달 테두리 색상도 초록색으로
        modal.querySelector('.modal-content').style.borderTop = '4px solid #10b981';

    } else if (notificationData.type === 'ROOM_REJECTED') {
        titleElement.textContent = '😔 방 입장 거절';
        titleElement.style.color = '#ef4444';
        messageElement.textContent = notificationData.message;
        approvedIcon.style.display = 'none';
        rejectedIcon.style.display = 'block';

        // 모달 테두리 색상도 빨간색으로
        modal.querySelector('.modal-content').style.borderTop = '4px solid #ef4444';
    }

    // 모달 표시
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    console.log('방 상태 알림 모달 표시:', notificationData.type);
}

window.closeRoomStatusModal = closeRoomStatusModal;

/**
 * 방 상태 모달 닫기
 */
function closeRoomStatusModal() {
    const modal = document.getElementById('roomStatusModal');
    if (!modal) return;

    modal.style.display = 'none';
    document.body.style.overflow = '';

    // 알림 확인 후 페이지 새로고침 (최신 상태 반영)
    setTimeout(() => {
        window.location.reload();
    }, 500);
}

/**
 * 특정 페이지의 매칭 아이템들만 표시
 */
function showPage(pageNumber) {
    console.log(`📄 ${activeTab} 탭 ${pageNumber}페이지 표시`);

    currentPage = pageNumber;

    // 모든 매칭 아이템 숨기기
    currentTabData.forEach(item => {
        item.style.display = 'none';
    });

    // 현재 페이지에 해당하는 아이템들만 표시
    const startIndex = (pageNumber - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, totalItems);

    for (let i = startIndex; i < endIndex; i++) {
        if (currentTabData[i]) {
            currentTabData[i].style.display = 'block';

            // 🎨 부드러운 애니메이션 효과
            currentTabData[i].style.opacity = '0';
            currentTabData[i].style.transform = 'translateY(20px)';

            setTimeout(() => {
                currentTabData[i].style.transition = 'all 0.3s ease';
                currentTabData[i].style.opacity = '1';
                currentTabData[i].style.transform = 'translateY(0)';
            }, i * 50); // 순차적으로 나타나는 효과
        }
    }

    // 페이징 버튼 상태 업데이트
    updatePaginationButtons();

    // 현재 페이지 정보 표시
    updatePageInfo();
}

/**
 * 페이징 네비게이션 컨트롤 생성
 */
function createPaginationControls() {
    const activeTabElement = document.querySelector('.tab-content.active');
    if (!activeTabElement) return;

    // 기존 페이징 컨트롤 제거
    const existingPagination = activeTabElement.querySelector('.pagination-controls');
    if (existingPagination) {
        existingPagination.remove();
    }

    // 페이징이 필요없으면 (총 페이지가 1개 이하) 생성하지 않음
    if (totalPages <= 1) return;

    const paginationHtml = `
        <div class="pagination-controls" style="
            display: flex; 
            justify-content: center; 
            align-items: center; 
            gap: 8px; 
            margin: 2rem 0; 
            padding: 1rem;
        ">
            <button class="pagination-btn prev-btn" onclick="goToPrevPage()" style="
                padding: 8px 16px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 6px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 500;
            ">
                ‹ 이전
            </button>
            
            <div class="page-numbers" style="display: flex; gap: 4px;">
                <!-- 페이지 번호들이 여기에 동적으로 생성됩니다 -->
            </div>
            
            <button class="pagination-btn next-btn" onclick="goToNextPage()" style="
                padding: 8px 16px;
                border: 1px solid #e5e7eb;
                background: white;
                border-radius: 6px;
                cursor: pointer;
                transition: all 0.2s;
                color: #374151;
                font-weight: 500;
            ">
                다음 ›
            </button>
        </div>
        
    `;

    // 매칭 리스트 래퍼 다음에 페이징 컨트롤 추가
    const matchingWrapper = activeTabElement.querySelector('.matching-list-wrapper');
    if (matchingWrapper) {
        matchingWrapper.insertAdjacentHTML('afterend', paginationHtml);

        // 호버 효과 추가
        addHoverEffects();
    }

    // 페이지 번호 버튼들 생성
    createPageNumbers();

    console.log('🎯 페이징 컨트롤 생성 완료');
}

/**
 * 호버 효과 추가
 */
function addHoverEffects() {
    const paginationButtons = document.querySelectorAll('.pagination-btn');

    paginationButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            if (!this.disabled && !this.classList.contains('active')) {
                this.style.backgroundColor = '#f3f4f6';
                this.style.borderColor = '#005792';
                this.style.transform = 'translateY(-1px)';
            }
        });

        button.addEventListener('mouseleave', function() {
            if (!this.disabled && !this.classList.contains('active')) {
                this.style.backgroundColor = 'white';
                this.style.borderColor = '#e5e7eb';
                this.style.transform = 'translateY(0)';
            }
        });
    });
}

/**
 * 페이지 번호 버튼들 생성
 */
function createPageNumbers() {
    const pageNumbersContainer = document.querySelector('.page-numbers');
    if (!pageNumbersContainer) return;

    pageNumbersContainer.innerHTML = '';

    // 표시할 페이지 번호 범위 계산 (최대 5개 버튼)
    const maxButtons = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxButtons - 1);

    // 끝 페이지가 조정되면 시작 페이지도 다시 조정
    if (endPage - startPage + 1 < maxButtons) {
        startPage = Math.max(1, endPage - maxButtons + 1);
    }

    // 페이지 번호 버튼 생성
    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        const pageButton = document.createElement('button');
        pageButton.className = `page-number-btn ${isActive ? 'active' : ''}`;
        pageButton.textContent = i;
        pageButton.onclick = () => goToPage(i);

        pageButton.style.cssText = `
            padding: 8px 12px;
            border: 1px solid ${isActive ? '#005792' : '#e5e7eb'};
            background: ${isActive ? '#005792' : 'white'};
            color: ${isActive ? 'white' : '#374151'};
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
            min-width: 40px;
            font-weight: 500;
        `;

        // 호버 효과
        if (!isActive) {
            pageButton.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f3f4f6';
                this.style.borderColor = '#005792';
            });

            pageButton.addEventListener('mouseleave', function() {
                this.style.backgroundColor = 'white';
                this.style.borderColor = '#e5e7eb';
            });
        }

        pageNumbersContainer.appendChild(pageButton);
    }
}

/**
 * 페이징 버튼 상태 업데이트
 */
function updatePaginationButtons() {
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');

    if (prevBtn) {
        prevBtn.disabled = currentPage === 1;
        prevBtn.style.opacity = currentPage === 1 ? '0.5' : '1';
        prevBtn.style.cursor = currentPage === 1 ? 'not-allowed' : 'pointer';
    }

    if (nextBtn) {
        nextBtn.disabled = currentPage === totalPages;
        nextBtn.style.opacity = currentPage === totalPages ? '0.5' : '1';
        nextBtn.style.cursor = currentPage === totalPages ? 'not-allowed' : 'pointer';
    }

    // 페이지 번호 버튼들 다시 생성 (활성 상태 반영)
    createPageNumbers();
}

/**
 * 현재 페이지 정보 업데이트
 */
function updatePageInfo() {
    const pageInfo = document.querySelector('.page-info');
    if (!pageInfo) return;

    const startItem = (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min(currentPage * itemsPerPage, totalItems);

    const tabName = activeTab === 'received' ? '입장한 방' : '만든 방';

    pageInfo.innerHTML = `
        총 <strong>${totalItems}</strong>개 ${tabName} 중 
        <strong>${startItem} - ${endItem}</strong>번째 표시 
        (${currentPage} / ${totalPages} 페이지)
    `;
}

// ==========================================
// 🔥 페이징 네비게이션 함수들 (전역 함수로 노출)
// ==========================================

/**
 * 특정 페이지로 이동
 */
function goToPage(pageNumber) {
    if (pageNumber < 1 || pageNumber > totalPages) return;

    console.log(`🎯 ${activeTab} 탭 ${pageNumber}페이지로 이동`);
    showPage(pageNumber);

    // 부드러운 스크롤 효과
    const activeTabElement = document.querySelector('.tab-content.active');
    if (activeTabElement) {
        activeTabElement.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

/**
 * 이전 페이지로 이동
 */
function goToPrevPage() {
    if (currentPage > 1) {
        goToPage(currentPage - 1);
    }
}

/**
 * 다음 페이지로 이동
 */
function goToNextPage() {
    if (currentPage < totalPages) {
        goToPage(currentPage + 1);
    }
}

/**
 * 키보드 네비게이션 활성화
 */
function enableKeyboardNavigation() {
    // 기존 키보드 이벤트 핸들러에 페이징 기능 추가
    document.addEventListener('keydown', function(e) {
        // 입력 필드에서는 단축키 비활성화
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
            return;
        }

        // 모달이 열려있으면 단축키 비활성화
        const visibleModals = document.querySelectorAll('.modal[style*="flex"]');
        if (visibleModals.length > 0) {
            return;
        }

        // 페이징 키보드 단축키
        if (e.key === 'ArrowLeft') {
            e.preventDefault();
            goToPrevPage();
        } else if (e.key === 'ArrowRight') {
            e.preventDefault();
            goToNextPage();
        }

        // 숫자 키로 직접 페이지 이동 (1-9)
        if (e.key >= '1' && e.key <= '9') {
            const pageNum = parseInt(e.key);
            if (pageNum <= totalPages) {
                e.preventDefault();
                goToPage(pageNum);
            }
        }
    });

    console.log('⌨️ 키보드 네비게이션 활성화 (← → 화살표, 1-9 숫자키)');
}

// ==========================================
// 🔄 기존 탭 전환 함수 수정 (페이징 초기화 추가)
// ==========================================

// 기존 initializeTabs 함수 수정
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.textContent.trim();

            if (targetTab === '내가 입장한 방') {
                window.location.href = '/mypage/my-matching?tab=received';
            } else if (targetTab === '내가 만든 방') {
                window.location.href = '/mypage/my-matching?tab=created';
            }
        });
    });

    // 🔥 페이지 로드 후 1초 뒤에 페이징 초기화 (DOM이 완전히 로드된 후)
    setTimeout(() => {
        initializePagination();
    }, 1000);
}

// ==========================================
// 🌐 전역 함수 노출 (HTML에서 직접 호출용) - 기존에 추가
// ==========================================
window.handleChatButtonClick = handleChatButtonClick;
window.approveRequest = approveRequest;
window.rejectRequest = rejectRequest;
window.checkAndDisableLeaderButtons = checkAndDisableLeaderButtons;

// 🔥 새로 추가: 페이징 관련 전역 함수들
window.goToPage = goToPage;
window.goToPrevPage = goToPrevPage;
window.goToNextPage = goToNextPage;
window.initializePagination = initializePagination;

// SSR 방식 사용 시 타임리프 사용을 위한 버튼 초기화
function initializeButtons() {
    document.addEventListener('click', function(e) {

        // 방 나가기 버튼
        if (e.target.matches('.btn-exit-room')) {
            if (e.target.disabled) {
                const userRole = e.target.getAttribute('data-user-role') ||
                    e.target.closest('.matching-item')?.getAttribute('data-user-role');

                if (userRole === 'LEADER') {
                    showNotification('info', '방장은 방을 나갈 수 없습니다. 방 삭제를 이용해주세요.');
                } else {
                    showNotification('error', '현재 이 기능을 사용할 수 없습니다.');
                }
                return;
            }

            const matchingItem = e.target.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const roomId = e.target.getAttribute('data-room-id');

            handleExitRoomClick(roomTitle, roomId, matchingItem);
        }

        // 방 삭제 버튼
        if (e.target.matches('.btn-delete-room')) {
            const matchingItem = e.target.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const roomId = e.target.getAttribute('data-room-id');

            handleDeleteRoomClick(roomTitle, roomId, matchingItem);
        }

        // 입장 요청 관리 버튼
        if (e.target.matches('.btn-manage-requests')) {
            const roomId = e.target.getAttribute('data-room-id');
            const matchingItem = e.target.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;

            handleManageRequestsClick(roomId, roomTitle);
        }

        // 스케줄 짜기 버튼
        if (e.target.matches('.btn-chat')) {
            const matchingItem = e.target.closest('.matching-item');
            const roomId = matchingItem.getAttribute('data-room-id');

            if (roomId) {
                window.location.href = `/scheduling/${roomId}`;
            } else {
                showNotification('error', '방 정보를 찾을 수 없습니다.', 'under-header');
            }
        }

        if (e.target.matches('.btn-report-room')) {
            const matchingItem = e.target.closest('.matching-item');
            const roomId = matchingItem.getAttribute('data-room-id');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;

            handleReportRoomClick(roomId, roomTitle);
        }

    });
}

// 입장한 방과 만든 방 탭 전환 시 사용
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.textContent.trim();

            if (targetTab === '내가 입장한 방') {
                window.location.href = '/mypage/my-matching?tab=received';
            } else if (targetTab === '내가 만든 방') {
                window.location.href = '/mypage/my-matching?tab=created';
            }
        });
    });
}

/**
 * 🔥 새로 추가: 방장인 경우 나가기 버튼 비활성화
 */
function checkAndDisableLeaderButtons() {
    console.log('🔍 방장 권한 체크 시작');

    const exitButtons = document.querySelectorAll('.btn-exit-room');

    exitButtons.forEach(button => {
        const matchingItem = button.closest('.matching-item');

        // HTML에서 역할 정보 가져오기 (data-user-role 속성으로)
        const userRole = matchingItem?.getAttribute('data-user-role') ||
            button.getAttribute('data-user-role');

        if (userRole === 'LEADER') {
            console.log('🚫 방장 계정 - 나가기 버튼 비활성화');

            // 버튼 비활성화
            button.disabled = true;

            // 텍스트 변경
            button.innerHTML = `
                <svg width="16" height="16" fill="currentColor" class="opacity-50">
                    <path d="M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2Z"/>
                </svg>
                방장은 나가기 불가
            `;

            // CSS 클래스 추가
            button.classList.add('btn-disabled');
            button.classList.remove('btn-secondary');

            // 툴팁 추가
            button.title = '방장은 방을 나갈 수 없습니다. 방 삭제를 이용해주세요.';

            // 커서 스타일 변경
            button.style.cursor = 'not-allowed';

            console.log('✅ 방장 나가기 버튼 비활성화 완료');
        } else {
            console.log('👤 일반 멤버 - 나가기 버튼 활성 상태 유지');
        }
    });

    // 🔥 추가: 방장인 경우 삭제 버튼에 특별한 스타일 적용
    const deleteButtons = document.querySelectorAll('.btn-delete-room');
    deleteButtons.forEach(button => {
        const matchingItem = button.closest('.matching-item');
        const userRole = matchingItem?.getAttribute('data-user-role');

        if (userRole === 'LEADER') {
            // 방장임을 표시하는 배지 추가
            if (!button.querySelector('.leader-badge')) {
                const badge = document.createElement('span');
                badge.className = 'leader-badge';
                badge.textContent = '방장';
                badge.style.cssText = `
                    background: #059669;
                    color: white;
                    font-size: 0.75rem;
                    padding: 2px 6px;
                    border-radius: 4px;
                    margin-left: 0.5rem;
                `;
                button.appendChild(badge);
            }
        }
    });

    console.log('🎯 방장 권한 체크 완료');
}

// 스케줄짜기 버튼 클릭 시
function handleChatButtonClick(roomId, roomTitle, hostName) {
    console.log(`💬 스케줄 버튼 클릭 - 방: ${roomTitle}, 방장: ${hostName}, ID: ${roomId}`);
    window.location.href = `/scheduling/${roomId}`;
}

// 방 나가기 api 호출
async function exitRoomApi(roomTitle, roomId, matchingItem) {
    console.log(`🚪 방 나가기 API 호출 - 방ID: ${roomId}`);

    try {
        matchingItem.style.opacity = '0.5';
        matchingItem.style.pointerEvents = 'none';

        const response = await fetch(`/api/v1/mypage/rooms/${roomId}/leave`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!response.ok) {
            // 서버에서 방장 나가기 시도 시 에러 메시지 처리
            const errorText = await response.text();
            if (response.status === 400 && errorText.includes('방장')) {
                throw new Error('방장은 방을 나갈 수 없습니다. 방 삭제를 이용해주세요.');
            }
            throw new Error('방 나가기에 실패했습니다.');
        }

        // 🔥 새로 추가: 다른 탭에 방 데이터 업데이트 알림
        const updateData = {
            type: 'MEMBER_LEFT',
            roomId: roomId,
            roomTitle: roomTitle,
            timestamp: Date.now()
        };
        localStorage.setItem('roomDataUpdate', JSON.stringify(updateData));
        console.log('📢 다른 탭에 방 나가기 알림 전송:', updateData);

        showNotification('success', `"${roomTitle}" 방에서 나갔습니다.`);

        setTimeout(() => {
            window.location.reload();
        }, 1500);

    } catch (error) {
        matchingItem.style.opacity = '1';
        matchingItem.style.pointerEvents = 'auto';
        showNotification('error', error.message || '방 나가기에 실패했습니다. 다시 시도해주세요.');
    }
}

// 방 나가기 확인용 모달
function handleExitRoomClick(roomTitle, roomId, matchingItem) {
    showModal('exitRoomModal', {
        title: roomTitle,
        onConfirm: () => exitRoomApi(roomTitle, roomId, matchingItem)
    });
}

async function deleteRoomApi(roomTitle, roomId, matchingItem) {
    console.log(`🗑️ 방 삭제 API 호출 - 방ID: ${roomId}`);

    try {
        matchingItem.style.opacity = '0.5';
        matchingItem.style.pointerEvents = 'none';

        const response = await fetch(`/api/v1/mypage/rooms/${roomId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('서버 응답:', errorText);
            throw new Error(`방 삭제 실패: ${response.status}`);
        }

        showNotification('success', `"${roomTitle}" 방이 삭제되었습니다.`);
        setTimeout(() => {
            window.location.reload();
        }, 1500);

    } catch (error) {
        console.error('방 삭제 에러:', error);
        matchingItem.style.opacity = '1';
        matchingItem.style.pointerEvents = 'auto';
        showNotification('error', '방 삭제에 실패했습니다. 다시 시도해주세요.');
    }
}

// 방 삭제 시 모달 확인용
function handleDeleteRoomClick(roomTitle, roomId, matchingItem) {
    showModal('deleteRoomModal', {
        title: roomTitle,
        onConfirm: () => deleteRoomApi(roomTitle, roomId, matchingItem)
    });
}

// ==========================================
// 🔥 입장 요청 관리 기능
// ==========================================

/**
 * 🎛️ 입장 요청 관리 모달 - 실제 API 호출로 업데이트
 */
async function handleManageRequestsClick(roomId, roomTitle) {
    console.log(`🎛️ 입장 요청 관리 클릭 - 방ID: ${roomId}, 방제목: ${roomTitle}`);

    try {
        // 🔥 실제 API 호출로 해당 방의 신청 목록 가져오기
        const response = await fetch(`/api/v1/join-requests/rooms/${roomId}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('신청 목록 조회 실패');
        }

        const requests = await response.json();
        console.log('📋 신청 목록:', requests);

        // 모달에 실제 데이터 표시
        showRequestsModal(roomId, roomTitle, requests);

    } catch (error) {
        console.error('신청 목록 조회 에러:', error);
        showNotification('error', '입장 요청을 불러올 수 없습니다.');
    }
}

/**
 * 🔥 업데이트된 입장 요청 관리 모달 표시
 */
function showRequestsModal(roomId, roomTitle, requests) {
    const modal = document.getElementById('manageRequestsModal');
    if (!modal) return;

    const requestsList = modal.querySelector('.requests-list');

    // 모달 제목 업데이트
    modal.querySelector('.modal-header h3').textContent = `${roomTitle} - 입장 요청 관리`;

    if (requests.length === 0) {
        // 요청 없을 때
        requestsList.innerHTML = `
        <div class="empty-requests" style="text-align: center; padding: 2rem; color: #64748b;">
            <h4>대기 중인 요청이 없습니다</h4>
            <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
        </div>
    `;
    } else {
        // ✅ 요청 있을 때: 안내 문구 + 확인 버튼
        requestsList.innerHTML = `
        <div class="request-summary" style="text-align: center; padding: 2rem;">
            <h4>📢 ${requests.length}건의 신청이 있습니다.</h4>
            <button class="btn btn-primary btn-go-requests" style="margin-top: 1rem;">
                확인하러 가기
            </button>
        </div>
    `;

        // 버튼 클릭 시 join-requests 페이지로 이동
        const goBtn = requestsList.querySelector('.btn-go-requests');
        goBtn.addEventListener('click', () => {
            window.location.href = '/mypage/join-requests';
        });
    }


    // 모달 표시
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

async function approveRequest(requestId, applicantName) {
    console.log(`✅ 신청 승인 - requestId: ${requestId}, 신청자: ${applicantName}`);

    try {
        const response = await fetch(`/api/v1/join-requests/${requestId}/approve`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('승인 처리 실패');
        }

        const result = await response.json();

        if (result.success) {
            showNotification('success', `${applicantName}님의 입장을 승인했습니다.`);

            // 해당 요청 항목을 UI에서 제거
            const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
            if (requestItem) {
                requestItem.style.opacity = '0.5';
                requestItem.innerHTML = `
                    <div style="text-align: center; padding: 1rem; color: #10b981;">
                        ✅ ${applicantName}님의 입장이 승인되었습니다.
                    </div>
                `;

                // 2초 후 요청 항목 제거
                setTimeout(() => {
                    requestItem.remove();

                    // 더 이상 요청이 없으면 빈 상태 표시
                    const remainingRequests = document.querySelectorAll('.request-item');
                    if (remainingRequests.length === 0) {
                        const requestsList = document.querySelector('.requests-list');
                        requestsList.innerHTML = `
                            <div class="empty-requests" style="text-align: center; padding: 2rem; color: #64748b;">
                                <h4>모든 요청을 처리했습니다!</h4>
                                <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                            </div>
                        `;
                    }
                }, 2000);
            }
        } else {
            showNotification('error', result.message || '승인 처리에 실패했습니다.');
        }

    } catch (error) {
        console.error('승인 처리 에러:', error);
        showNotification('error', '승인 처리 중 오류가 발생했습니다.');
    }
}

/**
 * 🎯 신청 거절 처리
 */
async function rejectRequest(requestId, applicantName) {
    console.log(`❌ 신청 거절 - requestId: ${requestId}, 신청자: ${applicantName}`);

    try {
        const response = await fetch(`/api/v1/join-requests/${requestId}/reject`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('거절 처리 실패');
        }

        const result = await response.json();

        if (result.success) {
            showNotification('info', `${applicantName}님의 입장을 거절했습니다.`);

            // 해당 요청 항목을 UI에서 제거
            const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
            if (requestItem) {
                requestItem.style.opacity = '0.5';
                requestItem.innerHTML = `
                    <div style="text-align: center; padding: 1rem; color: #dc2626;">
                        ❌ ${applicantName}님의 입장이 거절되었습니다.
                    </div>
                `;

                // 2초 후 요청 항목 제거
                setTimeout(() => {
                    requestItem.remove();

                    // 더 이상 요청이 없으면 빈 상태 표시
                    const remainingRequests = document.querySelectorAll('.request-item');
                    if (remainingRequests.length === 0) {
                        const requestsList = document.querySelector('.requests-list');
                        requestsList.innerHTML = `
                            <div class="empty-requests" style="text-align: center; padding: 2rem; color: #64748b;">
                                <h4>모든 요청을 처리했습니다!</h4>
                                <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                            </div>
                        `;
                    }
                }, 2000);
            }
        } else {
            showNotification('error', result.message || '거절 처리에 실패했습니다.');
        }

    } catch (error) {
        console.error('거절 처리 에러:', error);
        showNotification('error', '거절 처리 중 오류가 발생했습니다.');
    }
}

// ==========================================
// 🎨 UI 유틸리티 함수들
// ==========================================

function showNotification(type, message) {
    const container = document.getElementById('notification-container');
    if (!container) return;

    const existingNotification = container.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    const style = document.createElement('style');
    style.textContent = `
        .notification {
            margin: 55px auto;
            padding: 16px 24px;
            border-radius: 12px;
            color: white;
            font-weight: 600;
            max-width: 500px;
            z-index: 1001;
            animation: slideDown 0.3s ease, slideUp 0.3s ease 2.7s;
            box-shadow: 0 8px 24px rgba(0, 26, 44, 0.15);
            backdrop-filter: blur(10px);
            text-align: center;
        }

        .notification-success {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }

        .notification-error {
            background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
        }

        .notification-info {
            background: linear-gradient(135deg, #005792 0%, #001A2C 100%);
        }

        .btn-disabled {
            background-color: #e5e7eb !important;
            color: #9ca3af !important;
            cursor: not-allowed !important;
            opacity: 0.6 !important;
            border-color: #d1d5db !important;
        }

        .btn-disabled:hover {
            background-color: #e5e7eb !important;
            color: #9ca3af !important;
            transform: none !important;
            box-shadow: none !important;
        }

        @keyframes slideDown {
            from { transform: translateY(-20px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }

        @keyframes slideUp {
            from { transform: translateY(0); opacity: 1; }
            to { transform: translateY(-20px); opacity: 0; }
        }
    `;

    document.head.appendChild(style);
    container.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
            style.remove();
        }
    }, 3000);
}

// ==========================================
// 🎭 모달 관리
// ==========================================

/**
 * 모달 초기화
 */
function initializeModals() {
    // 모달 닫기 버튼 이벤트
    document.addEventListener('click', function(e) {
        if (e.target.matches('.modal-close') || e.target.matches('.btn-cancel')) {
            const modal = e.target.closest('.modal');
            if (modal) {
                hideModal(modal.id);
            }
        }

        // 모달 배경 클릭 시 닫기
        if (e.target.matches('.modal')) {
            hideModal(e.target.id);
        }
    });
}

/**
 * 모달 표시
 */
function showModal(modalId, options = {}) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    // 확인 버튼 이벤트 설정
    const confirmBtn = modal.querySelector('.btn-confirm');
    if (confirmBtn && options.onConfirm) {
        // 기존 이벤트 리스너 제거
        confirmBtn.replaceWith(confirmBtn.cloneNode(true));
        const newConfirmBtn = modal.querySelector('.btn-confirm');

        newConfirmBtn.addEventListener('click', function() {
            hideModal(modalId);
            options.onConfirm();
        });
    }

    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    console.log('📱 모달 열림:', modalId);
}

/**
 * 모달 숨기기
 */
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    modal.style.display = 'none';
    document.body.style.overflow = '';

    console.log('📱 모달 닫힘:', modalId);
}

// ==========================================
// 🌐 전역 함수 노출 (HTML에서 직접 호출용)
// ==========================================
window.handleChatButtonClick = handleChatButtonClick;
window.approveRequest = approveRequest;
window.rejectRequest = rejectRequest;
window.checkAndDisableLeaderButtons = checkAndDisableLeaderButtons; // 🔥 새로 추가

// ==========================================
// 🎯 키보드 단축키
// ==========================================
document.addEventListener('keydown', function(e) {
    // ESC 키로 모달 닫기
    if (e.key === 'Escape') {
        const visibleModals = document.querySelectorAll('.modal[style*="flex"]');
        visibleModals.forEach(modal => {
            hideModal(modal.id);
        });
    }

    // F5 키로 페이지 새로고침 (SSR 방식)
    if (e.key === 'F5') {
        console.log('🔄 페이지 새로고침');
    }
});

/**
 * 신고하기 버튼 클릭 시
 */
async function handleReportRoomClick(roomId, roomTitle) {
    console.log(`🚨 신고하기 클릭 - 방ID: ${roomId}, 방제목: ${roomTitle}`);

    try {
        // ✅ 방 멤버 조회 API 호출
        const response = await fetch(`/api/v1/room-members/${roomId}/members`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('방 멤버 조회 실패');
        }

        const members = await response.json();
        console.log('👥 방 멤버:', members);

        // 모달에 멤버 리스트 표시
        showReportModal(roomId, roomTitle, members);

    } catch (error) {
        console.error('신고 모달 오류:', error);
        showNotification('error', '멤버 목록을 불러올 수 없습니다.');
    }
}

/**
 * 신고 모달 표시
 */
function showReportModal(roomId, roomTitle, members) {
    const modal = document.getElementById('reportRoomModal');
    if (!modal) return;

    const memberList = modal.querySelector('#reportMemberList');
    modal.querySelector('.modal-header h3').textContent = `${roomTitle} - 신고하기`;

    if (!members || members.length === 0) {
        memberList.innerHTML = `
            <div style="text-align:center; padding:1rem; color:#64748b;">
                이 방에는 멤버가 없습니다.
            </div>`;
    } else {
        // ✅ 멤버 리스트 + 신고 사유 선택 + 상세 입력
        memberList.innerHTML = `
            <ul class="report-member-list">
                ${members.map(m => `
                    <li style="margin-bottom:8px;">
                        <label style="display:flex; align-items:center; gap:8px;">
                            <input type="radio" name="reportMember" value="${m.nickname}">
                            <span>${m.nickname} (${m.role})</span>
                        </label>
                    </li>
                `).join('')}
            </ul>

            <div class="report-reason-wrapper" style="margin-top:1rem;">
                <label for="reportReasonSelect" style="display:block; font-weight:500; margin-bottom:6px;">
                    신고 사유
                </label>
                <select id="reportReasonSelect" style="width:100%; padding:8px; border-radius:6px; border:1px solid #d1d5db;">
                    <option value="">-- 선택하세요 --</option>
                    <option value="SPAM">스팸/광고</option>
                    <option value="INAPPROPRIATE">부적절한 내용</option>
                    <option value="FRAUD">사기/허위정보</option>
                    <option value="HARASSMENT">괴롭힘/혐오발언</option>
                    <option value="OTHER">기타</option>
                </select>
            </div>

            <div class="report-message-wrapper" style="margin-top:1rem;">
                <label for="reportMessageTextarea" style="display:block; font-weight:500; margin-bottom:6px;">
                    상세 사유 (선택사항)
                </label>
                <textarea id="reportMessageTextarea" placeholder="상세 사유를 입력하세요"
                          style="width:100%; min-height:80px; padding:8px; border-radius:6px;
                                 border:1px solid #d1d5db; resize: vertical;"></textarea>
            </div>
        `;
    }

    const confirmBtn = modal.querySelector('#submitReportBtn');
    confirmBtn.onclick = async () => {
        const selected = modal.querySelector("input[name='reportMember']:checked");
        if (!selected) {
            showNotification('info', '신고 대상을 선택하세요.');
            return;
        }
        const reportedNickname = selected.value;
        const reason = modal.querySelector("#reportReasonSelect")?.value.toLowerCase();
        const message = modal.querySelector("#reportMessageTextarea")?.value.trim();

        if (!reason) {
            showNotification('info', '신고 사유를 선택하세요.');
            return;
        }

        try {
            const res = await fetch(`/api/v1/fires/rooms/${roomId}/members`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    reportedNickname: reportedNickname,
                    reportReason: reason.toUpperCase(),
                    reportMessage: message
                })
            });

            // 🔥 400 에러라도 정상 처리 (에러로 던지지 않음)
            const responseData = await res.json();

            if (responseData.success) {
                // 성공 케이스
                showNotification('success', '신고가 접수되었습니다.');
                hideModal('reportRoomModal');
            } else {
                // 실패 케이스 - 에러 메시지에 따라 알림 타입 구분
                if (responseData.message && responseData.message.includes('자신을 신고할 수 없습니다')) {
                    showNotification('error', '자기 자신은 신고할 수 없습니다.'); // 🔥 빨간색 error 타입
                } else {
                    showNotification('warning', responseData.message || '신고 처리 중 문제가 발생했습니다.');
                }
            }

        } catch (err) {
            // 🔥 네트워크 에러나 JSON 파싱 에러만 여기서 처리
            console.error('신고 요청 실패:', err);
            showNotification('error', '네트워크 오류가 발생했습니다.');
        }
    };

    // 모달 열기
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

console.log('🎉 MoodTrip 매칭 정보 페이지 JavaScript 로드 완료! (방장 권한 체크 포함)');
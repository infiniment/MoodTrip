// 매칭 정보 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 탭 기능 초기화
    initializeTabs();
    
    // 버튼 이벤트 초기화
    initializeButtons();
    
    // 페이지네이션 초기화
    initializePagination();
    
    // 모달 이벤트 초기화
    initializeModals();
    
    // 알림 배지 업데이트
    updateNotificationBadge();
    
    // 실시간 요청 개수 업데이트 (예시)
    updatePendingRequestsCount();
});

/**
 * 탭 기능 초기화
 */
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            // 모든 탭 버튼에서 active 클래스 제거
            tabButtons.forEach(btn => btn.classList.remove('active'));
            
            // 클릭된 탭 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 모든 탭 콘텐츠 숨기기
            tabContents.forEach(content => {
                content.classList.remove('active');
            });
            
            // 선택된 탭 콘텐츠 보이기
            const targetContent = document.getElementById(targetTab + '-tab');
            if (targetContent) {
                targetContent.classList.add('active');
            }
            
            // 탭 전환 애니메이션 효과
            addTabSwitchAnimation(targetContent);
        });
    });
}

/**
 * 탭 전환 애니메이션 효과
 */
function addTabSwitchAnimation(targetContent) {
    if (targetContent) {
        targetContent.style.opacity = '0';
        targetContent.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            targetContent.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            targetContent.style.opacity = '1';
            targetContent.style.transform = 'translateY(0)';
        }, 50);
    }
}

/**
 * 버튼 이벤트 초기화
 */
function initializeButtons() {
    // 채팅하기 버튼 이벤트
    const chatButtons = document.querySelectorAll('.btn-chat');
    chatButtons.forEach(button => {
        button.addEventListener('click', function() {
            const matchingItem = this.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const hostName = matchingItem.querySelector('.host-name').textContent;
            
            handleChatButtonClick(roomTitle, hostName);
        });
    });
    
    // 방 나가기/삭제 버튼 이벤트
    const exitButtons = document.querySelectorAll('.btn-exit-room, .btn-delete-room');
    exitButtons.forEach(button => {
        button.addEventListener('click', function() {
            const matchingItem = this.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const roomType = this.getAttribute('data-room-type');
            
            if (roomType === 'host') {
                handleDeleteRoomClick(roomTitle, matchingItem);
            } else {
                handleExitRoomClick(roomTitle, matchingItem);
            }
        });
    });
    
    // 입장 요청 관리 버튼 이벤트
    const manageButtons = document.querySelectorAll('.btn-manage-requests');
    manageButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const roomId = this.getAttribute('data-room-id');
                const matchingItem = this.closest('.matching-item');
                const roomTitle = matchingItem.querySelector('.matching-title').textContent;
                
                handleManageRequestsClick(roomId, roomTitle);
            }
        });
    });
}

/**
 * 채팅하기 버튼 클릭 처리
 */
function handleChatButtonClick(roomTitle, hostName) {
    const button = event.target.closest('.btn-chat');
    const originalText = button.textContent;
    
    button.disabled = true;
    button.textContent = '접속 중...';
    button.style.opacity = '0.7';
    
    // 실제 채팅방 이동 로직
    setTimeout(() => {
        // 채팅방 페이지로 이동
        // window.location.href = `/chat/room?title=${encodeURIComponent(roomTitle)}`;
        
        // 개발용 알림
        showNotification('success', `"${roomTitle}" 채팅방에 입장합니다.`);
        
        // 버튼 상태 복원
        button.disabled = false;
        button.textContent = originalText;
        button.style.opacity = '1';
    }, 1000);
}

/**
 * 방 나가기 버튼 클릭 처리
 */
function handleExitRoomClick(roomTitle, matchingItem) {
    showModal('exitRoomModal', {
        title: roomTitle,
        onConfirm: () => exitRoom(roomTitle, matchingItem)
    });
}

/**
 * 방 삭제 버튼 클릭 처리
 */
function handleDeleteRoomClick(roomTitle, matchingItem) {
    showModal('deleteRoomModal', {
        title: roomTitle,
        onConfirm: () => deleteRoom(roomTitle, matchingItem)
    });
}

/**
 * 입장 요청 관리 버튼 클릭 처리
 */
function handleManageRequestsClick(roomId, roomTitle) {
    // 요청 데이터 로드
    loadJoinRequests(roomId).then(requests => {
        showRequestsModal(roomId, roomTitle, requests);
    });
}

/**
 * 방 나가기 실행
 */
function exitRoom(roomTitle, matchingItem) {
    // 로딩 효과
    matchingItem.style.opacity = '0.5';
    matchingItem.style.pointerEvents = 'none';
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // 방 나가기 애니메이션
        matchingItem.style.transform = 'translateX(-100%)';
        matchingItem.style.transition = 'all 0.5s ease';
        
        setTimeout(() => {
            matchingItem.remove();
            showNotification('success', `"${roomTitle}" 방에서 나갔습니다.`);
            
            // 남은 항목이 없으면 메시지 표시
            checkEmptyState();
        }, 500);
    }, 1000);
}

/**
 * 방 삭제 실행
 */
function deleteRoom(roomTitle, matchingItem) {
    // 로딩 효과
    matchingItem.style.opacity = '0.5';
    matchingItem.style.pointerEvents = 'none';
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // 방 삭제 애니메이션
        matchingItem.style.transform = 'scale(0.8)';
        matchingItem.style.opacity = '0';
        matchingItem.style.transition = 'all 0.5s ease';
        
        setTimeout(() => {
            matchingItem.remove();
            showNotification('success', `"${roomTitle}" 방이 삭제되었습니다.`);
            
            // 남은 항목이 없으면 메시지 표시
            checkEmptyState();
        }, 500);
    }, 1000);
}

/**
 * 입장 요청 데이터 로드 (시뮬레이션)
 */
async function loadJoinRequests(roomId) {
    // 실제로는 API 호출
    return new Promise(resolve => {
        setTimeout(() => {
            // 샘플 데이터
            const sampleRequests = [
                {
                    id: 1,
                    name: '김여행',
                    avatar: '/static/image/mypage/test-profile-img.jpg',
                    requestTime: '2025-07-01 14:30',
                    verified: true,
                    contactRegistered: false,
                    message: '안녕하세요! 함께 여행하고 싶습니다.'
                },
                {
                    id: 2,
                    name: '박모험',
                    avatar: '/static/image/mypage/test-profile-img.jpg',
                    requestTime: '2025-07-01 16:20',
                    verified: false,
                    contactRegistered: true,
                    message: '여행 경험이 많아요. 잘 부탁드립니다!'
                }
            ];
            
            resolve(roomId === 'room-1' ? sampleRequests : []);
        }, 500);
    });
}

/**
 * 입장 요청 관리 모달 표시
 */
function showRequestsModal(roomId, roomTitle, requests) {
    const modal = document.getElementById('manageRequestsModal');
    const requestsList = modal.querySelector('.requests-list');
    
    // 모달 제목 업데이트
    modal.querySelector('.modal-header h3').textContent = `${roomTitle} - 입장 요청 관리`;
    
    // 요청 리스트 렌더링
    if (requests.length === 0) {
        requestsList.innerHTML = `
            <div class="empty-requests">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
                <h4>대기 중인 요청이 없습니다</h4>
                <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
            </div>
        `;
    } else {
        requestsList.innerHTML = requests.map(request => `
            <div class="request-item" data-request-id="${request.id}">
                <div class="request-avatar">
                    <img src="${request.avatar}" alt="${request.name}">
                </div>
                <div class="request-info">
                    <div class="request-name">${request.name}</div>
                    <div class="request-meta">
                        <span>${request.requestTime}</span>
                        <span class="separator">|</span>
                        <span class="verification-badge ${request.verified ? 'verified' : 'unverified'}">신원 인증</span>
                        <span class="verification-badge ${request.contactRegistered ? 'verified' : 'unverified'}">연락처 등록</span>
                    </div>
                    ${request.message ? `<div class="request-message" style="margin-top: 8px; font-size: 12px; color: #64748b;">${request.message}</div>` : ''}
                </div>
                <div class="request-actions">
                    <button class="btn-approve" onclick="approveRequest(${request.id}, '${roomId}')">승인</button>
                    <button class="btn-reject" onclick="rejectRequest(${request.id}, '${roomId}')">거절</button>
                </div>
            </div>
        `).join('');
    }
    
    // 모달 표시
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

/**
 * 입장 요청 승인
 */
function approveRequest(requestId, roomId) {
    const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
    const userName = requestItem.querySelector('.request-name').textContent;
    
    // 로딩 상태
    const approveBtn = requestItem.querySelector('.btn-approve');
    const rejectBtn = requestItem.querySelector('.btn-reject');
    
    approveBtn.disabled = true;
    rejectBtn.disabled = true;
    approveBtn.textContent = '처리 중...';
    
    // API 호출 시뮬레이션
    setTimeout(() => {
        // 요청 항목 제거 애니메이션
        requestItem.style.transform = 'translateX(100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showNotification('success', `${userName}님의 입장을 승인했습니다.`);
            
            // 대기 중인 요청 개수 업데이트
            updatePendingRequestsCount();
            
            // 요청이 모두 처리되었으면 빈 상태 표시
            const remainingRequests = document.querySelectorAll('.request-item');
            if (remainingRequests.length === 0) {
                const requestsList = document.querySelector('.requests-list');
                requestsList.innerHTML = `
                    <div class="empty-requests">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <h4>모든 요청을 처리했습니다</h4>
                        <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                    </div>
                `;
            }
        }, 300);
    }, 1000);
}

/**
 * 입장 요청 거절
 */
function rejectRequest(requestId, roomId) {
    const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
    const userName = requestItem.querySelector('.request-name').textContent;
    
    // 로딩 상태
    const approveBtn = requestItem.querySelector('.btn-approve');
    const rejectBtn = requestItem.querySelector('.btn-reject');
    
    approveBtn.disabled = true;
    rejectBtn.disabled = true;
    rejectBtn.textContent = '처리 중...';
    
    // API 호출 시뮬레이션
    setTimeout(() => {
        // 요청 항목 제거 애니메이션
        requestItem.style.transform = 'translateX(-100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showNotification('info', `${userName}님의 입장을 거절했습니다.`);
            
            // 대기 중인 요청 개수 업데이트
            updatePendingRequestsCount();
            
            // 요청이 모두 처리되었으면 빈 상태 표시
            const remainingRequests = document.querySelectorAll('.request-item');
            if (remainingRequests.length === 0) {
                const requestsList = document.querySelector('.requests-list');
                requestsList.innerHTML = `
                    <div class="empty-requests">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <h4>모든 요청을 처리했습니다</h4>
                        <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                    </div>
                `;
            }
        }, 300);
    }, 1000);
}

/**
 * 대기 중인 요청 개수 업데이트
 */
function updatePendingRequestsCount() {
    // 각 방의 대기 중인 요청 개수 시뮬레이션
    const pendingElements = document.querySelectorAll('.pending-requests');
    
    pendingElements.forEach(element => {
        const roomId = element.getAttribute('data-room-id');
        const currentCount = parseInt(element.textContent);
        
        // 실제로는 서버에서 가져올 데이터
        let newCount = Math.max(0, currentCount - 1);
        
        element.textContent = `${newCount}건`;
        element.setAttribute('data-count', newCount);
        
        // 관련 버튼 상태 업데이트
        const manageBtn = document.querySelector(`[data-room-id="${roomId}"]`);
        if (manageBtn) {
            manageBtn.disabled = newCount === 0;
        }
    });
    
    // 전체 알림 배지 업데이트
    updateNotificationBadge();
}

/**
 * 알림 배지 업데이트
 */
function updateNotificationBadge() {
    const badge = document.getElementById('join-requests-badge');
    if (!badge) return;
    
    // 모든 대기 중인 요청 개수 합계
    const pendingElements = document.querySelectorAll('.pending-requests');
    let totalCount = 0;
    
    pendingElements.forEach(element => {
        const count = parseInt(element.getAttribute('data-count') || element.textContent);
        totalCount += count;
    });
    
    if (totalCount > 0) {
        badge.textContent = totalCount;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

/**
 * 모달 초기화
 */
function initializeModals() {
    // 모달 닫기 버튼 이벤트
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            hideModal(modal.id);
        });
    });
    
    // 취소 버튼 이벤트
    const cancelButtons = document.querySelectorAll('.btn-cancel');
    cancelButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            hideModal(modal.id);
        });
    });
    
    // 오버레이 클릭 시 모달 닫기
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideModal(this.id);
            }
        });
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
    
    // 포커스 트랩
    const focusableElements = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (focusableElements.length > 0) {
        focusableElements[0].focus();
    }
}

/**
 * 모달 숨기기
 */
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

/**
 * 빈 상태 확인 및 메시지 표시
 */
function checkEmptyState() {
    const activeTab = document.querySelector('.tab-content.active');
    const matchingItems = activeTab.querySelectorAll('.matching-item');
    
    if (matchingItems.length === 0) {
        const emptyMessage = document.createElement('div');
        emptyMessage.className = 'empty-state';
        
        const isCreatedTab = activeTab.id === 'created-tab';
        emptyMessage.innerHTML = `
            <div style="text-align: center; padding: 4rem 2rem; color: #64748b;">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="margin-bottom: 1rem; opacity: 0.5;">
                    ${isCreatedTab ? 
                        '<path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>' :
                        '<circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path>'
                    }
                </svg>
                <h3 style="margin-bottom: 0.5rem; color: #475569;">
                    ${isCreatedTab ? '생성한 매칭이 없습니다' : '참여 중인 매칭이 없습니다'}
                </h3>
                <p style="color: #64748b;">
                    ${isCreatedTab ? '새로운 여행 매칭을 만들어보세요!' : '새로운 여행 매칭을 찾아보세요!'}
                </p>
            </div>
        `;
        
        activeTab.querySelector('.matching-list-wrapper').appendChild(emptyMessage);
    }
}

/**
 * 페이지네이션 초기화
 */
function initializePagination() {
    const pageButtons = document.querySelectorAll('.page-btn:not(.prev):not(.next)');
    const prevButtons = document.querySelectorAll('.page-btn.prev');
    const nextButtons = document.querySelectorAll('.page-btn.next');
    
    // 페이지 번호 버튼 이벤트
    pageButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled && !this.classList.contains('active')) {
                const pageNumber = this.textContent;
                changePage(pageNumber);
            }
        });
    });
    
    // 이전 페이지 버튼 이벤트
    prevButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const currentPage = document.querySelector('.page-btn.active');
                const currentPageNum = parseInt(currentPage.textContent);
                
                if (this.textContent.includes('‹‹') || this.querySelector('svg')) {
                    const targetPage = this.textContent.includes('‹‹') ? 1 : currentPageNum - 1;
                    changePage(targetPage);
                }
            }
        });
    });
    
    // 다음 페이지 버튼 이벤트
    nextButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const currentPage = document.querySelector('.page-btn.active');
                const currentPageNum = parseInt(currentPage.textContent);
                
                if (this.textContent.includes('››') || this.querySelector('svg')) {
                    const isLastPage = this.textContent.includes('››');
                    const targetPage = isLastPage ? getLastPageNumber() : currentPageNum + 1;
                    changePage(targetPage);
                }
            }
        });
    });
}

/**
 * 페이지 변경
 */
function changePage(pageNumber) {
    const currentActive = document.querySelector('.page-btn.active');
    if (currentActive) {
        currentActive.classList.remove('active');
    }
    
    const newActiveButton = Array.from(document.querySelectorAll('.page-btn')).find(btn => 
        btn.textContent.trim() === pageNumber.toString() && 
        !btn.classList.contains('prev') && 
        !btn.classList.contains('next')
    );
    
    if (newActiveButton) {
        newActiveButton.classList.add('active');
    }
    
    // 페이지 로딩 효과
    const activeTab = document.querySelector('.tab-content.active');
    const matchingList = activeTab.querySelector('.matching-list-wrapper');
    
    matchingList.style.opacity = '0.5';
    
    setTimeout(() => {
        matchingList.style.opacity = '1';
        showNotification('info', `${pageNumber}페이지로 이동했습니다.`);
        
        document.querySelector('.main-wrapper').scrollIntoView({ 
            behavior: 'smooth' 
        });
    }, 500);
    
    updatePaginationButtons(pageNumber);
}

/**
 * 페이지네이션 버튼 상태 업데이트
 */
function updatePaginationButtons(currentPage) {
    const prevButtons = document.querySelectorAll('.page-btn.prev');
    const nextButtons = document.querySelectorAll('.page-btn.next');
    const lastPage = getLastPageNumber();
    
    prevButtons.forEach(button => {
        button.disabled = currentPage <= 1;
    });
    
    nextButtons.forEach(button => {
        button.disabled = currentPage >= lastPage;
    });
}

/**
 * 마지막 페이지 번호 가져오기
 */
function getLastPageNumber() {
    const pageButtons = document.querySelectorAll('.page-btn:not(.prev):not(.next)');
    const lastButton = Array.from(pageButtons).pop();
    return lastButton ? parseInt(lastButton.textContent) : 1;
}

/**
 * 알림 메시지 표시
 */
function showNotification(type, message) {
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 12px;
            color: white;
            font-weight: 600;
            z-index: 1001;
            animation: slideInRight 0.3s ease, slideOutRight 0.3s ease 2.7s;
            box-shadow: 0 8px 24px rgba(0, 26, 44, 0.15);
            backdrop-filter: blur(10px);
            max-width: 400px;
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
        
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    
    document.head.appendChild(style);
    document.body.appendChild(notification);
    
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
            style.remove();
        }
    }, 3000);
}

/**
 * 키보드 이벤트 처리
 */
document.addEventListener('keydown', function(e) {
    // ESC 키로 모달 닫기
    if (e.key === 'Escape') {
        const visibleModals = document.querySelectorAll('.modal[style*="flex"]');
        visibleModals.forEach(modal => {
            hideModal(modal.id);
        });
    }
    
    // 방향키로 페이지네이션 제어
    if (e.key === 'ArrowLeft') {
        const prevButton = document.querySelector('.page-btn.prev:not(:disabled)');
        if (prevButton) {
            prevButton.click();
        }
    }
    
    if (e.key === 'ArrowRight') {
        const nextButton = document.querySelector('.page-btn.next:not(:disabled)');
        if (nextButton) {
            nextButton.click();
        }
    }
});

/**
 * 실시간 알림 시스템 (WebSocket 또는 polling 방식)
 */
function initializeRealTimeNotifications() {
    // 실제 환경에서는 WebSocket 연결
    // const socket = new WebSocket('ws://localhost:8080/notifications');
    
    // 데모용 polling 시뮬레이션
    setInterval(() => {
        checkForNewJoinRequests();
    }, 30000); // 30초마다 체크
}

/**
 * 새로운 입장 요청 확인 (시뮬레이션)
 */
function checkForNewJoinRequests() {
    // 실제로는 서버 API 호출
    const randomRoom = Math.random() > 0.7; // 30% 확률로 새 요청
    
    if (randomRoom) {
        // 랜덤하게 새 요청이 왔다고 시뮬레이션
        const roomElements = document.querySelectorAll('.pending-requests');
        if (roomElements.length > 0) {
            const randomRoomElement = roomElements[Math.floor(Math.random() * roomElements.length)];
            const currentCount = parseInt(randomRoomElement.getAttribute('data-count') || '0');
            const newCount = currentCount + 1;
            
            randomRoomElement.textContent = `${newCount}건`;
            randomRoomElement.setAttribute('data-count', newCount);
            
            // 관련 버튼 활성화
            const roomId = randomRoomElement.getAttribute('data-room-id');
            const manageBtn = document.querySelector(`[data-room-id="${roomId}"]`);
            if (manageBtn) {
                manageBtn.disabled = false;
            }
            
            // 알림 배지 업데이트
            updateNotificationBadge();
            
            // 브라우저 알림 표시
            if (Notification.permission === 'granted') {
                new Notification('새로운 입장 요청', {
                    body: '새로운 참가자가 입장을 요청했습니다.',
                    icon: '/static/image/icon/notification.png'
                });
            }
            
            // 사이트 내 알림
            showNotification('info', '새로운 입장 요청이 있습니다!');
        }
    }
}

/**
 * 브라우저 알림 권한 요청
 */
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
            if (permission === 'granted') {
                showNotification('success', '알림이 활성화되었습니다.');
            }
        });
    }
}

/**
 * 도움말 및 가이드 함수
 */
function showHelp() {
    const helpModal = document.createElement('div');
    helpModal.className = 'modal';
    helpModal.id = 'helpModal';
    helpModal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>매칭 관리 도움말</h3>
                <button class="modal-close">&times;</button>
            </div>
            <div class="modal-body">
                <h4>입장 요청 관리</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>승인:</strong> 참가자를 방에 추가합니다.</li>
                    <li><strong>거절:</strong> 요청을 거절하며, 신청자에게 알림이 전송됩니다.</li>
                    <li><strong>실시간 알림:</strong> 새로운 요청이 오면 자동으로 알림을 받을 수 있습니다.</li>
                </ul>
                
                <h4>방 관리</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>방 삭제:</strong> 모든 참가자가 제거되며 복구할 수 없습니다.</li>
                    <li><strong>방 나가기:</strong> 참가자로서 방을 떠날 수 있습니다.</li>
                    <li><strong>채팅:</strong> 방 참가자들과 실시간 대화가 가능합니다.</li>
                </ul>
                
                <h4>키보드 단축키</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>ESC:</strong> 열린 모달을 닫습니다.</li>
                    <li><strong>←/→:</strong> 페이지를 이동합니다.</li>
                </ul>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel">닫기</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(helpModal);
    
    // 이벤트 리스너 추가
    helpModal.querySelector('.modal-close').addEventListener('click', () => {
        helpModal.remove();
        document.body.style.overflow = '';
    });
    
    helpModal.querySelector('.btn-cancel').addEventListener('click', () => {
        helpModal.remove();
        document.body.style.overflow = '';
    });
    
    helpModal.addEventListener('click', (e) => {
        if (e.target === helpModal) {
            helpModal.remove();
            document.body.style.overflow = '';
        }
    });
    
    helpModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

/**
 * 데이터 내보내기 기능
 */
function exportMatchingData() {
    const matchingData = {
        exportDate: new Date().toISOString(),
        joinedRooms: [],
        createdRooms: []
    };
    
    // 참여한 방 데이터 수집
    const joinedTab = document.getElementById('received-tab');
    const joinedItems = joinedTab.querySelectorAll('.matching-item');
    
    joinedItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent;
        const host = item.querySelector('.host-name').textContent;
        const participants = item.querySelector('.participants-info .count').textContent;
        const date = item.querySelector('.travel-date .date').textContent;
        
        matchingData.joinedRooms.push({
            title, host, participants, date
        });
    });
    
    // 생성한 방 데이터 수집
    const createdTab = document.getElementById('created-tab');
    const createdItems = createdTab.querySelectorAll('.matching-item');
    
    createdItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent;
        const participants = item.querySelector('.participants-info .count').textContent;
        const date = item.querySelector('.travel-date .date').textContent;
        const pendingRequests = item.querySelector('.pending-requests')?.textContent || '0건';
        
        matchingData.createdRooms.push({
            title, participants, date, pendingRequests
        });
    });
    
    // JSON 파일로 다운로드
    const dataStr = JSON.stringify(matchingData, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = `matching_data_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    showNotification('success', '매칭 데이터를 내보냈습니다.');
}

/**
 * 성능 최적화를 위한 디바운스 함수
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * 검색 기능 (추후 확장용)
 */
function initializeSearch() {
    const searchInput = document.getElementById('matchingSearch');
    if (!searchInput) return;
    
    const debouncedSearch = debounce((query) => {
        filterMatchingItems(query);
    }, 300);
    
    searchInput.addEventListener('input', (e) => {
        debouncedSearch(e.target.value);
    });
}

/**
 * 매칭 항목 필터링
 */
function filterMatchingItems(query) {
    const activeTab = document.querySelector('.tab-content.active');
    const matchingItems = activeTab.querySelectorAll('.matching-item');
    
    matchingItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent.toLowerCase();
        const description = item.querySelector('.matching-description').textContent.toLowerCase();
        const tags = Array.from(item.querySelectorAll('.tag')).map(tag => tag.textContent.toLowerCase());
        
        const searchText = query.toLowerCase();
        const isMatch = title.includes(searchText) || 
                       description.includes(searchText) || 
                       tags.some(tag => tag.includes(searchText));
        
        item.style.display = isMatch ? 'block' : 'none';
    });
}

/**
 * 접근성 개선 함수
 */
function improveAccessibility() {
    // ARIA 라벨 추가
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        if (!button.getAttribute('aria-label') && button.textContent) {
            button.setAttribute('aria-label', button.textContent.trim());
        }
    });
    
    // 키보드 네비게이션 개선
    const focusableElements = document.querySelectorAll('a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])');
    focusableElements.forEach((element, index) => {
        element.addEventListener('keydown', (e) => {
            if (e.key === 'Tab' && e.shiftKey && index === 0) {
                e.preventDefault();
                focusableElements[focusableElements.length - 1].focus();
            } else if (e.key === 'Tab' && !e.shiftKey && index === focusableElements.length - 1) {
                e.preventDefault();
                focusableElements[0].focus();
            }
        });
    });
}

/**
 * 로컬 스토리지 관리 (설정 저장용)
 */
function saveUserPreferences(preferences) {
    try {
        localStorage.setItem('matchingPreferences', JSON.stringify(preferences));
    } catch (error) {
        console.warn('로컬 스토리지 저장 실패:', error);
    }
}

function loadUserPreferences() {
    try {
        const saved = localStorage.getItem('matchingPreferences');
        return saved ? JSON.parse(saved) : {};
    } catch (error) {
        console.warn('로컬 스토리지 로드 실패:', error);
        return {};
    }
}

/**
 * 오류 처리 및 재시도 로직
 */
function handleApiError(error, retryFn, maxRetries = 3) {
    console.error('API 오류:', error);
    
    if (maxRetries > 0) {
        showNotification('error', '요청 처리 중 오류가 발생했습니다. 재시도 중...');
        setTimeout(() => {
            retryFn(maxRetries - 1);
        }, 2000);
    } else {
        showNotification('error', '요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}

/**
 * 성능 모니터링
 */
function trackPerformance() {
    if (typeof performance !== 'undefined' && performance.mark) {
        performance.mark('page-interaction-start');
        
        // 중요한 상호작용 후 측정
        setTimeout(() => {
            performance.mark('page-interaction-end');
            performance.measure('page-interaction', 'page-interaction-start', 'page-interaction-end');
            
            const measures = performance.getEntriesByType('measure');
            measures.forEach(measure => {
                if (measure.duration > 1000) {
                    console.warn(`성능 경고: ${measure.name}이 ${measure.duration}ms 소요됨`);
                }
            });
        }, 100);
    }
}

// 페이지 로드 완료 후 추가 초기화
window.addEventListener('load', function() {
    // 실시간 알림 시스템 초기화
    initializeRealTimeNotifications();
    
    // 브라우저 알림 권한 요청
    requestNotificationPermission();
    
    // 검색 기능 초기화
    initializeSearch();
    
    // 접근성 개선
    improveAccessibility();
    
    // 성능 추적 시작
    trackPerformance();
    
    // 사용자 설정 로드
    const preferences = loadUserPreferences();
    if (preferences.autoRefresh) {
        setInterval(updatePendingRequestsCount, 60000); // 1분마다 업데이트
    }
    
    // 서비스 워커 등록 (PWA 지원)
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then(registration => {
            console.log('ServiceWorker registration successful');
        }).catch(error => {
            console.log('ServiceWorker registration failed');
        });
    }
    
    // 페이지 가시성 API를 사용한 성능 최적화
    if (typeof document.visibilityState !== 'undefined') {
        document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'visible') {
                // 페이지가 다시 보일 때 데이터 새로고침
                updatePendingRequestsCount();
                updateNotificationBadge();
            }
        });
    }
});

// 페이지 언로드 시 정리 작업
window.addEventListener('beforeunload', function() {
    // 진행 중인 요청 취소
    // 타이머 정리
    // 리소스 해제 등
});

// 전역 함수로 노출 (HTML에서 직접 호출용)
window.approveRequest = approveRequest;
window.rejectRequest = rejectRequest;
window.showHelp = showHelp;
window.exportMatchingData = exportMatchingData;// 매칭 정보 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 탭 기능 초기화
    initializeTabs();
    
    // 버튼 이벤트 초기화
    initializeButtons();
    
    // 페이지네이션 초기화
    initializePagination();
    
    // 모달 이벤트 초기화
    initializeModals();
    
    // 알림 배지 업데이트
    updateNotificationBadge();
    
    // 실시간 요청 개수 업데이트 (예시)
    updatePendingRequestsCount();
});

/**
 * 탭 기능 초기화
 */
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            // 모든 탭 버튼에서 active 클래스 제거
            tabButtons.forEach(btn => btn.classList.remove('active'));
            
            // 클릭된 탭 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 모든 탭 콘텐츠 숨기기
            tabContents.forEach(content => {
                content.classList.remove('active');
            });
            
            // 선택된 탭 콘텐츠 보이기
            const targetContent = document.getElementById(targetTab + '-tab');
            if (targetContent) {
                targetContent.classList.add('active');
            }
            
            // 탭 전환 애니메이션 효과
            addTabSwitchAnimation(targetContent);
        });
    });
}

/**
 * 탭 전환 애니메이션 효과
 */
function addTabSwitchAnimation(targetContent) {
    if (targetContent) {
        targetContent.style.opacity = '0';
        targetContent.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            targetContent.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            targetContent.style.opacity = '1';
            targetContent.style.transform = 'translateY(0)';
        }, 50);
    }
}

/**
 * 버튼 이벤트 초기화
 */
function initializeButtons() {
    // 채팅하기 버튼 이벤트
    const chatButtons = document.querySelectorAll('.btn-chat');
    chatButtons.forEach(button => {
        button.addEventListener('click', function() {
            const matchingItem = this.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const hostName = matchingItem.querySelector('.host-name').textContent;
            
            handleChatButtonClick(roomTitle, hostName);
        });
    });
    
    // 방 나가기/삭제 버튼 이벤트
    const exitButtons = document.querySelectorAll('.btn-exit-room, .btn-delete-room');
    exitButtons.forEach(button => {
        button.addEventListener('click', function() {
            const matchingItem = this.closest('.matching-item');
            const roomTitle = matchingItem.querySelector('.matching-title').textContent;
            const roomType = this.getAttribute('data-room-type');
            
            if (roomType === 'host') {
                handleDeleteRoomClick(roomTitle, matchingItem);
            } else {
                handleExitRoomClick(roomTitle, matchingItem);
            }
        });
    });
    
    // 입장 요청 관리 버튼 이벤트
    const manageButtons = document.querySelectorAll('.btn-manage-requests');
    manageButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const roomId = this.getAttribute('data-room-id');
                const matchingItem = this.closest('.matching-item');
                const roomTitle = matchingItem.querySelector('.matching-title').textContent;
                
                handleManageRequestsClick(roomId, roomTitle);
            }
        });
    });
}

/**
 * 채팅하기 버튼 클릭 처리
 */
function handleChatButtonClick(roomTitle, hostName) {
    const button = event.target.closest('.btn-chat');
    const originalText = button.textContent;
    
    button.disabled = true;
    button.textContent = '접속 중...';
    button.style.opacity = '0.7';
    
    // 실제 채팅방 이동 로직
    setTimeout(() => {
        // 채팅방 페이지로 이동
        // window.location.href = `/chat/room?title=${encodeURIComponent(roomTitle)}`;
        
        // 개발용 알림
        showNotification('success', `"${roomTitle}" 채팅방에 입장합니다.`);
        
        // 버튼 상태 복원
        button.disabled = false;
        button.textContent = originalText;
        button.style.opacity = '1';
    }, 1000);
}

/**
 * 방 나가기 버튼 클릭 처리
 */
function handleExitRoomClick(roomTitle, matchingItem) {
    showModal('exitRoomModal', {
        title: roomTitle,
        onConfirm: () => exitRoom(roomTitle, matchingItem)
    });
}

/**
 * 방 삭제 버튼 클릭 처리
 */
function handleDeleteRoomClick(roomTitle, matchingItem) {
    showModal('deleteRoomModal', {
        title: roomTitle,
        onConfirm: () => deleteRoom(roomTitle, matchingItem)
    });
}

/**
 * 입장 요청 관리 버튼 클릭 처리
 */
function handleManageRequestsClick(roomId, roomTitle) {
    // 요청 데이터 로드
    loadJoinRequests(roomId).then(requests => {
        showRequestsModal(roomId, roomTitle, requests);
    });
}

/**
 * 방 나가기 실행
 */
function exitRoom(roomTitle, matchingItem) {
    // 로딩 효과
    matchingItem.style.opacity = '0.5';
    matchingItem.style.pointerEvents = 'none';
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // 방 나가기 애니메이션
        matchingItem.style.transform = 'translateX(-100%)';
        matchingItem.style.transition = 'all 0.5s ease';
        
        setTimeout(() => {
            matchingItem.remove();
            showNotification('success', `"${roomTitle}" 방에서 나갔습니다.`);
            
            // 남은 항목이 없으면 메시지 표시
            checkEmptyState();
        }, 500);
    }, 1000);
}

/**
 * 방 삭제 실행
 */
function deleteRoom(roomTitle, matchingItem) {
    // 로딩 효과
    matchingItem.style.opacity = '0.5';
    matchingItem.style.pointerEvents = 'none';
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // 방 삭제 애니메이션
        matchingItem.style.transform = 'scale(0.8)';
        matchingItem.style.opacity = '0';
        matchingItem.style.transition = 'all 0.5s ease';
        
        setTimeout(() => {
            matchingItem.remove();
            showNotification('success', `"${roomTitle}" 방이 삭제되었습니다.`);
            
            // 남은 항목이 없으면 메시지 표시
            checkEmptyState();
        }, 500);
    }, 1000);
}

/**
 * 입장 요청 데이터 로드 (시뮬레이션)
 */
async function loadJoinRequests(roomId) {
    // 실제로는 API 호출
    return new Promise(resolve => {
        setTimeout(() => {
            // 샘플 데이터
            const sampleRequests = [
                {
                    id: 1,
                    name: '김여행',
                    avatar: '/static/image/mypage/test-profile-img.jpg',
                    requestTime: '2025-07-01 14:30',
                    verified: true,
                    contactRegistered: false,
                    message: '안녕하세요! 함께 여행하고 싶습니다.'
                },
                {
                    id: 2,
                    name: '박모험',
                    avatar: '/static/image/mypage/test-profile-img.jpg',
                    requestTime: '2025-07-01 16:20',
                    verified: false,
                    contactRegistered: true,
                    message: '여행 경험이 많아요. 잘 부탁드립니다!'
                }
            ];
            
            resolve(roomId === 'room-1' ? sampleRequests : []);
        }, 500);
    });
}

/**
 * 입장 요청 관리 모달 표시
 */
function showRequestsModal(roomId, roomTitle, requests) {
    const modal = document.getElementById('manageRequestsModal');
    const requestsList = modal.querySelector('.requests-list');
    
    // 모달 제목 업데이트
    modal.querySelector('.modal-header h3').textContent = `${roomTitle} - 입장 요청 관리`;
    
    // 요청 리스트 렌더링
    if (requests.length === 0) {
        requestsList.innerHTML = `
            <div class="empty-requests">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
                <h4>대기 중인 요청이 없습니다</h4>
                <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
            </div>
        `;
    } else {
        requestsList.innerHTML = requests.map(request => `
            <div class="request-item" data-request-id="${request.id}">
                <div class="request-avatar">
                    <img src="${request.avatar}" alt="${request.name}">
                </div>
                <div class="request-info">
                    <div class="request-name">${request.name}</div>
                    <div class="request-meta">
                        <span>${request.requestTime}</span>
                        <span class="separator">|</span>
                        <span class="verification-badge ${request.verified ? 'verified' : 'unverified'}">신원 인증</span>
                        <span class="verification-badge ${request.contactRegistered ? 'verified' : 'unverified'}">연락처 등록</span>
                    </div>
                    ${request.message ? `<div class="request-message">${request.message}</div>` : ''}
                </div>
                <div class="request-actions">
                    <button class="btn-approve" onclick="approveRequest(${request.id}, '${roomId}')">승인</button>
                    <button class="btn-reject" onclick="rejectRequest(${request.id}, '${roomId}')">거절</button>
                </div>
            </div>
        `).join('');
    }
    
    // 모달 표시
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

/**
 * 입장 요청 승인
 */
function approveRequest(requestId, roomId) {
    const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
    const userName = requestItem.querySelector('.request-name').textContent;
    
    // 로딩 상태
    const approveBtn = requestItem.querySelector('.btn-approve');
    const rejectBtn = requestItem.querySelector('.btn-reject');
    
    approveBtn.disabled = true;
    rejectBtn.disabled = true;
    approveBtn.textContent = '처리 중...';
    
    // API 호출 시뮬레이션
    setTimeout(() => {
        // 요청 항목 제거 애니메이션
        requestItem.style.transform = 'translateX(100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showNotification('success', `${userName}님의 입장을 승인했습니다.`);
            
            // 대기 중인 요청 개수 업데이트
            updatePendingRequestsCount();
            
            // 요청이 모두 처리되었으면 빈 상태 표시
            const remainingRequests = document.querySelectorAll('.request-item');
            if (remainingRequests.length === 0) {
                const requestsList = document.querySelector('.requests-list');
                requestsList.innerHTML = `
                    <div class="empty-requests">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <h4>모든 요청을 처리했습니다</h4>
                        <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                    </div>
                `;
            }
        }, 300);
    }, 1000);
}

/**
 * 입장 요청 거절
 */
function rejectRequest(requestId, roomId) {
    const requestItem = document.querySelector(`[data-request-id="${requestId}"]`);
    const userName = requestItem.querySelector('.request-name').textContent;
    
    // 로딩 상태
    const approveBtn = requestItem.querySelector('.btn-approve');
    const rejectBtn = requestItem.querySelector('.btn-reject');
    
    approveBtn.disabled = true;
    rejectBtn.disabled = true;
    rejectBtn.textContent = '처리 중...';
    
    // API 호출 시뮬레이션
    setTimeout(() => {
        // 요청 항목 제거 애니메이션
        requestItem.style.transform = 'translateX(-100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showNotification('info', `${userName}님의 입장을 거절했습니다.`);
            
            // 대기 중인 요청 개수 업데이트
            updatePendingRequestsCount();
            
            // 요청이 모두 처리되었으면 빈 상태 표시
            const remainingRequests = document.querySelectorAll('.request-item');
            if (remainingRequests.length === 0) {
                const requestsList = document.querySelector('.requests-list');
                requestsList.innerHTML = `
                    <div class="empty-requests">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <h4>모든 요청을 처리했습니다</h4>
                        <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                    </div>
                `;
            }
        }, 300);
    }, 1000);
}

/**
 * 대기 중인 요청 개수 업데이트
 */
function updatePendingRequestsCount() {
    // 각 방의 대기 중인 요청 개수 시뮬레이션
    const pendingElements = document.querySelectorAll('.pending-requests');
    
    pendingElements.forEach(element => {
        const roomId = element.getAttribute('data-room-id');
        const currentCount = parseInt(element.textContent);
        
        // 실제로는 서버에서 가져올 데이터
        let newCount = Math.max(0, currentCount - 1);
        
        element.textContent = `${newCount}건`;
        element.setAttribute('data-count', newCount);
        
        // 관련 버튼 상태 업데이트
        const manageBtn = document.querySelector(`[data-room-id="${roomId}"]`);
        if (manageBtn) {
            manageBtn.disabled = newCount === 0;
        }
    });
    
    // 전체 알림 배지 업데이트
    updateNotificationBadge();
}

/**
 * 알림 배지 업데이트
 */
function updateNotificationBadge() {
    const badge = document.getElementById('join-requests-badge');
    if (!badge) return;
    
    // 모든 대기 중인 요청 개수 합계
    const pendingElements = document.querySelectorAll('.pending-requests');
    let totalCount = 0;
    
    pendingElements.forEach(element => {
        const count = parseInt(element.getAttribute('data-count') || element.textContent);
        totalCount += count;
    });
    
    if (totalCount > 0) {
        badge.textContent = totalCount;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

/**
 * 모달 초기화
 */
function initializeModals() {
    // 모달 닫기 버튼 이벤트
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            hideModal(modal.id);
        });
    });
    
    // 취소 버튼 이벤트
    const cancelButtons = document.querySelectorAll('.btn-cancel');
    cancelButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            hideModal(modal.id);
        });
    });
    
    // 오버레이 클릭 시 모달 닫기
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideModal(this.id);
            }
        });
    });
    
    // 확인 버튼 이벤트는 각 모달별로 동적으로 처리
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
    
    // 포커스 트랩
    const focusableElements = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (focusableElements.length > 0) {
        focusableElements[0].focus();
    }
}

/**
 * 모달 숨기기
 */
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

/**
 * 빈 상태 확인 및 메시지 표시
 */
function checkEmptyState() {
    const activeTab = document.querySelector('.tab-content.active');
    const matchingItems = activeTab.querySelectorAll('.matching-item');
    
    if (matchingItems.length === 0) {
        const emptyMessage = document.createElement('div');
        emptyMessage.className = 'empty-state';
        
        const isCreatedTab = activeTab.id === 'created-tab';
        emptyMessage.innerHTML = `
            <div style="text-align: center; padding: 4rem 2rem; color: #64748b;">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="margin-bottom: 1rem; opacity: 0.5;">
                    ${isCreatedTab ? 
                        '<path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>' :
                        '<circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path>'
                    }
                </svg>
                <h3 style="margin-bottom: 0.5rem; color: #475569;">
                    ${isCreatedTab ? '생성한 매칭이 없습니다' : '참여 중인 매칭이 없습니다'}
                </h3>
                <p style="color: #64748b;">
                    ${isCreatedTab ? '새로운 여행 매칭을 만들어보세요!' : '새로운 여행 매칭을 찾아보세요!'}
                </p>
            </div>
        `;
        
        activeTab.querySelector('.matching-list-wrapper').appendChild(emptyMessage);
    }
}

/**
 * 페이지네이션 초기화
 */
function initializePagination() {
    const pageButtons = document.querySelectorAll('.page-btn:not(.prev):not(.next)');
    const prevButtons = document.querySelectorAll('.page-btn.prev');
    const nextButtons = document.querySelectorAll('.page-btn.next');
    
    // 페이지 번호 버튼 이벤트
    pageButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled && !this.classList.contains('active')) {
                const pageNumber = this.textContent;
                changePage(pageNumber);
            }
        });
    });
    
    // 이전 페이지 버튼 이벤트
    prevButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const currentPage = document.querySelector('.page-btn.active');
                const currentPageNum = parseInt(currentPage.textContent);
                
                if (this.textContent.includes('‹‹') || this.querySelector('svg')) {
                    const targetPage = this.textContent.includes('‹‹') ? 1 : currentPageNum - 1;
                    changePage(targetPage);
                }
            }
        });
    });
    
    // 다음 페이지 버튼 이벤트
    nextButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                const currentPage = document.querySelector('.page-btn.active');
                const currentPageNum = parseInt(currentPage.textContent);
                
                if (this.textContent.includes('››') || this.querySelector('svg')) {
                    const isLastPage = this.textContent.includes('››');
                    const targetPage = isLastPage ? getLastPageNumber() : currentPageNum + 1;
                    changePage(targetPage);
                }
            }
        });
    });
}

/**
 * 페이지 변경
 */
function changePage(pageNumber) {
    const currentActive = document.querySelector('.page-btn.active');
    if (currentActive) {
        currentActive.classList.remove('active');
    }
    
    const newActiveButton = Array.from(document.querySelectorAll('.page-btn')).find(btn => 
        btn.textContent.trim() === pageNumber.toString() && 
        !btn.classList.contains('prev') && 
        !btn.classList.contains('next')
    );
    
    if (newActiveButton) {
        newActiveButton.classList.add('active');
    }
    
    // 페이지 로딩 효과
    const activeTab = document.querySelector('.tab-content.active');
    const matchingList = activeTab.querySelector('.matching-list-wrapper');
    
    matchingList.style.opacity = '0.5';
    
    setTimeout(() => {
        matchingList.style.opacity = '1';
        showNotification('info', `${pageNumber}페이지로 이동했습니다.`);
        
        document.querySelector('.main-wrapper').scrollIntoView({ 
            behavior: 'smooth' 
        });
    }, 500);
    
    updatePaginationButtons(pageNumber);
}

/**
 * 페이지네이션 버튼 상태 업데이트
 */
function updatePaginationButtons(currentPage) {
    const prevButtons = document.querySelectorAll('.page-btn.prev');
    const nextButtons = document.querySelectorAll('.page-btn.next');
    const lastPage = getLastPageNumber();
    
    prevButtons.forEach(button => {
        button.disabled = currentPage <= 1;
    });
    
    nextButtons.forEach(button => {
        button.disabled = currentPage >= lastPage;
    });
}

/**
 * 마지막 페이지 번호 가져오기
 */
function getLastPageNumber() {
    const pageButtons = document.querySelectorAll('.page-btn:not(.prev):not(.next)');
    const lastButton = Array.from(pageButtons).pop();
    return lastButton ? parseInt(lastButton.textContent) : 1;
}

/**
 * 알림 메시지 표시
 */
function showNotification(type, message) {
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 12px;
            color: white;
            font-weight: 600;
            z-index: 1001;
            animation: slideInRight 0.3s ease, slideOutRight 0.3s ease 2.7s;
            box-shadow: 0 8px 24px rgba(0, 26, 44, 0.15);
            backdrop-filter: blur(10px);
            max-width: 400px;
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
        
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    
    document.head.appendChild(style);
    document.body.appendChild(notification);
    
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
            style.remove();
        }
    }, 3000);
}

/**
 * 키보드 이벤트 처리
 */
document.addEventListener('keydown', function(e) {
    // ESC 키로 모달 닫기
    if (e.key === 'Escape') {
        const visibleModals = document.querySelectorAll('.modal[style*="flex"]');
        visibleModals.forEach(modal => {
            hideModal(modal.id);
        });
    }
    
    // 방향키로 페이지네이션 제어
    if (e.key === 'ArrowLeft') {
        const prevButton = document.querySelector('.page-btn.prev:not(:disabled)');
        if (prevButton) {
            prevButton.click();
        }
    }
    
    if (e.key === 'ArrowRight') {
        const nextButton = document.querySelector('.page-btn.next:not(:disabled)');
        if (nextButton) {
            nextButton.click();
        }
    }
});

/**
 * 실시간 알림 시스템 (WebSocket 또는 polling 방식)
 */
function initializeRealTimeNotifications() {
    // 실제 환경에서는 WebSocket 연결
    // const socket = new WebSocket('ws://localhost:8080/notifications');
    
    // 데모용 polling 시뮬레이션
    setInterval(() => {
        checkForNewJoinRequests();
    }, 30000); // 30초마다 체크
}

/**
 * 새로운 입장 요청 확인 (시뮬레이션)
 */
function checkForNewJoinRequests() {
    // 실제로는 서버 API 호출
    const randomRoom = Math.random() > 0.7; // 30% 확률로 새 요청
    
    if (randomRoom) {
        // 랜덤하게 새 요청이 왔다고 시뮬레이션
        const roomElements = document.querySelectorAll('.pending-requests');
        if (roomElements.length > 0) {
            const randomRoomElement = roomElements[Math.floor(Math.random() * roomElements.length)];
            const currentCount = parseInt(randomRoomElement.getAttribute('data-count') || '0');
            const newCount = currentCount + 1;
            
            randomRoomElement.textContent = `${newCount}건`;
            randomRoomElement.setAttribute('data-count', newCount);
            
            // 관련 버튼 활성화
            const roomId = randomRoomElement.getAttribute('data-room-id');
            const manageBtn = document.querySelector(`[data-room-id="${roomId}"]`);
            if (manageBtn) {
                manageBtn.disabled = false;
            }
            
            // 알림 배지 업데이트
            updateNotificationBadge();
            
            // 브라우저 알림 표시
            if (Notification.permission === 'granted') {
                new Notification('새로운 입장 요청', {
                    body: '새로운 참가자가 입장을 요청했습니다.',
                    icon: '/static/image/icon/notification.png'
                });
            }
            
            // 사이트 내 알림
            showNotification('info', '새로운 입장 요청이 있습니다!');
        }
    }
}

/**
 * 브라우저 알림 권한 요청
 */
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
            if (permission === 'granted') {
                showNotification('success', '알림이 활성화되었습니다.');
            }
        });
    }
}

/**
 * 도움말 및 가이드 함수
 */
function showHelp() {
    const helpModal = document.createElement('div');
    helpModal.className = 'modal';
    helpModal.id = 'helpModal';
    helpModal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>매칭 관리 도움말</h3>
                <button class="modal-close">&times;</button>
            </div>
            <div class="modal-body">
                <h4>입장 요청 관리</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>승인:</strong> 참가자를 방에 추가합니다.</li>
                    <li><strong>거절:</strong> 요청을 거절하며, 신청자에게 알림이 전송됩니다.</li>
                    <li><strong>실시간 알림:</strong> 새로운 요청이 오면 자동으로 알림을 받을 수 있습니다.</li>
                </ul>
                
                <h4>방 관리</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>방 삭제:</strong> 모든 참가자가 제거되며 복구할 수 없습니다.</li>
                    <li><strong>방 나가기:</strong> 참가자로서 방을 떠날 수 있습니다.</li>
                    <li><strong>채팅:</strong> 방 참가자들과 실시간 대화가 가능합니다.</li>
                </ul>
                
                <h4>키보드 단축키</h4>
                <ul style="text-align: left; margin: 1rem 0; line-height: 1.6;">
                    <li><strong>ESC:</strong> 열린 모달을 닫습니다.</li>
                    <li><strong>←/→:</strong> 페이지를 이동합니다.</li>
                </ul>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel">닫기</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(helpModal);
    
    // 이벤트 리스너 추가
    helpModal.querySelector('.modal-close').addEventListener('click', () => {
        helpModal.remove();
    });
    
    helpModal.querySelector('.btn-cancel').addEventListener('click', () => {
        helpModal.remove();
    });
    
    helpModal.addEventListener('click', (e) => {
        if (e.target === helpModal) {
            helpModal.remove();
        }
    });
    
    helpModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

/**
 * 데이터 내보내기 기능
 */
function exportMatchingData() {
    const matchingData = {
        exportDate: new Date().toISOString(),
        joinedRooms: [],
        createdRooms: []
    };
    
    // 참여한 방 데이터 수집
    const joinedTab = document.getElementById('received-tab');
    const joinedItems = joinedTab.querySelectorAll('.matching-item');
    
    joinedItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent;
        const host = item.querySelector('.host-name').textContent;
        const participants = item.querySelector('.participants-info .count').textContent;
        const date = item.querySelector('.travel-date .date').textContent;
        
        matchingData.joinedRooms.push({
            title, host, participants, date
        });
    });
    
    // 생성한 방 데이터 수집
    const createdTab = document.getElementById('created-tab');
    const createdItems = createdTab.querySelectorAll('.matching-item');
    
    createdItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent;
        const participants = item.querySelector('.participants-info .count').textContent;
        const date = item.querySelector('.travel-date .date').textContent;
        const pendingRequests = item.querySelector('.pending-requests')?.textContent || '0건';
        
        matchingData.createdRooms.push({
            title, participants, date, pendingRequests
        });
    });
    
    // JSON 파일로 다운로드
    const dataStr = JSON.stringify(matchingData, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = `matching_data_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    showNotification('success', '매칭 데이터를 내보냈습니다.');
}

/**
 * 성능 최적화를 위한 디바운스 함수
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * 검색 기능 (추후 확장용)
 */
function initializeSearch() {
    const searchInput = document.getElementById('matchingSearch');
    if (!searchInput) return;
    
    const debouncedSearch = debounce((query) => {
        filterMatchingItems(query);
    }, 300);
    
    searchInput.addEventListener('input', (e) => {
        debouncedSearch(e.target.value);
    });
}

/**
 * 매칭 항목 필터링
 */
function filterMatchingItems(query) {
    const activeTab = document.querySelector('.tab-content.active');
    const matchingItems = activeTab.querySelectorAll('.matching-item');
    
    matchingItems.forEach(item => {
        const title = item.querySelector('.matching-title').textContent.toLowerCase();
        const description = item.querySelector('.matching-description').textContent.toLowerCase();
        const tags = Array.from(item.querySelectorAll('.tag')).map(tag => tag.textContent.toLowerCase());
        
        const searchText = query.toLowerCase();
        const isMatch = title.includes(searchText) || 
                       description.includes(searchText) || 
                       tags.some(tag => tag.includes(searchText));
        
        item.style.display = isMatch ? 'block' : 'none';
    });
}

/**
 * 접근성 개선 함수
 */
function improveAccessibility() {
    // ARIA 라벨 추가
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        if (!button.getAttribute('aria-label') && button.textContent) {
            button.setAttribute('aria-label', button.textContent.trim());
        }
    });
    
    // 키보드 네비게이션 개선
    const focusableElements = document.querySelectorAll('a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])');
    focusableElements.forEach((element, index) => {
        element.addEventListener('keydown', (e) => {
            if (e.key === 'Tab' && e.shiftKey && index === 0) {
                e.preventDefault();
                focusableElements[focusableElements.length - 1].focus();
            } else if (e.key === 'Tab' && !e.shiftKey && index === focusableElements.length - 1) {
                e.preventDefault();
                focusableElements[0].focus();
            }
        });
    });
}

// 페이지 로드 완료 후 추가 초기화
window.addEventListener('load', function() {
    // 실시간 알림 시스템 초기화
    initializeRealTimeNotifications();
    
    // 브라우저 알림 권한 요청
    requestNotificationPermission();
    
    // 검색 기능 초기화
    initializeSearch();
    
    // 접근성 개선
    improveAccessibility();
    
    // 서비스 워커 등록 (PWA 지원)
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then(registration => {
            console.log('ServiceWorker registration successful');
        }).catch(error => {
            console.log('ServiceWorker registration failed');
        });
    }
});

// 전역 함수로 노출 (HTML에서 직접 호출용)
window.approveRequest = approveRequest;
window.rejectRequest = rejectRequest;
window.showHelp = showHelp;
window.exportMatchingData = exportMatchingData;
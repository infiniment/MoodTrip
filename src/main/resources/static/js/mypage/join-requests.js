// 방 입장 요청 관리 
document.addEventListener('DOMContentLoaded', function() {
    // UI 초기화
    initializeFilters();
    initializeSearch();
    initializeBulkActions();
    initializeRequestActions();
    initializeModals();
    updateStats();
});

/**
 * 필터 기능 초기화 
 */
function initializeFilters() {
    const roomFilter = document.getElementById('room-filter');
    const priorityFilter = document.getElementById('priority-filter');
    
    roomFilter.addEventListener('change', applyFilters);
    priorityFilter.addEventListener('change', applyFilters);
}

/**
 * 검색 기능 초기화
 */
function initializeSearch() {
    const searchInput = document.getElementById('search-requests');
    
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
 * 대량 작업 버튼 초기화 
 */
function initializeBulkActions() {
    const approveAllBtn = document.getElementById('approve-all-btn');
    const rejectAllBtn = document.getElementById('reject-all-btn');
    
    approveAllBtn.addEventListener('click', handleApproveAll);
    rejectAllBtn.addEventListener('click', handleRejectAll);
    
    // 체크박스 전체 선택 기능
    const selectAllCheckbox = document.getElementById('select-all');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.request-select');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
                toggleRequestSelection(checkbox);
            });
        });
    }
    
    // 개별 체크박스 이벤트
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('request-select')) {
            toggleRequestSelection(e.target);
            updateBulkButtonStates();
        }
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
    const closeBtn = modal.querySelector('.modal-close');
    const cancelBtn = modal.querySelector('.btn-cancel');
    
    // 모달 닫기 이벤트
    closeBtn.addEventListener('click', hideModal);
    cancelBtn.addEventListener('click', hideModal);
    
    // 오버레이 클릭으로 닫기
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            hideModal();
        }
    });
    
    // ESC 키로 모달 닫기
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
    const roomFilter = document.getElementById('room-filter').value;
    const priorityFilter = document.getElementById('priority-filter').value;
    const searchTerm = document.getElementById('search-requests').value.toLowerCase();
    
    const sections = document.querySelectorAll('.room-section');
    let visibleRequests = 0;
    
    sections.forEach(section => {
        const roomTitle = section.querySelector('.room-title').textContent;
        const requests = section.querySelectorAll('.request-item-detailed');
        let sectionHasVisibleRequests = false;
        
        // 방 필터 적용
        if (roomFilter !== 'all' && !roomTitle.includes(roomFilter)) {
            section.classList.add('hidden');
            return;
        }
        
        // 각 요청 필터링
        requests.forEach(request => {
            const name = request.querySelector('.request-name-large').textContent.toLowerCase();
            const message = request.querySelector('.request-message').textContent.toLowerCase();
            const priority = request.querySelector('.priority-badge').classList.contains('priority-high') ? 'high' : 'normal';
            
            let visible = true;
            
            // 우선순위 필터
            if (priorityFilter !== 'all' && priority !== priorityFilter) {
                visible = false;
            }
            
            // 검색어 필터
            if (searchTerm && !name.includes(searchTerm) && !message.includes(searchTerm)) {
                visible = false;
            }
            
            if (visible) {
                request.classList.remove('hidden');
                sectionHasVisibleRequests = true;
                visibleRequests++;
            } else {
                request.classList.add('hidden');
            }
        });
        
        // 섹션 표시/숨김
        if (sectionHasVisibleRequests) {
            section.classList.remove('hidden');
        } else {
            section.classList.add('hidden');
        }
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
        document.querySelector('.main-wrapper').appendChild(noResultsDiv);
    } else if (!show && noResultsDiv) {
        noResultsDiv.remove();
    }
}

/**
 * 요청 선택 토글 
 */
function toggleRequestSelection(checkbox) {
    const requestItem = checkbox.closest('.request-item-detailed');
    if (checkbox.checked) {
        requestItem.classList.add('selected');
    } else {
        requestItem.classList.remove('selected');
    }
}

/**
 * 대량 작업 버튼 상태 업데이트 
 */
function updateBulkButtonStates() {
    const checkedBoxes = document.querySelectorAll('.request-select:checked');
    const approveAllBtn = document.getElementById('approve-all-btn');
    const rejectAllBtn = document.getElementById('reject-all-btn');
    
    const hasSelection = checkedBoxes.length > 0;
    approveAllBtn.disabled = !hasSelection;
    rejectAllBtn.disabled = !hasSelection;
}

/**
 * 모든 요청 승인 처리
 */
function handleApproveAll() {
    const selectedRequests = document.querySelectorAll('.request-select:checked');
    
    if (selectedRequests.length === 0) {
        showToast('info', '승인할 요청을 선택해주세요.');
        return;
    }
    
    const names = Array.from(selectedRequests).map(checkbox => {
        return checkbox.closest('.request-item-detailed').querySelector('.request-name-large').textContent.split('\n')[0].trim();
    });
    
    showConfirmModal(
        '모든 요청 승인',
        `선택된 ${selectedRequests.length}개의 요청을 모두 승인하시겠습니까?`,
        names.join(', '),
        () => {
            // 백엔드 작업: 실제 승인 API 호출
            // API: POST /api/join-requests/approve-multiple
            // 데이터: { requestIds: [1, 2, 3] }
            
            processBulkApproval(selectedRequests);
        }
    );
}

/**
 * 모든 요청 거절 처리 (UI만 - 확인 모달 표시)
 */
function handleRejectAll() {
    const selectedRequests = document.querySelectorAll('.request-select:checked');
    
    if (selectedRequests.length === 0) {
        showToast('info', '거절할 요청을 선택해주세요.');
        return;
    }
    
    const names = Array.from(selectedRequests).map(checkbox => {
        return checkbox.closest('.request-item-detailed').querySelector('.request-name-large').textContent.split('\n')[0].trim();
    });
    
    showConfirmModal(
        '모든 요청 거절',
        `선택된 ${selectedRequests.length}개의 요청을 모두 거절하시겠습니까?`,
        names.join(', '),
        () => {
            // 백엔드 작업: 실제 거절 API 호출
            // API: POST /api/join-requests/reject-multiple
            // 데이터: { requestIds: [1, 2, 3] }
            
            processBulkRejection(selectedRequests);
        }
    );
}

/**
 * 개별 요청 승인 처리 (UI만)
 */
function handleApproveRequest(requestId, roomId, buttonElement) {
    const requestItem = buttonElement.closest('.request-item-detailed');
    const nameElement = requestItem.querySelector('.request-name-large');
    const userName = nameElement.firstChild.textContent.trim();
    
    showConfirmModal(
        '입장 승인',
        `${userName}님의 입장을 승인하시겠습니까?`,
        '',
        () => {
            processRequestApproval(requestId, roomId, requestItem);
        }
    );
}

/**
 * 개별 요청 거절 처리 (UI만)
 */
function handleRejectRequest(requestId, roomId, buttonElement) {
    const requestItem = buttonElement.closest('.request-item-detailed');
    const nameElement = requestItem.querySelector('.request-name-large');
    const userName = nameElement.firstChild.textContent.trim();
    
    showConfirmModal(
        '입장 거절',
        `${userName}님의 입장을 거절하시겠습니까?`,
        '',
        () => {
            // 백엔드 작업: 실제 거절 API 호출
            // API: POST /api/join-requests/{requestId}/reject
            // 추가 처리: 신청자에게 거절 알림 전송
            
            processRequestRejection(requestId, roomId, requestItem);
        }
    );
}

/**
 * 대량 승인 처리 (UI 애니메이션만)
 */
function processBulkApproval(selectedRequests) {
    // 승인된 사용자 이름들을 수집
    const approvedNames = [];
    
    // UI 로딩 상태 표시
    selectedRequests.forEach((checkbox, index) => {
        const requestItem = checkbox.closest('.request-item-detailed');
        
        // 사용자 이름 추출
        const nameElement = requestItem.querySelector('.request-name-large');
        const userName = nameElement.firstChild.textContent.trim();
        approvedNames.push(userName);
        
        setTimeout(() => {
            // 로딩 상태
            requestItem.classList.add('loading');
            
            setTimeout(() => {
                // 승인 완료 애니메이션
                requestItem.style.transform = 'translateX(100%)';
                requestItem.style.opacity = '0';
                requestItem.style.transition = 'all 0.3s ease';
                
                setTimeout(() => {
                    requestItem.remove();
                    
                    // 마지막 요청 처리 완료 시
                    if (index === selectedRequests.length - 1) {
                        // 이름들을 포함한 메시지 생성
                        let message;
                        if (approvedNames.length === 1) {
                            message = `${approvedNames[0]}님의 요청을 승인했습니다.`;
                        } else if (approvedNames.length <= 3) {
                            message = `${approvedNames.join(', ')}님의 요청을 승인했습니다.`;
                        } else {
                            message = `${approvedNames.slice(0, 2).join(', ')} 외 ${approvedNames.length - 2}명의 요청을 승인했습니다.`;
                        }
                        
                        showToast('success', message);
                        updateStats();
                        updateNotificationBadge();
                        checkEmptySections();
                    }
                }, 300);
            }, 1000);
        }, index * 200); // 순차적으로 처리
    });
}

/**
 * 대량 거절 처리 (UI 애니메이션만)
 */
function processBulkRejection(selectedRequests) {
    // 거절된 사용자 이름들을 수집
    const rejectedNames = [];
    
    // UI 로딩 상태 표시
    selectedRequests.forEach((checkbox, index) => {
        const requestItem = checkbox.closest('.request-item-detailed');
        
        // 사용자 이름 추출
        const nameElement = requestItem.querySelector('.request-name-large');
        const userName = nameElement.firstChild.textContent.trim();
        rejectedNames.push(userName);
        
        setTimeout(() => {
            // 로딩 상태
            requestItem.classList.add('loading');
            
            setTimeout(() => {
                // 거절 완료 애니메이션
                requestItem.style.transform = 'translateX(-100%)';
                requestItem.style.opacity = '0';
                requestItem.style.transition = 'all 0.3s ease';
                
                setTimeout(() => {
                    requestItem.remove();
                    
                    // 마지막 요청 처리 완료 시
                    if (index === selectedRequests.length - 1) {
                        // 이름들을 포함한 메시지 생성
                        let message;
                        if (rejectedNames.length === 1) {
                            message = `${rejectedNames[0]}님의 요청을 거절했습니다.`;
                        } else if (rejectedNames.length <= 3) {
                            message = `${rejectedNames.join(', ')}님의 요청을 거절했습니다.`;
                        } else {
                            message = `${rejectedNames.slice(0, 2).join(', ')} 외 ${rejectedNames.length - 2}명의 요청을 거절했습니다.`;
                        }
                        
                        showToast('info', message);
                        updateStats();
                        updateNotificationBadge();
                        checkEmptySections();
                    }
                }, 300);
            }, 1000);
        }, index * 200); // 순차적으로 처리
    });
}

/**
 * 개별 요청 승인 처리 (UI 애니메이션만)
 */
function processRequestApproval(requestId, roomId, requestItem) {
    const requestItem = buttonElement.closest('.request-item-detailed');
    const nameElement = requestItem.querySelector('.request-name-large');
    const userName = nameElement.firstChild.textContent.trim();
    // 버튼 비활성화 및 로딩 상태
    const buttons = requestItem.querySelectorAll('button');
    buttons.forEach(btn => {
        btn.disabled = true;
        if (btn.classList.contains('btn-approve')) {
            btn.textContent = '처리 중...';
        }
    });
    
    // 1초 후 승인 처리 완료 시뮬레이션
    setTimeout(() => {
        // 승인 완료 애니메이션
        requestItem.style.transform = 'translateX(100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showToast('success', `${userName}님의 입장을 승인했습니다.`);
            updateStats();
            updateNotificationBadge();
            checkEmptySections();
        }, 300);
    }, 1000);
}

/**
 * 개별 요청 거절 처리 (UI 애니메이션만)
 */
function processRequestRejection(requestId, roomId, requestItem) {
    const userName = requestItem.querySelector('.request-name-large').textContent.split('\n')[0].trim();
    
    // 버튼 비활성화 및 로딩 상태
    const buttons = requestItem.querySelectorAll('button');
    buttons.forEach(btn => {
        btn.disabled = true;
        if (btn.classList.contains('btn-reject')) {
            btn.textContent = '처리 중...';
        }
    });
    
    // 1초 후 거절 처리 완료 시뮬레이션
    setTimeout(() => {
        // 거절 완료 애니메이션
        requestItem.style.transform = 'translateX(-100%)';
        requestItem.style.opacity = '0';
        requestItem.style.transition = 'all 0.3s ease';
        
        setTimeout(() => {
            requestItem.remove();
            showToast('info', `${userName}님의 입장을 거절했습니다.`);
            updateStats();
            updateNotificationBadge();
            checkEmptySections();
        }, 300);
    }, 1000);
}

/**
 * 통계 업데이트 (UI만 - 실제로는 백엔드에서 계산)
 */
function updateStats() {
    // 백엔드 작업: API에서 실시간 통계 데이터 가져오기
    // API: GET /api/join-requests/stats
    // 응답: { totalRequests: 3, todayRequests: 1, urgentRequests: 1 }
    
    // UI만: 현재 DOM에서 계산 (임시)
    const totalRequests = document.querySelectorAll('.request-item-detailed:not(.hidden)').length;
    const urgentRequests = document.querySelectorAll('.priority-high').length;
    const todayRequests = 1; // 실제로는 서버에서 계산
    
    document.getElementById('total-requests').textContent = totalRequests;
    document.getElementById('today-requests').textContent = todayRequests;
    document.getElementById('urgent-requests').textContent = urgentRequests;
    
    // 대기 건수 업데이트
    updateWaitingCounts();
}

/**
 * 대기 건수 업데이트
 */
function updateWaitingCounts() {
    // 백엔드 작업: 각 방별 대기 중인 요청 수 계산
    // API: GET /api/rooms/{roomId}/pending-requests-count
    
    const sections = document.querySelectorAll('.room-section');
    
    sections.forEach(section => {
        const requests = section.querySelectorAll('.request-item-detailed:not(.hidden)');
        const waitingCount = section.querySelector('.waiting-count');
        
        if (requests.length > 0) {
            waitingCount.textContent = `${requests.length}건 대기`;
            waitingCount.classList.remove('no-requests');
        } else {
            waitingCount.textContent = '요청 없음';
            waitingCount.classList.add('no-requests');
        }
    });
}

/**
 * 알림 배지 업데이트 (UI만)
 */
function updateNotificationBadge() {
    // 백엔드 작업: 전체 대기 중인 요청 수 계산
    // API: GET /api/join-requests/total-pending-count
    
    const badge = document.getElementById('join-requests-badge');
    const totalRequests = document.querySelectorAll('.request-item-detailed:not(.hidden)').length;
    
    if (totalRequests > 0) {
        badge.textContent = totalRequests;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

/**
 * 빈 섹션 확인 및 메시지 표시 (UI만)
 */
function checkEmptySections() {
    const sections = document.querySelectorAll('.room-section');
    
    sections.forEach(section => {
        const requests = section.querySelectorAll('.request-item-detailed');
        const container = section.querySelector('.requests-container');
        
        if (requests.length === 0) {
            container.innerHTML = `
                <div class="empty-section">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M9 12l2 2 4-4"></path>
                    </svg>
                    <h4>모든 요청을 처리했습니다</h4>
                    <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
                </div>
            `;
        }
    });
}

/**
 * 확인 모달 표시 (UI만)
 */
function showConfirmModal(title, message, details, onConfirm) {
    const modal = document.getElementById('confirmModal');
    const titleElement = document.getElementById('confirmTitle');
    const messageElement = document.getElementById('confirmMessage');
    const detailsElement = document.getElementById('confirmDetails');
    const confirmButton = document.getElementById('confirmAction');
    
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
 * 모달 숨기기 (UI만)
 */
function hideModal() {
    const modal = document.getElementById('confirmModal');
    modal.classList.remove('show');
    document.body.style.overflow = '';
}

/**
 * 토스트 알림 표시 (UI만)
 */
function showToast(type, message) {
    // 백엔드 작업: 사용자 알림 로그 저장 (선택적)
    // API: POST /api/notifications/log
    // 데이터: { type, message, timestamp }
    
    const container = document.getElementById('toast-container');
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    container.appendChild(toast);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 300);
    }, 3000);
}

/**
 * 디바운스 유틸리티 함수 (퍼블리싱에 필요)
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
 * 페이지 언로드 시 정리 작업 (UI만)
 */
window.addEventListener('beforeunload', function() {
    // 진행 중인 애니메이션 정리
    // 타이머 정리
    document.body.style.overflow = '';
});

/**
 * 키보드 접근성 개선 (퍼블리싱에 필요)
 */
document.addEventListener('keydown', function(e) {
    // 엔터키로 버튼 활성화
    if (e.key === 'Enter' && e.target.tagName === 'BUTTON') {
        e.target.click();
    }
    
    // 스페이스바로 체크박스 토글
    if (e.key === ' ' && e.target.type === 'checkbox') {
        e.preventDefault();
        e.target.checked = !e.target.checked;
        e.target.dispatchEvent(new Event('change'));
    }
});

// ==========================================
// 백엔드 연동 시 필요한 추가 작업들 (주석으로 설명)
// ==========================================

/*
백엔드 연동 시 추가해야 할 주요 작업들:

1. API 호출 함수들:
   - fetchJoinRequests(roomId): 방별 요청 목록 가져오기
   - approveRequest(requestId): 요청 승인
   - rejectRequest(requestId): 요청 거절
   - getRequestStats(): 통계 데이터 가져오기
   - getNotificationCount(): 알림 개수 가져오기

2. 실시간 데이터 업데이트:
   - WebSocket 연결: 새로운 요청 실시간 수신
   - 폴링: 주기적으로 데이터 새로고침
   - Server-Sent Events: 서버에서 푸시하는 이벤트 처리

3. 에러 처리:
   - API 호출 실패 시 재시도 로직
   - 네트워크 오류 처리
   - 사용자 친화적 에러 메시지 표시

4. 상태 관리:
   - 요청 처리 중 상태 관리
   - 낙관적 업데이트 (Optimistic Updates)
   - 서버 상태와 클라이언트 상태 동기화

5. 보안:
   - CSRF 토큰 처리
   - 요청 권한 검증
   - XSS 방지를 위한 데이터 이스케이프

6. 성능 최적화:
   - 가상 스크롤링 (대량 데이터 처리 시)
   - 이미지 지연 로딩
   - 캐싱 전략

7. 알림 시스템:
   - 브라우저 푸시 알림
   - 이메일 알림 설정
   - 앱 내 알림 표시

8. 로그 및 분석:
   - 사용자 행동 추적
   - 성능 모니터링
   - 에러 로깅

예시 API 연동 코드:

async function approveRequest(requestId) {
    try {
        const response = await fetch(`/api/join-requests/${requestId}/approve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': getCsrfToken()
            },
            body: JSON.stringify({ requestId })
        });
        
        if (!response.ok) {
            throw new Error('승인 처리 실패');
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API 오류:', error);
        showToast('error', '요청 처리 중 오류가 발생했습니다.');
        throw error;
    }
}

실시간 업데이트 예시:

const socket = new WebSocket('ws://localhost:8080/join-requests');
socket.onmessage = function(event) {
    const data = JSON.parse(event.data);
    
    if (data.type === 'NEW_REQUEST') {
        addNewRequestToUI(data.request);
        updateNotificationBadge();
        showToast('info', '새로운 입장 요청이 있습니다.');
    }
};

*/
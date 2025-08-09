document.addEventListener('DOMContentLoaded', function() {
    initializeTabs();
    initializeButtons();
    initializeModals();
});

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

// SSR 방식 사용 시 타임리프 사용을 위한 버튼 초기화
function initializeButtons() {
    document.addEventListener('click', function(e) {

        // 방 나가기 버튼
        if (e.target.matches('.btn-exit-room')) {
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
            if (!e.target.disabled) {
                const roomId = e.target.getAttribute('data-room-id');
                const matchingItem = e.target.closest('.matching-item');
                const roomTitle = matchingItem.querySelector('.matching-title').textContent;

                handleManageRequestsClick(roomId, roomTitle);
            }
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

    });
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
            throw new Error('방 나가기 실패');
        }

        showNotification('success', `"${roomTitle}" 방에서 나갔습니다.`);

        setTimeout(() => {
            window.location.reload();
        }, 1500);

    } catch (error) {
        matchingItem.style.opacity = '1';
        matchingItem.style.pointerEvents = 'auto';
        showNotification('error', '방 나가기에 실패했습니다. 다시 시도해주세요.');
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
                'Content-Type': 'application/json',  // 🔥 헤더 추가
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


/**
 * 입장 요청 관리 모달
 */
function handleManageRequestsClick(roomId, roomTitle) {
    console.log(`🎛️ 입장 요청 관리 클릭 - 방ID: ${roomId}, 방제목: ${roomTitle}`);

    // 임시로 빈 요청 상태 표시
    showRequestsModal(roomId, roomTitle, []);
}

/**
 * 입장 요청 관리 모달 표시
 */
function showRequestsModal(roomId, roomTitle, requests) {
    const modal = document.getElementById('manageRequestsModal');
    if (!modal) return;

    const requestsList = modal.querySelector('.requests-list');

    // 모달 제목 업데이트
    modal.querySelector('.modal-header h3').textContent = `${roomTitle} - 입장 요청 관리`;

    // 요청 리스트 렌더링
    if (requests.length === 0) {
        requestsList.innerHTML = `
            <div class="empty-requests" style="text-align: center; padding: 2rem; color: #64748b;">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 64px; height: 64px; margin-bottom: 1rem; opacity: 0.5;">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
                <h4>대기 중인 요청이 없습니다</h4>
                <p>새로운 참가 요청이 오면 여기에 표시됩니다.</p>
            </div>
        `;
    }

    // 모달 표시
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
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

console.log('🎉 MoodTrip 매칭 정보 페이지 JavaScript 로드 완료! (SSR 방식)');
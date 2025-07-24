document.querySelectorAll('.custom-switch').forEach((switchEl, index) => {
    const bg = switchEl.querySelector('.switch-bg');
    const handle = switchEl.querySelector('.switch-handle');
    const input = switchEl.querySelector('input[type="checkbox"]');

    switchEl.addEventListener('click', () => {
        const isChecked = input.getAttribute('aria-checked') === 'true';

        if (isChecked) {
            // OFF 상태
            input.setAttribute('aria-checked', 'false');
            input.checked = false;
            bg.style.background = 'rgb(200, 202, 210)';
            handle.style.transform = 'translateX(2px)';
        } else {
            // ON 상태
            input.setAttribute('aria-checked', 'true');
            input.checked = true;
            bg.style.background = 'rgb(33, 34, 36)';
            handle.style.transform = 'translateX(20px)';
        }

        // 마케팅 정보 수신 동의 스위치들만 모달 표시 (인덱스 4, 5번이 마케팅 스위치)
        if (index === 4 || index === 5) {
            const notificationType = index === 4 ? 'sms' : 'email';
            const isNowOn = !isChecked; // 토글 후 상태
            showModal(isNowOn, notificationType);
        }
    });
});

// 모달 HTML 생성 함수
function createModalHTML(isAgreed, notificationType) {
    const modalTitle = notificationType === 'sms' 
        ? '혜택알림 SMS알림 동의안내' 
        : '혜택알림 이메일알림 동의안내';
    
    const processStatus = isAgreed ? '동의 처리완료' : '거부 처리완료';
    
    return `
        <div class="moodtrip-modal-root" style="position: fixed; z-index: 9999; inset: 0px;">
            <div aria-hidden="true" data-testid="backdrop" class="modal-wrapper"></div>
            <div role="dialog" tabindex="0" class="dialog-wrapper">
                <div data-testid="modal-container" class="modal-container-wrapper">
                    <div id="modal-content" class="modal-content-wrapper">
                        <div class="exit-button-wrapper">
                            <button color="default" role="button" data-testid="close-button" class="exit-button">
                                <span class="exit-button-span-wrapper">
                                    <span color="#303441" role="img" rotate="0" class="exit-button-span">
                                        <svg aria-hidden="true" fill="currentColor" focusable="false" height="24" preserveAspectRatio="xMidYMid meet" viewBox="0 0 24 24" width="24" class="exit-svg">
                                            <path d="M4.99999 6.4142C4.60946 6.02367 4.60946 5.39051 4.99999 4.99999C5.39051 4.60946 6.02368 4.60946 6.4142 4.99999L12 10.5858L17.5858 4.99999C17.9763 4.60946 18.6095 4.60946 19 4.99999C19.3905 5.39051 19.3905 6.02367 19 6.4142L13.4142 12L19 17.5858C19.3905 17.9763 19.3905 18.6095 19 19C18.6095 19.3905 17.9763 19.3905 17.5858 19L12 13.4142L6.4142 19C6.02368 19.3905 5.39051 19.3905 4.99999 19C4.60946 18.6095 4.60946 17.9763 4.99999 17.5858L10.5858 12L4.99999 6.4142Z" xmlns="http://www.w3.org/2000/svg"></path>
                                        </svg>
                                    </span>
                                </span>
                            </button>
                        </div>
                        <div class="benefit-notification-total-wrapper">
                            <div class="benefit-notification-wrapper">
                                <div class="benefit-notification">${modalTitle}</div>
                            </div>
                        </div>
                        <div data-testid="modal-body" class="modal-body-wrapper">
                            <div class="modal-body">
                                전송자 : 무드트립 <br>
                                수신동의 일시 : 2025년 06월 29일 00시 <br>
                                처리내용 : <b>${processStatus}</b>
                                <div class="additional-info">(알림설정에서 변경가능)</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// 모달 표시 함수
function showModal(isAgreed, notificationType) {
    // 기존 모달들 모두 제거
    const existingModals = document.querySelectorAll('.moodtrip-modal-root');
    existingModals.forEach(modal => modal.remove());
    
    // 새 모달 생성 및 추가
    const modalHTML = createModalHTML(isAgreed, notificationType);
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // 모달 닫기 이벤트 리스너 추가
    const newModal = document.querySelector('.moodtrip-modal-root:last-child');
    const closeButton = newModal.querySelector('[data-testid="close-button"]');
    const backdrop = newModal.querySelector('[data-testid="backdrop"]');
    
    closeButton.addEventListener('click', closeModal);
    backdrop.addEventListener('click', closeModal);
}

// 모달 닫기 함수
function closeModal() {
    const modals = document.querySelectorAll('.moodtrip-modal-root');
    modals.forEach(modal => modal.remove());
}

// 페이지 로드 시 기존 모달들 숨기기
document.addEventListener('DOMContentLoaded', () => {
    const existingModals = document.querySelectorAll('.moodtrip-modal-root');
    existingModals.forEach(modal => {
        modal.style.display = 'none';
    });
});
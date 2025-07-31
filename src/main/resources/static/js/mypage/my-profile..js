// my-profile.js - 백엔드 연결 버전
console.log('🚀 my-profile.js 로드됨 (백엔드 연결 버전)');

let originalNickname = ''; // 전역 변수로 원래 닉네임 저장
let originalSelfIntro = ''; // 원래 자기소개 저장용

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ DOM 로드 완료 - 이벤트 리스너 등록 시작');

    // ========================================
    // 🎯 닉네임 수정 기능
    // ========================================

    // 수정 버튼 클릭 시
    const modifyNicknameBtn = document.querySelector('.modify-nickname');
    if (modifyNicknameBtn) {
        modifyNicknameBtn.addEventListener('click', function () {
            console.log('닉네임 수정 버튼 클릭됨');

            document.querySelector('.modify-nickname-wrapper').hidden = true;
            document.querySelector('.complete-nickname-wrapper').hidden = false;

            // input 활성화 및 스타일 변경
            const inputSection = document.querySelector('.name-input-section');
            const input = document.querySelector('#username');

            // 현재 이름 저장
            originalNickname = input.value;

            inputSection.style.backgroundColor = '#ffffff';
            input.disabled = false;
            input.style.color = '#000000';
            input.focus(); // 포커스 추가
        });
    }

    // 취소 버튼 클릭 시
    const cancelNicknameBtn = document.querySelector('.modify-nickname-cancel');
    if (cancelNicknameBtn) {
        cancelNicknameBtn.addEventListener('click', function () {
            console.log('닉네임 수정 취소 버튼 클릭됨');

            // input 다시 비활성화 + 스타일 원래대로 복구
            const inputSection = document.querySelector('.name-input-section');
            inputSection.style.backgroundColor = '';

            const input = document.querySelector('#username');
            input.disabled = true;
            input.style.color = '';

            // 원래 닉네임 복원
            input.value = originalNickname;

            document.querySelector('.modify-nickname-wrapper').hidden = false;
            document.querySelector('.complete-nickname-wrapper').hidden = true;
        });
    }

    // ✅ 닉네임 저장 버튼 클릭 시 (백엔드 연결)
    const saveNicknameBtn = document.querySelector('.save-nickname');
    if (saveNicknameBtn) {
        saveNicknameBtn.addEventListener('click', function () {
            console.log('닉네임 저장 버튼 클릭됨');

            const input = document.querySelector('#username');
            const inputSection = document.querySelector('.name-input-section');
            const newNickname = input.value.trim();

            // 유효성 검사
            if (!newNickname) {
                alert('닉네임을 입력해주세요.');
                return;
            }

            if (newNickname.length > 30) {
                alert('닉네임은 30자 이내로 입력해주세요.');
                return;
            }

            const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
            if (!nicknameRegex.test(newNickname)) {
                alert('닉네임은 한글, 영문, 숫자만 입력 가능합니다.');
                return;
            }

            // 로딩 상태 표시
            const originalBtnText = saveNicknameBtn.textContent;
            saveNicknameBtn.textContent = '저장 중...';
            saveNicknameBtn.disabled = true;

            // ✅ 백엔드 API 호출
            fetch('/api/v1/members/me/nickname', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({nickname: newNickname})
            })
                .then(response => {
                    console.log('닉네임 수정 응답 상태:', response.status);
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('닉네임 수정 성공:', data);

                    // 저장 완료 UI 전환
                    input.disabled = true;
                    inputSection.style.backgroundColor = '';
                    input.style.color = '';
                    document.querySelector('.modify-nickname-wrapper').hidden = false;
                    document.querySelector('.complete-nickname-wrapper').hidden = true;

                    // 원래 닉네임 갱신
                    originalNickname = newNickname;

                    // 성공 메시지 표시
                    showSuccessMessage('닉네임이 성공적으로 수정되었습니다!');
                })
                .catch(error => {
                    console.error('닉네임 수정 실패:', error);
                    showErrorMessage('닉네임 수정 중 오류가 발생했습니다. 다시 시도해주세요.');

                    // 실패 시 원래 닉네임으로 복원
                    input.value = originalNickname;
                })
                .finally(() => {
                    // 로딩 상태 해제
                    saveNicknameBtn.textContent = originalBtnText;
                    saveNicknameBtn.disabled = false;
                });
        });
    }

    // ========================================
    // 🎯 자기소개 수정 기능
    // ========================================

    // 자기소개 수정 버튼 클릭
    const editSelfIntroBtn = document.querySelector('.edit-self-introduction');
    if (editSelfIntroBtn) {
        editSelfIntroBtn.addEventListener('click', function () {
            console.log('자기소개 수정 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const completeWrapper = document.querySelector('.complete-self-introduction-wrapper');
            const editWrapper = document.querySelector('.edit-self-introduction-wrapper');

            originalSelfIntro = textarea.value; // 현재 내용 저장
            textarea.disabled = false;
            textarea.style.backgroundColor = '#ffffff';
            textarea.style.color = '#000000';
            textarea.style.border = '1px solid #cccccc';
            textarea.focus();

            editWrapper.hidden = true;
            completeWrapper.hidden = false;
        });
    }

    // 자기소개 취소 버튼 클릭
    const cancelSelfIntroBtn = document.querySelector('.complete-self-introduction-wrapper .modify-nickname-cancel');
    if (cancelSelfIntroBtn) {
        cancelSelfIntroBtn.addEventListener('click', function () {
            console.log('자기소개 취소 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const completeWrapper = document.querySelector('.complete-self-introduction-wrapper');
            const editWrapper = document.querySelector('.edit-self-introduction-wrapper');

            textarea.value = originalSelfIntro; // 원래대로 복원
            textarea.disabled = true;
            textarea.style.backgroundColor = '';
            textarea.style.color = '';
            textarea.style.border = '';

            editWrapper.hidden = false;
            completeWrapper.hidden = true;
        });
    }

    // ✅ 자기소개 저장 버튼 클릭 (백엔드 연결 예정)
    const saveSelfIntroBtn = document.querySelector('.complete-self-introduction-wrapper .save-nickname');
    if (saveSelfIntroBtn) {
        saveSelfIntroBtn.addEventListener('click', function () {
            console.log('자기소개 저장 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const newSelfIntro = textarea.value.trim();

            // 유효성 검사
            if (newSelfIntro.length === 0) {
                alert("자기소개를 입력해주세요.");
                return;
            }

            if (newSelfIntro.length > 1000) {
                alert("자기소개는 1000자 이내로 입력해주세요.");
                return;
            }

            // 저장 완료 처리
            textarea.disabled = true;
            textarea.style.backgroundColor = '';
            textarea.style.color = '';
            textarea.style.border = '';

            document.querySelector('.edit-self-introduction-wrapper').hidden = false;
            document.querySelector('.complete-self-introduction-wrapper').hidden = true;

            originalSelfIntro = newSelfIntro;

            // TODO: 자기소개 수정 API 구현 후 활성화
            showSuccessMessage("자기소개가 임시로 저장되었습니다. (백엔드 API 연결 예정)");

            /*
            // 자기소개 수정 API 호출 (준비 중)
            fetch('/api/v1/profiles/me/introduce', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({profileBio: newSelfIntro})
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('자기소개 수정 성공:', data);
                showSuccessMessage("자기소개가 성공적으로 저장되었습니다!");
            })
            .catch(error => {
                console.error('자기소개 수정 실패:', error);
                showErrorMessage("자기소개 저장 중 오류가 발생했습니다. 다시 시도해주세요.");
                textarea.value = originalSelfIntro;
            });
            */
        });
    }

    // ========================================
    // 🎯 회원 탈퇴 모달 기능
    // ========================================

    // 모달 관련 요소들
    const withdrawModal = document.getElementById('withdrawModal');
    const cancelWithdrawBtn = document.getElementById('cancelWithdraw');
    const confirmWithdrawBtn = document.getElementById('confirmWithdraw');

    // 모달 열기 함수
    function openWithdrawModal() {
        console.log('탈퇴 모달 열기');
        withdrawModal.style.display = 'flex';
        setTimeout(() => {
            withdrawModal.classList.add('show');
            withdrawModal.classList.remove('hide');
        }, 10);
        document.body.style.overflow = 'hidden';
    }

    // 모달 닫기 함수
    function closeWithdrawModal() {
        console.log('탈퇴 모달 닫기');
        withdrawModal.classList.add('hide');
        withdrawModal.classList.remove('show');

        setTimeout(() => {
            withdrawModal.classList.remove('hide');
            withdrawModal.style.display = 'none';
            document.body.style.overflow = '';
        }, 300);
    }

    // 탈퇴하기 버튼 클릭 이벤트
    const withdrawBtn = document.querySelector('.withdraw-btn');
    if (withdrawBtn) {
        withdrawBtn.addEventListener('click', function (e) {
            console.log('탈퇴하기 버튼 클릭됨');
            e.preventDefault();
            openWithdrawModal();
        });
    }

    // 취소 버튼 클릭 이벤트
    if (cancelWithdrawBtn) {
        cancelWithdrawBtn.addEventListener('click', function () {
            closeWithdrawModal();
        });
    }

    // ✅ 탈퇴 확인 버튼 클릭 이벤트 (백엔드 연결)
    if (confirmWithdrawBtn) {
        confirmWithdrawBtn.addEventListener('click', function () {
            console.log('탈퇴 확인 버튼 클릭됨');

            // 로딩 상태 표시
            const originalBtnText = confirmWithdrawBtn.textContent;
            confirmWithdrawBtn.textContent = '처리 중...';
            confirmWithdrawBtn.disabled = true;

            // 임시 처리 (실제 API는 주석 처리)
            setTimeout(() => {
                closeWithdrawModal();
                setTimeout(() => {
                    alert('탈퇴 처리가 완료되었습니다. (백엔드 연결 예정)');
                    console.log('탈퇴 처리 요청 - 사용자 확인 완료');
                }, 300);

                // 로딩 상태 해제
                confirmWithdrawBtn.textContent = originalBtnText;
                confirmWithdrawBtn.disabled = false;
            }, 1000);

            /*
            // ✅ 회원 탈퇴 API 호출 (준비되면 주석 해제)
            fetch('/api/v1/members/me', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                console.log('회원 탈퇴 응답 상태:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('회원 탈퇴 성공:', data);

                closeWithdrawModal();

                setTimeout(() => {
                    alert('탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.');

                    // 로컬 스토리지 정리
                    localStorage.clear();
                    sessionStorage.clear();

                    // 메인 페이지로 이동
                    window.location.href = '/';
                }, 300);
            })
            .catch(error => {
                console.error('회원 탈퇴 실패:', error);
                showErrorMessage('탈퇴 처리 중 오류가 발생했습니다. 고객센터에 문의해 주세요.');
                closeWithdrawModal();
            })
            .finally(() => {
                // 로딩 상태 해제
                confirmWithdrawBtn.textContent = originalBtnText;
                confirmWithdrawBtn.disabled = false;
            });
            */
        });
    }

    // 모달 오버레이 클릭 시 닫기 (배경 클릭 시)
    if (withdrawModal) {
        withdrawModal.addEventListener('click', function(e) {
            if (e.target === withdrawModal) {
                closeWithdrawModal();
            }
        });
    }

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && withdrawModal && withdrawModal.classList.contains('show')) {
            closeWithdrawModal();
        }
    });

    // 모달 내부 클릭 시 이벤트 버블링 방지
    const modalContainer = document.querySelector('.modal-container');
    if (modalContainer) {
        modalContainer.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    console.log('✅ 모든 이벤트 리스너 등록 완료');
});

// ========================================
// 🎯 유틸리티 함수들
// ========================================

/**
 * ✅ 성공 메시지 표시 함수
 */
function showSuccessMessage(message) {
    // 기존 메시지 제거
    removeMessage();

    // 새 성공 메시지 생성
    const messageDiv = document.createElement('div');
    messageDiv.className = 'success-message';
    messageDiv.style.cssText = `
        background: #d4edda; 
        color: #155724; 
        padding: 12px 15px; 
        border-radius: 5px; 
        margin-bottom: 20px;
        border: 1px solid #c3e6cb;
        animation: fadeIn 0.3s ease-in;
    `;
    messageDiv.innerHTML = `<p style="margin: 0;">${message}</p>`;

    // 메시지 삽입
    const infoTitle = document.querySelector('.info-title');
    if (infoTitle) {
        infoTitle.after(messageDiv);
    }

    // 3초 후 자동 제거
    setTimeout(() => {
        if (messageDiv && messageDiv.parentNode) {
            messageDiv.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                if (messageDiv && messageDiv.parentNode) {
                    messageDiv.remove();
                }
            }, 300);
        }
    }, 3000);
}

/**
 * ✅ 에러 메시지 표시 함수
 */
function showErrorMessage(message) {
    // 기존 메시지 제거
    removeMessage();

    // 새 에러 메시지 생성
    const messageDiv = document.createElement('div');
    messageDiv.className = 'error-message';
    messageDiv.style.cssText = `
        background: #f8d7da; 
        color: #721c24; 
        padding: 12px 15px; 
        border-radius: 5px; 
        margin-bottom: 20px;
        border: 1px solid #f5c6cb;
        animation: fadeIn 0.3s ease-in;
    `;
    messageDiv.innerHTML = `<p style="margin: 0;">${message}</p>`;

    // 메시지 삽입
    const infoTitle = document.querySelector('.info-title');
    if (infoTitle) {
        infoTitle.after(messageDiv);
    }

    // 5초 후 자동 제거 (에러는 조금 더 오래)
    setTimeout(() => {
        if (messageDiv && messageDiv.parentNode) {
            messageDiv.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                if (messageDiv && messageDiv.parentNode) {
                    messageDiv.remove();
                }
            }, 300);
        }
    }, 5000);
}

/**
 * ✅ 기존 메시지 제거 함수
 */
function removeMessage() {
    const existingMessages = document.querySelectorAll('.success-message, .error-message');
    existingMessages.forEach(msg => {
        if (msg && msg.parentNode) {
            msg.remove();
        }
    });
}

// ========================================
// 🎯 CSS 애니메이션 추가
// ========================================

// 페이지 로드 시 CSS 애니메이션 스타일 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    @keyframes fadeOut {
        from { opacity: 1; transform: translateY(0); }
        to { opacity: 0; transform: translateY(-10px); }
    }
    
    .success-message, .error-message {
        animation: fadeIn 0.3s ease-in;
    }
`;
document.head.appendChild(style);
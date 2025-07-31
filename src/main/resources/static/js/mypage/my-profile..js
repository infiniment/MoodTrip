let originalNickname = ''; // 전역 변수로 원래 닉네임 저장
let originalSelfIntro = ''; // 원래 자기소개 저장용

// 수정 버튼 클릭 시
document.querySelector('.modify-nickname').addEventListener('click', function() {
    document.querySelector('.modify-nickname-wrapper').hidden = true;
    document.querySelector('.complete-nickname-wrapper').hidden = false;

    // input 활성화 및 스타일 변경
    const inputSection = document.querySelector('.name-input-section');
    const input = document.querySelector('#username');

    //현재 이름 저장
    originalNickname = input.value;

    inputSection.style.backgroundColor = '#ffffff';
    input.disabled = false;
    input.style.color = '#000000';
    
});

 // 취소 버튼 클릭 시
document.querySelector('.modify-nickname-cancel').addEventListener('click', function () {
    // input 다시 비활성화 + 스타일 원래대로 복구
    const inputSection = document.querySelector('.name-input-section');
    inputSection.style.backgroundColor = '';
    
    const input = document.querySelector('#username');
    input.disabled = true; // false에서 true로 변경
    input.style.color = '';

    // 원래 닉네임 복원
    input.value = originalNickname;

    document.querySelector('.modify-nickname-wrapper').hidden = false;
    document.querySelector('.complete-nickname-wrapper').hidden = true;
});

document.querySelector('.save-nickname').addEventListener('click', function () {
    const input = document.querySelector('#username');
    const inputSection = document.querySelector('.name-input-section');
    const newNickname = input.value.trim();

    // 유효성 검사
    const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
    if (!nicknameRegex.test(newNickname)) {
        alert('닉네임은 한글, 영문, 숫자만 입력 가능합니다.');
        return;
    }

    // 저장 완료 UI 전환
    input.disabled = true;
    inputSection.style.backgroundColor = '';
    input.style.color = '';
    document.querySelector('.modify-nickname-wrapper').hidden = false;
    document.querySelector('.complete-nickname-wrapper').hidden = true;

    // 원래 닉네임 갱신
    originalNickname = newNickname;

    fetch('/me/nickname', {
        method: 'PATCH',  // PATCH 메서드로 변경
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({nickname: newNickname})
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('닉네임 수정 실패');
            }
            return response.json();
        })
        .then(data => {
            console.log('닉네임 수정 성공:', data);
            alert('닉네임이 성공적으로 수정되었습니다.');
        })
        .catch(error => {
            console.error(error);
            alert('닉네임 수정 중 오류가 발생했습니다.');
        });


// 자기소개 수정 버튼 클릭
    document.querySelector('.edit-self-introduction').addEventListener('click', function () {
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

// 자기소개 취소 버튼 클릭
    document.querySelector('.complete-self-introduction-wrapper .modify-nickname-cancel').addEventListener('click', function () {
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

// 저장 버튼 클릭
    document.querySelector('.complete-self-introduction-wrapper .save-nickname').addEventListener('click', function () {
        const textarea = document.querySelector('#selfIntroduction');
        const newSelfIntro = textarea.value.trim();

        // 간단한 유효성 검사
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

        alert("자기소개가 임시로 저장되었습니다. (백엔드 연결 예정)");

        // 나중에 fetch API로 전송 (백엔드 연결 시)
        /*
        fetch('/api/self-introduction', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ selfIntroduction: newSelfIntro })
        }).then(response => {
            if (response.ok) {
                alert("자기소개가 저장되었습니다.");
            } else {
                alert("저장 실패. 다시 시도해주세요.");
            }
        }).catch(error => {
            console.error(error);
            alert("서버 오류가 발생했습니다.");
        });
        */
    });

// 모달 관련 요소들
    const withdrawModal = document.getElementById('withdrawModal');
    const cancelWithdrawBtn = document.getElementById('cancelWithdraw');
    const confirmWithdrawBtn = document.getElementById('confirmWithdraw');

// 모달 열기 함수
    function openWithdrawModal() {
        withdrawModal.classList.add('show');
        withdrawModal.classList.remove('hide');
        // 페이지 스크롤 방지
        document.body.style.overflow = 'hidden';
    }

// 모달 닫기 함수
    function closeWithdrawModal() {
        withdrawModal.classList.add('hide');
        withdrawModal.classList.remove('show');

        // 애니메이션 완료 후 display: none 처리
        setTimeout(() => {
            withdrawModal.classList.remove('hide');
            withdrawModal.style.display = 'none';
            // 페이지 스크롤 복원
            document.body.style.overflow = '';
        }, 300);
    }

// 탈퇴하기 버튼 클릭 이벤트 (기존 버튼)
    document.querySelector('.withdraw-btn').addEventListener('click', function (e) {
        e.preventDefault();
        openWithdrawModal();
    });

// 취소 버튼 클릭 이벤트
    cancelWithdrawBtn.addEventListener('click', function () {
        closeWithdrawModal();
    });

// 탈퇴 확인 버튼 클릭 이벤트
    confirmWithdrawBtn.addEventListener('click', function () {
        // 실제 탈퇴 처리 (백엔드 연결 시 주석 해제)
        /*
        fetch('/api/user/withdraw', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token') // 토큰 있는 경우
            }
        }).then(response => {
            if (response.ok) {
                // 성공 시 처리
                alert('탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.');
                // 로컬 스토리지 정리
                localStorage.clear();
                sessionStorage.clear();
                // 로그인 페이지로 이동
                window.location.href = '/login';
            } else {
                // 실패 시 처리
                alert('탈퇴 처리 중 오류가 발생했습니다. 고객센터에 문의해 주세요.');
                closeWithdrawModal();
            }
        }).catch(error => {
            console.error('탈퇴 오류:', error);
            alert('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            closeWithdrawModal();
        });
        */

        // 임시 처리 (백엔드 연결 전)
        setTimeout(() => {
            closeWithdrawModal();
            setTimeout(() => {
                alert('탈퇴 처리가 완료되었습니다. (백엔드 연결 예정)');
                console.log('탈퇴 처리 요청 - 사용자 확인 완료');
            }, 300);
        }, 500);
    });

// 모달 오버레이 클릭 시 닫기 (배경 클릭 시)
    withdrawModal.addEventListener('click', function (e) {
        if (e.target === withdrawModal) {
            closeWithdrawModal();
        }
    });

// ESC 키로 모달 닫기
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && withdrawModal.classList.contains('show')) {
            closeWithdrawModal();
        }
    });

// 모달 내부 클릭 시 이벤트 버블링 방지
    document.querySelector('.modal-container').addEventListener('click', function (e) {
        e.stopPropagation();
    });}
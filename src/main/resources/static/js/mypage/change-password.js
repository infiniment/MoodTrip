// DOM 요소들 선택 
const form = document.querySelector('.input-password-form-wrapper');
const currentPasswordInput = document.querySelector('.current-password');
const newPasswordInput = document.querySelector('.input-change-password');
const confirmPasswordInput = document.querySelector('.input-change-new-confirm-password');
const changeButton = document.querySelector('.change-button');

// 각 입력 필드의 오류 메시지 영역
const currentPasswordError = document.querySelector('[data-testid="currentPassword"]');
const newPasswordError = document.querySelector('[data-testid="newPassword"]');
const confirmPasswordError = document.querySelector('[data-testid="newConfirmPassword"]');

// 비밀번호 유효성 검사 함수
function validatePassword(password) {
   // 8자 이상, 영문 대소문자, 숫자, 특수문자 포함
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return passwordRegex.test(password);
}

// 오류 메시지 표시 함수
function showError(errorContainer, message) {
    errorContainer.innerHTML = `
        <div style="margin-top: 8px; color: #ef4444; font-size: 12px; line-height: 16px;">
            ${message}
        </div>`;
    errorContainer.style.display = 'block';
}

// 오류 메시지 숨기기 함수
function hideError(errorContainer) {
    errorContainer.innerHTML = '';
    errorContainer.style.display = 'none';
}

// 입력 필드 테두리 색상 변경 함수
function setInputBorderColor(input, isError) {
   // 각 입력 필드에 따라 해당하는 wrapper 찾기
    let wrapper;
    if (input.classList.contains('current-password')) {
        wrapper = document.querySelector('.input-current-password');
    } else if (input.classList.contains('input-change-password')) {
        wrapper = document.querySelector('.change-password');
    } else if (input.classList.contains('input-change-new-confirm-password')) {
        wrapper = document.querySelector('.change-new-confirm-passowrd');
    }

    if (wrapper) {
        if (isError) {
            wrapper.style.borderColor = '#ef4444';
            wrapper.style.borderWidth = '1px';
            wrapper.style.borderStyle = 'solid';
        } else {
            // 원래 테두리 스타일로 복원 (기존 CSS 적용)
            wrapper.style.borderColor = '';
            wrapper.style.borderWidth = '';
            wrapper.style.borderStyle = '';
        }
    }
}

// 성공 모달 표시 함수
function showSuccessModal() {
    const modal = document.getElementById('successModal');
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

// 성공 모달 숨기기 함수
function hideSuccessModal() {
    const modal = document.getElementById('successModal');
    modal.classList.remove('show');
    document.body.style.overflow = '';
}

// 현재 비밀번호 유효성 검사
function validateCurrentPassword() {
    const currentPassword = currentPasswordInput.value.trim();

    if (!currentPassword) {
        showError(currentPasswordError, '현재 비밀번호를 입력해주세요.');
        setInputBorderColor(currentPasswordInput, true);
        return false;
    }

    // 실제로는 서버에서 확인해야 하지만, 여기서는 임시로 가정
    // 예: 현재 비밀번호가 'oldpassword123'이 아닌 경우
    if (currentPassword !== 'oldpassword123') {
        showError(currentPasswordError, '현재 비밀번호가 일치하지 않습니다.');
        setInputBorderColor(currentPasswordInput, true);
        return false;
    }

    hideError(currentPasswordError);
    setInputBorderColor(currentPasswordInput, false);
    return true;
}

// 새 비밀번호 유효성 검사
function validateNewPassword() {
    const newPassword = newPasswordInput.value.trim();

    if (!newPassword) {
        showError(newPasswordError, '새 비밀번호를 입력해주세요.');
        setInputBorderColor(newPasswordInput, true);
        return false;
    }

    if (!validatePassword(newPassword)) {
        showError(newPasswordError, '8자 이상, 영문 대소문자, 숫자, 특수문자를 사용하세요.');
        setInputBorderColor(newPasswordInput, true);
        return false;
    }

    // 현재 비밀번호와 같은지 확인
    if (newPassword === currentPasswordInput.value.trim()) {
        showError(newPasswordError, '현재 비밀번호와 다른 비밀번호를 입력해주세요.');
        setInputBorderColor(newPasswordInput, true);
        return false;
    }

    hideError(newPasswordError);
    setInputBorderColor(newPasswordInput, false);
    return true;
}

// 비밀번호 확인 검사
function validateConfirmPassword() {
    const newPassword = newPasswordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();

    if (!confirmPassword) {
        showError(confirmPasswordError, '비밀번호를 한번 더 입력해주세요.');
        setInputBorderColor(confirmPasswordInput, true);
        return false;
    }

    if (newPassword !== confirmPassword) {
        showError(confirmPasswordError, '비밀번호를 확인해 주세요.');
        setInputBorderColor(confirmPasswordInput, true);
        return false;
    }

    hideError(confirmPasswordError);
    setInputBorderColor(confirmPasswordInput, false);
    return true;
}

// DOM이 로드된 후 이벤트 리스너 추가
document.addEventListener('DOMContentLoaded', () => {
    // 초기 상태에서 모든 오류 메시지 숨기기
    hideError(currentPasswordError);
    hideError(newPasswordError);
    hideError(confirmPasswordError);

    // 실시간 유효성 검사 (입력할 때마다)
    currentPasswordInput.addEventListener('blur', validateCurrentPassword);
    newPasswordInput.addEventListener('blur', () => {
        validateNewPassword();
        // 새 비밀번호가 변경되면 확인 비밀번호도 다시 검사
        if (confirmPasswordInput.value) {
            validateConfirmPassword();
        }
    });
    confirmPasswordInput.addEventListener('blur', validateConfirmPassword);

    // 입력 중일 때 오류 메시지 숨기기
    currentPasswordInput.addEventListener('input', () => {
        if (currentPasswordError.innerHTML) {
            hideError(currentPasswordError);
            setInputBorderColor(currentPasswordInput, false);
        }
    });

    newPasswordInput.addEventListener('input', () => {
        if (newPasswordError.innerHTML) {
            hideError(newPasswordError);
            setInputBorderColor(newPasswordInput, false);
        }
    });

    confirmPasswordInput.addEventListener('input', () => {
        if (confirmPasswordError.innerHTML) {
            hideError(confirmPasswordError);
            setInputBorderColor(confirmPasswordInput, false);
        }
    });

    // 모달 확인 버튼 이벤트
    document.getElementById('successConfirm').addEventListener('click', hideSuccessModal);

    // 모달 배경 클릭 시 닫기
    document.getElementById('successModal').addEventListener('click', function(e) {
        if (e.target === this) {
            hideSuccessModal();
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && document.getElementById('successModal').classList.contains('show')) {
            hideSuccessModal();
        }
    });

    // 폼 제출 시 전체 유효성 검사
    form.addEventListener('submit', (e) => {
        e.preventDefault(); // 기본 제출 동작 방지
        
        console.log('폼 제출 시도'); // 디버깅용
        
        // 모든 필드 유효성 검사
        const isCurrentPasswordValid = validateCurrentPassword();
        const isNewPasswordValid = validateNewPassword();
        const isConfirmPasswordValid = validateConfirmPassword();
        
        console.log('유효성 검사 결과:', {
            current: isCurrentPasswordValid,
            new: isNewPasswordValid,
            confirm: isConfirmPasswordValid
        }); // 디버깅용
        
        // 모든 검사가 통과하면 폼 제출
        if (isCurrentPasswordValid && isNewPasswordValid && isConfirmPasswordValid) {
            // 실제 서버로 데이터 전송
            console.log('비밀번호 변경 요청 전송');
            
            // alert 대신 모달 표시
            showSuccessModal();
            
            // 폼 초기화
            form.reset();
            hideError(currentPasswordError);
            hideError(newPasswordError);
            hideError(confirmPasswordError);
            setInputBorderColor(currentPasswordInput, false);
            setInputBorderColor(newPasswordInput, false);
            setInputBorderColor(confirmPasswordInput, false);
        } else {
            console.log('유효성 검사 실패'); // 디버깅용
        }
    });
});
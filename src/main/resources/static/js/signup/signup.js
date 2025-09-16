document.addEventListener("DOMContentLoaded", function () {
    // ---------- 라벨 플로팅/포커스 ----------
    const inputContainers = document.querySelectorAll('[data-testid="design-system--text-field-container"]');
    inputContainers.forEach(container => {
        const input = container.querySelector('input');
        const label = container.querySelector('[data-testid="design-system--lable-text"]');
        const wrapper = container.querySelector('[data-testid="design-system--lable-input"]');
        if (wrapper) wrapper.classList.add('input-container-fix');
        if (input) input.classList.add('input-textbox-padding');
        const toggleLabel = () => {
            if (input.value.trim() !== '' || document.activeElement === input) {
                label.classList.add('label-float');
            } else {
                label.classList.remove('label-float');
            }
        };
        toggleLabel();
        input.addEventListener('focus', () => { container.classList.add('input-hover-focus'); toggleLabel(); });
        input.addEventListener('blur', () => { container.classList.remove('input-hover-focus'); toggleLabel(); });
        input.addEventListener('input', toggleLabel);
    });

    // ---------- 전화번호 자동 하이픈 ----------
    const phoneInput = document.querySelector('input[name="phone"], input[name$="phone"]');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 3) e.target.value = value;
            else if (value.length <= 7) e.target.value = value.slice(0,3) + '-' + value.slice(3);
            else e.target.value = value.slice(0,3) + '-' + value.slice(3,7) + '-' + value.slice(7,11);
        });
    }

    // ---------- 이용약관, 전체동의, 경고 메시지 ----------
    const agreeAll = document.querySelector('input[name="agreeAll"], input[name="agree_all"]');
    const checkboxes = [
        ...document.querySelectorAll(
            'input[name$="terms"], input[name$="marketing"], input[name$="marketingInfo"], input[name="terms"], input[name="marketing"], input[name="marketingInfo"], input[name="marketing_info"]'
        )
    ];
    const termsLabel = document.querySelector('input[name$="terms"], input[name="terms"]')?.closest("label");
    const warningMessage = document.createElement("div");
    warningMessage.textContent = "무드트립 서비스 이용을 위해서 반드시 동의를 해주셔야 합니다.";
    warningMessage.className = "text-w-red-500 typo-body1 mt-1 ml-6 terms-warning";
    warningMessage.style.color = "#e52929";
    function updateCheckboxUI(input) {
        const fakeCheckbox = input.parentElement.querySelector('[role="checkbox-button"]');
        if (!fakeCheckbox) return;
        if (input.checked) {
            fakeCheckbox.classList.add('bg-primary-500');
            fakeCheckbox.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" class="text-white" width="16" height="16" fill="none" viewBox="0 0 24 24"><path stroke="white" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>`;
        } else {
            fakeCheckbox.classList.remove('bg-primary-500');
            fakeCheckbox.innerHTML = '';
        }
    }
    function updateAllCheckState(checked) {
        checkboxes.forEach(chk => { chk.checked = checked; updateCheckboxUI(chk); });
        if (agreeAll) updateCheckboxUI(agreeAll);
        const existing = document.querySelector('.terms-warning');
        if (!checked && termsLabel) {
            if (!existing) termsLabel.insertAdjacentElement("afterend", warningMessage);
        } else {
            if (existing) existing.remove();
        }
    }
    if (agreeAll) {
        agreeAll.addEventListener("change", function() { updateAllCheckState(this.checked); });
    }
    checkboxes.forEach(chk => {
        chk.addEventListener("change", function () {
            updateCheckboxUI(chk);
            if (agreeAll) {
                agreeAll.checked = checkboxes.every(c => c.checked);
                updateCheckboxUI(agreeAll);
            }
            const existing = document.querySelector('.terms-warning');
            if ((this.name.endsWith("terms") || this.name==="terms") && !this.checked && termsLabel) {
                if (!existing) termsLabel.insertAdjacentElement("afterend", warningMessage);
            } else if ((this.name.endsWith("terms") || this.name==="terms") && this.checked) {
                if (existing) existing.remove();
            }
        });
    });
    updateAllCheckState(agreeAll ? agreeAll.checked : false);

    // ---------- 폼 유효성 검사 ----------
    const form = document.querySelector('form[data-testid="signup-form-contents"]') || document.querySelector('form');
    if(form) {
        const errorMessages = {
            email: '이메일을 입력해 주세요.',
            userId: '아이디를 입력해 주세요.',
            nickname: '닉네임을 입력해 주세요.',
            phone: '전화번호를 입력해 주세요.',
            password: '비밀번호를 입력해 주세요.',
            passwordConfirm: '비밀번호를 한 번 더 입력해 주세요.'
        };
        function validateEmail(email) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email); }
        function validateNickname(nick) { return nick.length >= 2 && nick.length <= 10;}
        function validatePhone(val) { return /^010-\d{4}-\d{4}$/.test(val);}
        function validatePassword(password) { return password.length >= 8 && password.length <= 32;}
        function validatePasswordConfirm(password, passwordConfirm) { return password === passwordConfirm;}
        form.addEventListener('submit', function(e) {
            let hasError = false;
            form.querySelectorAll('.input-error-message').forEach(el => el.remove());
            [
                { name: 'email', selector: 'input[name$="email"], input[name="email"]', validate: validateEmail, message: '올바른 이메일 형식으로 입력해 주세요.' },
                { name: 'userId', selector: 'input[name$="userId"], input[name="userId"], input[name="username"]' },
                { name: 'nickname', selector: 'input[name$="nickname"], input[name="nickname"]', validate: validateNickname, message: '닉네임은 2자 이상 10자 이하로 입력해 주세요.' },
                { name: 'phone', selector: 'input[name$="phone"], input[name="phone"]', validate: validatePhone, message: '010-1234-5678 형식으로 입력해 주세요.' },
                { name: 'password', selector: 'input[name$="password"], input[name="password"]', validate: validatePassword, message: '비밀번호는 8자 이상 32자 이하로 입력해 주세요.' },
                { name: 'passwordConfirm', selector: 'input[name$="passwordConfirm"], input[name="passwordConfirm"]' }
            ].forEach(({ name, selector, validate, message }) => {
                const input = form.querySelector(selector);
                let errorMessage = '';
                if(input && !input.value.trim()) {
                    hasError = true;
                    errorMessage = errorMessages[name];
                } else if(input && validate && !validate(input.value)) {
                    hasError = true;
                    errorMessage = message;
                }
                if(name === 'passwordConfirm' && !errorMessage) {
                    const pw = form.querySelector('input[name$="password"], input[name="password"]');
                    if(!validatePasswordConfirm(pw.value, input.value)) {
                        hasError = true;
                        errorMessage = '비밀번호가 일치하지 않습니다.';
                    }
                }
                if(errorMessage) {
                    const errorSpan = document.createElement('span');
                    errorSpan.className = 'input-error-message textbox typo-body2 text-w-red-500';
                    errorSpan.textContent = errorMessage;
                    const parentDiv = input.closest('.flex-col') || input.parentElement;
                    const supportText = parentDiv.querySelector('[data-testid="design-system--support-text-container"]');
                    if(supportText) supportText.insertAdjacentElement('beforebegin', errorSpan);
                    else parentDiv.appendChild(errorSpan);
                }
            });
            // 필수 약관 체크
            const termsCheckbox = form.querySelector('input[name$="terms"], input[name="terms"]');
            const termsErrorDiv = document.getElementById("terms-error");
            if(termsErrorDiv) {
                termsErrorDiv.style.display = "none";
                termsErrorDiv.textContent = "";
            }
            if(termsCheckbox && !termsCheckbox.checked) {
                if(termsErrorDiv) {
                    termsErrorDiv.textContent = "이용약관에 동의해야 회원가입이 가능합니다.";
                    termsErrorDiv.style.display = "block";
                }
                hasError = true;
            }
            if (hasError) e.preventDefault();
        });
    }

    // ---------- 실시간 비밀번호 일치 체크 ----------
    const pw = document.querySelector('input[name$="password"], input[name="password"]');
    const pwConfirm = document.querySelector('input[name$="passwordConfirm"], input[name="passwordConfirm"]');
    const pwdErrorDiv = document.getElementById("password-error");
    function checkPasswordMatch() {
        if (pw && pwConfirm && pwdErrorDiv) {
            if (pw.value !== pwConfirm.value) {
                pwdErrorDiv.textContent = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
                pwdErrorDiv.style.display = "block";
            } else {
                pwdErrorDiv.textContent = "";
                pwdErrorDiv.style.display = "none";
            }
        }
    }
    if (pw && pwConfirm && pwdErrorDiv) {
        pw.addEventListener("input", checkPasswordMatch);
        pwConfirm.addEventListener("input", checkPasswordMatch);
    }

    // ---------- Hover 효과 ----------
    function initHoverEffect() {
        inputContainers.forEach(container => {
            const label = container.querySelector('[data-testid="design-system--lable-text"]');
            container.addEventListener('mouseenter', () => {
                container.classList.add('input-hover-focus');
                if (label) label.classList.add('label-float-hover');
            });
            container.addEventListener('mouseleave', () => {
                container.classList.remove('input-hover-focus');
                if (label) label.classList.remove('label-float-hover');
            });
        });
        const customButtons = document.querySelectorAll(
            '.box-border.flex.cursor-pointer.right-items-center.right-justify-center.border'
        );
        customButtons.forEach(btn => {
            btn.addEventListener('mouseenter', () => { btn.classList.add('button-hover-focus'); });
            btn.addEventListener('mouseleave', () => { btn.classList.remove('button-hover-focus'); });
        });
    }
    initHoverEffect();
});
document.getElementById('signupForm').addEventListener('submit', function(event){
    event.preventDefault();
    // fetch('/api/member/signup', ...)
});

// 페이지가 완전히 로드된 후 스크립트가 실행되도록 합니다.
window.onload = function() {
    // 현재 페이지의 URL에서 쿼리 파라미터를 가져옵니다.
    const urlParams = new URLSearchParams(window.location.search);

    // 'error' 라는 이름의 파라미터 값을 추출합니다.
    const errorMessage = urlParams.get('error');

    // errorMessage 변수에 값이 존재할 경우에만 알림창을 띄웁니다.
    if (errorMessage) {
        // 서버에서 URL 인코딩된 메시지를 원래의 한글로 디코딩합니다.
        const decodedErrorMessage = decodeURIComponent(errorMessage);
        alert(decodedErrorMessage);
    }
};
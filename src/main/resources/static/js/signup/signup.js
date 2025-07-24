//input 
document.addEventListener("DOMContentLoaded", function () {
    const inputContainers = document.querySelectorAll('[data-testid="design-system--text-field-container"]');

    inputContainers.forEach(container => {
        const input = container.querySelector('input');
        const label = container.querySelector('[data-testid="design-system--lable-text"]');
        const wrapper = container.querySelector('[data-testid="design-system--lable-input"]');

        // 초기 클래스 세팅
        wrapper.classList.add('input-container-fix');
        input.classList.add('input-textbox-padding');

        const toggleLabel = () => {
            if (input.value.trim() !== '' || document.activeElement === input) {
                label.classList.add('label-float');
            } else {
                label.classList.remove('label-float');
            }
        };
        toggleLabel(); // 초기 상태

        input.addEventListener('focus', () => {
            container.classList.add('input-hover-focus');
            toggleLabel();
        });

        input.addEventListener('blur', () => {
            container.classList.remove('input-hover-focus');
            toggleLabel();
        });

        input.addEventListener('input', toggleLabel);
    });
});


//경고 메시지

document.addEventListener("DOMContentLoaded", function () {
    const agreeAll = document.querySelector('input[name="agree_all"]');
    const checkboxes = [
        ...document.querySelectorAll('input[name="terms"], input[name="marketing"], input[name="marketing_info"]')
    ];
    const termsLabel = document.querySelector('input[name="terms"]').closest("label");

    // 경고 메시지 요소
    const warningMessage = document.createElement("div");
    warningMessage.textContent = "무드트립 서비스 이용을 위해서 반드시 동의를 해주셔야 합니다.";
    warningMessage.className = "text-w-red-500 typo-body1 mt-1 ml-6 terms-warning";
    warningMessage.style.color = "#e52929";

    // 체크 상태에 따라 가짜 체크 UI에 클래스 추가
    function updateCheckboxUI(input) {
        const fakeCheckbox = input.parentElement.querySelector('[role="checkbox-button"]');
        if (input.checked) {
            fakeCheckbox.classList.add('bg-primary-500'); // 배경색
            fakeCheckbox.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" class="text-white" width="16" height="16" fill="none" viewBox="0 0 24 24"><path stroke="white" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>`;
        } else {
            fakeCheckbox.classList.remove('bg-primary-500');
            fakeCheckbox.innerHTML = '';
        }
    }

    function updateAllCheckState(checked) {
        checkboxes.forEach(chk => {
            chk.checked = checked;
            updateCheckboxUI(chk);
        });

        updateCheckboxUI(agreeAll);

        // 필수 항목 경고 처리
        const existing = document.querySelector('.terms-warning');
        if (!checked) {
            if (!existing) {
                termsLabel.insertAdjacentElement("afterend", warningMessage);
            }
        } else {
            if (existing) existing.remove();
        }
    }

    agreeAll.addEventListener("change", function () {
        updateAllCheckState(this.checked);
    });

    checkboxes.forEach(chk => {
        chk.addEventListener("change", function () {
            updateCheckboxUI(chk);
            const allChecked = checkboxes.every(c => c.checked);
            agreeAll.checked = allChecked;
            updateCheckboxUI(agreeAll);

            // 필수 항목 경고
            const existing = document.querySelector('.terms-warning');
            if (this.name === "terms" && !this.checked) {
                if (!existing) {
                    termsLabel.insertAdjacentElement("afterend", warningMessage);
                }
            } else if (this.name === "terms" && this.checked) {
                if (existing) existing.remove();
            }
        });
    });

    // 초기 UI 반영
    updateAllCheckState(agreeAll.checked);
});



/*js 수정*/
document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form[data-testid="signup-form-contents"]');
  const errorMessages = {
    email: '이메일을 입력해 주세요.',
    username: '아이디를 입력해 주세요.',
    password: '비밀번호를 입력해 주세요.',
    password_confirm: '비밀번호를 한 번 더 입력해 주세요.'
  };

  form.addEventListener('submit', function (e) {
    let hasError = false;

    // 기존 에러 메시지 제거
    form.querySelectorAll('.input-error-message').forEach(el => el.remove());

    // 각 input 검사
    ['email', 'username', 'password', 'password_confirm'].forEach(name => {
      const input = form.querySelector(`input[name="${name}"]`);
      if (input && !input.value.trim()) {
        hasError = true;
        // 에러 메시지 생성
        const errorSpan = document.createElement('span');
        errorSpan.className = 'input-error-message textbox typo-body2 text-w-red-500';
        errorSpan.textContent = errorMessages[name];

        // input의 부모 div(.flex-col) 하단에 삽입
        const parentDiv = input.closest('.flex-col');
        // 지원 텍스트(예: "비즈니스용 이메일 사용을 권장합니다.")가 있으면 그 위에, 없으면 마지막에 추가
        const supportText = parentDiv.querySelector('[data-testid="design-system--support-text-container"]');
        if (supportText) {
          supportText.insertAdjacentElement('beforebegin', errorSpan);
        } else {
          parentDiv.appendChild(errorSpan);
        }
      }
    });

    if (hasError) {
      e.preventDefault();
    }
  });
});

//마우스 올릴 시
function initHoverEffect() {
    // Input 영역 hover 시 테두리/라벨 색상 변경
    const inputContainers = document.querySelectorAll('[data-testid="design-system--text-field-container"]');
    inputContainers.forEach(container => {
        const label = container.querySelector('[data-testid="design-system--lable-text"]');

        // 마우스 오버 시
        container.addEventListener('mouseenter', () => {
            container.classList.add('input-hover-focus');
            if (label) label.classList.add('label-float-hover');
        });
        // 마우스 아웃 시
        container.addEventListener('mouseleave', () => {
            container.classList.remove('input-hover-focus');
            if (label) label.classList.remove('label-float-hover');
        });
    });

    // 버튼 hover 시 테두리/텍스트 색상 변경
    const customButtons = document.querySelectorAll(
        '.box-border.flex.cursor-pointer.right-items-center.right-justify-center.border'
    );
    customButtons.forEach(btn => {
        btn.addEventListener('mouseenter', () => {
            btn.classList.add('button-hover-focus');
        });
        btn.addEventListener('mouseleave', () => {
            btn.classList.remove('button-hover-focus');
        });
    });
}

if (document.readyState !== 'loading') {
    initHoverEffect();
} else {
    document.addEventListener('DOMContentLoaded', initHoverEffect);
}

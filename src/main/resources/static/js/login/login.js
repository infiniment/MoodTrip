/******************************************************
 * 1. 인풋 라벨 플로팅(아이디/비밀번호 입력창 라벨 위로 이동 효과)
 ******************************************************/


function initInputLabelEffect() {
    const inputContainers = document.querySelectorAll('[data-testid="design-system--text-field-container"]');

    inputContainers.forEach(container => {
        const input = container.querySelector('input');
        const label = container.querySelector('[data-testid="design-system--lable-text"]');
        const wrapper = container.querySelector('[data-testid="design-system--lable-input"]');

        if (!input || !label || !wrapper) return;

        // 초기 클래스 세팅
        wrapper.classList.add('input-container-fix');
        input.classList.add('input-textbox-padding');

        // 라벨 플로팅 토글 함수
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
}

// DOMContentLoaded 또는 이미 로드된 경우 실행
if (document.readyState !== 'loading') {
    initInputLabelEffect();
} else {
    document.addEventListener('DOMContentLoaded', initInputLabelEffect);
}


/******************************************************
 * 2. Hover 효과 (인풋박스/버튼)
 ******************************************************/

function initHoverEffect() {
    // 1. 인풋 컨테이너 hover 효과
    document.querySelectorAll('[data-testid="design-system--text-field-container"]').forEach(container => {
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

    // 2. 버튼 hover 효과 (회원가입, 구글, 페이스북 등)
    document.querySelectorAll(
        '.box-border.flex.cursor-pointer.right-items-center.right-justify-center.border'
    ).forEach(btn => {
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

/******************************************************
 * 3. 로그인 상태 유지 체크박스 (체크 시 파란 아이콘 표시)
 ******************************************************/
document.addEventListener('DOMContentLoaded', function() {
  const rememberCheckbox = document.querySelector('input[type="checkbox"][name="remember"]');
  const svgIcon = rememberCheckbox.nextElementSibling.querySelector('svg');

  // 페이지 로드시 체크 상태에 따라 표시
  if (rememberCheckbox.checked) {
    svgIcon.style.visibility = 'visible';
  } else {
    svgIcon.style.visibility = 'hidden';
  }

  // 체크 상태 변경 시 SVG 표시 제어
  rememberCheckbox.addEventListener('change', function() {
    if (this.checked) {
      svgIcon.style.visibility = 'visible';
    } else {
      svgIcon.style.visibility = 'hidden';
    }
  });
});
/******************************************************
 * 4. 로그인 버튼 클릭 시 (에러 메시지 및 유효성 검사)
 *    (1) 기존 텍스트 에러 메시지 (빨간 안내문)
 ******************************************************/
//
// document.addEventListener("DOMContentLoaded", function () {
//   const loginBtn = document.getElementById("loginBtn");
//   const emailInput = document.querySelector('input[name="emailOrId"]');
//   const passwordInput = document.querySelector('input[name="password"]');
//   const emailError = document.getElementById("emailError");
//   const passwordError = document.getElementById("passwordError");
//
//   loginBtn.addEventListener("click", function (e) {
//     e.preventDefault();
//     let hasError = false;
//
//     if (!emailInput.value.trim()) {
//       emailError.style.display = "block";
//       hasError = true;
//     } else {
//       emailError.style.display = "none";
//     }
//
//     if (!passwordInput.value.trim()) {
//       passwordError.style.display = "block";
//       hasError = true;
//     } else {
//       passwordError.style.display = "none";
//     }
//
//     if (!hasError) {
//       // 실제 로그인 처리
//       console.log("로그인 시도");
//       // document.querySelector("form").submit();
//     }
//   });
// });

//
// /******************************************************
//  * 5. 로그인 버튼 클릭 시 (모달 팝업 에러 알림)
//  *    (2) 모달 창 활용 (입력 누락 시 경고 모달 표시)
//  ******************************************************/
// document.addEventListener("DOMContentLoaded", function () {
//   const loginBtn = document.querySelector('.loginBtn');
//   const emailLabel = document.querySelectorAll('[data-testid="design-system--lable-text"]')[0];
//   const passwordLabel = document.querySelectorAll('[data-testid="design-system--lable-text"]')[1];
//   const emailInput = document.querySelector('input[name="emailOrId"]');
//   const passwordInput = document.querySelector('input[name="password"]');
//   const modal = document.getElementById("inputErrorModal");
//   const closeModal = document.getElementById("closeInputModal");
//
//   loginBtn.addEventListener("click", function (e) {
//     e.preventDefault();
//     let hasError = false;
//
//     if (!emailInput.value.trim()) {
//       emailLabel.style.color = "#E52929";
//       hasError = true;
//     } else {
//       emailLabel.style.color = "";
//     }
//
//     if (!passwordInput.value.trim()) {
//       passwordLabel.style.color = "#E52929";
//       hasError = true;
//     } else {
//       passwordLabel.style.color = "";
//     }
//
//     if (hasError) {
//       modal.style.display = "flex";
//       return;
//     }
//   });
//
//   closeModal.addEventListener("click", function () {
//     modal.style.display = "none";
//   });
//
//   emailInput.addEventListener('input', function() {
//     emailLabel.style.color = "";
//   });
//   passwordInput.addEventListener('input', function() {
//     passwordLabel.style.color = "";
//   });
// });
//
// /******************************************************
//  * 6. 로그인 버튼 클릭 시 (모달 닫기 기능 + 배경 클릭 닫기 추가)
//  ******************************************************/

//
// document.addEventListener("DOMContentLoaded", function () {
//   const loginBtn = document.querySelector(".loginBtn");
//   const emailInput = document.querySelector('input[name="emailOrId"]');
//   const passwordInput = document.querySelector('input[name="password"]');
//   const emailLabel = document.querySelectorAll('[data-testid="design-system--lable-text"]')[0];
//   const passwordLabel = document.querySelectorAll('[data-testid="design-system--lable-text"]')[1];
//   const modal = document.getElementById("inputErrorModal");
//   const closeBtn = document.getElementById("closeInputModal");
//
//   loginBtn.addEventListener("click", function (e) {
//     e.preventDefault();
//     let hasError = false;
//
//     if (!emailInput.value.trim()) {
//       emailLabel.style.color = "#E52929";
//       hasError = true;
//     } else {
//       emailLabel.style.color = "";
//     }
//
//     if (!passwordInput.value.trim()) {
//       passwordLabel.style.color = "#E52929";
//       hasError = true;
//     } else {
//       passwordLabel.style.color = "";
//     }
//
//     // 모달 표시
//     if (hasError) {
//       modal.style.display = "flex";
//       return;
//     }
//
//     // TODO: 정상 로그인 처리
//   });
//
//   // 입력 시 오류 색상 제거
//   emailInput.addEventListener("input", () => emailLabel.style.color = "");
//   passwordInput.addEventListener("input", () => passwordLabel.style.color = "");
//
//   // 닫기 버튼
//   closeBtn.addEventListener("click", () => {
//     modal.style.display = "none";
//   });
//
//   // 배경 클릭 시 닫히도록
//   modal.addEventListener("click", function (e) {
//     if (e.target === modal) {
//       modal.style.display = "none";
//     }
//   });
// });

document.addEventListener("DOMContentLoaded", function () {
  const inputContainers = document.querySelectorAll('[data-testid="design-system--text-field-container"]');

  inputContainers.forEach(container => {
    const input = container.querySelector("input");
    const label = container.querySelector('[data-testid="design-system--lable-text"]') || container.querySelector("div.absolute");
    const wrapper = container.querySelector('[data-testid="design-system--lable-input"]') || container.querySelector("div.relative");

    if (!input || !label || !wrapper) return;

    // 초기 클래스 세팅
    wrapper.classList.add("input-container-fix");
    input.classList.add("input-textbox-padding");

    const toggleLabel = () => {
      if (input.value.trim() !== "" || input === document.activeElement) {
        label.classList.add("label-float");
      } else {
        label.classList.remove("label-float");
      }
    };

    // 초기 렌더링시 라벨 상태
    toggleLabel();

    input.addEventListener("focus", () => {
      container.classList.add("input-hover-focus");
      label.classList.add("label-float-hover");
      toggleLabel();
    });

    input.addEventListener("blur", () => {
      container.classList.remove("input-hover-focus");
      label.classList.remove("label-float-hover");
      toggleLabel();
    });

    input.addEventListener("input", toggleLabel);

    container.addEventListener("mouseenter", () => {
      container.classList.add("input-hover-focus");
      label.classList.add("label-float-hover");
    });

    container.addEventListener("mouseleave", () => {
      container.classList.remove("input-hover-focus");
      if (input !== document.activeElement && input.value.trim() === "") {
        label.classList.remove("label-float-hover");
      }
    });
  });
});

document.addEventListener("DOMContentLoaded", function () {
  // 기존 input floating 관련 코드 유지...

  // ✅ "비밀번호 재설정 메일 보내기" 버튼 클릭 시 임시 비밀번호 input 보이게
  const resetButton = document.querySelector('button[type="submit"]');
  const tempPasswordContainer = document.getElementById("temp-password-container");
  const tempPasswordCheckBtn = document.getElementById("temp-password-check-btn"); // ✅ 추가

  if (resetButton && tempPasswordContainer) {
    resetButton.addEventListener("click", function (e) {
      e.preventDefault(); // 실제 폼 제출 방지 (필요시 제거 가능)
      
      // 이메일 입력값 검증 (옵션)
      const emailInput = document.querySelector('input[name="email"]');
      if (!emailInput.value.trim()) {
        alert("이메일을 입력해주세요.");
        return;
      }

      // ✅ 두 번째 input창 보이게
      tempPasswordContainer.classList.remove("hidden");
      tempPasswordCheckBtn.classList.remove("hidden");


         // ✅ "임시 비밀번호 입력 확인" 버튼 클릭 시 페이지 이동
    tempPasswordCheckBtn.addEventListener("click", function () {
      window.location.href = "new-password.html";
    });

    });
  }
});

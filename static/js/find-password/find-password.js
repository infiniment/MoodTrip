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


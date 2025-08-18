document.addEventListener("DOMContentLoaded", function () {
    // 모든 emotion-item의 weight input 기본값 자동 포맷팅
    document.querySelectorAll('.emotion-item input[name="weight"]').forEach(input => {
        input.addEventListener("blur", function () {
            if (this.value && !isNaN(this.value)) {
                this.value = parseFloat(this.value).toFixed(1); // 소수점 한 자리로 통일
            }
        });
    });

    // 체크박스 클릭 시 weight input 활성화/비활성화
    document.querySelectorAll('.emotion-item').forEach(item => {
        const checkbox = item.querySelector('input[type="checkbox"]');
        const weightInput = item.querySelector('input[name="weight"]');

        if (checkbox && weightInput) {
            weightInput.disabled = !checkbox.checked; // 처음엔 체크 안 되어 있으면 비활성화

            checkbox.addEventListener("change", () => {
                weightInput.disabled = !checkbox.checked;
                if (!checkbox.checked) {
                    weightInput.value = ""; // 체크 해제 시 값 초기화
                }
            });
        }
    });
});

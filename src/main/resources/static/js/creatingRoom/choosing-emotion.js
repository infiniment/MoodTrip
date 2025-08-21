// 선택된 감정들을 저장할 배열
let selectedEmotions = [];

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    initializeCategoryButtons();
    restoreTemporaryEmotions();
    applyEmotionPrefill();
    initializeNextButton();
});

// 프리필 감정 자동 적용(최대 3개, 이미 선택한 건 유지)
function applyEmotionPrefill() {
    // 이미 사용자가 뭔가 골라놓았으면 건드리지 않음(뒤로가기 등 중복 적용 방지)
    if (selectedEmotions.length > 0) return;

    const p = getRoomPrefill();
    if (!p || !Array.isArray(p.emotions) || p.emotions.length === 0) return;

    // 최대 3개까지만 채움
    const remain = Math.max(0, 3 - selectedEmotions.length);
    if (remain === 0) return;

    p.emotions.slice(0, remain).forEach(name => {
        const em = findEmotionByName(name);
        if (em) addEmotionTag(em, 'preset');
    });

    // 현재 보이는 카테고리 그리드에 체크박스가 있다면 체크 상태와 동기화
    document.querySelectorAll('#emotionTagsGrid input.emotion-checkbox').forEach(chk => {
        const id = (chk.id || '').replace('emotion-', '');
        if (selectedEmotions.some(e => String(e.id) === id)) chk.checked = true;
    });

    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();
}

// 상세 페이지에서 sessionStorage에 저장한 프리필 읽기
function getRoomPrefill() {
    try { return JSON.parse(sessionStorage.getItem('room_prefill') || 'null'); }
    catch { return null; }
}

// emotionCategoryData 안에서 '감정 이름'으로 태그(id, 이름) 찾기
function findEmotionByName(name) {
    const target = String(name || '').trim();
    for (const cat of (window.emotionCategoryData || [])) {
        const em = (cat.emotions || []).find(e =>
            e.tagName === target || e.name === target || e.text === target
        );
        if (em) return { id: em.tagId, text: em.tagName };
    }
    return null;
}


// 카테고리 버튼 초기화
function initializeCategoryButtons() {
    const categoryButtons = document.querySelectorAll('.category-btn');
    const closeBtn = document.getElementById('closeTagsBtn');

    categoryButtons.forEach(button => {
        button.addEventListener('click', function() {
            const categoryId = this.getAttribute('data-category-id');
            showCategoryEmotions(categoryId, this);
        });
    });

    if (closeBtn) {
        closeBtn.addEventListener('click', hideCategoryEmotions);
    }
}

// 카테고리 감정 표시
function showCategoryEmotions(categoryId, button) {
    const displayArea = document.getElementById('emotionTagsDisplay');
    const titleElement = document.getElementById('selectedCategoryTitle');
    const gridElement = document.getElementById('emotionTagsGrid');

    // 버튼 UI 상태 업데이트
    document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');

    // 카테고리 정보 설정
    const categoryIcon = button.querySelector('.category-icon').textContent;
    const categoryName = button.querySelector('.category-name').textContent;
    titleElement.innerHTML = `${categoryIcon} ${categoryName}`;

    // 소분류 감정 가져오기
    const category = emotionCategoryData.find(c => c.emotionCategoryId == categoryId);
    const emotions = category ? category.emotions : [];

    // 감정 태그 렌더링
    gridElement.innerHTML = '';
    emotions.forEach(emotion => {
        const tagItem = createEmotionTagItem({ id: emotion.tagId, text: emotion.tagName }, categoryId);
        gridElement.appendChild(tagItem);
    });

    displayArea.style.display = 'block';
    displayArea.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// 감정 태그 아이템 생성
function createEmotionTagItem(emotion, category) {
    const tagItem = document.createElement('div');
    tagItem.className = 'emotion-tag-item';
    tagItem.setAttribute('data-emotion', emotion.text);

    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.id = `emotion-${emotion.id}`;
    checkbox.name = 'emotions';
    checkbox.value = emotion.text;
    checkbox.className = 'emotion-checkbox';

    // 이미 선택된 감정인지 확인
    if (isEmotionAlreadySelected(emotion.text)) {
        checkbox.checked = true;
    }

    const label = document.createElement('label');
    label.setAttribute('for', checkbox.id);
    label.className = 'emotion-label';
    label.textContent = emotion.text;

    // 체크박스 이벤트
    checkbox.addEventListener('change', function () {
        if (this.checked) {
            const added = addEmotionTag({ id: emotion.id, text: emotion.text }, 'preset');
            if (!added) {
                this.checked = false; // 3개 초과일 경우 체크 해제
            }
        } else {
            removeEmotionTag(emotion);
        }
    });


    tagItem.appendChild(checkbox);
    tagItem.appendChild(label);

    return tagItem;
}

// 카테고리 감정 숨기기
function hideCategoryEmotions() {
    const displayArea = document.getElementById('emotionTagsDisplay');

    // 모든 버튼 비활성화
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 표시 영역 숨기기
    displayArea.style.display = 'none';
}

// 감정 태그 추가
function addEmotionTag(emotion, type) {
    if (selectedEmotions.length >= 3 && !isEmotionAlreadySelected(emotion)) {
        alert("감정은 최대 3개까지 선택할 수 있습니다.");
        return false; // 추가 실패
    }

    if (isEmotionAlreadySelected(emotion)) {
        return false;
    }

    selectedEmotions.push({ id: emotion.id, text: emotion.text, type: type || 'preset' });

    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();
    return true; // 추가 성공
}

// 감정 태그 제거
function removeEmotionTag(emotion) {
    // 배열에서 제거
    selectedEmotions = selectedEmotions.filter(item => item.id !== emotion.id);

    // UI 업데이트
    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();
}

// 이미 선택된 감정인지 확인
function isEmotionAlreadySelected(emotion) {
    const id   = (emotion && typeof emotion === 'object') ? emotion.id   : null;
    const text = (emotion && typeof emotion === 'object') ? emotion.text : String(emotion);
    return selectedEmotions.some(item => (id != null ? item.id === id : item.text === text));
}
// 선택된 감정 태그 UI 업데이트
function updateSelectedEmotionsDisplay() {
    const container = document.getElementById('selectedEmotionsContainer');
    const list = document.getElementById('selectedEmotionsList');

    // 리스트 비우기
    list.innerHTML = '';

    // 선택된 감정이 없으면 컨테이너 숨기기
    if (selectedEmotions.length === 0) {
        container.style.display = 'none';
        return;
    }

    // 컨테이너 보이기
    container.style.display = 'block';

    // 각 감정 태그 생성
    selectedEmotions.forEach(emotion => {
        const tagElement = createEmotionTagElement(emotion);
        list.appendChild(tagElement);
    });
}

// 감정 태그 엘리먼트 생성
function createEmotionTagElement(emotion) {
    const tag = document.createElement('div');
    tag.className = `selected-emotion-tag ${emotion.type}`;
    tag.setAttribute('data-emotion', emotion.text);

    // 태그 텍스트
    const tagText = document.createElement('span');
    tagText.textContent = `# ${emotion.text}`;
    tag.appendChild(tagText);

    // 삭제 버튼
    const removeBtn = document.createElement('button');
    removeBtn.className = 'emotion-tag-remove';
    removeBtn.innerHTML = '×';
    removeBtn.setAttribute('type', 'button');
    removeBtn.setAttribute('aria-label', `${emotion.text} 태그 삭제`);

    // 삭제 버튼 클릭 이벤트
    removeBtn.addEventListener('click', function() {
        removeEmotionTag(emotion);

        // 현재 표시된 카테고리에서 해당 체크박스 해제
        const checkbox = document.querySelector(`input[value="${emotion.text}"]`);
        if (checkbox) {
            checkbox.checked = false;
        }
    });

    tag.appendChild(removeBtn);

    return tag;
}

// 카테고리 버튼 뱃지 업데이트
function updateCategoryButtonBadges() {
    emotionCategoryData.forEach(category => {
        const button = document.querySelector(`[data-category-id="${category.emotionCategoryId}"]`);
        if (!button) return;

        // 기존 뱃지 제거
        const existingBadge = button.querySelector('.emotion-count-badge');
        if (existingBadge) {
            existingBadge.remove();
        }

        const selectedCount = selectedEmotions.filter(emotion =>
            category.emotions.some(e => e.tagId === emotion.id)
        ).length;

        if (selectedCount > 0) {
            const badge = document.createElement('span');
            badge.className = 'emotion-count-badge';
            badge.textContent = selectedCount;
            button.appendChild(badge);
        }
    });
}

// 임시 저장된 감정 복원
function restoreTemporaryEmotions() {
    const tempEmotions = localStorage.getItem('temp_selected_emotions');
    if (tempEmotions) {
        try {
            const emotions = JSON.parse(tempEmotions);

            emotions.forEach(emotion => {
                // id, text, type 모두 포함된 객체로 전달
                addEmotionTag({ id: emotion.id, text: emotion.text }, emotion.type);
            });

            // 임시 저장 데이터 삭제
            localStorage.removeItem('temp_selected_emotions');
        } catch (e) {
            console.error('임시 저장된 감정 데이터 복원 실패:', e);
        }
    }
}

// 페이지를 벗어나면 감정 초기화
window.addEventListener('beforeunload', function() {
    if (selectedEmotions.length > 0) {
        localStorage.setItem('temp_selected_emotions', JSON.stringify(selectedEmotions));
    }
});

// 폼 제출 시 선택된 감정들을 hidden input에 추가
function prepareFormSubmission() {
    const form = document.getElementById('temporary_room_phase_1');

    // 기존 hidden input들 제거
    const existingInputs = form.querySelectorAll('input[name="selected_emotions"]');
    existingInputs.forEach(input => input.remove());

    // 선택된 감정들을 hidden input으로 추가
    selectedEmotions.forEach(emotion => {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'selected_emotions';
        hiddenInput.value =  JSON.stringify({ id: emotion.id, text: emotion.text });
        form.appendChild(hiddenInput);
    });
}

// 폼 유효성 검사
function validationPhase(form) {
    // 선택된 감정이 없으면 경고
    if (selectedEmotions.length === 0) {
        alert('최소 하나의 감정 태그를 선택해주세요.');
        return false;
    }

    // 폼 제출 준비
    prepareFormSubmission();

    // 선택된 감정들을 다음 페이지로 전달하기 위해 저장
    saveEmotionsForNextPage();

    return true;
}

// 뒤로가기 함수
function exitWithSubmit(formId, canSubmit) {
    // 현재 선택된 감정들을 로컬 스토리지에 저장
    if (selectedEmotions.length > 0) {
        localStorage.setItem('temp_selected_emotions', JSON.stringify(selectedEmotions));
    }

    // 이전 페이지로 이동
    window.location.href = '/companion-rooms/create';
}

// 다음 버튼 초기화
function initializeNextButton() {
    const nextButton = document.getElementById('nextButton');
    if (nextButton) {
        nextButton.addEventListener('click', function(e) {
            e.preventDefault();

            // 유효성 검사
            if (selectedEmotions.length === 0) {
                alert('최소 하나의 감정 태그를 선택해주세요.');
                return;
            }

            // 선택된 감정들 저장
            saveEmotionsForNextPage();

            // 다음 페이지로 이동
            goToNextPage();
        });
    }
}

// 다음 페이지로 전달할 감정 데이터 저장
function saveEmotionsForNextPage() {
    localStorage.setItem('selected_emotions', JSON.stringify(selectedEmotions));
    sessionStorage.setItem('selected_emotions', JSON.stringify(selectedEmotions));
    console.log('다음 페이지로 전달할 감정 데이터 저장 완료:', selectedEmotions);
}

// 다음 페이지로 이동
function goToNextPage() {
    window.location.href = "/companion-rooms/attraction";
}

// 저장된 감정 데이터 불러오기
function getSelectedEmotionsFromPreviousPage() {
    try {
        let emotions = localStorage.getItem('selected_emotions');
        if (emotions) {
            return JSON.parse(emotions);
        }

        emotions = sessionStorage.getItem('selected_emotions');
        if (emotions) {
            return JSON.parse(emotions);
        }

        return [];
    } catch (e) {
        console.error('저장된 감정 데이터 불러오기 실패:', e);
        return [];
    }
}

// 감정 데이터 정리
function clearEmotionData() {
    localStorage.removeItem('selected_emotions');
    sessionStorage.removeItem('selected_emotions');
    localStorage.removeItem('temp_selected_emotions');
}

// 도움말 모달 관련 함수들
function openHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeHelpModal();
        hideCategoryEmotions();
    }
});

// 빠른 선택 기능
function selectQuickEmotions(type) {
    const emotionSets = {
        'positive': ['기쁨', '즐거움', '행복', '만족'],
        'healing': ['평온', '안정', '휴식', '여유'],
        'adventure': ['모험', '스릴', '도전', '짜릿함'],
        'comfort': ['위로', '공감', '이해', '지지']
    };

    // 기존 선택 해제
    selectedEmotions = [];
    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();

    // 새로운 감정들 선택
    if (emotionSets[type]) {
        emotionSets[type].forEach(emotion => {
            addEmotionTag({id: emotion.id, text: emotion.text}, 'preset');
        });
    }

    // 모달 닫기
    closeHelpModal();
}

// 키보드 단축키 지원
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + Enter: 다음 버튼 클릭
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        const nextButton = document.getElementById('nextButton');
        if (nextButton) {
            nextButton.click();
        }
    }

    // 숫자키로 카테고리 선택 (1-9, 0)
    const keyNumber = parseInt(e.key);
    if (keyNumber >= 0 && keyNumber <= 9) {
        const categoryButtons = document.querySelectorAll('.category-btn');
        const index = keyNumber === 0 ? 9 : keyNumber - 1; // 0은 10번째 버튼
        if (categoryButtons[index]) {
            categoryButtons[index].click();
        }
    }
});


// 감정 통계 정보
function getEmotionStats() {
    const counts = {};
    const total = selectedEmotions.length;
    const custom = selectedEmotions.filter(e => e.type === 'custom').length;

    Object.keys(categoryEmotions).forEach(category => {
        const categoryEmotionTexts = categoryEmotions[category].map(e => e.text);
        const selectedCount = selectedEmotions.filter(emotion =>
            categoryEmotionTexts.includes(emotion.text)
        ).length;
        counts[category] = selectedCount;
    });

    return {
        total: total,
        preset: total - custom,
        custom: custom,
        byCategory: counts
    };
}

// 페이지 언로드 시 임시 저장
window.addEventListener('beforeunload', function() {
    if (selectedEmotions.length > 0) {
        localStorage.setItem('temp_selected_emotions', JSON.stringify(selectedEmotions));
    }
});

// 디버그 정보 (개발용)
function getDebugInfo() {
    return {
        selectedEmotions: selectedEmotions,
        stats: getEmotionStats(),
        categoryNames: categoryNames
    };
}
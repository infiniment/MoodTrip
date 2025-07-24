// 선택된 감정들을 저장할 배열
let selectedEmotions = [];

// 카테고리별 감정 데이터 (사진의 모든 카테고리 포함)
const categoryEmotions = {
    'healing': [
        { id: 'peace', text: '평온' },
        { id: 'stable', text: '안정' },
        { id: 'rest', text: '휴식' },
        { id: 'freedom', text: '자유' },
        { id: 'meditation', text: '명상' },
        { id: 'quiet', text: '고요' },
        { id: 'tired', text: '힘듦' },
        { id: 'leisure', text: '여유' }
    ],
    'romance': [
        { id: 'excitement', text: '설렘' },
        { id: 'romance', text: '낭만' },
        { id: 'love', text: '사랑' },
        { id: 'affection', text: '애정' },
        { id: 'sweetness', text: '달콤함' },
        { id: 'tender', text: '애틋함' },
        { id: 'longing', text: '그리움' },
        { id: 'emotion', text: '감성' }
    ],
    'adventure': [
        { id: 'adventure', text: '모험' },
        { id: 'thrill', text: '스릴' },
        { id: 'challenge', text: '도전' },
        { id: 'exciting', text: '짜릿함' },
        { id: 'courage', text: '용기' },
        { id: 'boldness', text: '대담함' },
        { id: 'dynamic', text: '역동성' },
        { id: 'energetic', text: '활기참' }
    ],
    'freedom': [
        { id: 'free', text: '자유' },
        { id: 'liberation', text: '해방' },
        { id: 'independence', text: '독립' },
        { id: 'openness', text: '개방' },
        { id: 'unbound', text: '무구속' },
        { id: 'escape', text: '탈출' },
        { id: 'refreshing', text: '시원함' },
        { id: 'release', text: '해소' }
    ],
    'joy': [
        { id: 'joy', text: '기쁨' },
        { id: 'fun', text: '즐거움' },
        { id: 'happiness', text: '행복' },
        { id: 'satisfaction', text: '만족' },
        { id: 'ecstasy', text: '희열감' },
        { id: 'cheerful', text: '즐거운' },
        { id: 'enthusiasm', text: '흥겨움' },
        { id: 'delight', text: '기뻐함' }
    ],
    'artistic': [
        { id: 'sensitivity', text: '감성적' },
        { id: 'inspiration', text: '영감' },
        { id: 'creativity', text: '창조성' },
        { id: 'artistic', text: '예술적' },
        { id: 'beauty', text: '아름다움' },
        { id: 'elegance', text: '우아함' },
        { id: 'aesthetic', text: '미적감각' },
        { id: 'poetic', text: '시적' }
    ],
    'effort': [
        { id: 'passion', text: '열정' },
        { id: 'energy', text: '에너지' },
        { id: 'vitality', text: '활력' },
        { id: 'powerful', text: '힘참' },
        { id: 'motivation', text: '의욕' },
        { id: 'driven', text: '동기부여' },
        { id: 'dynamic2', text: '생동감' },
        { id: 'lively', text: '발랄' }
    ],
    'growth': [
        { id: 'growth', text: '성장' },
        { id: 'reflection', text: '사색' },
        { id: 'contemplation', text: '고민' },
        { id: 'depth', text: '깊이' },
        { id: 'philosophical', text: '철학적' },
        { id: 'introspection', text: '내면탐구' },
        { id: 'serious', text: '진중함' },
        { id: 'wisdom', text: '지혜' }
    ],
    'comfort': [
        { id: 'comfort', text: '위로' },
        { id: 'empathy', text: '공감' },
        { id: 'understanding', text: '이해' },
        { id: 'support', text: '지지' },
        { id: 'care', text: '보살핌' },
        { id: 'warmth', text: '따뜻함' },
        { id: 'kindness', text: '친절' },
        { id: 'gentle', text: '부드러움' }
    ],
    'hope': [
        { id: 'hope', text: '희망' },
        { id: 'positive', text: '긍정' },
        { id: 'optimism', text: '낙관' },
        { id: 'bright', text: '밝음' },
        { id: 'expectation', text: '기대' },
        { id: 'confidence', text: '확신' },
        { id: 'faith', text: '믿음' },
        { id: 'trust', text: '신뢰' }
    ],
    'sadness': [
        { id: 'depression', text: '우울' },
        { id: 'sadness', text: '슬픔' },
        { id: 'loneliness', text: '외로움' },
        { id: 'melancholy', text: '우수' },
        { id: 'sorrow', text: '슬픔' },
        { id: 'grief', text: '상심' },
        { id: 'despair', text: '절망' },
        { id: 'gloom', text: '침울함' }
    ],
    'calm': [
        { id: 'peace2', text: '평안' },
        { id: 'intellectual', text: '지적' },
        { id: 'calm', text: '차분' },
        { id: 'serenity', text: '고요' },
        { id: 'tranquil', text: '안온함' },
        { id: 'stillness', text: '정적' },
        { id: 'composure', text: '침착함' },
        { id: 'mindful', text: '의식적' }
    ],
    'anger': [
        { id: 'anger', text: '분노' },
        { id: 'irritation', text: '짜증' },
        { id: 'frustration', text: '좌절' },
        { id: 'annoyance', text: '어긋남' },
        { id: 'rage', text: '격분' },
        { id: 'hostility', text: '적의' },
        { id: 'resentment', text: '원망' },
        { id: 'fury', text: '분함' }
    ],
    'quiz': [
        { id: 'quiz', text: '퀴즈' },
        { id: 'heavy', text: '무거움' },
        { id: 'serious2', text: '진지' },
        { id: 'difficult', text: '난해함' },
        { id: 'complex', text: '복잡함' },
        { id: 'mysterious', text: '신비' },
        { id: 'enigmatic', text: '수수께끼' },
        { id: 'profound', text: '심오함' }
    ],
    'honesty': [
        { id: 'honesty', text: '솔직함' },
        { id: 'wonder', text: '신기함' },
        { id: 'authenticity', text: '진실함' },
        { id: 'sincerity', text: '성의' },
        { id: 'genuine', text: '순수함' },
        { id: 'transparent', text: '투명함' },
        { id: 'openness2', text: '개방성' },
        { id: 'curious', text: '호기심' }
    ]
};

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    initializeCategoryButtons();
    initializeCustomEmotionInput();
    restoreTemporaryEmotions();
    initializeNextButton();
});

// 카테고리 버튼 초기화
function initializeCategoryButtons() {
    const categoryButtons = document.querySelectorAll('.category-btn');
    const closeBtn = document.getElementById('closeTagsBtn');
    
    categoryButtons.forEach(button => {
        button.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            showCategoryEmotions(category, this);
        });
    });
    
    // 닫기 버튼
    if (closeBtn) {
        closeBtn.addEventListener('click', function() {
            hideCategoryEmotions();
        });
    }
}

// 카테고리 감정 표시
function showCategoryEmotions(category, button) {
    const displayArea = document.getElementById('emotionTagsDisplay');
    const titleElement = document.getElementById('selectedCategoryTitle');
    const gridElement = document.getElementById('emotionTagsGrid');
    
    // 모든 버튼 비활성화
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 선택된 버튼 활성화
    button.classList.add('active');
    
    // 제목 설정
    const categoryIcon = button.querySelector('.category-icon').textContent;
    const categoryName = button.querySelector('.category-name').textContent;
    titleElement.innerHTML = `${categoryIcon} ${categoryName}`;
    
    // 감정 태그들 생성
    gridElement.innerHTML = '';
    const emotions = categoryEmotions[category] || [];
    
    emotions.forEach(emotion => {
        const tagItem = createEmotionTagItem(emotion, category);
        gridElement.appendChild(tagItem);
    });
    
    // 표시 영역 보이기
    displayArea.style.display = 'block';
    
    // 부드러운 스크롤
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
    checkbox.addEventListener('change', function() {
        if (this.checked) {
            addEmotionTag(emotion.text, 'preset');
        } else {
            removeEmotionTag(emotion.text);
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

// 커스텀 감정 입력 초기화
function initializeCustomEmotionInput() {
    const customInput = document.getElementById('customEmotionInput');
    
    customInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const emotion = this.value.trim();
            
            if (emotion && !isEmotionAlreadySelected(emotion)) {
                addEmotionTag(emotion, 'custom');
                this.value = ''; // 입력창 비우기
            } else if (isEmotionAlreadySelected(emotion)) {
                alert('이미 선택된 감정 태그입니다.');
                this.value = '';
            }
        }
    });
}

// 감정 태그 추가
function addEmotionTag(emotion, type) {
    // 중복 체크
    if (isEmotionAlreadySelected(emotion)) {
        return;
    }
    
    // 배열에 추가
    selectedEmotions.push({
        text: emotion,
        type: type
    });
    
    // UI 업데이트
    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();
}

// 감정 태그 제거
function removeEmotionTag(emotion) {
    // 배열에서 제거
    selectedEmotions = selectedEmotions.filter(item => item.text !== emotion);
    
    // UI 업데이트
    updateSelectedEmotionsDisplay();
    updateCategoryButtonBadges();
}

// 이미 선택된 감정인지 확인
function isEmotionAlreadySelected(emotion) {
    return selectedEmotions.some(item => item.text === emotion);
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
        removeEmotionTag(emotion.text);
        
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
    Object.keys(categoryEmotions).forEach(category => {
        const button = document.querySelector(`[data-category="${category}"]`);
        if (!button) return;
        
        // 기존 뱃지 제거
        const existingBadge = button.querySelector('.emotion-count-badge');
        if (existingBadge) {
            existingBadge.remove();
        }
        
        // 해당 카테고리의 선택된 감정 개수 계산
        const categoryEmotionTexts = categoryEmotions[category].map(e => e.text);
        const selectedCount = selectedEmotions.filter(emotion => 
            categoryEmotionTexts.includes(emotion.text)
        ).length;
        
        // 선택된 감정이 있으면 뱃지 추가
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
                addEmotionTag(emotion.text, emotion.type);
            });
            
            // 임시 저장 데이터 삭제
            localStorage.removeItem('temp_selected_emotions');
        } catch (e) {
            console.error('임시 저장된 감정 데이터 복원 실패:', e);
        }
    }
}

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
        hiddenInput.value = emotion.text;
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
    window.location.href = '/templates/creatingRoom/creatingRoom-detail.html';
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
    localStorage.setItem('selected_emotions_step2', JSON.stringify(selectedEmotions));
    sessionStorage.setItem('selected_emotions_step2', JSON.stringify(selectedEmotions));
    console.log('다음 페이지로 전달할 감정 데이터 저장 완료:', selectedEmotions);
}

// 다음 페이지로 이동
function goToNextPage() {
    window.location.href = "/templates/creatingRoom/choosing-tour.html"; 
}

// 저장된 감정 데이터 불러오기
function getSelectedEmotionsFromPreviousPage() {
    try {
        let emotions = localStorage.getItem('selected_emotions_step2');
        if (emotions) {
            return JSON.parse(emotions);
        }
        
        emotions = sessionStorage.getItem('selected_emotions_step2');
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
    localStorage.removeItem('selected_emotions_step2');
    sessionStorage.removeItem('selected_emotions_step2');
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
            addEmotionTag(emotion, 'preset');
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

// 카테고리 이름 매핑
const categoryNames = {
    'healing': '평온 & 힐링',
    'romance': '사랑 & 로맨스',
    'adventure': '모험 & 스릴',
    'freedom': '자유 & 해방',
    'joy': '기쁨 & 즐거움',
    'artistic': '감성 & 예술',
    'effort': '열정 & 에너지',
    'growth': '성장 & 사색',
    'comfort': '위로 & 공감',
    'hope': '희망 & 긍정',
    'sadness': '우울 & 슬픔',
    'calm': '평안 & 지적',
    'anger': '분노 & 짜증',
    'quiz': '퀴즈 & 무거움',
    'honesty': '솔직함 & 신기함'
};

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
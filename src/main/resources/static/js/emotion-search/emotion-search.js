// 선택된 태그들을 저장할 배열
let selectedTags = [];
const MAX_TAGS = 3; // 최대 태그 개수 제한

function toggleEmotionCategories() {
    const categories = document.getElementById('emotionCategories');
    const toggleText = document.querySelector('.toggle-text');
    const toggleIcon = document.querySelector('.toggle-icon');
    
    if (categories.classList.contains('show')) {
        categories.classList.remove('show');
        toggleText.textContent = '감정 카테고리 보기';
        toggleIcon.style.transform = 'rotate(0deg)';
    } else {
        categories.classList.add('show');
        toggleText.textContent = '감정 카테고리 숨기기';
        toggleIcon.style.transform = 'rotate(180deg)';
    }
}

// 정렬 드롭다운 토글
function toggleSortDropdown() {
    const dropdown = document.querySelector('.sort-dropdown');
    dropdown.classList.toggle('active');
}

// 정렬 옵션 선택
function selectSortOption(option, text) {
    const sortText = document.querySelector('.sort-text');
    const dropdown = document.querySelector('.sort-dropdown');
    const allOptions = document.querySelectorAll('.sort-option');
    
    // 현재 선택된 옵션 업데이트
    sortText.textContent = text;
    
    // active 클래스 업데이트
    allOptions.forEach(opt => opt.classList.remove('active'));
    event.target.classList.add('active');
    
    // 드롭다운 닫기
    dropdown.classList.remove('active');
    
    // 여기에 실제 정렬 로직 추가 가능
    console.log('정렬 기준:', option, text);
}

// 알림 메시지 표시 함수
function showNotification(message, type = 'info') {
    // 기존 알림 제거
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'warning' ? 'linear-gradient(135deg, #f59e0b, #d97706)' : 'linear-gradient(135deg, #005792, #001A2C)'};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        z-index: 1000;
        animation: slideIn 0.3s ease;
        max-width: 300px;
    `;
    
    notification.textContent = message;
    
    // 애니메이션 추가
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
    
    document.body.appendChild(notification);
    
    // 3초 후 제거
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
            if (style.parentNode) {
                style.remove();
            }
        }, 300);
    }, 3000);
}

// 태그 추가 함수 (3개 제한)
function addTag(tagText) {
    // 빈 문자열 체크
    if (!tagText.trim()) {
        return;
    }
    
    // 이미 존재하는 태그인지 확인
    if (selectedTags.includes(tagText)) {
        showNotification('이미 선택된 태그입니다.', 'warning');
        return;
    }
    
    // 최대 개수 체크
    if (selectedTags.length >= MAX_TAGS) {
        showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
        return;
    }
    
    // 태그 배열에 추가
    selectedTags.push(tagText);
    
    // UI 업데이트
    renderTags();
    updateTagCounter();
    updateInputState();
    
    // 입력창 클리어
    const inputArea = document.getElementById('emotionInput');
    if (inputArea) {
        inputArea.value = '';
    }
    
    // 성공 메시지
    showNotification(`'${tagText}' 태그가 추가되었습니다.`, 'info');
}

// 태그 제거 함수
function removeTag(tagText) {
    const index = selectedTags.indexOf(tagText);
    if (index > -1) {
        selectedTags.splice(index, 1);
        renderTags();
        updateTagCounter();
        updateInputState();
        showNotification(`'${tagText}' 태그가 제거되었습니다.`, 'info');
    }
}

// 태그 카운터 업데이트
function updateTagCounter() {
    const counter = document.querySelector('.tag-counter');
    if (counter) {
        counter.textContent = `${selectedTags.length}/${MAX_TAGS}`;
        
        // 색상 변경
        if (selectedTags.length >= MAX_TAGS) {
            counter.style.color = '#f59e0b';
            counter.style.fontWeight = '600';
        } else {
            counter.style.color = '#64748b';
            counter.style.fontWeight = '500';
        }
    }
}

// 입력 상태 업데이트 (최대 개수 도달 시 비활성화)
function updateInputState() {
    const inputArea = document.getElementById('emotionInput');
    const searchBtn = document.querySelector('.search-btn');
    const emotionTags = document.querySelectorAll('.emotion-tag');
    const popularTags = document.querySelectorAll('.popular-tag');
    
    const isMaxReached = selectedTags.length >= MAX_TAGS;
    
    if (inputArea) {
        inputArea.disabled = isMaxReached;
        if (isMaxReached) {
            inputArea.placeholder = ``;
        } else {
            inputArea.placeholder = selectedTags.length > 0 
                ? '추가할 감정을 입력하세요...' 
                : '원하는 감정을 선택해보세요 (예: 힐링, 설렘, 평온)';
            inputArea.style.background = '';
            inputArea.style.color = '';
        }
    }
    
    
    // 감정 태그들 비활성화
    emotionTags.forEach(tag => {
        if (isMaxReached) {
            tag.style.opacity = '0.5';
            tag.style.cursor = 'not-allowed';
            tag.style.pointerEvents = 'none';
        } else {
            tag.style.opacity = '';
            tag.style.cursor = '';
            tag.style.pointerEvents = '';
        }
    });
    
    // 인기 태그들 비활성화
    popularTags.forEach(tag => {
        if (isMaxReached) {
            tag.style.opacity = '0.5';
            tag.style.cursor = 'not-allowed';
            tag.style.pointerEvents = 'none';
        } else {
            tag.style.opacity = '';
            tag.style.cursor = '';
            tag.style.pointerEvents = '';
        }
    });
}

// 태그들을 화면에 렌더링
function renderTags() {
    const selectedTagsContainer = document.getElementById('selectedTags');
    if (!selectedTagsContainer) return;
    
    selectedTagsContainer.innerHTML = '';
    
    selectedTags.forEach(tag => {
        const tagElement = document.createElement('div');
        tagElement.className = 'tag-item';
        tagElement.innerHTML = `
            <span class="tag-text">${tag}</span>
            <button class="tag-remove" onclick="removeTag('${tag}')" title="태그 제거">×</button>
        `;
        selectedTagsContainer.appendChild(tagElement);
    });
}

// 입력창에서 엔터 입력 처리
function handleInputKeyPress(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        const inputValue = event.target.value.trim();
        if (inputValue) {
            addTag(inputValue);
        }
    }
}

// 감정 태그 클릭 시 추가
function addEmotionTag(emotion) {
    addTag(emotion);
}

// 인기 태그에서 # 제거하고 추가
function addPopularTag(tagText) {
    // # 제거하고 태그 추가
    const emotion = tagText.replace('#', '');
    addTag(emotion);
}

// 하트 버튼 토글 기능
function toggleLike(button) {
    button.classList.toggle('liked');
    
    if (button.classList.contains('liked')) {
        button.innerHTML = '♥'; // 채워진 하트
        button.style.color = '#ff4757'; // 빨간색
        button.style.background = 'rgba(255, 255, 255, 0.95)';
    } else {
        button.innerHTML = '♡'; // 빈 하트
        button.style.color = '#005792'; // 원래 색상
        button.style.background = 'rgba(255, 255, 255, 0.9)';
    }
}

// 모든 태그 초기화
function clearAllTags() {
    if (selectedTags.length === 0) {
        showNotification('선택된 태그가 없습니다.', 'warning');
        return;
    }
    
    selectedTags = [];
    renderTags();
    updateTagCounter();
    updateInputState();
    showNotification('모든 태그가 제거되었습니다.', 'info');
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 초기 상태 설정
    updateTagCounter();
    updateInputState();
    
    // 감정 태그 클릭 시 추가
    const emotionTags = document.querySelectorAll('.emotion-tag');
    
    emotionTags.forEach(tag => {
        tag.addEventListener('click', function() {
            // 최대 개수 체크
            if (selectedTags.length >= MAX_TAGS) {
                return;
            }
            
            const emotion = this.textContent.trim();
            addEmotionTag(emotion);
            
            // 선택된 태그 시각적 피드백
            this.style.background = 'linear-gradient(135deg, #005792 0%, #001A2C 100%)';
            this.style.color = 'white';
            this.style.transform = 'scale(0.95)';
            
            // 2초 후 원래 스타일로 복원
            setTimeout(() => {
                this.style.background = '';
                this.style.color = '';
                this.style.transform = '';
            }, 2000);
        });
    });

    // 인기 태그 클릭 시 추가
    const popularTags = document.querySelectorAll('.popular-tag');
    popularTags.forEach(tag => {
        tag.addEventListener('click', function() {
            // 최대 개수 체크
            if (selectedTags.length >= MAX_TAGS) {
                return;
            }
            
            const tagText = this.textContent.trim();
            addPopularTag(tagText);
            
            // 선택된 태그 시각적 피드백
            this.style.background = 'linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.8) 100%)';
            this.style.color = '#001A2C';
            this.style.transform = 'scale(0.95)';
            
            // 2초 후 원래 스타일로 복원
            setTimeout(() => {
                this.style.background = '';
                this.style.color = '';
                this.style.transform = '';
            }, 2000);
        });
    });
    
    // 입력창 엔터 키 이벤트
    const inputArea = document.getElementById('emotionInput');
    if (inputArea) {
        inputArea.addEventListener('keypress', handleInputKeyPress);
    }
    
    // 하트 버튼 이벤트 리스너 추가
    const likeButtons = document.querySelectorAll('.like-btn');
    likeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleLike(this);
        });
    });
    
    // 정렬 옵션 클릭 이벤트
    const sortOptions = document.querySelectorAll('.sort-option');
    sortOptions.forEach(option => {
        option.addEventListener('click', function() {
            const sortValue = this.getAttribute('data-sort');
            const sortText = this.textContent;
            selectSortOption(sortValue, sortText);
        });
    });
    
    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', function(e) {
        const dropdown = document.querySelector('.sort-dropdown');
        if (dropdown && !dropdown.contains(e.target)) {
            dropdown.classList.remove('active');
        }
    });
    
    // 검색 버튼 클릭 이벤트
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const inputValue = inputArea?.value.trim();
            if (inputValue && selectedTags.length < MAX_TAGS) {
                addTag(inputValue);
            }
            // 여기에 실제 검색 로직 추가
            if (selectedTags.length > 0) {
                console.log('검색할 태그들:', selectedTags);
                // 실제 검색 API 호출 등
            }
        });
    }
    
});
// HTML 문서가 모두 로드된 후, 이 안의 모든 코드를 안전하게 실행합니다.
document.addEventListener('DOMContentLoaded', function() {

    // --- 전역 변수 및 상수 선언 ---
    let selectedTags = [];
    const MAX_TAGS = 3;

    // --- 모든 함수 정의 ---

    // 감정 카테고리 토글 함수
    function toggleEmotionCategories() {
        const categoriesContainer = document.getElementById('emotionCategories');
        const toggleButton = document.querySelector('.category-toggle-btn');
        const toggleText = document.querySelector('.toggle-text');
        const toggleIcon = document.querySelector('.toggle-icon');

        if (!categoriesContainer || !toggleButton) {
            console.error('카테고리 토글에 필요한 요소를 찾을 수 없습니다.');
            return;
        }

        // 'active' 클래스를 토글하여 보이기/숨기기 상태를 제어합니다.
        const isActive = categoriesContainer.classList.toggle('active');
        toggleButton.classList.toggle('active', isActive);

        if (toggleText) {
            toggleText.textContent = isActive ? '감정 카테고리 숨기기' : '감정 카테고리 보기';
        }
    }

    // 정렬 드롭다운 토글 함수
    function toggleSortDropdown() {
        const dropdown = document.querySelector('.sort-dropdown');
        if (dropdown) {
            dropdown.classList.toggle('active');
        }
    }

    // 정렬 옵션 선택 함수
    function selectSortOption(event, option, text) {
        const sortText = document.querySelector('.sort-text');
        const dropdown = document.querySelector('.sort-dropdown');
        const allOptions = document.querySelectorAll('.sort-option');

        if (sortText) sortText.textContent = text;

        allOptions.forEach(opt => opt.classList.remove('active'));
        if (event && event.target) {
            event.target.classList.add('active');
        }

        if (dropdown) dropdown.classList.remove('active');
        console.log('정렬 기준:', option, text);
    }

    // 알림 메시지 표시 함수
    function showNotification(message, type = 'info') {
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.style.cssText = `
            position: fixed; top: 20px; right: 20px;
            background: ${type === 'warning' ? 'linear-gradient(135deg, #f59e0b, #d97706)' : 'linear-gradient(135deg, #005792, #001A2C)'};
            color: white; padding: 12px 20px; border-radius: 8px; font-size: 14px;
            font-weight: 500; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            z-index: 1000; animation: slideIn 0.3s ease; max-width: 300px;
        `;
        notification.textContent = message;

        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
            @keyframes slideOut { from { transform: translateX(0); opacity: 1; } to { transform: translateX(100%); opacity: 0; } }
        `;
        document.head.appendChild(style);
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease forwards';
            notification.addEventListener('animationend', () => {
                notification.remove();
                style.remove();
            });
        }, 3000);
    }

    // 태그 추가 함수 (3개 제한)
    function addTag(tagText) {
        if (!tagText.trim()) return;

        if (selectedTags.includes(tagText)) {
            showNotification('이미 선택된 태그입니다.', 'warning');
            return;
        }
        if (selectedTags.length >= MAX_TAGS) {
            showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
            return;
        }
        selectedTags.push(tagText);
        renderTags();
        updateInputState();
        const inputArea = document.getElementById('emotionInput');
        if (inputArea) inputArea.value = '';
        showNotification(`'${tagText}' 태그가 추가되었습니다.`, 'info');
    }

    // 태그 제거 함수
    function removeTag(tagText) {
        const index = selectedTags.indexOf(tagText);
        if (index > -1) {
            selectedTags.splice(index, 1);
            renderTags();
            updateInputState();
            showNotification(`'${tagText}' 태그가 제거되었습니다.`, 'info');
        }
    }

    // 태그 카운터 업데이트
    function updateTagCounter() {
        const counter = document.querySelector('.tag-counter');
        if (!counter) return;
        counter.textContent = `${selectedTags.length}/${MAX_TAGS}`;
        if (selectedTags.length >= MAX_TAGS) {
            counter.style.color = '#f59e0b';
            counter.style.fontWeight = '600';
        } else {
            counter.style.color = '#64748b';
            counter.style.fontWeight = '500';
        }
    }

    // 입력 상태 업데이트
    function updateInputState() {
        const inputArea = document.getElementById('emotionInput');
        const allTags = document.querySelectorAll('.emotion-tag, .popular-tag');
        const isMaxReached = selectedTags.length >= MAX_TAGS;

        if (inputArea) {
            inputArea.disabled = isMaxReached;
            inputArea.placeholder = isMaxReached ? '' : (selectedTags.length > 0 ? '추가할 감정을 입력하세요...' : '원하는 감정을 선택해보세요 (예: 힐링, 설렘, 평온)');
        }

        allTags.forEach(tag => {
            tag.style.opacity = isMaxReached ? '0.5' : '1';
            tag.style.cursor = isMaxReached ? 'not-allowed' : 'pointer';
            tag.style.pointerEvents = isMaxReached ? 'none' : 'auto';
        });
    }

    // 선택된 태그들을 화면에 렌더링
    function renderTags() {
        const selectedTagsContainer = document.getElementById('selectedTags');
        if (!selectedTagsContainer) return;
        selectedTagsContainer.innerHTML = '';
        selectedTags.forEach(tag => {
            const tagElement = document.createElement('div');
            tagElement.className = 'tag-item';
            tagElement.innerHTML = `<span class="tag-text">${tag}</span><button class="tag-remove" title="태그 제거">×</button>`;
            tagElement.querySelector('.tag-remove').onclick = () => removeTag(tag);
            selectedTagsContainer.appendChild(tagElement);
        });
    }

    // 감정 태그 클릭 시 추가
    function addEmotionTag(emotion) {
        addTag(emotion);
    }

    // 인기 태그 클릭 시 추가
    function addPopularTag(tagText) {
        const emotion = tagText.replace('#', '');
        addTag(emotion);
    }

    // 하트 버튼 토글
    function toggleLike(button) {
        button.classList.toggle('liked');
        if (button.classList.contains('liked')) {
            button.innerHTML = '♥';
            button.style.color = '#ff4757';
            button.style.background = 'rgba(255, 255, 255, 0.95)';
        } else {
            button.innerHTML = '♡';
            button.style.color = '#005792';
            button.style.background = 'rgba(255, 255, 255, 0.9)';
        }
    }

    // --- 이벤트 리스너 연결 ---

    // [핵심] 카테고리 토글 버튼
    const categoryToggleButton = document.querySelector('.category-toggle-btn');
    if (categoryToggleButton) {
        categoryToggleButton.addEventListener('click', toggleEmotionCategories);
    }

    // 정렬 버튼
    const sortButton = document.querySelector('.sort-btn');
    if (sortButton) {
        sortButton.addEventListener('click', toggleSortDropdown);
    }

    // 동적으로 생성된 감정 태그들
    document.querySelectorAll('.emotion-tag').forEach(tag => {
        tag.addEventListener('click', function() {
            addEmotionTag(this.textContent.trim());
        });
    });

    // 인기 태그들
    document.querySelectorAll('.popular-tag').forEach(tag => {
        tag.addEventListener('click', function() {
            addPopularTag(this.textContent.trim());
        });
    });

    // 입력창 엔터 키
    const inputArea = document.getElementById('emotionInput');
    if (inputArea) {
        inputArea.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                addTag(this.value.trim());
            }
        });
    }

    // 검색 버튼
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn && inputArea) {
        searchBtn.addEventListener('click', function() {
            addTag(inputArea.value.trim());
            if (selectedTags.length > 0) {
                console.log('검색할 태그들:', selectedTags);
            }
        });
    }

    // 하트 버튼들
    document.querySelectorAll('.like-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleLike(this);
        });
    });

    // 정렬 옵션들
    document.querySelectorAll('.sort-option').forEach(option => {
        option.addEventListener('click', function(event) {
            const sortValue = this.getAttribute('data-sort');
            const sortText = this.textContent;
            selectSortOption(event, sortValue, sortText);
        });
    });

    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', function(e) {
        const dropdown = document.querySelector('.sort-dropdown');
        if (dropdown && !e.target.closest('.sort-filter')) {
            dropdown.classList.remove('active');
        }
    });

    // --- 페이지 로드 시 초기 상태 설정 ---
    updateTagCounter();
    updateInputState();
});

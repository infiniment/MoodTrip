// 선택된 태그를 ID(key)와 이름(value)으로 저장하기 위해 Map 객체 사용
let selectedTags = new Map();
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

// 태그 추가 함수: ID와 이름을 함께 받도록 변경
function addTag(tagId, tagName) {
    // ID를 기준으로 이미 존재하는 태그인지 확인
    if (selectedTags.has(tagId)) {
        showNotification('이미 선택된 태그입니다.', 'warning');
        return;
    }

    // 최대 개수 체크
    if (selectedTags.size >= MAX_TAGS) {
        showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
        return;
    }

    // Map에 태그 ID와 이름 추가
    selectedTags.set(tagId, tagName);

    // UI 업데이트
    renderTags();
    updateTagCounter();
    updateInputState();

    showNotification(`'${tagName}' 태그가 추가되었습니다.`, 'info');
}

// 태그 제거 함수: ID를 기준으로 제거
function removeTag(tagId) {
    const tagName = selectedTags.get(tagId);
    if (selectedTags.delete(tagId)) {
        renderTags();
        updateTagCounter();
        updateInputState();
        showNotification(`'${tagName}' 태그가 제거되었습니다.`, 'info');
    }
}

// 태그 카운터 업데이트: selectedTags.size 사용
function updateTagCounter() {
    const counter = document.querySelector('.tag-counter');
    if (counter) {
        counter.textContent = `${selectedTags.size}/${MAX_TAGS}`;
        if (selectedTags.size >= MAX_TAGS) {
            counter.style.color = '#f59e0b';
            counter.style.fontWeight = '600';
        } else {
            counter.style.color = '#64748b';
            counter.style.fontWeight = '500';
        }
    }
}

// 입력 상태 업데이트: selectedTags.size 사용
function updateInputState() {
    const inputArea = document.getElementById('emotionInput');
    const isMaxReached = selectedTags.size >= MAX_TAGS;

    if (inputArea) {
        inputArea.disabled = isMaxReached;
        inputArea.placeholder = isMaxReached ? '' : '원하는 감정을 선택해보세요 (예: 힐링, 설렘, 평온)';
        inputArea.style.background = '';
        inputArea.style.color = '';
    }

    // 감정 태그들 비활성화
    document.querySelectorAll('.emotion-tag').forEach(tag => {
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
    document.querySelectorAll('.popular-tag').forEach(tag => {
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

// 태그들을 화면에 렌더링: Map 객체를 순회하며 UI 생성
function renderTags() {
    const selectedTagsContainer = document.getElementById('selectedTags');
    if (!selectedTagsContainer) return;

    selectedTagsContainer.innerHTML = '';

    selectedTags.forEach((name, id) => {
        const tagElement = document.createElement('div');
        tagElement.className = 'tag-item';
        // removeTag 함수에 ID를 전달하도록 수정
        tagElement.innerHTML = `
            <span class="tag-text">${name}</span>
            <button class="tag-remove" onclick="removeTag('${id}')" title="태그 제거">×</button>
        `;
        selectedTagsContainer.appendChild(tagElement);
    });
}

// 입력창에서 엔터 입력 처리 (ID 없는 직접 입력은 현재 검색 API와 연동 불가)
function handleInputKeyPress(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        const inputValue = event.target.value.trim();
        if (inputValue) {
            showNotification('직접 입력 태그는 현재 검색 기능과 연동되지 않습니다.', 'warning');
        }
        // inputArea.value = ''; // 입력창 비우기
    }
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
    if (selectedTags.size === 0) {
        showNotification('선택된 태그가 없습니다.', 'warning');
        return;
    }

    selectedTags.clear();
    renderTags();
    updateTagCounter();
    updateInputState();
    showNotification('모든 태그가 제거되었습니다.', 'info');
}

// 검색 결과를 바탕으로 여행지 카드를 동적으로 생성하는 함수
function renderResults(attractions) {
    const destinationGrid = document.querySelector('.destination-grid');
    destinationGrid.innerHTML = ''; // 기존 결과 초기화

    if (!attractions || attractions.length === 0) {
        destinationGrid.innerHTML = '<p class="no-results">선택한 감정과 일치하는 여행지가 없습니다.</p>';
        return;
    }

    attractions.forEach(attr => {
        const cardHTML = `
            <div class="destination-card">
                <div class="card-image">
                    <img src="${attr.firstImage || '/static/image/emotion-search/default-image.png'}" alt="${attr.title}" />
                    <button class="like-btn" onclick="toggleLike(this)">♡</button>
                </div>
                <div class="card-content">
                    <div class="destination-header">
                        <h3 class="destination-name">${attr.title}</h3>
                        <span class="destination-location">${attr.addr1 || '위치 정보 없음'}</span>
                    </div>
                    <p class="destination-description">${attr.description || '여행지에 대한 설명이 준비중입니다.'}</p>
                </div>
            </div>
        `;
        destinationGrid.insertAdjacentHTML('beforeend', cardHTML);
    });
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 초기 상태 설정
    updateTagCounter();
    updateInputState();

    // 감정 태그 클릭 시 ID와 이름을 함께 사용하여 태그 추가
    document.querySelectorAll('.emotion-tag').forEach(tag => {
        tag.addEventListener('click', function() {
            if (selectedTags.size >= MAX_TAGS) {
                // 최대 태그 개수 초과 시 알림
                showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
                return;
            }

            const tagId = this.getAttribute('data-tag-id');
            const tagName = this.textContent.trim();
            addTag(tagId, tagName);
        });
    });

    // 인기 태그 클릭 시 추가 (data-tag-id가 없으므로 검색 API와 연동이 어려움)
    const popularTags = document.querySelectorAll('.popular-tag');
    popularTags.forEach(tag => {
        tag.addEventListener('click', function() {
            showNotification('인기 태그는 현재 검색 기능과 연동되지 않습니다.', 'warning');
            // 만약 popular tags도 data-tag-id를 가질 수 있도록 HTML이 수정된다면,
            // emotion-tag와 동일한 addTag(tagId, tagName) 로직을 사용할 수 있습니다.
        });
    });

    // 입력창 엔터 키 이벤트 (직접 입력 태그는 현재 검색 API와 연동 불가)
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

    // 검색 버튼 클릭 이벤트: 실제 API 호출 로직으로 변경
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', async function() {
            if (selectedTags.size === 0) {
                showNotification('하나 이상의 감정을 선택해주세요.', 'warning');
                return;
            }

            // Map의 key(ID)들을 배열로 변환 후 콤마로 연결
            const ids = Array.from(selectedTags.keys()).join(',');

            try {
                // 서버에 검색 요청 (예: /api/attractions/search?emotionIds=1,5,12)
                const response = await fetch(`/api/attractions/search?emotionIds=${ids}`);
                if (!response.ok) {
                    throw new Error('서버에서 데이터를 가져오는 데 실패했습니다.');
                }
                const attractions = await response.json();

                // 결과 렌더링
                renderResults(attractions);

                // 검색 결과 섹션으로 스크롤 이동 (선택 사항)
                document.querySelector('.search-results-section').scrollIntoView({ behavior: 'smooth' });

            } catch (error) {
                console.error('Search Error:', error);
                showNotification('여행지 검색 중 오류가 발생했습니다.', 'warning');
            }
        });
    }

    // clearAllTags 함수를 호출하는 버튼이 있다면 여기에 이벤트 리스너 추가
    // 예: const clearBtn = document.getElementById('clearAllTagsBtn');
    // if (clearBtn) { clearBtn.addEventListener('click', clearAllTags); }

});

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
// 페이지 로드 시, 현재 정렬 상태를 드롭다운에 반영하는 로직 (선택 사항이지만 권장)
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    const currentSort = params.get('sort') || 'recommended'; // URL에 sort값이 없으면 recommended

    const activeOption = document.querySelector(`.sort-option[data-sort="${currentSort}"]`);
    if (activeOption) {
        document.querySelector('.sort-text').textContent = activeOption.textContent;

        document.querySelectorAll('.sort-option').forEach(opt => opt.classList.remove('active'));
        activeOption.classList.add('active');
    }

    // ... 기존의 다른 DOMContentLoaded 리스너 내용들 ...
});
// 정렬 옵션 선택
function selectSortOption(sortValue, sortText) {
    const sortTextElement = document.querySelector('.sort-text');
    const dropdown = document.querySelector('.sort-dropdown');

    // UI 업데이트
    sortTextElement.textContent = sortText;
    dropdown.classList.remove('active');

    // --- [핵심] URL을 변경하여 페이지를 다시 로드하는 로직 ---

    // 1. 현재 URL의 쿼리 파라미터를 가져옵니다.
    const currentParams = new URLSearchParams(window.location.search);

    // 2. 'sort' 파라미터 값을 새로 선택한 값으로 설정합니다.
    currentParams.set('sort', sortValue);

    // 3. 새로운 URL로 페이지를 이동시킵니다. (기존 tagId 등은 유지됩니다)
    window.location.href = window.location.pathname + '?' + currentParams.toString();
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
// function toggleLike(button) {
//     button.classList.toggle('liked');
//
//     if (button.classList.contains('liked')) {
//         button.innerHTML = '♥'; // 채워진 하트
//         button.style.color = '#ff4757'; // 빨간색
//         button.style.background = 'rgba(255, 255, 255, 0.95)';
//     } else {
//         button.innerHTML = '♡'; // 빈 하트
//         button.style.color = '#005792'; // 원래 색상
//         button.style.background = 'rgba(255, 255, 255, 0.9)';
//     }
// }

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
        // 서버에서 isLikedByCurrentUser와 같은 이름으로 현재 사용자의 찜 상태를 전달받아야 합니다.
        const isLiked = attr.isLikedByCurrentUser || false;

        const cardHTML = `
            <a href="/attractions/detail/${attr.contentId}" class="destination-card-link">
                <div class="destination-card">
                    <div class="card-image">
                        <img src="${attr.firstImage || '/static/image/emotion-search/default-image.png'}" alt="${attr.title}" />
                        
                        <button class="like-btn ${isLiked ? 'liked' : ''}" data-attraction-id="${attr.attractionId}">
                            <span>${isLiked ? '♥' : '♡'}</span>
                        </button>
                    </div>
                    <div class="card-content">
                        <div class="destination-header">
                            <h3 class="destination-name">${attr.title}</h3>
                            <span class="destination-location">${attr.addr1 || '위치 정보 없음'}</span>
                        </div>
                        <p class="destination-description">${attr.description || '여행지에 대한 설명이 준비중입니다.'}</p>
                    </div>
                </div>
            </a>    
        `;
        destinationGrid.insertAdjacentHTML('beforeend', cardHTML);
    });
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 초기 상태 설정
    updateTagCounter();
    updateInputState();

    const destinationGrid = document.querySelector('.destination-grid');
    if (destinationGrid) {
        destinationGrid.addEventListener('click', function(event) {
            // 클릭된 요소가 .like-btn이 맞는지 확인
            const likeButton = event.target.closest('.like-btn');
            if (likeButton) {
                event.preventDefault();
                event.stopPropagation();
                handleLikeClick(likeButton); // 2단계에서 추가한 함수 호출
            }
        });
    }
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
    // const likeButtons = document.querySelectorAll('.like-btn');
    // likeButtons.forEach(button => {
    //     button.addEventListener('click', function(e) {
    //         e.preventDefault();
    //         e.stopPropagation();
    //         toggleLike(this);
    //     });
    // });


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

    // 검색 버튼 클릭 이벤트(실질적인 로직 감정검색 -> 가중치 기반 관광지)
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


                const resultsSubtitle = document.querySelector('.results-subtitle');
                if (resultsSubtitle) {
                    resultsSubtitle.textContent = `${attractions.length}개 결과가 있어요`;
                }


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






});




async function handleLikeClick(button) {
    const attractionId = button.dataset.attractionId;
    if (!attractionId) {
        console.error('Attraction ID not found!');
        return;
    }

    const isLiked = button.classList.contains('liked');
    const method = isLiked ? 'DELETE' : 'POST';
    const url = `/api/likes/${attractionId}`; // 백엔드 찜 API 주소

    try {
        const response = await fetch(url, {
            method: method,
            // headers: { 'X-CSRF-TOKEN': '...' } // Spring Security CSRF 보호 사용 시 필요
        });

        if (response.ok) {
            button.classList.toggle('liked');

            // 버튼 안의 span 태그를 찾아서 내용만 변경
            const heartSpan = button.querySelector('span');
            if (heartSpan) {
                // 버튼에 'liked' 클래스가 있는지 여부로 하트 모양 결정
                heartSpan.textContent = button.classList.contains('liked') ? '♥' : '♡';   } else {
                button.innerHTML = '♡';
                button.style.color = '#005792'; // 기본 색상으로 변경
            }
        } else {
            // 서버가 오류를 반환한 경우
            const errorData = await response.json().catch(() => null);
            const errorMessage = errorData?.message || '요청에 실패했습니다.';
            showNotification(errorMessage, 'warning');
        }
    } catch (error) {
        console.error('Like Error:', error);
        showNotification('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', 'warning');
    }
}

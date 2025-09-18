// 선택된 태그를 ID(key)와 이름(value)으로 저장하기 위해 Map 객체 사용
let selectedTags = new Map();
const MAX_TAGS = 3; // 최대 태그 개수 제한

// ✅ 페이징 상태
let page = 0;
let size = 12;
let totalPages = 0;
let totalElements = 0;

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

// 페이지 로드 시, 현재 정렬 상태를 드롭다운에 반영
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    const currentSort = params.get('sort') || 'recommended';

    const activeOption = document.querySelector(`.sort-option[data-sort="${currentSort}"]`);
    if (activeOption) {
        document.querySelector('.sort-text').textContent = activeOption.textContent;

        document.querySelectorAll('.sort-option').forEach(opt => opt.classList.remove('active'));
        activeOption.classList.add('active');
    }

    // 초기 상태 설정
    updateTagCounter();
    updateInputState();

    const destinationGrid = document.querySelector('.destination-grid');
    if (destinationGrid) {
        destinationGrid.addEventListener('click', function(event) {
            const likeButton = event.target.closest('.like-btn');
            if (likeButton) {
                event.preventDefault();
                event.stopPropagation();
                handleLikeClick(likeButton);
            }
        });
    }

    // 감정 태그 클릭 시 ID와 이름을 함께 사용하여 태그 추가
    document.querySelectorAll('.emotion-tag').forEach(tag => {
        tag.addEventListener('click', function() {
            if (selectedTags.size >= MAX_TAGS) {
                showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
                return;
            }

            const tagId = this.getAttribute('data-tag-id');
            const tagName = this.textContent.trim();
            addTag(tagId, tagName);
        });
    });

    // 인기 태그 클릭 시 (data-tag-id 없으면 연동 불가)
    const popularTags = document.querySelectorAll('.popular-tag');
    popularTags.forEach(tag => {
        tag.addEventListener('click', function() {
            showNotification('인기 태그는 현재 검색 기능과 연동되지 않습니다.', 'warning');
        });
    });

    // 입력창 엔터 키 이벤트 (직접 입력 태그는 현재 검색 API와 연동 불가)
    const inputArea = document.getElementById('emotionInput');
    if (inputArea) {
        inputArea.addEventListener('keypress', handleInputKeyPress);
    }

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

    // 검색 버튼 클릭 이벤트(감정 태그 기반 검색)
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', async function() {
            if (selectedTags.size === 0) {
                showNotification('하나 이상의 감정을 선택해주세요.', 'warning');
                return;
            }
            // 검색 시 항상 첫 페이지부터
            page = 0;
            await doSearch({ append: false });
        });
    }
});

// 정렬 옵션 선택
function selectSortOption(sortValue, sortText) {
    const sortTextElement = document.querySelector('.sort-text');
    const dropdown = document.querySelector('.sort-dropdown');

    // UI 업데이트
    sortTextElement.textContent = sortText;
    dropdown.classList.remove('active');

    // URL 파라미터 업데이트 후 새로고침
    const currentParams = new URLSearchParams(window.location.search);
    currentParams.set('sort', sortValue);
    window.location.href = window.location.pathname + '?' + currentParams.toString();
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

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) notification.remove();
            if (style.parentNode) style.remove();
        }, 300);
    }, 3000);
}

// 태그 추가 함수
function addTag(tagId, tagName) {
    if (selectedTags.has(tagId)) {
        showNotification('이미 선택된 태그입니다.', 'warning');
        return;
    }
    if (selectedTags.size >= MAX_TAGS) {
        showNotification(`감정 태그는 최대 ${MAX_TAGS}개까지 선택할 수 있습니다.`, 'warning');
        return;
    }

    selectedTags.set(tagId, tagName);

    renderTags();
    updateTagCounter();
    updateInputState();

    showNotification(`'${tagName}' 태그가 추가되었습니다.`, 'info');
}

// 태그 제거 함수
function removeTag(tagId) {
    const tagName = selectedTags.get(tagId);
    if (selectedTags.delete(tagId)) {
        renderTags();
        updateTagCounter();
        updateInputState();
        showNotification(`'${tagName}' 태그가 제거되었습니다.`, 'info');
    }
}

// 태그 카운터 업데이트
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

// 입력 상태 업데이트
function updateInputState() {
    const inputArea = document.getElementById('emotionInput');
    const isMaxReached = selectedTags.size >= MAX_TAGS;

    if (inputArea) {
        inputArea.disabled = isMaxReached;
        inputArea.placeholder = isMaxReached ? '' : '원하는 감정을 선택해보세요 (예: 힐링, 설렘, 평온)';
        inputArea.style.background = '';
        inputArea.style.color = '';
    }

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

// 태그 렌더링
function renderTags() {
    const selectedTagsContainer = document.getElementById('selectedTags');
    if (!selectedTagsContainer) return;

    selectedTagsContainer.innerHTML = '';

    selectedTags.forEach((name, id) => {
        const tagElement = document.createElement('div');
        tagElement.className = 'tag-item';
        tagElement.innerHTML = `
            <span class="tag-text">${name}</span>
            <button class="tag-remove" onclick="removeTag('${id}')" title="태그 제거">×</button>
        `;
        selectedTagsContainer.appendChild(tagElement);
    });
}

// 입력창에서 엔터 입력 처리 (직접 입력 태그는 현재 검색 API와 연동 불가)
function handleInputKeyPress(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        const inputValue = event.target.value.trim();
        if (inputValue) {
            showNotification('직접 입력 태그는 현재 검색 기능과 연동되지 않습니다.', 'warning');
        }
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

// 결과 카드 렌더링 (append 지원)
function renderResults(attractions, { append = false } = {}) {
    const destinationGrid = document.querySelector('.destination-grid');
    if (!destinationGrid) return;

    if (!append) {
        destinationGrid.innerHTML = ''; // 기존 결과 초기화
    }

    if (!attractions || attractions.length === 0) {
        if (!append) {
            destinationGrid.innerHTML = '<p class="no-results">선택한 감정과 일치하는 여행지가 없습니다.</p>';
        }
        return;
    }

    attractions.forEach(attr => {
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
            </a>`;
        destinationGrid.insertAdjacentHTML('beforeend', cardHTML);
    });
    console.log('renderResults()', { append, count: attractions.length });
}

// 서버 호출 공통 함수 (페이징 대응)
async function doSearch({ append = false } = {}) {
    const ids = Array.from(selectedTags.keys()).join(',');
    if (!ids) {
        showNotification('하나 이상의 감정을 선택해주세요.', 'warning');
        return;
    }

    const url = new URL('/api/attractions/search/paged', window.location.origin);
    url.searchParams.set('emotionIds', ids);
    url.searchParams.set('page', page);
    url.searchParams.set('size', size);
    // 정렬 쓰려면: url.searchParams.set('sort', 'title,asc');

    try {
        const response = await fetch(url.toString(), {
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error('서버에서 데이터를 가져오는 데 실패했습니다.');

        const data = await response.json();
        // data: { content, totalElements, totalPages, number, size, ... }
        totalPages = data.totalPages ?? 0;
        totalElements = data.totalElements ?? (data.content?.length || 0);

        // 결과 렌더링
        renderResults(data.content || [], { append });

        // 부제목 업데이트
        const resultsSubtitle = document.querySelector('.results-subtitle');
        if (resultsSubtitle) {
            resultsSubtitle.textContent = `${totalElements}개 결과가 있어요`;
        }

        // 스크롤 이동
        document.querySelector('.search-results-section')
            ?.scrollIntoView({ behavior: 'smooth' });

        console.log('doSearch()', { page, size, totalPages, totalElements, received: data.content?.length || 0 });
        renderPagination();
    } catch (error) {
        console.error('Search Error:', error);
        showNotification('여행지 검색 중 오류가 발생했습니다.', 'warning');
    }



}

// 좋아요 토글
async function handleLikeClick(button) {
    const attractionId = button.dataset.attractionId;
    if (!attractionId) {
        console.error('Attraction ID not found!');
        return;
    }

    const isLiked = button.classList.contains('liked');
    const method = isLiked ? 'DELETE' : 'POST';
    const url = `/api/likes/${attractionId}`;

    try {
        const response = await fetch(url, { method });
        if (response.ok) {
            button.classList.toggle('liked');
            const heartSpan = button.querySelector('span');
            if (heartSpan) {
                heartSpan.textContent = button.classList.contains('liked') ? '♥' : '♡';
            } else {
                button.innerHTML = '♡';
                button.style.color = '#005792';
            }
        } else {
            const errorData = await response.json().catch(() => null);
            const errorMessage = errorData?.message || '요청에 실패했습니다.';
            showNotification(errorMessage, 'warning');
        }
    } catch (error) {
        console.error('Like Error:', error);
        showNotification('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', 'warning');
    }
}

// (선택) 다음 페이지 로딩 예시 — 무한스크롤/더보기 버튼에서 사용
async function loadNextPage() {
    if (page + 1 >= totalPages) {
        showNotification('마지막 페이지입니다.', 'info');
        return;
    }
    page += 1;
    await doSearch({ append: true });
}
// ✅ 페이지 이동 공통 함수
async function goToPage(targetPage) {
    if (targetPage < 0) targetPage = 0;
    if (totalPages && targetPage > totalPages - 1) targetPage = totalPages - 1;
    if (targetPage === page) return;

    page = targetPage;
    await doSearch({ append: false });
    renderPagination();
}

// ✅ 숫자 페이징 렌더링 (1..10 스타일)
function renderPagination() {
    const container = document.getElementById('pagination');
    if (!container) return;

    // Page 응답이 아닌 경우/1페이지 이하인 경우 숨김
    if (!totalPages || totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    // 서버가 Page를 주면 number(현재 페이지, 0-based)를 신뢰
    // 혹시 서버가 number를 안 준다면 클라이언트 page 유지
    // (원하면 여기서 page = data.number; 로 동기화 가능)

    const maxButtons = 10;         // 한 화면에 보여줄 최대 숫자 버튼
    const half = Math.floor(maxButtons / 2);

    // 윈도우 계산 (0-based)
    let start = Math.max(0, page - half);
    let end = start + maxButtons - 1;
    if (end > totalPages - 1) {
        end = totalPages - 1;
        start = Math.max(0, end - (maxButtons - 1));
    }

    let html = '';

    // Prev
    html += `<button class="page-nav prev" ${page === 0 ? 'disabled' : ''} data-role="prev">이전</button>`;

    // 처음으로/앞쪽 생략
    if (start > 0) {
        html += `<button class="page-btn" data-page="0">1</button>`;
        if (start > 1) html += `<span class="page-ellipsis">…</span>`;
    }

    // 숫자 버튼
    for (let i = start; i <= end; i++) {
        html += `<button class="page-btn ${i === page ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
    }

    // 뒤쪽 생략/마지막으로
    if (end < totalPages - 1) {
        if (end < totalPages - 2) html += `<span class="page-ellipsis">…</span>`;
        html += `<button class="page-btn" data-page="${totalPages - 1}">${totalPages}</button>`;
    }

    // Next
    html += `<button class="page-nav next" ${page >= totalPages - 1 ? 'disabled' : ''} data-role="next">다음</button>`;

    container.innerHTML = html;

    // 이벤트 바인딩
    container.querySelectorAll('.page-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const target = parseInt(btn.dataset.page, 10);
            if (Number.isNaN(target)) return;
            goToPage(target);
        });
    });
    const prev = container.querySelector('[data-role="prev"]');
    const next = container.querySelector('[data-role="next"]');

    if (prev) prev.addEventListener('click', () => goToPage(page - 1));
    if (next) next.addEventListener('click', () => goToPage(page + 1));
}

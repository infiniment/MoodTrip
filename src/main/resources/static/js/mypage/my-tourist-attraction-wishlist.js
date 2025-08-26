// 전역 상태 관리
const wishlistState = {
    currentPage: 1,
    itemsPerPage: 9,
    totalItems: 0,
    wishlistItems: []
};

// DOM이 로드되면 실행
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
});

// 찜 목록 초기화
function initializeWishlist() {
    // 로컬 스토리지에서 찜 목록 불러오기 (실제로는 서버에서 가져와야 함)
    loadWishlistFromStorage();
    renderWishlistGrid();
    updatePagination();
}

// 이벤트 리스너 설정
function setupEventListeners() {
    const wishlistGrid = document.querySelector('.wishlist-grid');
    if (wishlistGrid) {
        wishlistGrid.addEventListener('click', function(event) {
            const heartButton = event.target.closest('.heart-button');
            if (heartButton) {
                handleHeartClick(heartButton);
            }
        });
    }
}

// 하트 버튼 클릭 처리
async function handleHeartClick(button) {
    const card = button.closest('.wishlist-card');
    const attractionId = card.dataset.attractionId; // Thymeleaf에서 설정한 data-attraction-id 사용

    if (!attractionId) {
        console.error('Attraction ID not found!');
        return;
    }

    // 사용자에게 정말 삭제할 것인지 확인
    if (!confirm('이 관광지를 찜 목록에서 삭제하시겠습니까?')) {
        return;
    }



    // 서버에 찜 취소(DELETE) 요청
    try {
        const response = await fetch(`/api/likes/${attractionId}`, {
            method: 'DELETE',
            // headers: { 'X-CSRF-TOKEN': '...' } // CSRF 보호가 활성화된 경우 필요
        });

        if (response.ok) {
            // 요청이 성공하면 화면에서 해당 카드를 애니메이션과 함께 제거
            card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';

            setTimeout(() => {
                card.remove();

                // 만약 모든 카드가 삭제되면 "찜 목록 없음" 메시지를 표시
                if (document.querySelectorAll('.wishlist-card').length === 0) {
                    showEmptyState();
                }
            }, 300);

        } else {
            // 서버에서 오류 응답 시
            alert('찜 목록 삭제에 실패했습니다. 다시 시도해주세요.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('네트워크 오류가 발생했습니다.');
    }
}

// 카드 클릭 처리 (상세 페이지로 이동)
function handleCardClick(card) {
    const itemId = card.dataset.itemId;
    // 실제로는 관광지 상세 페이지로 이동
    console.log('Navigate to attraction detail:', itemId);
    // window.location.href = `/attraction/${itemId}`;
}

// 페이지 버튼 클릭 처리
function handlePageClick(button) {
    if (button.classList.contains('prev')) {
        if (button.textContent.includes('«')) {
            wishlistState.currentPage = 1;
        } else {
            wishlistState.currentPage = Math.max(1, wishlistState.currentPage - 1);
        }
    } else if (button.classList.contains('next')) {
        const totalPages = Math.ceil(wishlistState.totalItems / wishlistState.itemsPerPage);
        if (button.textContent.includes('»')) {
            wishlistState.currentPage = totalPages;
        } else {
            wishlistState.currentPage = Math.min(totalPages, wishlistState.currentPage + 1);
        }
    } else {
        wishlistState.currentPage = parseInt(button.textContent);
    }

    renderWishlistGrid();
    updatePagination();

    // 스크롤 상단으로
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 찜 목록 렌더링
function renderWishlistGrid() {
    const grid = document.querySelector('.wishlist-grid');
    if (!grid) return;

    const startIndex = (wishlistState.currentPage - 1) * wishlistState.itemsPerPage;
    const endIndex = startIndex + wishlistState.itemsPerPage;
    const pageItems = wishlistState.wishlistItems.slice(startIndex, endIndex);

    // 기존 카드 제거
    grid.innerHTML = '';

    // 새 카드 추가
    pageItems.forEach((item, index) => {
        const card = createWishlistCard(item);
        // 순차적 애니메이션
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 50);
        grid.appendChild(card);
    });
}

// 찜 카드 생성
function createWishlistCard(item) {
    const card = document.createElement('div');
    card.className = 'wishlist-card';
    card.dataset.itemId = item.id;
    card.style.opacity = '0';
    card.style.transform = 'translateY(20px)';
    card.style.transition = 'all 0.3s ease';

    card.innerHTML = `
        <div class="card-image-wrapper">
            <img src="${item.image}" alt="${item.title}" class="card-image">
            <button class="heart-button active">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
            </button>
        </div>
        <div class="card-content">
            <div class="card-meta">
                <span class="category">${item.category}</span>
            </div>
            <h3 class="card-title">${item.title}</h3>
            <p class="card-description">${item.description}</p>
        </div>
    `;

    return card;
}

// 페이지네이션 업데이트
function updatePagination() {
    const pagination = document.querySelector('.pagination');
    if (!pagination) return;

    const totalPages = Math.ceil(wishlistState.totalItems / wishlistState.itemsPerPage);
    const currentPage = wishlistState.currentPage;

    let paginationHTML = '';

    // 첫 페이지로 버튼
    paginationHTML += `
        <button class="page-btn prev" ${currentPage === 1 ? 'disabled' : ''}>
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor">
                <path d="M8.354 1.646a.5.5 0 0 1 0 .708L2.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
                <path d="M12.354 1.646a.5.5 0 0 1 0 .708L6.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
            </svg>
        </button>
    `;

    // 이전 페이지 버튼
    paginationHTML += `
        <button class="page-btn prev" ${currentPage === 1 ? 'disabled' : ''}>
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor">
                <path d="M8.354 1.646a.5.5 0 0 1 0 .708L2.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
            </svg>
        </button>
    `;

    // 페이지 번호 버튼
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <button class="page-btn ${i === currentPage ? 'active' : ''}">${i}</button>
        `;
    }

    // 다음 페이지 버튼
    paginationHTML += `
        <button class="page-btn next" ${currentPage === totalPages ? 'disabled' : ''}>
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor">
                <path d="M7.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L13.293 8 7.646 2.354a.5.5 0 0 1 0-.708z"/>
            </svg>
        </button>
    `;

    // 마지막 페이지로 버튼
    paginationHTML += `
        <button class="page-btn next" ${currentPage === totalPages ? 'disabled' : ''}>
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor">
                <path d="M3.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L9.293 8 3.646 2.354a.5.5 0 0 1 0-.708z"/>
                <path d="M7.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L13.293 8 7.646 2.354a.5.5 0 0 1 0-.708z"/>
            </svg>
        </button>
    `;

    pagination.innerHTML = paginationHTML;
}

// 빈 상태 표시
function showEmptyState() {
    const totalWrapper = document.querySelector('.total-wrapper');
    const grid = document.querySelector('.wishlist-grid');
    const pagination = document.querySelector('.pagination-wrapper');

    // 기존 그리드와 페이지네이션 숨기기
    if (grid) grid.style.display = 'none';
    if (pagination) pagination.style.display = 'none';

    // "찜 목록 없음" 메시지가 이미 있는지 확인 후, 없으면 추가
    if (!document.querySelector('.no-results-message')) {
        const emptyMessage = document.createElement('div');
        emptyMessage.className = 'no-results-message';
        emptyMessage.innerHTML = '<p>찜한 여행지가 없습니다. 마음에 드는 여행지를 추가해보세요!</p>';
        totalWrapper.appendChild(emptyMessage);
    }
}

// 로컬 스토리지에서 찜 목록 불러오기
function loadWishlistFromStorage() {
    // 실제로는 서버 API 호출
    // 여기서는 더미 데이터 사용
    const dummyData = [
        {
            id: '1',
            title: '경복궁',
            category: '문화재',
            description: '조선왕조의 정궁으로 서울의 대표적인 고궁입니다.',
            image: '/static/image/mypage/sample1.png'
        },
        {
            id: '2',
            title: '남산타워',
            category: '관광명소',
            description: '서울의 랜드마크로 멋진 야경을 볼 수 있습니다.',
            image: '/static/image/mypage/sample2.png'
        },
        {
            id: '3',
            title: '북촌한옥마을',
            category: '전통마을',
            description: '전통 한옥이 모여있는 아름다운 마을입니다.',
            image: '/static/image/mypage/sample3.png'
        },
        // 더 많은 더미 데이터...
    ];

    // 실제로는 localStorage.getItem('wishlist') 등을 사용
    wishlistState.wishlistItems = dummyData;
    wishlistState.totalItems = dummyData.length;
}

// 찜 목록에서 제거
function removeFromWishlist(itemId) {
    // 실제로는 서버 API 호출
    wishlistState.wishlistItems = wishlistState.wishlistItems.filter(item => item.id !== itemId);
    wishlistState.totalItems = wishlistState.wishlistItems.length;

    // 로컬 스토리지 업데이트
    // localStorage.setItem('wishlist', JSON.stringify(wishlistState.wishlistItems));

    // 현재 페이지에 아이템이 없으면 이전 페이지로
    const totalPages = Math.ceil(wishlistState.totalItems / wishlistState.itemsPerPage);
    if (wishlistState.currentPage > totalPages && totalPages > 0) {
        wishlistState.currentPage = totalPages;
    }
}

// 찜 상태 토글 (다른 페이지에서 사용)
function toggleWishlist(itemId, itemData) {
    const index = wishlistState.wishlistItems.findIndex(item => item.id === itemId);

    if (index > -1) {
        // 이미 찜한 상태면 제거
        wishlistState.wishlistItems.splice(index, 1);
    } else {
        // 찜하지 않은 상태면 추가
        wishlistState.wishlistItems.push(itemData);
    }

    wishlistState.totalItems = wishlistState.wishlistItems.length;

    // 로컬 스토리지 업데이트
    // localStorage.setItem('wishlist', JSON.stringify(wishlistState.wishlistItems));
}

// 찜 여부 확인 (다른 페이지에서 사용)
function isWishlisted(itemId) {
    return wishlistState.wishlistItems.some(item => item.id === itemId);
}
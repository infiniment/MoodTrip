// 전역 변수
let allReviews = [];
let filteredReviews = [];
let currentPage = 1;
let reviewsPerPage = 10;

// DOM 요소들
const reviewsContainer = document.querySelector('.reviews-container');
const emptyReviews = document.querySelector('.empty-reviews');
const ratingFilter = document.getElementById('rating-filter');
const sortFilter = document.getElementById('sort-filter');
const searchInput = document.getElementById('search-reviews');
const loadMoreBtn = document.getElementById('load-more-btn');
const deleteModal = document.getElementById('deleteModal');
const editModal = document.getElementById('editModal');
const imageModal = document.getElementById('imageModal');
const toastContainer = document.getElementById('toast-container');

// 통계 요소들
const totalReviewsEl = document.getElementById('total-reviews');
const averageRatingEl = document.getElementById('average-rating');
const thisMonthReviewsEl = document.getElementById('this-month-reviews');
const totalLikesEl = document.getElementById('total-likes');

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('페이지 로드됨'); // 디버깅용
    initializeReviews();
    bindEvents();
    updateStatistics();
    
    // 더 보기 버튼 강제 표시 (테스트용)
    const loadMoreContainer = document.querySelector('.load-more-container');
    if (loadMoreContainer) {
        loadMoreContainer.style.display = 'flex';
        console.log('더 보기 버튼 강제 표시'); // 디버깅용
    }
});

// 초기 후기 데이터 로드
function initializeReviews() {
    // 현재 HTML에 있는 후기들을 데이터로 변환
    allReviews = extractReviewsFromDOM();
    filteredReviews = [...allReviews];
    
    if (allReviews.length === 0) {
        showEmptyState();
    } else {
        hideEmptyState();
        renderReviews();
    }
}

// DOM에서 후기 데이터 추출
function extractReviewsFromDOM() {
    const reviewItems = document.querySelectorAll('.review-item');
    const reviews = [];
    
    reviewItems.forEach((item, index) => {
        const reviewId = item.dataset.reviewId;
        const destinationName = item.querySelector('.destination-name').textContent;
        const reviewDate = item.querySelector('.review-date').textContent;
        const ratingText = item.querySelector('.rating-text').textContent;
        const reviewText = item.querySelector('.review-text').textContent;
        const stars = item.querySelectorAll('.star.filled').length;
        
        reviews.push({
            id: reviewId,
            destination: destinationName,
            date: reviewDate,
            rating: parseFloat(ratingText),
            stars: stars,
            text: reviewText,
            createdAt: new Date(reviewDate.split(' | ')[1].replace(/\./g, '-')),
            element: item
        });
    });
    
    return reviews;
}

// 이벤트 바인딩
function bindEvents() {
    // 필터 및 검색 이벤트
    ratingFilter.addEventListener('change', handleFilterChange);
    sortFilter.addEventListener('change', handleFilterChange);
    searchInput.addEventListener('input', debounce(handleFilterChange, 300));
    
    // 더 보기 버튼
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', loadMoreReviews);
    }
    
    // 수정/삭제 버튼 이벤트
    document.addEventListener('click', handleActionClick);
    
    // 모달 이벤트
    bindModalEvents();
}

// 액션 버튼 클릭 처리
function handleActionClick(event) {
    const target = event.target;
    
    if (target.classList.contains('btn-edit')) {
        const reviewId = target.dataset.reviewId;
        handleEditReview(reviewId);
    } else if (target.classList.contains('btn-delete')) {
        const reviewId = target.dataset.reviewId;
        handleDeleteReview(reviewId);
    }
}

// 후기 수정 처리
function handleEditReview(reviewId) {
    const review = allReviews.find(r => r.id === reviewId);
    if (!review) return;
    
    // 수정 모달 표시
    showEditModal(review);
}

// 수정 모달 표시
function showEditModal(review) {
    const modal = editModal;
    const destinationInput = document.getElementById('edit-destination');
    const reviewTextarea = document.getElementById('edit-review-text');
    const starRating = document.getElementById('edit-star-rating');
    const ratingValue = document.getElementById('edit-rating-value');
    const saveBtn = document.getElementById('saveEdit');
    
    // 모달 내용 설정
    destinationInput.value = review.destination;
    reviewTextarea.value = review.text;
    ratingValue.textContent = review.rating.toFixed(1);
    
    // 별점 설정
    updateStarRating(starRating, review.stars);
    
    // 저장 버튼에 이벤트 추가
    saveBtn.onclick = () => saveEditReview(review.id);
    
    // 별점 클릭 이벤트 설정
    setupStarRatingEvents(starRating, ratingValue);
    
    // 모달 표시
    modal.style.display = 'block';
}

// 별점 업데이트
function updateStarRating(container, rating) {
    const stars = container.querySelectorAll('.star-input');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

// 별점 클릭 이벤트 설정
function setupStarRatingEvents(container, valueDisplay) {
    const stars = container.querySelectorAll('.star-input');
    
    stars.forEach((star, index) => {
        star.addEventListener('click', () => {
            const rating = index + 1;
            updateStarRating(container, rating);
            valueDisplay.textContent = rating.toFixed(1);
        });
        
        star.addEventListener('mouseenter', () => {
            const rating = index + 1;
            updateStarRating(container, rating);
        });
    });
    
    container.addEventListener('mouseleave', () => {
        const currentRating = parseFloat(valueDisplay.textContent);
        updateStarRating(container, currentRating);
    });
}

// 후기 수정 저장
function saveEditReview(reviewId) {
    const reviewTextarea = document.getElementById('edit-review-text');
    const ratingValue = document.getElementById('edit-rating-value');
    
    const newText = reviewTextarea.value.trim();
    const newRating = parseFloat(ratingValue.textContent);
    
    if (!newText) {
        showToast('후기 내용을 입력해주세요.', 'warning');
        return;
    }
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // 데이터 업데이트
        const review = allReviews.find(r => r.id === reviewId);
        if (review) {
            review.text = newText;
            review.rating = newRating;
            review.stars = Math.round(newRating);
            
            // DOM 업데이트
            const reviewElement = document.querySelector(`[data-review-id="${reviewId}"]`);
            if (reviewElement) {
                const textElement = reviewElement.querySelector('.review-text');
                const ratingTextElement = reviewElement.querySelector('.rating-text');
                const starsContainer = reviewElement.querySelector('.stars');
                
                textElement.textContent = newText;
                ratingTextElement.textContent = newRating.toFixed(1);
                
                // 별점 업데이트
                const starElements = starsContainer.querySelectorAll('.star');
                starElements.forEach((star, index) => {
                    if (index < review.stars) {
                        star.classList.add('filled');
                    } else {
                        star.classList.remove('filled');
                    }
                });
            }
        }
        
        // 모달 닫기
        editModal.style.display = 'none';
        
        // 통계 업데이트
        updateStatistics();
        
        // 성공 메시지
        showToast('후기가 수정되었습니다.', 'success');
        
    }, 500); // API 호출 시뮬레이션을 위한 딜레이
}
function handleDeleteReview(reviewId) {
    const review = allReviews.find(r => r.id === reviewId);
    if (!review) return;
    
    // 삭제 확인 모달 표시
    showDeleteModal(reviewId, review.destination);
}

// 삭제 확인 모달 표시
function showDeleteModal(reviewId, destinationName) {
    const modal = deleteModal;
    const confirmBtn = document.getElementById('confirmDelete');
    
    // 모달 내용 업데이트
    modal.querySelector('.modal-body p').textContent = 
        `"${destinationName}" 후기를 정말로 삭제하시겠습니까?`;
    
    // 확인 버튼에 이벤트 추가
    confirmBtn.onclick = () => confirmDeleteReview(reviewId);
    
    // 모달 표시
    modal.style.display = 'block';
}

// 후기 삭제 확인
function confirmDeleteReview(reviewId) {
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
        // DOM에서 후기 제거
        const reviewElement = document.querySelector(`[data-review-id="${reviewId}"]`);
        if (reviewElement) {
            reviewElement.remove();
        }
        
        // 데이터에서 후기 제거
        allReviews = allReviews.filter(r => r.id !== reviewId);
        filteredReviews = filteredReviews.filter(r => r.id !== reviewId);
        
        // 모달 닫기
        deleteModal.style.display = 'none';
        
        // 통계 업데이트
        updateStatistics();
        
        // 빈 상태 체크
        if (allReviews.length === 0) {
            showEmptyState();
        }
        
        // 성공 메시지
        showToast('후기가 삭제되었습니다.', 'success');
        
    }, 500); // API 호출 시뮬레이션을 위한 딜레이
}

// 필터 및 정렬 처리
function handleFilterChange() {
    const ratingValue = ratingFilter.value;
    const sortValue = sortFilter.value;
    const searchValue = searchInput.value.toLowerCase().trim();
    
    // 필터링
    filteredReviews = allReviews.filter(review => {
        // 평점 필터
        if (ratingValue !== 'all' && review.stars !== parseInt(ratingValue)) {
            return false;
        }
        
        // 검색 필터
        if (searchValue && !review.destination.toLowerCase().includes(searchValue)) {
            return false;
        }
        
        return true;
    });
    
    // 정렬
    filteredReviews.sort((a, b) => {
        switch (sortValue) {
            case 'newest':
                return b.createdAt - a.createdAt;
            case 'oldest':
                return a.createdAt - b.createdAt;
            case 'rating-high':
                return b.rating - a.rating;
            case 'rating-low':
                return a.rating - b.rating;
            case 'likes':
                // 좋아요 수가 없으므로 최신순으로 대체
                return b.createdAt - a.createdAt;
            default:
                return b.createdAt - a.createdAt;
        }
    });
    
    // 페이지 리셋
    currentPage = 1;
    
    // 화면 업데이트
    renderReviews();
}

// 후기 렌더링
function renderReviews() {
    const container = reviewsContainer;
    const loadMoreContainer = container.querySelector('.load-more-container');
    
    // 기존 후기 아이템들 숨기기
    document.querySelectorAll('.review-item').forEach(item => {
        item.style.display = 'none';
    });
    
    if (filteredReviews.length === 0) {
        showEmptyState();
        if (loadMoreContainer) {
            loadMoreContainer.style.display = 'none';
        }
        return;
    }
    
    hideEmptyState();
    
    // 현재 페이지까지의 후기들 표시
    const endIndex = currentPage * reviewsPerPage;
    const reviewsToShow = filteredReviews.slice(0, endIndex);
    
    reviewsToShow.forEach(review => {
        if (review.element) {
            review.element.style.display = 'block';
        }
    });
    
    // 더 보기 버튼 표시/숨김 - 강제로 표시하도록 수정
    if (loadMoreContainer) {
        if (endIndex < filteredReviews.length) {
            loadMoreContainer.style.display = 'flex';
            console.log('더 보기 버튼 표시됨'); // 디버깅용
        } else {
            loadMoreContainer.style.display = 'none';
            console.log('더 보기 버튼 숨김됨'); // 디버깅용
        }
    } else {
        console.log('더 보기 컨테이너를 찾을 수 없음'); // 디버깅용
    }
}

// 더 보기 로드
function loadMoreReviews() {
    currentPage++;
    renderReviews();
}

// 통계 업데이트
function updateStatistics() {
    if (allReviews.length === 0) {
        totalReviewsEl.textContent = '0';
        averageRatingEl.textContent = '0.0';
        thisMonthReviewsEl.textContent = '0';
        totalLikesEl.textContent = '0';
        return;
    }
    
    // 총 후기 수
    totalReviewsEl.textContent = allReviews.length;
    
    // 평균 평점
    const averageRating = allReviews.reduce((sum, review) => sum + review.rating, 0) / allReviews.length;
    averageRatingEl.textContent = averageRating.toFixed(1);
    
    // 이번 달 작성된 후기 수
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    const thisMonthCount = allReviews.filter(review => {
        return review.createdAt.getMonth() === currentMonth && 
               review.createdAt.getFullYear() === currentYear;
    }).length;
    thisMonthReviewsEl.textContent = thisMonthCount;
    
    // 총 좋아요 수 (현재는 고정값, 실제로는 API에서 가져와야 함)
    totalLikesEl.textContent = '42';
}

// 빈 상태 표시
function showEmptyState() {
    if (emptyReviews) {
        emptyReviews.style.display = 'block';
    }
    if (reviewsContainer) {
        reviewsContainer.style.display = 'none';
    }
}

// 빈 상태 숨김
function hideEmptyState() {
    if (emptyReviews) {
        emptyReviews.style.display = 'none';
    }
    if (reviewsContainer) {
        reviewsContainer.style.display = 'block';
    }
}

// 모달 이벤트 바인딩
function bindModalEvents() {
    // 삭제 모달
    const deleteModalClose = deleteModal.querySelector('.modal-close');
    const cancelBtn = deleteModal.querySelector('.btn-cancel');
    
    deleteModalClose.addEventListener('click', () => {
        deleteModal.style.display = 'none';
    });
    
    cancelBtn.addEventListener('click', () => {
        deleteModal.style.display = 'none';
    });
    
    // 수정 모달
    const editModalClose = editModal.querySelector('.modal-close');
    const editCancelBtn = editModal.querySelector('.btn-cancel');
    
    editModalClose.addEventListener('click', () => {
        editModal.style.display = 'none';
    });
    
    editCancelBtn.addEventListener('click', () => {
        editModal.style.display = 'none';
    });
    
    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (event) => {
        if (event.target === deleteModal) {
            deleteModal.style.display = 'none';
        }
        if (event.target === editModal) {
            editModal.style.display = 'none';
        }
        if (event.target === imageModal) {
            imageModal.style.display = 'none';
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            deleteModal.style.display = 'none';
            editModal.style.display = 'none';
            imageModal.style.display = 'none';
        }
    });
}

// 토스트 메시지 표시
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    toastContainer.appendChild(toast);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 3000);
}

// 디바운스 함수
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 날짜 포맷팅 함수
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

// API 호출 시뮬레이션 함수들 (실제 구현 시 사용)
async function fetchReviews() {
    try {
        // 실제 API 호출
        // const response = await fetch('/api/reviews/my');
        // const data = await response.json();
        // return data.reviews;
        
        // 시뮬레이션을 위한 더미 데이터 반환
        return allReviews;
    } catch (error) {
        console.error('후기 목록 조회 실패:', error);
        showToast('후기 목록을 불러오는데 실패했습니다.', 'error');
        return [];
    }
}

async function deleteReviewAPI(reviewId) {
    try {
        // 실제 API 호출
        // const response = await fetch(`/api/reviews/${reviewId}`, {
        //     method: 'DELETE'
        // });
        // return response.ok;
        
        // 시뮬레이션
        return true;
    } catch (error) {
        console.error('후기 삭제 실패:', error);
        showToast('후기 삭제에 실패했습니다.', 'error');
        return false;
    }
}
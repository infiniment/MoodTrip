document.addEventListener('DOMContentLoaded', function() {
    // URL에서 검색어 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const query = urlParams.get('query');

    if (query) {
        document.getElementById('support-search-input').value = query;
        document.getElementById('search-query-display').textContent = `"${query}" 검색 결과`;
        performSearch(query);
    }

    // 검색 버튼 이벤트
    document.getElementById('search-button').addEventListener('click', function() {
        const searchTerm = document.getElementById('support-search-input').value.trim();
        if (searchTerm) {
            window.history.pushState({}, '', `/customer-center/search?query=${encodeURIComponent(searchTerm)}`);
            document.getElementById('search-query-display').textContent = `"${searchTerm}" 검색 결과`;
            performSearch(searchTerm);
        }
    });

    // 엔터키 검색 (수정)
    document.getElementById('support-search-input').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('search-button').click();
        }
    });


    // 결과 탭 전환
    const resultTabs = document.querySelectorAll('.result-tab');
    resultTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // 모든 탭 비활성화
            resultTabs.forEach(t => t.classList.remove('active'));
            // 클릭된 탭 활성화
            this.classList.add('active');

            const type = this.getAttribute('data-type');
            filterResults(type);
        });
    });
});

// 검색 수행
function performSearch(query) {
    fetch(`/customer-center/search/api?query=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            displaySearchResults(data);
            updateSearchCount(data.totalCount);
        })
        .catch(error => {
            console.error('검색 오류:', error);
            showNoResults();
        });
}

// 검색 결과 표시
function displaySearchResults(data) {
    const faqResults = data.faq || [];
    const noticeResults = data.notice || [];

    // FAQ 결과 표시
    displayFaqResults(faqResults);

    // 공지사항 결과 표시
    displayNoticeResults(noticeResults);

    // 결과가 없으면 메시지 표시
    if (data.totalCount === 0) {
        showNoResults();
    } else {
        hideNoResults();
    }
}

// FAQ 결과 표시
function displayFaqResults(faqs) {
    const faqResultList = document.getElementById('faq-result-list');
    faqResultList.innerHTML = '';

    if (faqs.length === 0) {
        faqResultList.innerHTML = '<p class="no-result-message">FAQ 검색 결과가 없습니다.</p>';
        return;
    }

    faqs.forEach(faq => {
        const faqItem = document.createElement('div');
        faqItem.className = 'search-result-item faq-item';
        faqItem.innerHTML = `
            
            <div class="result-category">[${faq.category}]</div>
            <div class="result-title">
                <a href="/customer-center/faq-detail?id=${faq.id}" class="result-link">
                    ${faq.title}
                </a>
            </div>
            
            <div class="result-meta">
                <span class="result-views">조회 ${faq.viewCount}회</span>
                <span class="result-date">${formatDate(faq.createdAt)}</span>
            </div>
        `;
        faqResultList.appendChild(faqItem);
    });
}

// 공지사항 결과 표시
function displayNoticeResults(notices) {
    const noticeResultList = document.getElementById('notice-result-list');
    noticeResultList.innerHTML = '';

    if (notices.length === 0) {
        noticeResultList.innerHTML = '<p class="no-result-message">공지사항 검색 결과가 없습니다.</p>';
        return;
    }

    notices.forEach(notice => {
        const noticeItem = document.createElement('div');
        noticeItem.className = 'search-result-item notice-item';
        noticeItem.innerHTML = `
            <div class="result-category">[공지사항]</div>
            <div class="result-title">
                <a href="/customer-center/announcement-detail?id=${notice.id}" class="result-link">
                    ${notice.title}
                </a>
            </div>
            <div class="result-meta">
                <span class="result-views">조회 ${notice.viewCount}회</span>
                <span class="result-date">${formatDate(notice.registeredDate)}</span>
            </div>
        `;
        noticeResultList.appendChild(noticeItem);
    });
}

// 검색 결과 개수 업데이트
function updateSearchCount(count) {
    document.getElementById('search-count').textContent = `총 ${count}개의 결과`;
}

// 결과 필터링
function filterResults(type) {
    const faqSection = document.getElementById('faq-results');
    const noticeSection = document.getElementById('notice-results');

    switch(type) {
        case 'all':
            faqSection.style.display = 'block';
            noticeSection.style.display = 'block';
            break;
        case 'faq':
            faqSection.style.display = 'block';
            noticeSection.style.display = 'none';
            break;
        case 'notice':
            faqSection.style.display = 'none';
            noticeSection.style.display = 'block';
            break;
    }
}

// 검색 결과 없음 표시
function showNoResults() {
    document.getElementById('no-results').style.display = 'block';
    document.getElementById('faq-results').style.display = 'none';
    document.getElementById('notice-results').style.display = 'none';
}

// 검색 결과 없음 숨기기
function hideNoResults() {
    document.getElementById('no-results').style.display = 'none';
    document.getElementById('faq-results').style.display = 'block';
    document.getElementById('notice-results').style.display = 'block';
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}
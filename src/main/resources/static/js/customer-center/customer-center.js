// 간단한 탭 전환 기능
function searchFAQ() {
    const searchInput = document.getElementById('support-search-input');
    const searchTerm = searchInput.value.trim();

    if (searchTerm) {
        // 검색 페이지로 이동
        window.location.href = `/customer-center/search?query=${encodeURIComponent(searchTerm)}`;
    }
}

// 엔터키 검색 지원
document.getElementById('support-search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        searchFAQ();
    }
});

// 탭 전환 기능
document.addEventListener('DOMContentLoaded', function() {
    const navTabs = document.querySelectorAll('.nav-tab');
    const faqSection = document.getElementById('faq-section');
    const noticeSection = document.getElementById('notice-section');

    navTabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 모든 탭에서 active 클래스 제거
            navTabs.forEach(t => t.classList.remove('active'));
            
            // 클릭된 탭에 active 클래스 추가
            this.classList.add('active');
            
            // 탭 데이터 속성 가져오기
            const tabType = this.getAttribute('data-tab');
            
            // 모든 섹션 숨기기
            faqSection.classList.remove('active');
            noticeSection.classList.remove('active');
            
            // 선택된 섹션 보이기
            if (tabType === 'faq') {
                faqSection.classList.add('active');
            } else if (tabType === 'notice') {
                noticeSection.classList.add('active');
            }
        });
    });

    // FAQ 카테고리 버튼 클릭 이벤트
    const categoryButtons = document.querySelectorAll('.category-button');
    
    categoryButtons.forEach(button => {
        button.addEventListener('click', function() {
            // 모든 카테고리 버튼에서 active 클래스 제거
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            
            // 클릭된 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 선택된 카테고리 가져오기
            const selectedCategory = this.getAttribute('data-category');
            console.log('선택된 카테고리:', selectedCategory);
            
            // 여기에 카테고리별 FAQ 필터링 로직 구현
            filterFAQByCategory(selectedCategory);
        });
    });

    // 페이지 로드 시 기본적으로 서비스 소개 카테고리 표시
    filterFAQByCategory('service');

// 카테고리별 FAQ 필터링 함수 수정
    function filterFAQByCategory(category) {
        const faqList = document.getElementById('faq-list');

        // 매핑: 카테고리 버튼의 data-category 값을 실제 DB 카테고리 값으로 변환
        const categoryMapping = {
            'service': '서비스 소개',
            'profile': '회원 정보',
            'usage': '이용 방법',
            'payment': '결제',
            'refund': '취소·환불',
            'dispute': '분쟁·페널티'
        };

        const dbCategory = categoryMapping[category] || category;

        // 서버에서 카테고리별 FAQ 데이터 가져오기
        fetch(`/customer-center/faq/data?category=${encodeURIComponent(dbCategory)}`)
            .then(response => response.json())
            .then(faqs => {
                faqList.innerHTML = '';

                faqs.forEach(faq => {
                    const faqItem = document.createElement('a');
                    faqItem.href = `/customer-center/faq-detail?id=${faq.id}`;
                    faqItem.className = 'faq-item';
                    faqItem.target = '_blank';
                    faqItem.textContent = `[${faq.category}] ${faq.title}`;

                    faqList.appendChild(faqItem);
                });

                if (faqs.length === 0) {
                    const noDataMessage = document.createElement('div');
                    noDataMessage.className = 'no-faq-message';
                    noDataMessage.textContent = '해당 카테고리의 FAQ가 없습니다.';
                    faqList.appendChild(noDataMessage);
                }
            })
            .catch(error => {
                console.error('FAQ 로드 오류:', error);
                // 에러 시에도 빈 상태 표시
                faqList.innerHTML = '<div class="no-faq-message">FAQ를 불러오는 중 오류가 발생했습니다.</div>';
            });
    }
});

// 공지 카드 전체 클릭 가능하게
document.querySelectorAll('.notice-item-card').forEach((card) => {
    // 카드 안에 상세 링크가 이미 있을 때 그 링크를 따라감
    const link = card.querySelector('a[href*="/customer-center/announcement-detail"]');
    if (!link) return;

    card.style.cursor = 'pointer';

    // 버튼/링크 등 인터랙티브 요소 클릭은 예외
    card.addEventListener('click', (e) => {
        if (e.target.closest('a, button, [role="button"], input, select, textarea')) return;
        window.location.href = link.href;
    });

    // 키보드 접근성
    card.setAttribute('tabindex', '0');
    card.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            window.location.href = link.href;
        }
    });
});
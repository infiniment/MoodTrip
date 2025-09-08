document.addEventListener('DOMContentLoaded', function() {
    // 검색 기능
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.getElementById('query');

    searchForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const searchTerm = searchInput.value.trim();

        if (searchTerm) {
            performSearch(searchTerm);
        }
    });

    function performSearch(searchTerm) {
        console.log('검색어:', searchTerm);

        const faqCards = document.querySelectorAll('.faq-item-card');
        let visibleCount = 0;

        faqCards.forEach(card => {
            const link = card.querySelector('.faq-item-link');
            const text = link.textContent.toLowerCase();
            const searchLower = searchTerm.toLowerCase();

            if (text.includes(searchLower)) {
                card.style.display = 'flex';
                highlightSearchTerm(link, searchTerm);
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        updateResultCount(visibleCount);

        if (visibleCount === 0) {
            showNoResults(searchTerm);
        } else {
            hideNoResults();
        }
    }

    function highlightSearchTerm(element, searchTerm) {
        const originalText = element.getAttribute('data-original-text') || element.textContent;
        if (!element.getAttribute('data-original-text')) {
            element.setAttribute('data-original-text', originalText);
        }

        const regex = new RegExp(`(${searchTerm})`, 'gi');
        const highlightedText = originalText.replace(regex, '<mark style="background: linear-gradient(to right, #005792, #001A2C); color: white; padding: 2px 4px; border-radius: 3px;">$1</mark>');
        element.innerHTML = highlightedText;
    }

    function showNoResults(searchTerm) {
        hideNoResults();

        const container = document.querySelector('.faq-list-container');
        const noResultsDiv = document.createElement('div');
        noResultsDiv.className = 'no-results';
        noResultsDiv.innerHTML = `
        <div style="font-size: 48px; margin-bottom: 16px;">🔍</div>
        <h3>'${searchTerm}'에 대한 검색 결과가 없습니다</h3>
        <p>다른 키워드로 검색해보세요</p>
        <button onclick="clearSearch()">전체 목록 보기</button>
    `;
        container.appendChild(noResultsDiv);
    }

    function hideNoResults() {
        const noResults = document.querySelector('.no-results');
        if (noResults) {
            noResults.remove();
        }
    }

    window.clearSearch = function() {
        searchInput.value = '';
        const faqCards = document.querySelectorAll('.faq-item-card');

        faqCards.forEach(card => {
            card.style.display = 'flex';
            const link = card.querySelector('.faq-item-link');
            const originalText = link.getAttribute('data-original-text');
            if (originalText) {
                link.textContent = originalText;
                link.removeAttribute('data-original-text');
            }
        });

        hideNoResults();
        // 전체 목록 개수로 복원
        const totalItems = document.querySelectorAll('.faq-item-card').length;
        updateResultCount(totalItems);
    };

    // 실시간 검색
    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            const searchTerm = this.value.trim();
            if (searchTerm.length >= 2) {
                performSearch(searchTerm);
            } else if (searchTerm.length === 0) {
                clearSearch();
            }
        }, 300);
    });


    // 카테고리 버튼 클릭 이벤트 (AJAX 추가)
    document.querySelectorAll('.category-button').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.category-button').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const category = this.getAttribute('data-category');
            filterFAQByCategory(category);
        });
    });

    // 카테고리별 FAQ 필터링 함수
    function filterFAQByCategory(category) {
        const faqList = document.getElementById('faq-list');

        // 전체 카테고리인 경우
        if (category === 'all') {
            window.location.href = '/customer-center/faq';
            return;
            // // 서버에서 전체 FAQ 데이터 가져오기
            // fetch(`/customer-center/faq/data`)
            //     .then(response => response.json())
            //     .then(faqs => {
            //         renderFAQList(faqs);
            //     })
            //     .catch(error => {
            //         console.error('FAQ 로드 오류:', error);
            //         showErrorMessage();
            //     });
            // return;
        }

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
                renderFAQList(faqs);
            })
            .catch(error => {
                console.error('FAQ 로드 오류:', error);
                showErrorMessage();
            });
    }


// FAQ 목록 렌더링 함수
    function renderFAQList(faqs) {
        const faqList = document.getElementById('faq-list');

        // 애니메이션 효과
        faqList.style.opacity = '0.3';
        faqList.style.transform = 'translateY(10px)';

        setTimeout(() => {
            faqList.innerHTML = '';

            faqs.forEach(faq => {
                const faqItem = document.createElement('div');
                faqItem.className = 'faq-item-card';
                faqItem.innerHTML = `
                <div class="faq-category-tag">${faq.category}</div>
                <div class="faq-content">
                    <a class="faq-item-link" href="/customer-center/faq-detail?id=${faq.id}">
                        [${faq.category}] ${faq.title}
                    </a>
                    <div class="faq-meta">
                        <span class="faq-views">조회 ${faq.viewCount}</span>
                    </div>
                </div>
                <div class="faq-action">
                    <button type="button" class="helpful-button" data-type="helpful" data-faq-id="${faq.id}">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                        </svg>
                    </button>
                </div>
            `;
                faqList.appendChild(faqItem);
            });

            // 결과 개수 업데이트
            updateResultCount(faqs.length);

            // 검색 결과가 없으면 메시지 표시
            if (faqs.length === 0) {
                const noDataMessage = document.createElement('div');
                noDataMessage.className = 'no-faq-message';
                noDataMessage.textContent = '해당 카테고리의 FAQ가 없습니다.';
                faqList.appendChild(noDataMessage);
            }

            // 애니메이션 복원
            faqList.style.transition = 'all 0.3s ease';
            faqList.style.opacity = '1';
            faqList.style.transform = 'translateY(0)';

            // 도움됨 버튼 이벤트 재등록
            attachHelpfulButtonEvents();
        }, 150);
    }

    // 에러 메시지 표시
    function showErrorMessage() {
        const faqList = document.getElementById('faq-list');
        faqList.innerHTML = '<div class="no-faq-message">FAQ를 불러오는 중 오류가 발생했습니다.</div>';

        // 애니메이션 복원
        setTimeout(() => {
            faqList.style.transition = 'all 0.3s ease';
            faqList.style.opacity = '1';
            faqList.style.transform = 'translateY(0)';
        }, 150);
    }

    // 결과 개수 업데이트
    function updateResultCount(count) {
        const resultCountElement = document.querySelector('.result-count strong');
        if (resultCountElement) {
            resultCountElement.textContent = count;
        }
    }

    // 도움됨 버튼 이벤트 등록
    function attachHelpfulButtonEvents() {
        document.querySelectorAll('.helpful-button').forEach(button => {
            // 기존 이벤트 리스너 제거 후 새로 등록
            button.replaceWith(button.cloneNode(true));
        });

        document.querySelectorAll('.helpful-button').forEach(button => {
            button.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (this.disabled) return;      // 중복 클릭 가드
                this.disabled = true;

                const faqId = this.getAttribute('data-faq-id');

                fetch(`/customer-center/faq/helpful/${faqId}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (response.ok) {
                            this.classList.add('clicked');
                            this.setAttribute('aria-pressed', 'true');
                            alert('도움이 되었다는 의견이 반영되었습니다.');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                    })
                    .catch(() => {
                        this.disabled = false;
                    })
            });
        });
    }

    // 정렬 버튼 클릭 이벤트
    document.querySelectorAll('.sort-button').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.sort-button').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const sortType = this.getAttribute('data-sort');
            console.log('정렬 방식:', sortType);

            sortFAQs(sortType);
        });
    });

    function sortFAQs(sortType) {
        const container = document.querySelector('.faq-list-container');
        const cards = Array.from(container.querySelectorAll('.faq-item-card'));

        cards.sort((a, b) => {
            switch(sortType) {
                case 'popular':
                    // 조회수 기준 정렬
                    const viewsA = parseInt(a.querySelector('.faq-views').textContent.replace(/[^0-9]/g, ''));
                    const viewsB = parseInt(b.querySelector('.faq-views').textContent.replace(/[^0-9]/g, ''));
                    return viewsB - viewsA;

                case 'latest':
                    // 최신순 정렬 (실제로는 서버에서 날짜 데이터가 필요)
                    return Math.random() - 0.5; // 임시로 랜덤 정렬

                case 'alphabetical':
                    // 가나다순 정렬
                    const titleA = a.querySelector('.faq-item-link').textContent;
                    const titleB = b.querySelector('.faq-item-link').textContent;
                    return titleA.localeCompare(titleB, 'ko');

                default:
                    return 0;
            }
        });

        // DOM 재배치
        cards.forEach((card, index) => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';

            setTimeout(() => {
                container.appendChild(card);
                card.style.transition = 'all 0.3s ease';
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, index * 50);
        });
    }

    // 페이지네이션 클릭 이벤트
    document.querySelectorAll('.pagination-number').forEach(button => {
        button.addEventListener('click', function() {
            const page = this.getAttribute('data-page');
            const currentUrl = new URL(window.location);
            currentUrl.searchParams.set('page', page);
            window.location.href = currentUrl.toString();
        });
    });

    // 이전/다음 버튼 클릭 이벤트
    document.querySelector('.pagination-button.prev')?.addEventListener('click', function() {
        if (!this.disabled) {
            const currentPage = parseInt(new URLSearchParams(window.location.search).get('page') || '1');
            if (currentPage > 1) {
                const currentUrl = new URL(window.location);
                currentUrl.searchParams.set('page', currentPage - 1);
                window.location.href = currentUrl.toString();
            }
        }
    });

    document.querySelector('.pagination-button.next')?.addEventListener('click', function() {
        if (!this.disabled) {
            const currentPage = parseInt(new URLSearchParams(window.location.search).get('page') || '1');
            const currentUrl = new URL(window.location);
            currentUrl.searchParams.set('page', currentPage + 1);
            window.location.href = currentUrl.toString();
        }
    });

    // FAQ 아이템 클릭 시 애니메이션
    document.querySelectorAll('.faq-item-link').forEach(link => {
        link.addEventListener('click', function(e) {
            const card = this.closest('.faq-item-card');
            card.style.transform = 'scale(0.98)';
            card.style.transition = 'transform 0.1s ease';

            setTimeout(() => {
                card.style.transform = 'scale(1)';
            }, 100);
        });
    });

    // 키보드 네비게이션
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && searchInput.value) {
            searchInput.value = '';
            searchInput.blur();
        }
    });

    // 페이지 로드 시 도움됨 버튼 이벤트 등록
    attachHelpfulButtonEvents();
});
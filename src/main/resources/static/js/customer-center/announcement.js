// announcement-list.js

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
        
        // 검색 결과에 따라 UI 업데이트
        const announcements = document.querySelectorAll('.announcement-list-item');
        let visibleCount = 0;
        
        announcements.forEach(item => {
            const link = item.querySelector('.announcement-list-item-link');
            const text = link.textContent.toLowerCase();
            const searchLower = searchTerm.toLowerCase();
            
            if (text.includes(searchLower)) {
                item.style.display = 'flex';
                // 검색어 하이라이트
                highlightSearchTerm(link, searchTerm);
                visibleCount++;
            } else {
                item.style.display = 'none';
            }
        });
        
        // 검색 결과 개수 업데이트
        updateResultCount(visibleCount);
        
        // 검색 결과가 없는 경우
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

    function updateResultCount(count) {
        const resultCount = document.querySelector('.result-count');
        resultCount.innerHTML = `검색 결과 <strong>${count}</strong>개`;
    }

    function showNoResults(searchTerm) {
        hideNoResults(); // 기존 메시지 제거
        
        const container = document.querySelector('.announcement-list-container');
        const noResultsDiv = document.createElement('div');
        noResultsDiv.className = 'no-results';
        noResultsDiv.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #64748b;">
                <div style="font-size: 48px; margin-bottom: 16px;">🔍</div>
                <h3 style="font-size: 18px; margin-bottom: 8px; color: #334155;">'${searchTerm}'에 대한 검색 결과가 없습니다</h3>
                <p style="font-size: 14px;">다른 키워드로 검색해보세요</p>
                <button onclick="clearSearch()" style="margin-top: 16px; background: linear-gradient(to bottom right, #005792, #001A2C); color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer;">전체 목록 보기</button>
            </div>
        `;
        container.appendChild(noResultsDiv);
    }

    function hideNoResults() {
        const noResults = document.querySelector('.no-results');
        if (noResults) {
            noResults.remove();
        }
    }

    // 검색 초기화 함수 (전역으로 선언)
    window.clearSearch = function() {
        searchInput.value = '';
        const announcements = document.querySelectorAll('.announcement-list-item');
        
        announcements.forEach(item => {
            item.style.display = 'flex';
            const link = item.querySelector('.announcement-list-item-link');
            const originalText = link.getAttribute('data-original-text');
            if (originalText) {
                link.textContent = originalText;
                link.removeAttribute('data-original-text');
            }
        });
        
        hideNoResults();
        updateResultCount(42); // 원래 개수로 복원
    };

    // 정렬 버튼 클릭 이벤트
    document.querySelectorAll('.sort-button').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.sort-button').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            const sortType = this.getAttribute('data-sort');
            console.log('정렬 방식:', sortType);
            
            sortAnnouncements(sortType);
        });
    });

    function sortAnnouncements(sortType) {
        const container = document.querySelector('.announcement-list-container');
        const items = Array.from(container.querySelectorAll('.announcement-list-item'));
        
        items.sort((a, b) => {
            switch(sortType) {
                case 'latest':
                    // 날짜 기준 최신순 정렬
                    const dateA = new Date(a.querySelector('.announcement-date').textContent.replace(/\./g, '-'));
                    const dateB = new Date(b.querySelector('.announcement-date').textContent.replace(/\./g, '-'));
                    return dateB - dateA;
                    
                case 'important':
                    // 중요 공지 우선 정렬
                    const isImportantA = a.classList.contains('important');
                    const isImportantB = b.classList.contains('important');
                    if (isImportantA && !isImportantB) return -1;
                    if (!isImportantA && isImportantB) return 1;
                    return 0;
                    
                case 'popular':
                    // 조회수 기준 정렬
                    const viewsA = parseInt(a.querySelector('.announcement-views').textContent.replace(/[^0-9]/g, ''));
                    const viewsB = parseInt(b.querySelector('.announcement-views').textContent.replace(/[^0-9]/g, ''));
                    return viewsB - viewsA;
                    
                default:
                    return 0;
            }
        });
        
        // 정렬된 순서로 DOM 재배치
        items.forEach(item => container.appendChild(item));
        
        // 정렬 애니메이션 효과
        items.forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                item.style.transition = 'all 0.3s ease';
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, index * 50);
        });
    }

    // 페이지네이션 클릭 이벤트
    document.querySelectorAll('.pagination-number').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.pagination-number').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            const pageNumber = this.getAttribute('data-page');
            console.log('페이지 번호:', pageNumber);
            
            goToPage(parseInt(pageNumber));
        });
    });

    // 이전/다음 버튼 이벤트
    document.querySelector('.pagination-button.prev').addEventListener('click', function() {
        if (!this.disabled) {
            const currentPage = getCurrentPage();
            if (currentPage > 1) {
                goToPage(currentPage - 1);
            }
        }
    });

    document.querySelector('.pagination-button.next').addEventListener('click', function() {
        if (!this.disabled) {
            const currentPage = getCurrentPage();
            const totalPages = getTotalPages();
            if (currentPage < totalPages) {
                goToPage(currentPage + 1);
            }
        }
    });

    function getCurrentPage() {
        const activePage = document.querySelector('.pagination-number.active');
        return activePage ? parseInt(activePage.getAttribute('data-page')) : 1;
    }

    function getTotalPages() {
        const pageNumbers = document.querySelectorAll('.pagination-number');
        let maxPage = 1;
        pageNumbers.forEach(btn => {
            const page = parseInt(btn.getAttribute('data-page'));
            if (page > maxPage) maxPage = page;
        });
        return maxPage;
    }

    function goToPage(pageNumber) {
        console.log(`${pageNumber}페이지로 이동`);
        
        // 페이지 번호 업데이트
        document.querySelectorAll('.pagination-number').forEach(btn => {
            btn.classList.remove('active');
            if (parseInt(btn.getAttribute('data-page')) === pageNumber) {
                btn.classList.add('active');
            }
        });
        
        // 이전/다음 버튼 상태 업데이트
        const prevBtn = document.querySelector('.pagination-button.prev');
        const nextBtn = document.querySelector('.pagination-button.next');
        const totalPages = getTotalPages();
        
        prevBtn.disabled = pageNumber <= 1;
        nextBtn.disabled = pageNumber >= totalPages;
        
        // 페이지 정보 업데이트
        const startItem = (pageNumber - 1) * 10 + 1;
        const endItem = Math.min(pageNumber * 10, 42);
        document.querySelector('.pagination-info span').textContent = `${startItem} - ${endItem} / 42개`;
        
        // 페이지 전환 애니메이션
        const container = document.querySelector('.announcement-list-container');
        container.style.opacity = '0.3';
        container.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            container.style.transition = 'all 0.3s ease';
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
            
            // 스크롤을 상단으로 부드럽게 이동
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        }, 150);
    }

    // 공지사항 아이템 클릭 시 애니메이션
    document.querySelectorAll('.announcement-list-item-link').forEach(link => {
        link.addEventListener('click', function(e) {
            // 실제 링크가 없는 경우 기본 동작 방지
            if (this.getAttribute('href') === '' || this.getAttribute('href') === '#') {
                e.preventDefault();
                
                // 클릭 애니메이션
                const item = this.closest('.announcement-list-item');
                item.style.transform = 'scale(0.98)';
                item.style.transition = 'transform 0.1s ease';
                
                setTimeout(() => {
                    item.style.transform = 'scale(1)';
                }, 100);
                
                console.log('공지사항 클릭:', this.textContent);
            }
        });
    });

    // 키보드 네비게이션 지원
    document.addEventListener('keydown', function(e) {
        const currentPage = getCurrentPage();
        const totalPages = getTotalPages();
        
        // 좌우 화살표로 페이지 이동
        if (e.key === 'ArrowLeft' && currentPage > 1) {
            e.preventDefault();
            goToPage(currentPage - 1);
        } else if (e.key === 'ArrowRight' && currentPage < totalPages) {
            e.preventDefault();
            goToPage(currentPage + 1);
        }
        
        // ESC 키로 검색 초기화
        if (e.key === 'Escape' && searchInput.value) {
            clearSearch();
            searchInput.blur();
        }
    });
});
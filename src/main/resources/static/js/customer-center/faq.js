// faq.js

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

    function updateResultCount(count) {
        const resultCount = document.querySelector('.result-count');
        resultCount.innerHTML = `검색 결과 <strong>${count}</strong>개`;
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
        updateResultCount(28);
    };

    // 사용자 유형 탭 기능
    document.querySelectorAll('.user-type-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            document.querySelectorAll('.user-type-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            const userType = this.getAttribute('data-type');
            console.log('사용자 유형:', userType);
            
            filterByUserType(userType);
        });
    });

    function filterByUserType(userType) {
        // 사용자 유형에 따른 FAQ 데이터
        const faqData = {
            client: {
                service: [
                    '[서비스 소개] 쉽고, 편하고, 안전한 무드트립 이용 방법',
                    '[서비스 소개] 무드트립 이용가이드: 의뢰인 편',
                    '[서비스 소개] 품질보장 서비스를 소개합니다!',
                    '[서비스 소개] 에스크로 결제방식은 무엇인가요?',
                    '[서비스 소개] Prime 서비스란 무엇인가요?'
                ],
                profile: [
                    '[회원 정보] 회원가입은 어떻게 하나요?',
                    '[회원 정보] 비밀번호를 잊어버렸어요',
                    '[회원 정보] 프로필 정보 수정 방법',
                    '[회원 정보] 회원 탈퇴는 어떻게 하나요?'
                ],
                usage: [
                    '[이용 방법] 서비스 이용 가이드',
                    '[이용 방법] 전문가 찾기 방법',
                    '[이용 방법] 프로젝트 의뢰 방법',
                    '[이용 방법] 채팅 사용법'
                ],
                payment: [
                    '[결제] 결제 방법 안내',
                    '[결제] 결제 오류 해결 방법',
                    '[결제] 세금계산서 발급',
                    '[결제] 카드 결제 문제 해결'
                ],
                refund: [
                    '[취소·환불] 환불 정책 안내',
                    '[취소·환불] 환불 신청 방법',
                    '[취소·환불] 부분 환불 가능한가요?',
                    '[취소·환불] 환불 처리 기간'
                ],
                dispute: [
                    '[분쟁·페널티] 분쟁 신고 방법',
                    '[분쟁·페널티] 페널티 정책',
                    '[분쟁·페널티] 중재 서비스 이용'
                ]
            },
            expert: {
                service: [
                    '[서비스 소개] 전문가 가입 가이드',
                    '[서비스 소개] 전문가 등급 시스템',
                    '[서비스 소개] 포트폴리오 작성법',
                    '[서비스 소개] 서비스 등록 방법'
                ],
                profile: [
                    '[회원 정보] 전문가 프로필 최적화',
                    '[회원 정보] 경력 및 자격증 등록',
                    '[회원 정보] 프로필 인증 방법'
                ],
                usage: [
                    '[이용 방법] 견적서 작성 가이드',
                    '[이용 방법] 고객과의 소통 방법',
                    '[이용 방법] 프로젝트 관리 시스템'
                ],
                payment: [
                    '[정산] 수수료 정책 안내',
                    '[정산] 정산 신청 방법',
                    '[정산] 세금 관련 안내'
                ],
                refund: [
                    '[환불] 전문가 환불 정책',
                    '[환불] 환불 처리 절차'
                ],
                dispute: [
                    '[분쟁·페널티] 전문가 페널티 정책',
                    '[분쟁·페널티] 분쟁 대응 방법'
                ]
            }
        };

        // 현재 선택된 카테고리 가져오기
        const activeCategory = document.querySelector('.category-button.active').getAttribute('data-category');
        
        // 해당 카테고리의 FAQ 업데이트
        filterFAQByCategory(activeCategory, userType);
    }

    // 카테고리 버튼 클릭 이벤트
    document.querySelectorAll('.category-button').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.category-button').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            const category = this.getAttribute('data-category');
            const userType = document.querySelector('.user-type-tab.active').getAttribute('data-type');
            
            filterFAQByCategory(category, userType);
        });
    });

    function filterFAQByCategory(category, userType = 'client') {
        console.log('카테고리:', category, '사용자 유형:', userType);
        
        // 카테고리 버튼 활성화 상태 업데이트
        document.querySelectorAll('.category-button').forEach(btn => {
            btn.classList.remove('active');
            if (btn.getAttribute('data-category') === category) {
                btn.classList.add('active');
            }
        });
        
        // 애니메이션 효과
        const container = document.querySelector('.faq-list-container');
        container.style.opacity = '0.3';
        container.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            // 여기서 실제로는 서버에서 데이터를 가져와야 함
            // 현재는 클라이언트 사이드에서 시뮬레이션
            
            container.style.transition = 'all 0.3s ease';
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
        }, 150);
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

    // 도움됨 버튼 클릭 이벤트
    document.querySelectorAll('.helpful-button').forEach(button => {
        button.addEventListener('click', function() {
            this.classList.toggle('active');
            
            const faqCard = this.closest('.faq-item-card');
            const faqTitle = faqCard.querySelector('.faq-item-link').textContent;
            
            if (this.classList.contains('active')) {
                console.log('도움됨 클릭:', faqTitle);
                
                // 피드백 애니메이션
                this.style.transform = 'scale(1.2)';
                setTimeout(() => {
                    this.style.transform = 'scale(1)';
                }, 200);
            } else {
                console.log('도움됨 취소:', faqTitle);
            }
        });
    });

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
        const endItem = Math.min(pageNumber * 10, 28);
        document.querySelector('.pagination-info span').textContent = `${startItem} - ${endItem} / 28개`;
        
        // 페이지 전환 애니메이션
        const container = document.querySelector('.faq-list-container');
        container.style.opacity = '0.3';
        container.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            container.style.transition = 'all 0.3s ease';
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
            
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        }, 150);
    }

    // FAQ 아이템 클릭 시 애니메이션
    document.querySelectorAll('.faq-item-link').forEach(link => {
        link.addEventListener('click', function(e) {
            if (this.getAttribute('href') === '' || this.getAttribute('href') === '#') {
                e.preventDefault();
                
                const card = this.closest('.faq-item-card');
                card.style.transform = 'scale(0.98)';
                card.style.transition = 'transform 0.1s ease';
                
                setTimeout(() => {
                    card.style.transform = 'scale(1)';
                }, 100);
                
                console.log('FAQ 클릭:', this.textContent);
            }
        });
    });

    // 키보드 네비게이션
    document.addEventListener('keydown', function(e) {
        const currentPage = getCurrentPage();
        const totalPages = getTotalPages();
        
        if (e.key === 'ArrowLeft' && currentPage > 1) {
            e.preventDefault();
            goToPage(currentPage - 1);
        } else if (e.key === 'ArrowRight' && currentPage < totalPages) {
            e.preventDefault();
            goToPage(currentPage + 1);
        }
        
        if (e.key === 'Escape' && searchInput.value) {
            clearSearch();
            searchInput.blur();
        }
    });

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

    // 초기 로드 시 기본 카테고리 설정
    filterFAQByCategory('service', 'client');
});
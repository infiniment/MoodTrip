// 간단한 탭 전환 기능
function searchFAQ() {
    const searchInput = document.getElementById('support-search-input');
    const searchTerm = searchInput.value.trim();
    
    if (searchTerm) {
        console.log('검색어:', searchTerm);
        // 여기에 실제 검색 로직 구현
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

    // 카테고리별 FAQ 필터링 함수
    function filterFAQByCategory(category) {
        const faqList = document.getElementById('faq-list');
        
        // 카테고리별 FAQ 데이터 (실제로는 서버에서 가져와야 함)
        const faqData = {
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
                '[회원 정보] 회원 탈퇴는 어떻게 하나요?',
                '[회원 정보] 프로필 정보 수정 방법'
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
                '[분쟁·페널티] 중재 서비스 이용',
                '[분쟁·페널티] 신고 처리 절차'
            ]
        };

        // FAQ 목록 업데이트
        const categoryFAQs = faqData[category] || [];
        
        faqList.innerHTML = '';
        
        categoryFAQs.forEach(faqTitle => {
            const faqItem = document.createElement('a');
            faqItem.href = '#'; // 실제 FAQ 페이지 링크로 변경 필요
            faqItem.className = 'faq-item';
            faqItem.target = '_blank';
            faqItem.textContent = faqTitle;
            
            faqList.appendChild(faqItem);
        });

        // FAQ가 없는 경우 메시지 표시
        if (categoryFAQs.length === 0) {
            const noDataMessage = document.createElement('div');
            noDataMessage.className = 'no-faq-message';
            noDataMessage.textContent = '해당 카테고리의 FAQ가 없습니다.';
            faqList.appendChild(noDataMessage);
        }
    }

    // 페이지 로드 시 기본적으로 서비스 소개 카테고리 표시
    filterFAQByCategory('service');
});
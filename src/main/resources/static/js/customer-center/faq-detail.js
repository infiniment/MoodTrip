// faq-detail.js

document.addEventListener('DOMContentLoaded', function() {
    // ========= 공통 유틸 =========
    function getAuthHeader() {
        const keys = ['accessToken', 'jwt', 'token'];
        for (const k of keys) {
            const v = (window.localStorage && localStorage.getItem(k)) || (window.sessionStorage && sessionStorage.getItem(k));
            if (v) return { Authorization: `Bearer ${v}` };
        }
        const m = document.cookie.match(/(?:^|;\s*)accessToken=([^;]+)/);
        if (m) return { Authorization: `Bearer ${decodeURIComponent(m[1])}` };
        return {};
    }


    // 토스트 메시지 표시
    function showToast(message) {
        const existingToast = document.querySelector('.toast');
        if (existingToast) existingToast.remove();

        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.textContent = message;
        toast.style.cssText = `
      position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%);
      background: linear-gradient(to bottom right, #005792, #001A2C);
      color: white; padding: 12px 24px; border-radius: 8px; font-size: 14px;
      font-weight: 500; box-shadow: 0 4px 12px rgba(0, 87, 146, 0.3);
      z-index: 1000; animation: slideUp 0.3s ease;
    `;
        const style = document.createElement('style');
        style.textContent = `
      @keyframes slideUp { from {opacity:0; transform: translateX(-50%) translateY(20px);} to {opacity:1; transform: translateX(-50%) translateY(0);} }
      @keyframes slideDown { from {opacity:1;} to {opacity:0;} }
    `;
        document.head.appendChild(style);
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.animation = 'slideDown 0.3s ease';
            setTimeout(() => { toast.remove(); style.remove(); }, 300);
        }, 3000);
    }


    // 도움됨/도움안됨 버튼 기능
    const helpfulYesButton = document.getElementById('helpful-yes');
    const helpfulNoButton  = document.getElementById('helpful-no');

    // 버튼이 없으면 나머지 로직 생략(다른 페이지 보호)
    if (!helpfulYesButton || !helpfulNoButton) return;

    // 페이지 전역에서 사용할 faqId (둘 중 아무 버튼의 data-faq-id 사용)
    const faqId =
        helpfulYesButton.getAttribute('data-faq-id') ||
        helpfulNoButton.getAttribute('data-faq-id');

    // 클릭 핸들러
    helpfulYesButton.addEventListener('click', function () {
        handleHelpfulClick('yes', this);
    });
    helpfulNoButton.addEventListener('click', function () {
        handleHelpfulClick('no', this);
    });

    function handleHelpfulClick(type, button) {
        // (안전) 버튼에서 다시 읽어도 됨
        const id = button.getAttribute('data-faq-id') || faqId;

        const endpoint =
            type === 'yes'
                ? `/customer-center/faq/helpful/${id}`
                : `/customer-center/faq/not-helpful/${id}`;

        const headers = { 'X-Requested-With': 'XMLHttpRequest', ...getAuthHeader() };

        fetch(endpoint, {
            method: 'POST',
            headers,
            credentials: 'include', // 세션/쿠키 방식도 지원
        })
            .then(async (res) => {
                if (res.status === 401) {
                    requireLoginUI();
                    return;
                }
                if (res.status === 403) {
                    showToast('권한이 없습니다.');
                    return;
                }
                if (!res.ok) {
                    const txt = await res.text();
                    showToast(txt || '오류가 발생했습니다.');
                    return;
                }

                // 성공 처리
                const other = type === 'yes' ? helpfulNoButton : helpfulYesButton;
                other.classList.remove('active');
                button.classList.add('active');

                button.style.transform = 'scale(1.05)';
                setTimeout(() => (button.style.transform = 'scale(1)'), 200);

                localStorage.setItem(`faq-vote-${id}`, type);
                showToast('피드백이 전송되었습니다.');
            })
            .catch(() => showToast('오류가 발생했습니다. 다시 시도해주세요.'));
    }


    // ===== 로그인 모달 =====
    const loginModal = document.getElementById('login-modal');
    const loginBtn   = document.getElementById('login-btn');
    const closeBtn   = document.getElementById('close-btn');

    function openLoginModal() {
        if (!loginModal) return;
        loginModal.classList.remove('hidden');
        requestAnimationFrame(() => loginModal.classList.add('show')); // ← 핵심
        document.body.style.overflow = 'hidden';
    }

    function closeLoginModal() {
        if (!loginModal) return;
        loginModal.classList.remove('show');
        setTimeout(() => loginModal.classList.add('hidden'), 200);
        document.body.style.overflow = '';
    }

    loginBtn?.addEventListener('click', () => location.href = '/login');
    closeBtn?.addEventListener('click', closeLoginModal);

    // 오버레이 바깥 클릭 또는 data-close 속성 클릭 시 닫기
    loginModal?.addEventListener('click', (e) => {
        if (e.target === loginModal || e.target.hasAttribute('data-close')) closeLoginModal();
    });

    // ESC로 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && loginModal && !loginModal.classList.contains('hidden')) {
            closeLoginModal();
        }
    });

    // 401 발생 시 호출되는 함수
    function requireLoginUI() { openLoginModal(); }



    // 이전/다음 FAQ 버튼 기능
    const prevButton = document.querySelector('.prev-btn');
    const nextButton = document.querySelector('.next-btn');

    if (prevButton) {
        prevButton.addEventListener('click', function () {
            this.style.transform = 'translateX(-2px)';
            setTimeout(() => (this.style.transform = 'translateX(0)'), 200);
        });
    }
    if (nextButton) {
        nextButton.addEventListener('click', function () {
            this.style.transform = 'translateX(2px)';
            setTimeout(() => (this.style.transform = 'translateX(0)'), 200);
        });
    }
    
    // 관련 FAQ 클릭 애니메이션
    document.querySelectorAll('.related-faq-item').forEach(item => {
        item.addEventListener('click', function(e) {
            // href가 # 인 경우 기본 동작 방지
            if (this.getAttribute('href') === '#') {
                e.preventDefault();
                
                // 클릭 애니메이션
                this.style.transform = 'translateX(8px) scale(0.98)';
                setTimeout(() => {
                    this.style.transform = 'translateX(4px) scale(1)';
                }, 150);
                
                console.log('관련 FAQ 클릭:', this.querySelector('.related-question').textContent);
            }
        });
    });
    
    // 도움 옵션 클릭 애니메이션
    document.querySelectorAll('.help-option').forEach(item => {
        item.addEventListener('click', function(e) {
            // 전화번호가 아닌 경우 기본 동작 방지 (데모용)
            if (!this.getAttribute('href').startsWith('tel:')) {
                e.preventDefault();
                
                // 클릭 애니메이션
                this.style.transform = 'translateX(8px) scale(0.98)';
                setTimeout(() => {
                    this.style.transform = 'translateX(4px) scale(1)';
                }, 150);
                
                console.log('도움 옵션 클릭:', this.querySelector('strong').textContent);
            }
        });
    });
    
    // 프로세스 플로우 스텝 호버 시 연결된 스텝 하이라이트
    const flowSteps = document.querySelectorAll('.flow-step');
    
    flowSteps.forEach((step, index) => {
        step.addEventListener('mouseenter', function() {
            // 현재 스텝과 다음 스텝 하이라이트
            flowSteps.forEach((s, i) => {
                if (i <= index) {
                    s.style.background = 'linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%)';
                    s.style.borderColor = '#005792';
                } else {
                    s.style.background = 'white';
                    s.style.borderColor = '#e2e8f0';
                }
            });
        });
        
        step.addEventListener('mouseleave', function() {
            // 모든 스텝 원상복구
            flowSteps.forEach(s => {
                s.style.background = 'white';
                s.style.borderColor = '#e2e8f0';
            });
        });
    });
    
    // 서브 FAQ 아이템 클릭 시 확장/축소 효과
    document.querySelectorAll('.sub-faq-item').forEach(item => {
        item.addEventListener('click', function() {
            this.style.background = '#e0f2fe';
            this.style.borderLeftColor = '#0369a1';
            
            setTimeout(() => {
                this.style.background = '#f8fafc';
                this.style.borderLeftColor = '#005792';
            }, 300);
        });
    });
    
    // 스크롤 시 상단 고정 네비게이션
    let lastScrollY = window.scrollY;
    const breadcrumbNav = document.querySelector('.breadcrumb-nav');
    
    window.addEventListener('scroll', function() {
        const currentScrollY = window.scrollY;
        
        if (currentScrollY > 100) {
            breadcrumbNav.style.position = 'sticky';
            breadcrumbNav.style.top = '0';
            breadcrumbNav.style.backgroundColor = 'rgba(255, 255, 255, 0.95)';
            breadcrumbNav.style.backdropFilter = 'blur(8px)';
            breadcrumbNav.style.zIndex = '100';
        } else {
            breadcrumbNav.style.position = 'static';
            breadcrumbNav.style.backgroundColor = 'transparent';
            breadcrumbNav.style.backdropFilter = 'none';
        }
        
        lastScrollY = currentScrollY;
    });
    
    // 키보드 네비게이션
    document.addEventListener('keydown', function(e) {
        // ESC 키로 뒤로 가기
        if (e.key === 'Escape') {
            history.back();
        }
        
        // 좌우 화살표로 이전/다음 FAQ 이동
        if (e.key === 'ArrowLeft') {
            prevButton.click();
        } else if (e.key === 'ArrowRight') {
            nextButton.click();
        }
        
        // Y 키로 도움됨 표시
        if (e.key === 'y' || e.key === 'Y') {
            helpfulYesButton.click();
        }
        
        // N 키로 도움안됨 표시
        if (e.key === 'n' || e.key === 'N') {
            helpfulNoButton.click();
        }
    });
    
    // 페이지 로드 시 애니메이션
    function animateOnLoad() {
        const elements = [
            '.faq-header',
            '.content-summary',
            '.content-body',
            '.faq-footer',
            '.related-faqs'
        ];
        
        elements.forEach((selector, index) => {
            const element = document.querySelector(selector);
            if (element) {
                element.style.opacity = '0';
                element.style.transform = 'translateY(20px)';
                
                setTimeout(() => {
                    element.style.transition = 'all 0.6s ease';
                    element.style.opacity = '1';
                    element.style.transform = 'translateY(0)';
                }, index * 150);
            }
        });
    }
    
    // 페이지 로드 완료 후 애니메이션 실행
    setTimeout(animateOnLoad, 100);
    
    // 장점 카드 순차 애니메이션
    const benefitCards = document.querySelectorAll('.benefit-card');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
    }, { threshold: 0.1 });
    
    benefitCards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';
        observer.observe(card);
    });
    
    // URL 해시가 있는 경우 해당 섹션으로 스크롤
    if (window.location.hash) {
        const targetElement = document.querySelector(window.location.hash);
        if (targetElement) {
            setTimeout(() => {
                targetElement.scrollIntoView({ behavior: 'smooth' });
            }, 500);
        }
    }
    
    // 텍스트 복사 기능 (Ctrl+C로 FAQ 제목 복사)
    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'c' && !window.getSelection().toString()) {
            const faqTitle = document.querySelector('.faq-title').textContent;
            navigator.clipboard.writeText(faqTitle).then(() => {
                showToast('FAQ 제목이 복사되었습니다.');
            });
        }
    });
});
// // announcement-detail.js
//
// document.addEventListener('DOMContentLoaded', function() {
//
//     // 공유 버튼 기능
//     const shareButton = document.getElementById('share-button');
//     if (shareButton) {
//         shareButton.addEventListener('click', function () {
//             if (navigator.share) {
//                 // Web Share API 지원하는 경우
//                 navigator.share({
//                     title: document.querySelector('.announcement-title').textContent,
//                     text: '무드트립 공지사항을 확인해보세요.',
//                     url: window.location.href
//                 }).then(() => {
//                     console.log('공유 성공');
//                 }).catch((error) => {
//                     console.log('공유 실패:', error);
//                     fallbackShare();
//                 });
//             } else {
//                 // Web Share API 지원하지 않는 경우
//                 fallbackShare();
//             }
//         });
//     }
//     function fallbackShare() {
//         // URL 복사 기능
//         navigator.clipboard.writeText(window.location.href).then(() => {
//             showToast('링크가 클립보드에 복사되었습니다.');
//         }).catch(() => {
//             // 클립보드 API도 지원하지 않는 경우
//             const textArea = document.createElement('textarea');
//             textArea.value = window.location.href;
//             document.body.appendChild(textArea);
//             textArea.select();
//             document.execCommand('copy');
//             document.body.removeChild(textArea);
//             showToast('링크가 클립보드에 복사되었습니다.');
//         });
//     }
//
//     // 토스트 메시지 표시
//     function showToast(message) {
//         // 기존 토스트 제거
//         const existingToast = document.querySelector('.toast');
//         if (existingToast) {
//             existingToast.remove();
//         }
//
//         const toast = document.createElement('div');
//         toast.className = 'toast';
//         toast.textContent = message;
//         toast.style.cssText = `
//             position: fixed;
//             bottom: 20px;
//             left: 50%;
//             transform: translateX(-50%);
//             background: linear-gradient(to bottom right, #005792, #001A2C);
//             color: white;
//             padding: 12px 24px;
//             border-radius: 8px;
//             font-size: 14px;
//             font-weight: 500;
//             box-shadow: 0 4px 12px rgba(0, 87, 146, 0.3);
//             z-index: 1000;
//             animation: slideUp 0.3s ease;
//         `;
//
//         // 애니메이션 추가
//         const style = document.createElement('style');
//         style.textContent = `
//             @keyframes slideUp {
//                 from {
//                     opacity: 0;
//                     transform: translateX(-50%) translateY(20px);
//                 }
//                 to {
//                     opacity: 1;
//                     transform: translateX(-50%) translateY(0);
//                 }
//             }
//         `;
//         document.head.appendChild(style);
//
//         document.body.appendChild(toast);
//
//         // 3초 후 제거
//         setTimeout(() => {
//             toast.style.animation = 'slideDown 0.3s ease';
//             setTimeout(() => {
//                 if (toast.parentNode) {
//                     toast.remove();
//                 }
//                 if (style.parentNode) {
//                     style.remove();
//                 }
//             }, 300);
//         }, 3000);
//     }
//
//     // 인증 헤더 재사용 유틸
//     function getAuthHeader() {
//         const keys = ['accessToken','jwt','token'];
//         for (const k of keys) {
//             const v = (localStorage && localStorage.getItem(k)) || (sessionStorage && sessionStorage.getItem(k));
//             if (v) return { Authorization: `Bearer ${v}` };
//         }
//         const m = document.cookie.match(/(?:^|;\s*)accessToken=([^;]+)/);
//         if (m) return { Authorization: `Bearer ${decodeURIComponent(m[1])}` };
//         return {};
//     }
//
//     // 도움됨 버튼
//     const helpfulButton = document.getElementById('helpful-button');
//     if (helpfulButton) {
//         const noticeId = helpfulButton.getAttribute('data-notice-id');
//         const helpfulText = document.getElementById('helpful-text');
//         const helpfulCount = document.getElementById('helpful-count');
//
//         helpfulButton.addEventListener('click', () => {
//             fetch(`/customer-center/announcement/helpful/${noticeId}`, {
//                 method: 'POST',
//                 headers: { 'X-Requested-With': 'XMLHttpRequest', ...getAuthHeader() },
//                 credentials: 'include'
//             })
//                 .then(async (res) => {
//                     if (res.status === 401) {
//                         // 여기에서 로그인 모달을 띄우거나 /login으로 이동
//                         // window.location.href = '/login';
//                         return null;
//                     }
//                     if (!res.ok) {
//                         const txt = await res.text();
//                         showToast(txt || '오류가 발생했습니다.');
//                         return null;
//                     }
//                     return res.json();
//                 })
//                 .then((data) => {
//                     if (!data) return;
//                     helpfulButton.classList.toggle('active', data.active);
//                     if (helpfulText) helpfulText.textContent = data.active ? '도움이 되었어요!' : '도움이 되었나요?';
//                     if (helpfulCount) helpfulCount.textContent = data.count;
//
//                     helpfulButton.style.transform = 'scale(1.05)';
//                     setTimeout(() => helpfulButton.style.transform = 'scale(1)', 200);
//                     showToast('피드백이 전송되었습니다.');
//                 })
//                 .catch(() => showToast('오류가 발생했습니다. 다시 시도해주세요.'));
//         });
//     }
//
//     // 이전/다음 글 버튼 기능
//     const prevButton = document.querySelector('.prev-btn');
//     const nextButton = document.querySelector('.next-btn');
//
//     prevButton.addEventListener('click', function() {
//         // 실제로는 이전 글 URL로 이동
//         console.log('이전 글로 이동');
//         this.style.transform = 'translateX(-2px)';
//         setTimeout(() => {
//             this.style.transform = 'translateX(0)';
//         }, 200);
//     });
//
//     nextButton.addEventListener('click', function() {
//         // 실제로는 다음 글 URL로 이동
//         console.log('다음 글로 이동');
//         this.style.transform = 'translateX(2px)';
//         setTimeout(() => {
//             this.style.transform = 'translateX(0)';
//         }, 200);
//     });
//
//     // 관련 공지사항 클릭 애니메이션
//     document.querySelectorAll('.related-item').forEach(item => {
//         item.addEventListener('click', function(e) {
//             // href가 # 인 경우 기본 동작 방지
//             if (this.getAttribute('href') === '#') {
//                 e.preventDefault();
//
//                 // 클릭 애니메이션
//                 this.style.transform = 'translateX(8px) scale(0.98)';
//                 setTimeout(() => {
//                     this.style.transform = 'translateX(4px) scale(1)';
//                 }, 150);
//
//                 console.log('관련 공지사항 클릭:', this.querySelector('.related-text').textContent);
//             }
//         });
//     });
//
//     // 스크롤 시 상단 고정 네비게이션 (선택사항)
//     let lastScrollY = window.scrollY;
//     const breadcrumbNav = document.querySelector('.breadcrumb-nav');
//
//     window.addEventListener('scroll', function() {
//         const currentScrollY = window.scrollY;
//
//         if (currentScrollY > 100) {
//             breadcrumbNav.style.position = 'sticky';
//             breadcrumbNav.style.top = '0';
//             breadcrumbNav.style.backgroundColor = 'rgba(255, 255, 255, 0.95)';
//             breadcrumbNav.style.backdropFilter = 'blur(8px)';
//             breadcrumbNav.style.zIndex = '100';
//         } else {
//             breadcrumbNav.style.position = 'static';
//             breadcrumbNav.style.backgroundColor = 'transparent';
//             breadcrumbNav.style.backdropFilter = 'none';
//         }
//
//         lastScrollY = currentScrollY;
//     });
//
//     // 키보드 네비게이션
//     document.addEventListener('keydown', function(e) {
//         // ESC 키로 뒤로 가기
//         if (e.key === 'Escape') {
//             history.back();
//         }
//
//         // 좌우 화살표로 이전/다음 글 이동
//         if (e.key === 'ArrowLeft') {
//             prevButton.click();
//         } else if (e.key === 'ArrowRight') {
//             nextButton.click();
//         }
//
//         // Ctrl/Cmd + D로 도움됨 표시
//         if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
//             e.preventDefault();
//             helpfulButton.click();
//         }
//
//         // Ctrl/Cmd + S로 공유
//         if ((e.ctrlKey || e.metaKey) && e.key === 's') {
//             e.preventDefault();
//             shareButton.click();
//         }
//     });
//
//     // 페이지 로드 시 애니메이션
//     function animateOnLoad() {
//         const elements = [
//             '.announcement-header',
//             '.announcement-content',
//             '.announcement-footer',
//             '.related-announcements'
//         ];
//
//         elements.forEach((selector, index) => {
//             const element = document.querySelector(selector);
//             if (element) {
//                 element.style.opacity = '0';
//                 element.style.transform = 'translateY(20px)';
//
//                 setTimeout(() => {
//                     element.style.transition = 'all 0.6s ease';
//                     element.style.opacity = '1';
//                     element.style.transform = 'translateY(0)';
//                 }, index * 150);
//             }
//         });
//     }
//
//     // 페이지 로드 완료 후 애니메이션 실행
//     setTimeout(animateOnLoad, 100);
//
//     // 인쇄 버튼 이벤트는 이미 HTML에서 onclick으로 처리됨
//
//     // 텍스트 선택 시 공유 버튼 표시 (고급 기능)
//     let selectionTimeout;
//
//     document.addEventListener('mouseup', function() {
//         clearTimeout(selectionTimeout);
//         selectionTimeout = setTimeout(() => {
//             const selection = window.getSelection();
//             if (selection.toString().length > 10) {
//                 showSelectionShareButton(selection);
//             } else {
//                 hideSelectionShareButton();
//             }
//         }, 100);
//     });
//
//     function showSelectionShareButton(selection) {
//         hideSelectionShareButton();
//
//         const range = selection.getRangeAt(0);
//         const rect = range.getBoundingClientRect();
//
//         const shareBtn = document.createElement('button');
//         shareBtn.id = 'selection-share';
//         shareBtn.innerHTML = '📋 복사';
//         shareBtn.style.cssText = `
//             position: fixed;
//             top: ${rect.top - 40}px;
//             left: ${rect.left + (rect.width / 2) - 25}px;
//             background: linear-gradient(to bottom right, #005792, #001A2C);
//             color: white;
//             border: none;
//             padding: 6px 12px;
//             border-radius: 6px;
//             font-size: 12px;
//             font-weight: 500;
//             cursor: pointer;
//             z-index: 1000;
//             box-shadow: 0 2px 8px rgba(0, 87, 146, 0.3);
//         `;
//
//         shareBtn.addEventListener('click', function() {
//             navigator.clipboard.writeText(selection.toString()).then(() => {
//                 showToast('선택한 텍스트가 복사되었습니다.');
//                 hideSelectionShareButton();
//                 selection.removeAllRanges();
//             });
//         });
//
//         document.body.appendChild(shareBtn);
//     }
//
//     function hideSelectionShareButton() {
//         const existing = document.getElementById('selection-share');
//         if (existing) {
//             existing.remove();
//         }
//     }
//
//     // 다른 곳 클릭 시 선택 해제
//     document.addEventListener('click', function(e) {
//         if (e.target.id !== 'selection-share') {
//             hideSelectionShareButton();
//         }
//     });
// });


document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    /* ========== Toast ========== */
    function showToast(message) {
        const existing = document.querySelector('.toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.textContent = message;
        toast.style.cssText = `
      position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%);
      background: linear-gradient(to bottom right, #005792, #001A2C);
      color: #fff; padding: 12px 24px; border-radius: 8px; font-size: 14px;
      font-weight: 600; box-shadow: 0 4px 12px rgba(0,87,146,.3); z-index: 1000;
      animation: slideUp .3s ease;
    `;
        const style = document.createElement('style');
        style.textContent = `
      @keyframes slideUp { from {opacity:0; transform: translateX(-50%) translateY(20px);} to {opacity:1; transform: translateX(-50%) translateY(0);} }
      @keyframes slideDown { from {opacity:1;} to {opacity:0;} }
    `;
        document.head.appendChild(style);
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideDown .3s ease';
            setTimeout(() => { toast.remove(); style.remove(); }, 300);
        }, 2500);
    }

    /* ========== 인증 헤더 유틸 ========== */
    function getAuthHeader() {
        const keys = ['accessToken', 'jwt', 'token'];
        for (const k of keys) {
            const v = (localStorage && localStorage.getItem(k)) || (sessionStorage && sessionStorage.getItem(k));
            if (v) return { Authorization: `Bearer ${v}` };
        }
        const m = document.cookie.match(/(?:^|;\s*)accessToken=([^;]+)/);
        if (m) return { Authorization: `Bearer ${decodeURIComponent(m[1])}` };
        return {};
    }

    /* ========== 로그인 모달 ========== */
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


    /* ========== 공유 버튼 ========== */
    const shareButton = document.getElementById('share-button');
    if (shareButton) {
        shareButton.addEventListener('click', function () {
            const title = (document.querySelector('.announcement-title') || {}).textContent || document.title;
            if (navigator.share) {
                navigator.share({ title, text: '무드트립 공지사항을 확인해보세요.', url: window.location.href })
                    .catch(() => fallbackShare());
            } else {
                fallbackShare();
            }
        });
    }
    function fallbackShare() {
        (navigator.clipboard?.writeText?.(window.location.href) || Promise.reject())
            .then(() => showToast('링크가 클립보드에 복사되었습니다.'))
            .catch(() => {
                const ta = document.createElement('textarea');
                ta.value = window.location.href;
                document.body.appendChild(ta);
                ta.select();
                document.execCommand('copy');
                ta.remove();
                showToast('링크가 클립보드에 복사되었습니다.');
            });
    }

    /* ========== 도움됨(토글) ========== */
    const helpfulButton = document.getElementById('helpful-button');
    if (helpfulButton) {
        const noticeId    = helpfulButton.getAttribute('data-notice-id');
        const helpfulText = document.getElementById('helpful-text');
        const countEl     = document.getElementById('helpful-count');

        function setCount(n) {
            if (!countEl) return;
            countEl.textContent = n;
            countEl.classList.toggle('is-zero', Number(n) === 0);
            countEl.classList.remove('pop');
            requestAnimationFrame(() => countEl.classList.add('pop'));
        }

        helpfulButton.addEventListener('click', () => {
            fetch(`/customer-center/announcement/helpful/${noticeId}`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest', ...getAuthHeader() },
                credentials: 'include'
            })
                .then(async (res) => {
                    if (res.status === 401) { requireLoginUI(); return null; }
                    if (!res.ok) { const t = await res.text(); showToast(t || '오류가 발생했습니다.'); return null; }
                    // 서버 응답: { active:boolean, count:number } 형태여야 함
                    return res.json();
                })
                .then((data) => {
                    if (!data) return;
                    if (data.active) {
                        helpfulButton.classList.add('active');
                    } else {
                        helpfulButton.classList.remove('active');
                    }
                    helpfulButton.setAttribute('aria-pressed', data.active ? 'true' : 'false');
                    if (helpfulText) helpfulText.textContent = data.active ? '도움이 되었어요!' : '도움이 되었나요?';
                    setCount(data.count);

                    helpfulButton.style.transform = 'scale(1.05)';
                    setTimeout(() => helpfulButton.style.transform = 'scale(1)', 160);
                    showToast('피드백이 전송되었습니다.');
                })
                .catch(() => showToast('오류가 발생했습니다. 다시 시도해주세요.'));
        });
    }

    /* ========== 이전/다음 버튼(가드 처리) ========== */
    const prevButton = document.querySelector('.prev-btn');
    const nextButton = document.querySelector('.next-btn');
    if (prevButton) {
        prevButton.addEventListener('click', function () {
            this.style.transform = 'translateX(-2px)';
            setTimeout(() => this.style.transform = 'translateX(0)', 200);
        });
    }
    if (nextButton) {
        nextButton.addEventListener('click', function () {
            this.style.transform = 'translateX(2px)';
            setTimeout(() => this.style.transform = 'translateX(0)', 200);
        });
    }

    /* ========== 관련 공지 클릭 애니메이션 ========== */
    document.querySelectorAll('.related-item').forEach((item) => {
        item.addEventListener('click', function (e) {
            if (this.getAttribute('href') === '#') {
                e.preventDefault();
                this.style.transform = 'translateX(8px) scale(0.98)';
                setTimeout(() => { this.style.transform = 'translateX(4px) scale(1)'; }, 150);
            }
        });
    });

    /* ========== 스크롤 sticky ========== */
    const breadcrumbNav = document.querySelector('.breadcrumb-nav');
    if (breadcrumbNav) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 100) {
                breadcrumbNav.style.position = 'sticky';
                breadcrumbNav.style.top = '0';
                breadcrumbNav.style.backgroundColor = 'rgba(255,255,255,.95)';
                breadcrumbNav.style.backdropFilter  = 'blur(8px)';
                breadcrumbNav.style.zIndex = '100';
            } else {
                breadcrumbNav.style.position = 'static';
                breadcrumbNav.style.backgroundColor = 'transparent';
                breadcrumbNav.style.backdropFilter  = 'none';
            }
        });
    }

    /* ========== 단축키 ========== */
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') history.back();
        if (e.key === 'ArrowLeft'  && prevButton) prevButton.click();
        if (e.key === 'ArrowRight' && nextButton) nextButton.click();
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'd' && helpfulButton) { e.preventDefault(); helpfulButton.click(); }
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's' && shareButton) { e.preventDefault(); shareButton.click(); }
    });

    /* ========== 페이드 인 ========== */
    function animateOnLoad() {
        ['.announcement-header', '.announcement-content', '.announcement-footer', '.related-announcements']
            .forEach((sel, i) => {
                const el = document.querySelector(sel);
                if (!el) return;
                el.style.opacity = '0';
                el.style.transform = 'translateY(20px)';
                setTimeout(() => {
                    el.style.transition = 'all .6s ease';
                    el.style.opacity = '1';
                    el.style.transform = 'translateY(0)';
                }, i * 150);
            });
    }
    setTimeout(animateOnLoad, 100);

    /* ========== 텍스트 선택 복사 버튼 ========== */
    let selectionTimeout;
    document.addEventListener('mouseup', function () {
        clearTimeout(selectionTimeout);
        selectionTimeout = setTimeout(() => {
            const sel = window.getSelection();
            if (sel && sel.toString().length > 10) showSelectionShareButton(sel);
            else hideSelectionShareButton();
        }, 100);
    });

    function showSelectionShareButton(selection) {
        hideSelectionShareButton();
        if (!selection.rangeCount) return;

        const rect = selection.getRangeAt(0).getBoundingClientRect();
        const shareBtn = document.createElement('button');
        shareBtn.id = 'selection-share';
        shareBtn.textContent = '📋 복사';
        shareBtn.style.cssText = `
      position: fixed; top: ${rect.top - 40}px; left: ${rect.left + rect.width / 2 - 25}px;
      background: linear-gradient(to bottom right, #005792, #001A2C); color: #fff; border: none;
      padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer;
      z-index: 1000; box-shadow: 0 2px 8px rgba(0,87,146,.3);
    `;
        shareBtn.addEventListener('click', function () {
            navigator.clipboard.writeText(selection.toString()).then(() => {
                showToast('선택한 텍스트가 복사되었습니다.');
                hideSelectionShareButton();
                selection.removeAllRanges();
            });
        });
        document.body.appendChild(shareBtn);
    }
    function hideSelectionShareButton() {
        const existing = document.getElementById('selection-share');
        if (existing) existing.remove();
    }
    document.addEventListener('click', function (e) {
        if (e.target && e.target.id !== 'selection-share') hideSelectionShareButton();
    });
});

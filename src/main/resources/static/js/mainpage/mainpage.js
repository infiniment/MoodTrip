// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
  initScrollAnimations();
  initWeatherHoverEffects();
  initSmoothScroll();
  initEmotionInteractions(); // 새로운 감정 인터랙션 추가
});

// 1. 스크롤 애니메이션
function initScrollAnimations() {
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
      }
    });
  }, observerOptions);

  // 애니메이션 대상 요소들
  const elements = document.querySelectorAll('.feature-item, .room-card, .weather-card, .section-header');
  elements.forEach(el => {
    observer.observe(el);
  });
}

// 2. 날씨 호버 효과
function initWeatherHoverEffects() {
  const weatherCards = document.querySelectorAll('.weather-card');
  const weatherSection = document.querySelector('.weather-travel');

  if (!weatherCards.length || !weatherSection) return;

  const backgroundColors = {
    sunny: 'linear-gradient(180deg, #fef3c7 0%, #fde68a 100%)',
    rainy: 'linear-gradient(180deg, #dbeafe 0%, #bfdbfe 100%)',
    cloudy: 'linear-gradient(180deg, #f3f4f6 0%, #e5e7eb 100%)',
    snowy: 'linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)'
  };

  weatherCards.forEach(card => {
    card.addEventListener('mouseenter', () => {
      const weatherType = [...card.classList].find(cls =>
          cls === 'sunny' || cls === 'rainy' || cls === 'cloudy' || cls === 'snowy'
      );
      const bgColor = backgroundColors[weatherType] || backgroundColors.sunny;

      weatherSection.style.background = bgColor;
      weatherSection.style.transition = 'background 0.3s ease';
    });

    card.addEventListener('mouseleave', () => {
      weatherSection.style.background = 'linear-gradient(180deg, #ffffff 0%, #f8fafc 100%)';
    });
  });
}

// 3. 새로운 감정 인터랙션 기능
function initEmotionInteractions() {
  const emotionItems = document.querySelectorAll('.emotion-item');
  const emotionCenter = document.querySelector('.emotion-center');
  const emotionBrand = document.querySelector('.emotion-brand');
  const emotionSubtitle = document.querySelector('.emotion-subtitle');

  if (!emotionItems.length || !emotionCenter) return;

  // 감정별 메시지 매핑
  const emotionMessages = {
    '행복': {
      brand: 'HAPPY',
      subtitle: '즐거운 순간을 만들어요',
      color: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)'
    },
    '설레임': {
      brand: 'EXCITING',
      subtitle: '새로운 모험이 기다려요',
      color: 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)'
    },
    '평온': {
      brand: 'PEACEFUL',
      subtitle: '마음의 평화를 찾아요',
      color: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
    },
    '신남': {
      brand: 'AMAZING',
      subtitle: '신나는 여행을 떠나요',
      color: 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)'
    },
    '힐링': {
      brand: 'HEALING',
      subtitle: '지친 마음을 달래요',
      color: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)'
    },
    '모험': {
      brand: 'ADVENTURE',
      subtitle: '스릴 넘치는 여행이에요',
      color: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
    },
    '로맨틱': {
      brand: 'ROMANTIC',
      subtitle: '사랑스러운 순간들이에요',
      color: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)'
    },
    '자유': {
      brand: 'FREEDOM',
      subtitle: '자유로운 여행을 즐겨요',
      color: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)'
    }
  };

  const defaultMessage = {
    brand: 'MOOD TRIP',
    subtitle: '감정을 찾아보세요',
    color: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)'
  };

  // 감정 아이템 호버 이벤트
  emotionItems.forEach(item => {
    const emotion = item.dataset.emotion;

    item.addEventListener('mouseenter', () => {
      const message = emotionMessages[emotion] || defaultMessage;

      // 텍스트 변경
      emotionBrand.textContent = message.brand;
      emotionSubtitle.textContent = message.subtitle;

      // 색상 변경
      emotionBrand.style.background = message.color;
      emotionBrand.style.webkitBackgroundClip = 'text';
      emotionBrand.style.webkitTextFillColor = 'transparent';
      emotionBrand.style.backgroundClip = 'text';

      // 중앙 카드 효과
      emotionCenter.style.transform = 'scale(1.15)';
      emotionCenter.style.boxShadow = '0 25px 50px rgba(0, 0, 0, 0.15)';
    });

    item.addEventListener('mouseleave', () => {
      // 기본 상태로 복원
      emotionBrand.textContent = defaultMessage.brand;
      emotionSubtitle.textContent = defaultMessage.subtitle;
      emotionBrand.style.background = defaultMessage.color;
      emotionBrand.style.webkitBackgroundClip = 'text';
      emotionBrand.style.webkitTextFillColor = 'transparent';
      emotionBrand.style.backgroundClip = 'text';

      emotionCenter.style.transform = 'scale(1.1)';
      emotionCenter.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.1)';
    });

    // 클릭 이벤트 (감정 선택)
    item.addEventListener('click', () => {
      const emotion = item.dataset.emotion;
      console.log(`${emotion} 감정이 선택되었습니다!`);

      // 선택된 감정으로 페이지 이동 (실제 구현 시)
      // window.location.href = `/emotion-search?emotion=${encodeURIComponent(emotion)}`;

      // 임시 피드백
      showEmotionFeedback(emotion);
    });
  });
}

// 감정 선택 피드백 표시
function showEmotionFeedback(emotion) {
  // 기존 피드백 제거
  const existingFeedback = document.querySelector('.emotion-feedback');
  if (existingFeedback) {
    existingFeedback.remove();
  }

  // 새 피드백 생성
  const feedback = document.createElement('div');
  feedback.className = 'emotion-feedback';
  feedback.innerHTML = `
    <div style="
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 0, 0, 0.9);
      color: white;
      padding: 20px 40px;
      border-radius: 16px;
      font-size: 1.2rem;
      font-weight: 600;
      z-index: 9999;
      backdrop-filter: blur(10px);
      animation: fadeInScale 0.3s ease-out;
    ">
      ${emotion} 감정이 선택되었습니다! ✨
    </div>
  `;

  // 애니메이션 스타일 추가
  const style = document.createElement('style');
  style.textContent = `
    @keyframes fadeInScale {
      from {
        opacity: 0;
        transform: translate(-50%, -50%) scale(0.8);
      }
      to {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
      }
    }
  `;
  document.head.appendChild(style);

  document.body.appendChild(feedback);

  // 2초 후 제거
  setTimeout(() => {
    feedback.style.animation = 'fadeInScale 0.3s ease-out reverse';
    setTimeout(() => {
      feedback.remove();
      style.remove();
    }, 300);
  }, 2000);
}

// 4. 부드러운 스크롤
function initSmoothScroll() {
  // 섹션으로 스크롤하는 함수
  window.scrollToSection = function(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    }
  };
}

// 5. 모든 날씨 여행지 보기
window.showAllWeatherSpots = function() {
  console.log('모든 날씨 여행지 페이지로 이동');
  // 실제 백엔드 연동 시 페이지 이동 처리
  // window.location.href = '/weather-spots';
};

// 6. 유틸리티 함수들
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

function throttle(func, limit) {
  let inThrottle;
  return function() {
    const args = arguments;
    const context = this;
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

// 7. 성능 최적화
function initPerformanceOptimizations() {
  // 이미지 지연 로딩
  const images = document.querySelectorAll('img[data-src]');
  if (images.length > 0) {
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target;
          img.src = img.dataset.src;
          img.removeAttribute('data-src');
          imageObserver.unobserve(img);
        }
      });
    });

    images.forEach(img => imageObserver.observe(img));
  }
}

// 8. 접근성 개선
function initAccessibility() {
  // 키보드 네비게이션
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
      document.body.classList.add('keyboard-navigation');
    }
  });

  document.addEventListener('mousedown', () => {
    document.body.classList.remove('keyboard-navigation');
  });

  // 스크린 리더를 위한 라이브 영역
  const liveRegion = document.createElement('div');
  liveRegion.setAttribute('aria-live', 'polite');
  liveRegion.setAttribute('aria-atomic', 'true');
  liveRegion.className = 'sr-only';
  liveRegion.style.cssText = 'position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0;';
  document.body.appendChild(liveRegion);

  // 라이브 영역 업데이트 함수
  window.updateLiveRegion = function(message) {
    liveRegion.textContent = message;
  };
}

// 9. 에러 처리
function initErrorHandling() {
  window.addEventListener('error', (e) => {
    console.error('JavaScript Error:', e.error);

    // 개발 환경에서만 에러 로깅
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      console.warn('Development error logged for debugging');
    }
  });

  // 비동기 에러 처리
  window.addEventListener('unhandledrejection', (e) => {
    console.error('Unhandled Promise Rejection:', e.reason);
    e.preventDefault();
  });
}

// 10. 스크롤 진행률 표시
function initScrollProgress() {
  const progressBar = document.createElement('div');
  progressBar.className = 'scroll-progress';
  progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0%;
        height: 3px;
        background: linear-gradient(90deg, #005792 0%, #3b82f6 100%);
        z-index: 9999;
        transition: width 0.1s ease;
    `;
  document.body.appendChild(progressBar);

  const updateProgress = throttle(() => {
    const scrollTop = window.pageYOffset;
    const docHeight = document.body.scrollHeight - window.innerHeight;
    const scrollPercent = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;
    progressBar.style.width = Math.min(100, Math.max(0, scrollPercent)) + '%';
  }, 10);

  window.addEventListener('scroll', updateProgress);
}

// 11. 모바일 최적화
function initMobileOptimizations() {
  // iOS Safari viewport 버그 수정
  function setViewportHeight() {
    const vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
  }

  setViewportHeight();
  window.addEventListener('resize', debounce(setViewportHeight, 100));

  // 터치 디바이스 감지
  const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
  if (isTouchDevice) {
    document.body.classList.add('touch-device');

    // 모바일에서 감정 아이템 터치 최적화
    const emotionItems = document.querySelectorAll('.emotion-item');
    emotionItems.forEach(item => {
      item.addEventListener('touchstart', (e) => {
        e.preventDefault();
        item.click();
      });
    });
  }
}

// 12. 초기화 함수 통합
function initAllFeatures() {
  initPerformanceOptimizations();
  initAccessibility();
  initErrorHandling();
  initScrollProgress();
  initMobileOptimizations();
}

// 13. 페이지 로드 완료 후 추가 기능 초기화
window.addEventListener('load', () => {
  initAllFeatures();

  // 페이지 로드 성능 측정
  if (window.performance && window.performance.timing) {
    const loadTime = window.performance.timing.loadEventEnd - window.performance.timing.navigationStart;
    console.log('Page load time:', loadTime + 'ms');
  }

  // 사용자에게 로딩 완료 상태 표시
  setTimeout(() => {
    document.body.classList.add('loaded');
  }, 100);
});

// 14. 브라우저 호환성 체크
function checkBrowserCompatibility() {
  const isModernBrowser = (
      'IntersectionObserver' in window &&
      'Promise' in window &&
      CSS.supports && CSS.supports('display', 'grid')
  );

  if (!isModernBrowser) {
    console.warn('이 브라우저는 일부 기능이 제한될 수 있습니다. 최신 브라우저를 사용해주세요.');

    // 폴백 UI 표시
    const notice = document.createElement('div');
    notice.innerHTML = `
            <div style="background: #fef3c7; color: #92400e; padding: 12px 20px; text-align: center; font-size: 14px; border-bottom: 1px solid #f59e0b;">
                ⚠️ 최적의 경험을 위해 최신 브라우저를 사용해주세요.
            </div>
        `;
    document.body.insertBefore(notice, document.body.firstChild);
  }
}

// 15. 방 카드 버튼 비활성화 (백엔드 연동 전까지)
document.addEventListener('DOMContentLoaded', function() {
  // 모든 방 카드 버튼들을 비활성화
  const roomButtons = document.querySelectorAll('.btn-details, .btn-join, .btn-report');
  roomButtons.forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      // 클릭해도 아무 동작하지 않음
      return false;
    });
  });
});

// 16. 감정 애니메이션 강화 (더 부드럽게)
function enhanceEmotionAnimations() {
  const emotionItems = document.querySelectorAll('.emotion-item');

  // 각 감정 아이템에 고정된 지연시간 적용 (랜덤 제거)
  const delays = [0, 0.8, 1.6, 2.4, 3.2, 4.0, 4.8, 5.6];

  emotionItems.forEach((item, index) => {
    const delay = delays[index] || 0;
    item.style.animationDelay = `${delay}s`;
    item.style.animationDuration = '6s'; // 더 긴 주기로 변경

    // 성능 최적화를 위한 설정
    item.style.willChange = 'transform';
    item.style.backfaceVisibility = 'hidden';
    item.style.perspective = '1000px';
  });

  // 주기적 특별 효과 제거 (흔들림 방지)
  // 대신 더 안정적인 애니메이션 유지
}

// 페이지 로드 후 감정 애니메이션 강화 실행
window.addEventListener('load', () => {
  setTimeout(enhanceEmotionAnimations, 1000);
});

// 브라우저 호환성 체크 실행
checkBrowserCompatibility();
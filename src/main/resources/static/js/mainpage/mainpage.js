// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
  initScrollAnimations();
  initWeatherHoverEffects();
  initSmoothScroll();
  initEmotionInteractions(); // 새로운 감정 인터랙션 추가

  initSeoulWeatherRecommendations(); // [ADD] 서울 고정 날씨 추천 3개
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
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  const animatedElements = document.querySelectorAll('.animate-on-scroll');
  animatedElements.forEach(el => observer.observe(el));
}

// 2. 날씨 카드 호버 효과
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
      subtitle: '두근거림 가득한 순간',
      color: 'linear-gradient(135deg, #34d399 0%, #10b981 100%)'
    },
    '위로': {
      brand: 'COMFORT',
      subtitle: '따뜻한 위로가 필요한 날',
      color: 'linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%)'
    },
    '호기심': {
      brand: 'CURIOUS',
      subtitle: '새로움을 탐험해요',
      color: 'linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%)'
    },
    '낭만': {
      brand: 'ROMANTIC',
      subtitle: '감성 가득한 공간',
      color: 'linear-gradient(135deg, #fb7185 0%, #f43f5e 100%)'
    }
  };

  // 기본 상태 설정
  const defaultMessage = {
    brand: 'MOODTRIP',
    subtitle: '오늘의 기분을 골라 여행해요',
    color: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  };

  // 초기 상태 세팅
  if (emotionBrand && emotionSubtitle) {
    emotionBrand.textContent = defaultMessage.brand;
    emotionSubtitle.textContent = defaultMessage.subtitle;
    emotionBrand.style.background = defaultMessage.color;
    emotionBrand.style.webkitBackgroundClip = 'text';
    emotionBrand.style.webkitTextFillColor = 'transparent';
    emotionBrand.style.backgroundClip = 'text';
  }

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

      // 중앙 카드 효과 초기화
      emotionCenter.style.transform = 'scale(1)';
      emotionCenter.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.08)';
    });
  });

  // 스크롤 시 인터랙션 추가
  window.addEventListener('scroll', () => {
    const rect = emotionCenter.getBoundingClientRect();
    const inView = rect.top < window.innerHeight && rect.bottom > 0;

    if (inView) {
      emotionCenter.style.transform = 'scale(1.05)';
      emotionCenter.style.transition = 'transform 0.3s ease';
    } else {
      emotionCenter.style.transform = 'scale(1)';
    }
  });
}

// 4. 부드러운 스크롤 (이미 있는 함수)
function initSmoothScroll() {
  const links = document.querySelectorAll('a[href^="#"]:not([href="#"])');

  links.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();

      const targetId = this.getAttribute('href');
      const target = document.querySelector(targetId);

      if (target) {
        window.scrollTo({
          top: target.offsetTop - 80,
          behavior: 'smooth'
        });
      }
    });
  });
}

// 5. 성능 최적화
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

// 6. 접근성 개선
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

  // 포커스 스타일
  const focusableElements = document.querySelectorAll('a, button, input, select, textarea');
  focusableElements.forEach(el => {
    el.addEventListener('focus', () => el.classList.add('focus-visible'));
    el.addEventListener('blur', () => el.classList.remove('focus-visible'));
  });
}

// 7. 사용자 행동 추적 (예: 클릭/스크롤)
function initUserTracking() {
  const track = (eventName, data = {}) => {
    // 실제 분석 도구 연동 시 이곳에 구현
    // console.log('[Track]', eventName, data);
  };

  // 예시: CTA 버튼 클릭 추적
  const ctaButtons = document.querySelectorAll('.cta-button');
  ctaButtons.forEach(btn => {
    btn.addEventListener('click', () => track('cta_click', { id: btn.id }));
  });

  // 예시: 페이지 스크롤량 추적
  let lastScroll = 0;
  window.addEventListener('scroll', () => {
    const current = window.scrollY;
    if (Math.abs(current - lastScroll) > 250) {
      track('scroll', { y: current });
      lastScroll = current;
    }
  });
}

// 8. 모바일 행동 최적화
function initMobileOptimizations() {
  // 터치 반응 개선
  document.addEventListener('touchstart', () => {}, { passive: true });

  // 300ms 지연 제거 (iOS 구버전 고려)
  const links = document.querySelectorAll('a');
  links.forEach(link => link.addEventListener('touchend', () => {}, { passive: true }));
}

// 9. 폼 UX 개선
function initFormUX() {
  const inputs = document.querySelectorAll('input, textarea, select');

  inputs.forEach(input => {
    input.addEventListener('focus', () => input.classList.add('input-focus'));
    input.addEventListener('blur', () => input.classList.remove('input-focus'));
  });
}

// 10. 다크 모드 지원 (옵션)
function initDarkModeToggle() {
  const toggle = document.getElementById('darkModeToggle');
  if (!toggle) return;

  const applyTheme = (isDark) => {
    document.documentElement.classList.toggle('dark', isDark);
    localStorage.setItem('prefers-dark', isDark ? '1' : '0');
  };

  // 초기 상태
  const saved = localStorage.getItem('prefers-dark') === '1';
  applyTheme(saved);
  toggle.checked = saved;

  // 토글 이벤트
  toggle.addEventListener('change', (e) => applyTheme(e.target.checked));
}

// 11. 헤더 고정/축소 효과
function initStickyHeader() {
  const header = document.querySelector('header.site-header');
  if (!header) return;

  let lastY = 0;
  window.addEventListener('scroll', () => {
    const y = window.scrollY;
    header.classList.toggle('is-scrolled', y > 10);
    header.classList.toggle('scroll-up', y < lastY);
    lastY = y;
  });
}

// 12. 툴팁
function initTooltips() {
  const tips = document.querySelectorAll('[data-tooltip]');
  tips.forEach(el => {
    el.addEventListener('mouseenter', () => {
      const text = el.getAttribute('data-tooltip');
      const tip = document.createElement('div');
      tip.className = 'tooltip';
      tip.textContent = text;
      document.body.appendChild(tip);
      const rect = el.getBoundingClientRect();
      tip.style.left = `${rect.left + rect.width/2}px`;
      tip.style.top = `${rect.top - 8}px`;
    });
    el.addEventListener('mouseleave', () => {
      document.querySelectorAll('.tooltip').forEach(t => t.remove());
    });
  });
}

// 13. 감정 애니메이션 강화 (시각 효과)
function enhanceEmotionAnimations() {
  const items = document.querySelectorAll('.emotion-item');

  // 초깃값
  items.forEach((item, i) => {
    item.style.transition = 'transform .25s ease, box-shadow .25s ease';
    item.style.transform = 'translateZ(0)';
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

/* ===========================
 * [ADD] 서울 고정 날씨 추천 3개 + weather-detail 이동 + API 아이콘 사용
 * =========================== */

// 날씨 main → UI 타입 클래스 매핑 (sunny|cloudy|rainy|snowy)
function mapWeatherToType(main) {
  if (!main) return 'sunny';
  const m = String(main).toLowerCase();
  if (m.includes('rain') || m.includes('drizzle') || m.includes('thunder')) return 'rainy';
  if (m.includes('snow')) return 'snowy';
  if (m.includes('cloud')) return 'cloudy';
  return 'sunny';
}

// 백엔드 WeatherEmotionMapper와 동일 키(대분류)
const weatherToCategories = {
  Clear:       ["기쁨 & 즐거움", "자유 & 해방", "희망 & 긍정"],
  Clouds:      ["평온 & 힐링", "성찰 & 사색", "감성 & 예술"],
  Rain:        ["위로 & 공감", "우울 & 슬픔", "감성 & 예술"],
  Drizzle:     ["위로 & 공감", "우울 & 슬픔", "평온 & 힐링"],
  Thunderstorm:["모험 & 스릴", "열정 & 에너지", "놀라움 & 신기함"],
  Snow:        ["평온 & 힐링", "기쁨 & 즐거움", "감성 & 예술"],
  Mist:        ["성찰 & 사색", "감성 & 예술", "평온 & 힐링"],
  Fog:         ["성찰 & 사색", "감성 & 예술", "평온 & 힐링"],
  Haze:        ["성찰 & 사색", "감성 & 예술"],
  Dust:        ["불안 & 걱정", "분노 & 짜증"],
  Sand:        ["불안 & 걱정", "분노 & 짜증"],
  Smoke:       ["불안 & 걱정", "우울 & 슬픔"],
  Squall:      ["모험 & 스릴", "놀라움 & 신기함"],
  Tornado:     ["모험 & 스릴", "불안 & 걱정", "놀라움 & 신기함"]
};

// OpenWeather 아이콘 코드 → URL
function buildIconUrlFromCode(iconCode) {
  return iconCode ? `https://openweathermap.org/img/wn/${iconCode}@2x.png` : '';
}

// YYYY-MM-DD
function todayStr() {
  const d = new Date();
  return d.toISOString().slice(0,10);
}

// 서울 고정 추천 초기화
async function initSeoulWeatherRecommendations() {
  const lat = 37.5665, lon = 126.9780;

  try {
    const [w, list] = await Promise.all([
      fetch(`/api/weather/current?lat=${lat}&lon=${lon}`).then(r=>r.json()),
      fetch(`/api/weather/recommend/attractions?lat=${lat}&lon=${lon}`).then(r=>r.json())
    ]);

    const weatherMain = (w && w.weather) ? w.weather : 'Clear';
    const weatherType = mapWeatherToType(weatherMain);
    const categories  = weatherToCategories[weatherMain] || ["기쁨 & 즐거움"];

    renderWeatherCards((list || []).slice(0,3), {
      weatherMain,
      weatherType,
      categories,
      temperature: (w && typeof w.temp === 'number') ? w.temp : undefined,
      iconCode: w && w.icon ? w.icon : undefined,
      iconUrl:  w && w.iconUrl ? w.iconUrl : undefined
    });

    // 동적으로 추가된 카드에 기존 효과 재적용
    if (typeof initWeatherHoverEffects === 'function') initWeatherHoverEffects();
    if (typeof initScrollAnimations === 'function')    initScrollAnimations();
  } catch (e) {
    console.error('서울 날씨 추천 로딩 실패:', e);
  }
}

// 렌더: 기존 구조 유지, overlay/day 미사용
function renderWeatherCards(items, context) {
  const slider = document.getElementById('weatherSlider');
  const tpl = document.getElementById('weather-card-template');
  if (!slider || !tpl) return;

  // 기존 카드 제거(템플릿 제외)
  Array.from(slider.querySelectorAll('.weather-card:not(#weather-card-template)')).forEach(el => el.remove());

  items.forEach((it) => {
    const card = tpl.cloneNode(true);
    card.id = '';
    card.style.display = '';
    card.classList.remove('sunny','cloudy','rainy','snowy');
    card.classList.add(context.weatherType);
    card.dataset.weather = context.weatherMain;

    const link = card.querySelector('.weather-link');
    const img  = card.querySelector('.weather-image img');
    const title= card.querySelector('.weather-title');
    const addr = card.querySelector('.weather-location');
    const desc = card.querySelector('.weather-description');
    const temp = card.querySelector('.weather-temp');
    const iconImg = card.querySelector('.weather-icon');
    const tags = card.querySelector('.weather-tags');

    // 상세: /weather/detail 로 이동 (쿼리 사용)
    const params = new URLSearchParams({
      attractionId: it.attractionId,
      weather: context.weatherMain,
      date: todayStr()
    }).toString();
    if (link) link.href = `/weather/detail?${params}`;

    if (img)   { img.src = it.firstImage || '/image/mainpage/sample1.png'; img.alt = it.title || ''; }
    if (title) title.textContent = it.title || '';
    if (addr)  addr.textContent  = it.addr1 ? `📍 ${it.addr1}` : '';
    if (desc)  desc.textContent  = it.overview ? (it.overview.length>80 ? it.overview.slice(0,80)+'…' : it.overview) : '';

    if (temp)  temp.textContent  = Number.isFinite(context.temperature) ? `${Math.round(context.temperature)}°` : '--°';
    if (iconImg) {
      if (context.iconUrl) {
        iconImg.src = context.iconUrl;
      } else if (context.iconCode) {
        iconImg.src = buildIconUrlFromCode(context.iconCode);
      } else {
        iconImg.removeAttribute('src');
      }
    }

    if (tags) {
      tags.innerHTML = (context.categories || []).slice(0,2)
          .map(c => `<span class="tag">${c}</span>`).join('');
    }

    card.classList.add('animate-in');
    slider.appendChild(card);
  });
}

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
            <div style="background: #fef3c7; color: #92400e; pad...gn: center; font-size: 14px; border-bottom: 1px solid #f59e0b;">
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
      alert('해당 기능은 준비 중입니다. 곧 제공될 예정이에요!');
    });
  });
});

// 16. 감정 카드들의 작은 인터랙션 보완
document.addEventListener('DOMContentLoaded', function() {
  const items = document.querySelectorAll('.emotion-item');

  items.forEach(item => {
    // 호버 시 약한 떠오름 효과
    item.addEventListener('mouseenter', () => {
      item.style.transform = 'translateY(-4px)';
      item.style.boxShadow = '0 12px 24px rgba(0,0,0,.12)';
    });

    item.addEventListener('mouseleave', () => {
      item.style.transform = 'translateY(0)';
      item.style.boxShadow = '0 8px 16px rgba(0,0,0,.08)';
    });
  });
});

// 17. 감정 섹션 초깃값 스타일 정리
document.addEventListener('DOMContentLoaded', function() {
  const items = document.querySelectorAll('.emotion-item');
  items.forEach(item => {
    item.style.transition = 'transform .25s ease, box-shadow .25s ease';
    item.style.transform = 'translateZ(0)';
    item.style.willChange = 'transform';
    item.style.backfaceVisibility = 'hidden';
    item.style.perspective = '1000px';
  });

  // 주기적 특별 효과 제거 (흔들림 방지)
  // 대신 더 안정적인 애니메이션 유지
});

// 페이지 로드 후 감정 애니메이션 강화 실행
window.addEventListener('load', () => {
  setTimeout(enhanceEmotionAnimations, 1000);
});

// 브라우저 호환성 체크 실행
checkBrowserCompatibility();

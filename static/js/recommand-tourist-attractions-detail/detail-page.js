// DOM이 로드되면 실행
document.addEventListener("DOMContentLoaded", function () {
  
  // 토글 버튼 기능
  const toggleBtn = document.querySelector('.toggle-btn');
  const extraInfo = document.querySelector('.extra-info');
  const toggleText = document.querySelector('.toggle-text');
  const toggleIcon = document.querySelector('.toggle-icon');

  if (toggleBtn && extraInfo) {
    toggleBtn.addEventListener('click', () => {
      const isHidden = extraInfo.classList.contains('hidden');
      
      // 클래스 토글
      extraInfo.classList.toggle('hidden');
      toggleBtn.classList.toggle('active');
      
      // 텍스트와 아이콘 변경
      if (isHidden) {
        toggleText.textContent = '내용 닫기';
        toggleIcon.textContent = '▲';
      } else {
        toggleText.textContent = '더 자세히 보기';
        toggleIcon.textContent = '▼';
      }
    });
  }

  // 스크롤 애니메이션
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.opacity = '1';
        entry.target.style.transform = 'translateY(0)';
      }
    });
  }, observerOptions);

  // 애니메이션 대상 요소들 선택
  const animatedElements = document.querySelectorAll('.placeInfo, .place-detail-info, .place-detail-wrapper, .container-match-service');
  
  // 초기 상태 설정 및 관찰자 등록
  animatedElements.forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(el);
  });

  // 태그 호버 효과
  const tagItems = document.querySelectorAll('.tag-item');
  tagItems.forEach(tag => {
    tag.addEventListener('mouseenter', () => {
      tag.style.transform = 'translateY(-3px) scale(1.05)';
    });
    
    tag.addEventListener('mouseleave', () => {
      tag.style.transform = 'translateY(0) scale(1)';
    });
  });

  // 버튼 클릭 애니메이션
  const buttons = document.querySelectorAll('.guide-btn, .ask-btn');
  buttons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      // 클릭 이펙트 생성
      const ripple = document.createElement('span');
      const rect = btn.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      const x = e.clientX - rect.left - size / 2;
      const y = e.clientY - rect.top - size / 2;
      
      ripple.style.cssText = `
        position: absolute;
        width: ${size}px;
        height: ${size}px;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 50%;
        left: ${x}px;
        top: ${y}px;
        pointer-events: none;
        animation: ripple 0.6s ease-out;
      `;
      
      btn.style.position = 'relative';
      btn.style.overflow = 'hidden';
      btn.appendChild(ripple);
      
      // 애니메이션 후 제거
      setTimeout(() => {
        ripple.remove();
      }, 600);
    });
  });

  // 이미지 레이지 로딩 및 오류 처리
  const images = document.querySelectorAll('img');
  images.forEach(img => {
    img.addEventListener('error', () => {
      img.style.background = 'linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%)';
      img.style.display = 'flex';
      img.style.alignItems = 'center';
      img.style.justifyContent = 'center';
      img.innerHTML = '<span style="color: #a0aec0; font-size: 14px;">이미지를 불러올 수 없습니다</span>';
    });
  });

  // 부드러운 스크롤
  const smoothScrollLinks = document.querySelectorAll('a[href^="#"]');
  smoothScrollLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetId = link.getAttribute('href');
      const targetElement = document.querySelector(targetId);
      
      if (targetElement) {
        targetElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    });
  });

  // 스크롤 시 헤더 효과
  let lastScrollY = window.scrollY;
  const header = document.querySelector('.place-header');
  
  window.addEventListener('scroll', () => {
    const currentScrollY = window.scrollY;
    
    if (header) {
      if (currentScrollY > lastScrollY && currentScrollY > 100) {
        // 아래로 스크롤
        header.style.transform = 'translateY(-10px)';
        header.style.opacity = '0.8';
      } else {
        // 위로 스크롤
        header.style.transform = 'translateY(0)';
        header.style.opacity = '1';
      }
    }
    
    lastScrollY = currentScrollY;
  });

  // 정보 항목 카운터 애니메이션
  const infoItems = document.querySelectorAll('.info-item');
  infoItems.forEach((item, index) => {
    item.style.animationDelay = `${index * 0.1}s`;
    item.classList.add('fade-in-up');
  });

  // 모달 또는 툴팁 기능 (필요시 확장 가능)
  const tagItems2 = document.querySelectorAll('.tag-item');
  tagItems2.forEach(tag => {
    tag.addEventListener('click', () => {
      // 태그 클릭 시 관련 정보 표시 (예: 해당 태그의 설명)
      const tagText = tag.textContent;
      showTagInfo(tagText);
    });
  });

  function showTagInfo(tagText) {
    // 간단한 알림 또는 툴팁 표시
    const notification = document.createElement('div');
    notification.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 0, 0, 0.8);
      color: white;
      padding: 16px 24px;
      border-radius: 8px;
      z-index: 1000;
      font-size: 14px;
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.3s ease;
    `;
    
    notification.textContent = `${tagText} 관련 장소들을 더 찾아보세요!`;
    document.body.appendChild(notification);
    
    // 페이드 인
    setTimeout(() => {
      notification.style.opacity = '1';
    }, 100);
    
    // 3초 후 제거
    setTimeout(() => {
      notification.style.opacity = '0';
      setTimeout(() => {
        document.body.removeChild(notification);
      }, 300);
    }, 3000);
  }

  // 키보드 접근성 개선
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      const focusedElement = document.activeElement;
      
      // 토글 버튼에 포커스가 있을 때
      if (focusedElement === toggleBtn) {
        e.preventDefault();
        toggleBtn.click();
      }
      
      // 태그 아이템에 포커스가 있을 때
      if (focusedElement.classList.contains('tag-item')) {
        e.preventDefault();
        focusedElement.click();
      }
    }
  });

  // 반응형 네비게이션 (모바일)
  function handleResize() {
    const isMobile = window.innerWidth <= 768;
    const container = document.querySelector('.container');
    
    if (container) {
      if (isMobile) {
        container.style.flexDirection = 'column';
        container.style.gap = '20px';
      } else {
        container.style.flexDirection = 'row';
        container.style.gap = '40px';
      }
    }
  }

  // 초기 실행 및 리사이즈 이벤트 등록
  handleResize();
  window.addEventListener('resize', handleResize);

  // 성능 최적화: 디바운스 함수
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

  // 스크롤 이벤트 최적화
  const optimizedScrollHandler = debounce(() => {
    // 스크롤 관련 최적화된 로직
    const scrollPercent = (window.scrollY / (document.documentElement.scrollHeight - window.innerHeight)) * 100;
    
    // 스크롤 진행률에 따른 추가 효과 (선택사항)
    if (scrollPercent > 50) {
      document.body.classList.add('scroll-halfway');
    } else {
      document.body.classList.remove('scroll-halfway');
    }
  }, 100);

  window.addEventListener('scroll', optimizedScrollHandler);

  console.log('🎉 사려니숲길 페이지가 성공적으로 로드되었습니다!');
});
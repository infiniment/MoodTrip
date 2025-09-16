document.addEventListener('DOMContentLoaded', function () {
  console.log('header.js 로드 시작');

  // 요소 존재 여부 확인 후 실행
  const header = document.querySelector('header.header');
  if (!header) {
    console.warn('헤더 요소를 찾을 수 없습니다.');
    return;
  }

  // 올바른 클래스명으로 요소 선택 (CSS와 일치시킴)
  const leftNav = header.querySelector('.header-left-nav');
  const dropdown = header.querySelector('.header-dropdown-nav-container');
  const menuItems = header.querySelectorAll('.header-left-nav-menu[data-menu]');

  let openTimeout, closeTimeout;
  let activeMenu = null;

  console.log('요소 체크:', {
    leftNav: !!leftNav,
    dropdown: !!dropdown,
    menuItems: menuItems.length
  });

  // 모바일 감지 함수
  function isMobileView() {
    return window.innerWidth <= 768;
  }

  // 100vh 버그 해결 (CSS 변수 설정)
  function setVhProperty() {
    const vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
  }

  setVhProperty();

  // 리사이즈 시 vh 값 업데이트
  let resizeTimeout;
  window.addEventListener('resize', () => {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
      setVhProperty();
      // 감정 선택기가 열려있으면 닫기 (UX)
      const emotionSelector = document.getElementById('header-emotion-selector');
      if (emotionSelector && !emotionSelector.classList.contains('hidden')) {
        emotionSelector.classList.add('hidden');
        document.body.classList.remove('modal-open');
      }
    }, 100);
  });

  // 모바일 방향 전환 시
  window.addEventListener('orientationchange', () => {
    setTimeout(() => {
      setVhProperty();
    }, 300);
  });

  // 메뉴 드롭다운 기능 (모바일이 아닐 때만 실행)
  if (!isMobileView() && leftNav && dropdown && menuItems.length > 0) {
    initMenuDropdowns();
  } else {
    console.log('모바일 뷰이거나 메뉴 요소가 없어서 드롭다운 초기화 스킵');
  }

  function initMenuDropdowns() {
    console.log('메뉴 드롭다운 초기화 시작');

    // 요소가 실제로 보이는지 확인
    const leftNavStyle = window.getComputedStyle(leftNav);
    if (leftNavStyle.display === 'none' || leftNavStyle.visibility === 'hidden') {
      console.log('메뉴가 숨겨져 있어 드롭다운 초기화를 건너뜁니다.');
      return;
    }

    // 드롭다운 높이 계산
    function getDropdownHeight() {
      const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
      allDropdowns.forEach(menu => {
        menu.style.display = 'flex';
      });

      dropdown.style.maxHeight = 'auto';
      dropdown.style.visibility = 'hidden';
      dropdown.style.display = 'block';

      const height = dropdown.scrollHeight;

      dropdown.style.maxHeight = '0px';
      dropdown.style.visibility = 'visible';
      dropdown.style.display = '';

      showDropdownForMenu(activeMenu);
      return height;
    }

    function showDropdownForMenu(menuType) {
      const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
      allDropdowns.forEach(menu => {
        menu.style.display = 'none';
      });

      const targetDropdown = dropdown.querySelector(`#${menuType}-dropdown`);
      if (targetDropdown) {
        targetDropdown.style.display = 'flex';
        activeMenu = menuType;
      }
    }

    function openDropdown() {
      if (activeMenu) {
        const height = getDropdownHeight();
        dropdown.style.maxHeight = height + 'px';
        dropdown.style.overflow = 'visible';
        dropdown.style.background = '#fff';
        dropdown.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.1)';
        dropdown.classList.add('active');
      }
    }

    function closeDropdown() {
      dropdown.style.maxHeight = '0px';
      dropdown.style.overflow = 'hidden';
      dropdown.classList.remove('active');
      activeMenu = null;
    }

    // 각 메뉴 항목에 호버 이벤트 추가
    menuItems.forEach(menuItem => {
      menuItem.addEventListener('mouseenter', function () {
        clearTimeout(closeTimeout);
        const menuType = this.getAttribute('data-menu');

        if (menuType !== activeMenu) {
          showDropdownForMenu(menuType);
          clearTimeout(openTimeout);
          openTimeout = setTimeout(openDropdown, 100);
        }
      });
    });

    // 전체 leftNav에서 마우스가 나갈 때
    leftNav.addEventListener('mouseleave', function () {
      clearTimeout(openTimeout);
      closeTimeout = setTimeout(() => {
        if (!dropdown.matches(':hover')) {
          closeDropdown();
        }
      }, 180);
    });

    // 드롭다운에서 마우스 이벤트
    dropdown.addEventListener('mouseenter', function () {
      clearTimeout(closeTimeout);
    });

    dropdown.addEventListener('mouseleave', function () {
      closeTimeout = setTimeout(closeDropdown, 180);
    });

    console.log('메뉴 드롭다운 초기화 완료');
  }

  // 화면 크기 변경 시 메뉴 재초기화
  window.addEventListener('resize', function() {
    clearTimeout(window.menuResizeTimeout);
    window.menuResizeTimeout = setTimeout(function() {
      const isNowMobile = isMobileView();

      if (!isNowMobile && leftNav && dropdown && menuItems.length > 0) {
        // 데스크톱으로 전환 - 메뉴 드롭다운 재초기화
        const leftNavStyle = window.getComputedStyle(leftNav);
        if (leftNavStyle.display !== 'none') {
          console.log('데스크톱으로 전환 - 메뉴 드롭다운 재초기화');
          initMenuDropdowns();
        }
      } else {
        // 모바일로 전환 - 드롭다운 닫기
        if (dropdown) {
          dropdown.style.maxHeight = '0px';
          dropdown.classList.remove('active');
        }
      }
    }, 250);
  });

  console.log('header.js 초기화 완료');
});

// 감정 선택기 - 완전히 클래스 기반
document.addEventListener('DOMContentLoaded', function () {
  console.log('감정 선택기 초기화 시작');

  const emotionCategories = [
    { name: '평온 & 힐링', emotions: ['평온', '안정', '휴식', '치유', '명상', '고요', '위안', '여유'] },
    { name: '사랑 & 로맨스', emotions: ['설렘', '낭만', '사랑', '애정', '달콤함', '애틋함', '그리움', '감성'] },
    { name: '모험 & 스릴', emotions: ['모험', '스릴', '도전', '짜릿함', '흥미', '용기', '대담함', '역동성'] },
    { name: '자유 & 해방', emotions: ['자유', '해방', '독립', '개방감', '무구', '탈출', '경쾌함', '시원함'] },
    { name: '기쁨 & 즐거움', emotions: ['기쁨', '즐거움', '행복', '만족', '희열', '황홀감', '즐거운', '흥겨움'] },
    { name: '감성 & 예술', emotions: ['감성', '영감', '창조력', '미적감각', '몽환적', '신비로움', '예술적', '감동'] },
    { name: '열정 & 에너지', emotions: ['열정', '에너지', '활력', '패기', '의욕', '동기부여', '생동감', '박차'] },
    { name: '성찰 & 사색', emotions: ['성찰', '사색', '고민', '깊이', '철학적', '내면탐구', '명상적', '깨달음'] },
    { name: '위로 & 공감', emotions: ['위로', '공감', '따뜻함', '포근함', '친밀감', '소속감', '이해', '연대감'] },
    { name: '희망 & 긍정', emotions: ['희망', '긍정', '낙관', '기대', '설렘', '비전', '미래지향', '희망찬'] },
    { name: '우울 & 슬픔', emotions: ['우울', '슬픔', '눈물', '상실감', '외로움', '하루하루', '황량함', '야윈함'] },
    { name: '불안 & 걱정', emotions: ['불안', '걱정', '초조함', '두려움', '긴장', '스트레스', '압박감', '부담'] },
    { name: '분노 & 짜증', emotions: ['분노', '짜증', '화남', '역울함', '분함', '참김', '갑갑함', '답답함'] },
    { name: '피로 & 무기력', emotions: ['피로', '무기력', '지침', '나른함', '무효율', '권태', '침체', '소진'] },
    { name: '놀라움 & 신기함', emotions: ['놀라움', '신기함', '경이로움', '신선함', '호기심', '흥미진진', '충격', '감탄'] }
  ];

  const selectedEmotions = new Set();
  let activeCategory = null;
  let recentEmotions = [];
  const MAX_RECENT = 5;

  function saveRecentEmotion(emotion) {
    recentEmotions = recentEmotions.filter(e => e !== emotion);
    recentEmotions.unshift(emotion);
    if (recentEmotions.length > MAX_RECENT) {
      recentEmotions = recentEmotions.slice(0, MAX_RECENT);
    }
  }

  function deleteRecentEmotion(emotion) {
    recentEmotions = recentEmotions.filter(e => e !== emotion);
  }

  function createRecentSection(onClick) {
    const section = document.createElement('div');
    section.id = 'header-recent-emotion-section';
    section.className = 'recent-section';

    if (recentEmotions.length === 0) {
      section.classList.add('hidden');
      return section;
    }

    const label = document.createElement('span');
    label.textContent = '최근 검색어:';
    label.className = 'recent-label';
    section.appendChild(label);

    recentEmotions.forEach(emotion => {
      const btnWrap = document.createElement('span');
      btnWrap.className = 'recent-btn-wrap';

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.textContent = emotion;
      btn.className = 'recent-btn';
      btn.addEventListener('click', () => onClick(emotion));

      const delBtn = document.createElement('button');
      delBtn.type = 'button';
      delBtn.innerHTML = '&times;';
      delBtn.className = 'recent-del-btn';
      delBtn.setAttribute('aria-label', '최근 검색어 삭제');
      delBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        deleteRecentEmotion(emotion);
        const newRecent = createRecentSection(onClick);
        section.parentNode.replaceChild(newRecent, section);
      });

      btnWrap.appendChild(btn);
      btnWrap.appendChild(delBtn);
      section.appendChild(btnWrap);
    });

    return section;
  }

  function createHeaderEmotionSelector() {
    const container = document.createElement('div');
    container.id = 'header-emotion-selector';
    container.className = 'emotion-selector hidden';

    const input = document.querySelector('.header-emotion-search-input');
    if (!input) {
      console.warn('감정 검색 input을 찾을 수 없습니다.');
      return container;
    }

    const recentSection = createRecentSection(function(emotion) {
      selectedEmotions.clear();
      selectedEmotions.add(emotion);
      updateAllEmotionBtnStyles();
      input.value = Array.from(selectedEmotions).join(', ');
    });
    container.appendChild(recentSection);

    const mainRow = document.createElement('div');
    mainRow.className = 'emotion-main-row';

    const catList = document.createElement('div');
    catList.className = 'emotion-category-list';

    const emotionPanel = document.createElement('div');
    emotionPanel.className = 'emotion-panel';

    // 카테고리 버튼 생성
    emotionCategories.forEach(category => {
      const catBtn = document.createElement('button');
      catBtn.textContent = category.name;
      catBtn.className = 'category-btn';
      catBtn.dataset.category = category.name;

      // 모바일과 데스크탑 이벤트 구분
      const isMobile = 'ontouchstart' in window;
      const eventType = isMobile ? 'click' : 'mouseenter';

      catBtn.addEventListener(eventType, () => {
        if (activeCategory !== category.name) {
          // 기존 활성 버튼 비활성화
          catList.querySelector('.category-btn.active')?.classList.remove('active');

          activeCategory = category.name;
          catBtn.classList.add('active');
          showEmotions(category);
        }
      });

      catBtn.addEventListener('focus', () => {
        if (activeCategory !== category.name) {
          catList.querySelector('.category-btn.active')?.classList.remove('active');
          activeCategory = category.name;
          catBtn.classList.add('active');
          showEmotions(category);
        }
      });

      catList.appendChild(catBtn);
    });

    function showEmotions(category) {
      emotionPanel.innerHTML = '';
      const grid = document.createElement('div');
      grid.className = 'emotion-grid';

      category.emotions.forEach(emotion => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = emotion;
        btn.className = 'emotion-btn';
        btn.dataset.emotion = emotion;

        btn.addEventListener('click', (e) => {
          e.preventDefault();
          if (selectedEmotions.has(emotion)) {
            selectedEmotions.delete(emotion);
            btn.classList.remove('selected');
          } else {
            if (selectedEmotions.size >= 3) {
              alert('최대 3개까지 선택할 수 있습니다.');
              return;
            }
            selectedEmotions.add(emotion);
            btn.classList.add('selected');
            saveRecentEmotion(emotion);

            const newRecent = createRecentSection(function(em) {
              selectedEmotions.clear();
              selectedEmotions.add(em);
              updateAllEmotionBtnStyles();
              input.value = Array.from(selectedEmotions).join(', ');
            });
            container.replaceChild(newRecent, container.firstChild);
          }
          input.value = Array.from(selectedEmotions).join(', ');
        });

        grid.appendChild(btn);
      });

      emotionPanel.appendChild(grid);
      emotionPanel.classList.add('visible');
    }

    // 데스크탑에서만 mouseleave 이벤트
    const isMobile = 'ontouchstart' in window;
    if (!isMobile) {
      container.addEventListener('mouseleave', () => {
        activeCategory = null;
        catList.querySelector('.category-btn.active')?.classList.remove('active');
        emotionPanel.classList.remove('visible');
      });
    }

    mainRow.appendChild(catList);
    mainRow.appendChild(emotionPanel);
    container.appendChild(mainRow);

    function updateAllEmotionBtnStyles() {
      const allBtns = container.querySelectorAll('.emotion-btn');
      allBtns.forEach(btn => {
        const emotion = btn.dataset.emotion;
        if (selectedEmotions.has(emotion)) {
          btn.classList.add('selected');
        } else {
          btn.classList.remove('selected');
        }
      });
    }
    container.updateAllEmotionBtnStyles = updateAllEmotionBtnStyles;

    return container;
  }

  function closeEmotionSelector() {
    const emotionSelector = document.getElementById('header-emotion-selector');
    if (emotionSelector) {
      emotionSelector.classList.add('hidden');
      document.body.classList.remove('modal-open');
    }
  }

  // 위치 계산 함수 (데스크탑 전용)
  function updateEmotionSelectorPosition(headerInput, headerEmotionSelector) {
    if (window.innerWidth > 768) {
      const rect = headerInput.getBoundingClientRect();
      headerEmotionSelector.dataset.top = rect.bottom + window.scrollY + 4;
      headerEmotionSelector.dataset.left = rect.left + window.scrollX;
    }
  }

  // 헤더 검색 input 처리
  const headerInput = document.querySelector('.header-emotion-search-input');
  if (headerInput) {
    console.log('감정 검색 input 발견 - 감정 선택기 연결');

    const headerEmotionSelector = createHeaderEmotionSelector();
    document.body.appendChild(headerEmotionSelector);

    headerInput.addEventListener('focus', () => {
      // 위치 데이터만 저장 (CSS가 읽어서 처리)
      updateEmotionSelectorPosition(headerInput, headerEmotionSelector);

      // 클래스로 표시/숨기기 제어
      headerEmotionSelector.classList.remove('hidden');
      if (window.innerWidth <= 768) {
        document.body.classList.add('modal-open');
      }

      const newRecent = createRecentSection(function(emotion) {
        selectedEmotions.clear();
        selectedEmotions.add(emotion);
        headerEmotionSelector.updateAllEmotionBtnStyles();
        headerInput.value = Array.from(selectedEmotions).join(', ');
      });
      headerEmotionSelector.replaceChild(newRecent, headerEmotionSelector.firstChild);
      headerEmotionSelector.updateAllEmotionBtnStyles();
    });

    // 외부 클릭 시 닫기
    document.addEventListener('mousedown', function(e) {
      if (
          !headerEmotionSelector.classList.contains('hidden') &&
          !e.target.closest('#header-emotion-selector') &&
          e.target !== headerInput
      ) {
        closeEmotionSelector();
      }
    }, true);

    headerInput.addEventListener('blur', (e) => {
      setTimeout(() => {
        if (!headerEmotionSelector.contains(document.activeElement)) {
          closeEmotionSelector();
        }
      }, 200);
    });

    console.log('감정 선택기 초기화 완료');
  } else {
    console.log('감정 검색 input을 찾을 수 없음 - 감정 선택기 스킵');
  }

  // ESC 키 지원 (접근성)
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeEmotionSelector();
    }
  });
});
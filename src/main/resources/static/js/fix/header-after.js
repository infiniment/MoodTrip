document.addEventListener('DOMContentLoaded', function () {
  console.log('🚀 header-after.js 로드 시작');

  try {
    // 사용자 정보 로드 (실제로는 서버에서 가져와야 함)
    loadUserProfile();

    // 드롭다운 토글 기능
    initProfileDropdown();

    // 버튼 이벤트 리스너 설정
    initButtonEvents();

    // 메뉴 드롭다운 기능 초기화
    initMenuDropdowns();

    // 로그인 후 헤더 전용 감정 선택기 초기화
    initLoggedInHeaderEmotionSelector();

    console.log('✅ header-after.js 초기화 완료');
  } catch (error) {
    console.error('❌ header-after.js 초기화 실패:', error);
  }
});

/**
 * 메뉴 드롭다운 기능 초기화 (감정여행, 동행매칭, 공지사항)
 */
function initMenuDropdowns() {
  const header = document.querySelector('header.header');
  if (!header) return;

  const leftNav = header.querySelector('.left-nav');
  const dropdown = header.querySelector('.dropdown-nav-container');
  const menuItems = header.querySelectorAll('.left-nav-menu[data-menu]');
  let openTimeout, closeTimeout;
  let activeMenu = null;

  if (!leftNav || !dropdown || menuItems.length === 0) {
    console.warn('⚠️ 메뉴 드롭다운 요소를 찾을 수 없습니다.');
    return;
  }

  // 드롭다운 실제 높이 계산
  function getDropdownHeight() {
    const allDropdowns = dropdown.querySelectorAll('.dropdown-menu-list');
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
    const allDropdowns = dropdown.querySelectorAll('.dropdown-menu-list');
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
}

/**
 * 로그인 후 헤더 전용 감정 선택기 기능 초기화
 */
function initLoggedInHeaderEmotionSelector() {
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

  // 최근 검색어 관리 (메모리 사용)
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
    section.id = 'logged-in-header-recent-emotion-section';
    section.style.marginBottom = '12px';
    section.style.display = 'flex';
    section.style.flexWrap = 'wrap';
    section.style.gap = '8px';
    section.style.alignItems = 'center';

    if (recentEmotions.length === 0) {
      section.style.display = 'none';
      return section;
    }

    const label = document.createElement('span');
    label.textContent = '최근 검색어:';
    label.style.fontSize = '13px';
    label.style.color = '#757575';
    section.appendChild(label);

    recentEmotions.forEach(emotion => {
      const btnWrap = document.createElement('span');
      btnWrap.style.display = 'inline-flex';
      btnWrap.style.alignItems = 'center';
      btnWrap.style.marginRight = '4px';

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.textContent = emotion;
      btn.style.padding = '4px 10px';
      btn.style.border = '1px solid #bbb';
      btn.style.borderRadius = '14px';
      btn.style.background = '#f7f7f7';
      btn.style.color = '#333';
      btn.style.fontSize = '13px';
      btn.style.cursor = 'pointer';
      btn.style.marginRight = '2px';

      btn.addEventListener('click', () => onClick(emotion));

      const delBtn = document.createElement('button');
      delBtn.type = 'button';
      delBtn.innerHTML = '&times;';
      delBtn.style.background = 'transparent';
      delBtn.style.border = 'none';
      delBtn.style.color = '#aaa';
      delBtn.style.fontSize = '15px';
      delBtn.style.cursor = 'pointer';
      delBtn.style.marginLeft = '2px';
      delBtn.style.display = 'flex';
      delBtn.style.alignItems = 'center';
      delBtn.style.height = '22px';
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

  function createLoggedInHeaderEmotionSelector() {
    const container = document.createElement('div');
    container.id = 'logged-in-header-emotion-selector';
    container.style.position = 'absolute';
    container.style.background = '#fff';
    container.style.border = '1.5px solid #bbb';
    container.style.borderRadius = '18px';
    container.style.padding = '18px 16px 16px 16px';
    container.style.display = 'flex';
    container.style.flexDirection = 'column';
    container.style.width = '260px';
    container.style.height = '480px';
    container.style.boxShadow = '0 4px 24px rgba(0,0,0,0.15)';
    container.style.zIndex = '3000';
    container.style.maxHeight = '80vh';
    container.style.overflowY = 'auto';
    container.style.fontFamily = "'Pretendard', 'Noto Sans KR', sans-serif";
    container.style.fontSize = '18px';
    container.style.transition = 'width 0.2s, height 0.2s';

    const input = document.querySelector('.logged-in-header-emotion-search-input');
    const recentSection = createRecentSection(function(emotion) {
      selectedEmotions.clear();
      selectedEmotions.add(emotion);
      updateAllEmotionBtnStyles();
      input.value = Array.from(selectedEmotions).join(', ');
    });
    container.appendChild(recentSection);

    // 메인 flex row 컨테이너
    const mainRow = document.createElement('div');
    mainRow.style.display = 'flex';
    mainRow.style.flexDirection = 'row';
    mainRow.style.gap = '0px';
    mainRow.style.alignItems = 'flex-start';
    mainRow.style.height = '420px';

    // 카테고리 리스트 (좌측)
    const catList = document.createElement('div');
    catList.style.display = 'flex';
    catList.style.flexDirection = 'column';
    catList.style.gap = '2px';
    catList.style.minWidth = '210px';
    catList.style.maxWidth = '210px';
    catList.style.height = '420px';
    catList.style.overflowY = 'auto';
    catList.style.background = '#f8fafd';
    catList.style.borderRadius = '14px';
    catList.style.padding = '10px 0px';

    // 감정 버튼 영역 (우측)
    const emotionPanel = document.createElement('div');
    emotionPanel.style.display = 'flex';
    emotionPanel.style.flexDirection = 'column';
    emotionPanel.style.flexGrow = '1';
    emotionPanel.style.justifyContent = 'flex-start';
    emotionPanel.style.marginLeft = '24px';
    emotionPanel.style.transition = 'opacity 0.2s';
    emotionPanel.style.height = '100%';
    emotionPanel.style.visibility = 'hidden';

    // 카테고리 버튼 스타일 함수
    function styleCatBtn(btn, active) {
      btn.style.fontWeight = active ? 'bold' : '500';
      btn.style.fontSize = '18px';
      btn.style.letterSpacing = '0.5px';
      btn.style.color = active ? '#fff' : '#005792';
      btn.style.background = active
          ? 'linear-gradient(90deg, #005792 60%, #2e5eaa 100%)'
          : 'transparent';
      btn.style.border = 'none';
      btn.style.borderRadius = '10px';
      btn.style.padding = '12px 20px';
      btn.style.margin = '0 8px';
      btn.style.boxShadow = active
          ? '0 2px 10px 0 rgba(0, 87, 146, 0.09)'
          : 'none';
      btn.style.cursor = 'pointer';
      btn.style.transition = 'background 0.2s, color 0.2s, box-shadow 0.2s';
    }

    // 카테고리 버튼 생성
    emotionCategories.forEach(category => {
      const catBtn = document.createElement('button');
      catBtn.textContent = category.name;
      styleCatBtn(catBtn, false);

      catBtn.addEventListener('mouseenter', () => {
        if (activeCategory !== category.name) {
          activeCategory = category.name;
          updateCatBtnStyles();
          showEmotions(category);
        }
      });

      catBtn.addEventListener('focus', () => {
        if (activeCategory !== category.name) {
          activeCategory = category.name;
          updateCatBtnStyles();
          showEmotions(category);
        }
      });

      catList.appendChild(catBtn);
    });

    // 카테고리 버튼 스타일 갱신
    function updateCatBtnStyles() {
      const btns = catList.querySelectorAll('button');
      btns.forEach((btn, idx) => {
        styleCatBtn(btn, emotionCategories[idx].name === activeCategory);
      });
    }

    // 감정 버튼 그리기
    function showEmotions(category) {
      emotionPanel.innerHTML = '';
      const grid = document.createElement('div');
      grid.style.display = 'grid';
      grid.style.gridTemplateColumns = 'repeat(2, max-content)';
      grid.style.gap = '18px 4px';
      grid.style.marginBottom = '8px';
      grid.style.marginTop = '8px';

      category.emotions.forEach(emotion => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = emotion;
        btn.style.width = '120px';
        btn.style.height = '44px';
        btn.style.padding = '0';
        btn.style.margin = '0';
        btn.style.fontSize = '17px';
        btn.style.border = '1.5px solid #005792';
        btn.style.borderRadius = '13px';
        btn.style.background = 'white';
        btn.style.color = '#005792';
        btn.style.cursor = 'pointer';
        btn.style.fontWeight = 'bold';
        btn.style.transition = 'background 0.2s, color 0.2s, border 0.2s';

        function updateBtnStyle() {
          if (selectedEmotions.has(emotion)) {
            btn.style.background = 'linear-gradient(to right, #005792, #001A2C)';
            btn.style.color = '#fff';
            btn.style.border = '1.5px solid #001A2C';
          } else {
            btn.style.background = 'white';
            btn.style.color = '#005792';
            btn.style.border = '1.5px solid #005792';
          }
        }
        updateBtnStyle();

        btn.addEventListener('mouseenter', () => {
          if (!selectedEmotions.has(emotion)) {
            btn.style.background = 'linear-gradient(to right, #005792, #2e5eaa)';
            btn.style.color = '#fff';
            btn.style.border = '1.5px solid #2e5eaa';
          }
        });
        btn.addEventListener('mouseleave', () => updateBtnStyle());

        btn.addEventListener('click', (e) => {
          e.preventDefault();
          if (selectedEmotions.has(emotion)) {
            selectedEmotions.delete(emotion);
          } else {
            if (selectedEmotions.size >= 3) {
              alert('최대 3개까지 선택할 수 있습니다.');
              return;
            }
            selectedEmotions.add(emotion);
            saveRecentEmotion(emotion);
            const newRecent = createRecentSection(function(em) {
              selectedEmotions.clear();
              selectedEmotions.add(em);
              updateAllEmotionBtnStyles();
              input.value = Array.from(selectedEmotions).join(', ');
            });
            container.replaceChild(newRecent, container.firstChild);
          }
          updateBtnStyle();
          input.value = Array.from(selectedEmotions).join(', ');
        });

        btn.dataset.emotion = emotion;
        grid.appendChild(btn);
      });
      emotionPanel.appendChild(grid);

      emotionPanel.style.visibility = 'visible';
      setTimeout(() => {
        container.style.width = '540px';
        emotionPanel.style.opacity = '1';
      }, 10);
    }

    container.addEventListener('mouseleave', () => {
      activeCategory = null;
      updateCatBtnStyles();
      emotionPanel.style.visibility = 'hidden';
      container.style.width = '260px';
    });

    // 스크롤바 숨기기
    container.style.msOverflowStyle = 'none';
    container.style.scrollbarWidth = 'none';
    const style = document.createElement('style');
    style.textContent = `#logged-in-header-emotion-selector::-webkit-scrollbar { display: none; }`;
    document.head.appendChild(style);

    mainRow.appendChild(catList);
    mainRow.appendChild(emotionPanel);
    container.appendChild(mainRow);

    function updateAllEmotionBtnStyles() {
      const allBtns = container.querySelectorAll('button[data-emotion]');
      allBtns.forEach(btn => {
        const emotion = btn.dataset.emotion;
        if (selectedEmotions.has(emotion)) {
          btn.style.background = 'linear-gradient(to right, #005792, #001A2C)';
          btn.style.color = '#fff';
          btn.style.border = '1.5px solid #001A2C';
        } else {
          btn.style.background = 'white';
          btn.style.color = '#005792';
          btn.style.border = '1.5px solid #005792';
        }
      });
    }
    container.updateAllEmotionBtnStyles = updateAllEmotionBtnStyles;

    return container;
  }

  // 로그인 후 헤더 전용 검색 input이 존재할 때만 감정 선택기 초기화
  const loggedInHeaderInput = document.querySelector('.logged-in-header-emotion-search-input');
  if (loggedInHeaderInput) {
    const loggedInHeaderEmotionSelector = createLoggedInHeaderEmotionSelector();
    document.body.appendChild(loggedInHeaderEmotionSelector);
    loggedInHeaderEmotionSelector.style.display = 'none';

    loggedInHeaderInput.addEventListener('focus', () => {
      const rect = loggedInHeaderInput.getBoundingClientRect();
      loggedInHeaderEmotionSelector.style.top = rect.bottom + window.scrollY + 4 + 'px';
      loggedInHeaderEmotionSelector.style.left = rect.left + window.scrollX + 'px';
      loggedInHeaderEmotionSelector.style.display = 'flex';
      const newRecent = createRecentSection(function(emotion) {
        selectedEmotions.clear();
        selectedEmotions.add(emotion);
        loggedInHeaderEmotionSelector.updateAllEmotionBtnStyles();
        loggedInHeaderInput.value = Array.from(selectedEmotions).join(', ');
      });
      loggedInHeaderEmotionSelector.replaceChild(newRecent, loggedInHeaderEmotionSelector.firstChild);
      loggedInHeaderEmotionSelector.updateAllEmotionBtnStyles();
    });

    // 외부 클릭 시 닫기
    document.addEventListener('mousedown', function(e) {
      if (
          loggedInHeaderEmotionSelector.style.display !== 'none' &&
          !e.target.closest('#logged-in-header-emotion-selector') &&
          e.target !== loggedInHeaderInput
      ) {
        loggedInHeaderEmotionSelector.style.display = 'none';
      }
    }, true);

    loggedInHeaderInput.addEventListener('blur', (e) => {
      setTimeout(() => {
        if (!loggedInHeaderEmotionSelector.contains(document.activeElement)) {
          loggedInHeaderEmotionSelector.style.display = 'none';
        }
      }, 200);
    });
  }
}

/**
 * 사용자 프로필 정보 로드
 * 실제로는 서버 API에서 데이터를 가져와야 함
 */
function loadUserProfile() {
  fetch('/api/v1/profiles/me', {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Accept': 'application/json'
    }
  })
      .then(response => {
        if (!response.ok) {
          throw new Error('서버 응답 오류');
        }
        return response.json();
      })
      .then(userData => {
        // null 또는 빈 이미지 처리 → 기본 이미지 fallback
        const nicknameElement = document.getElementById('profileNickname');
        const emailElement = document.getElementById('profileEmail');
        const profileImgElement = document.getElementById('profileImg');

        if (nicknameElement) nicknameElement.textContent = userData.nickname || '(알 수 없음)';
        if (emailElement) emailElement.textContent = userData.email || '(이메일 없음)';
        if (profileImgElement) {
          profileImgElement.src = userData.profileImage && userData.profileImage.trim() !== ''
              ? userData.profileImage
              : '/static/image/fix/moodtrip.png'; // 정확한 경로로 수정
        }

        console.log('✅ 사용자 프로필 로드 완료');
      })
      .catch(error => {
        console.error('❌ 사용자 프로필 로드 실패:', error);
      });
}

/**
 * 프로필 드롭다운 토글 기능 초기화
 */
function initProfileDropdown() {
  console.log('🔍 프로필 드롭다운 초기화 시작');

  const profileThumb = document.getElementById('profileThumb');
  const profileDropdown = document.getElementById('profileDropdown');

  if (!profileThumb || !profileDropdown) {
    console.warn('⚠️ 프로필 드롭다운 요소를 찾을 수 없습니다.');
    console.log('📋 현재 페이지 요소 확인:');
    console.log('- profileThumb:', profileThumb);
    console.log('- profileDropdown:', profileDropdown);

    // 모든 id 요소 출력 (디버깅용)
    const allIds = Array.from(document.querySelectorAll('[id]')).map(el => el.id);
    console.log('📋 페이지의 모든 ID들:', allIds);

    return; // 에러 던지지 않고 조용히 종료
  }

  try {
    // 프로필 썸네일 클릭 시 드롭다운 토글
    profileThumb.addEventListener('click', function (e) {
      e.stopPropagation();
      const isVisible = profileDropdown.style.display === 'block';
      profileDropdown.style.display = isVisible ? 'none' : 'block';
      console.log(`📋 드롭다운 토글: ${isVisible ? '닫힘' : '열림'}`);
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('mousedown', function (e) {
      if (!profileDropdown.contains(e.target) && e.target !== profileThumb) {
        profileDropdown.style.display = 'none';
      }
    });

    // ESC 키로 드롭다운 닫기
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && profileDropdown.style.display === 'block') {
        profileDropdown.style.display = 'none';
      }
    });

    console.log('✅ 프로필 드롭다운 초기화 완료');
  } catch (error) {
    console.error('❌ 프로필 드롭다운 초기화 실패:', error);
  }
}

/**
 * 버튼 이벤트 리스너 초기화
 */
function initButtonEvents() {
  try {
    // 프로필 관리 버튼
    const profileManageBtn = document.getElementById('profileManageBtn');
    if (profileManageBtn) {
      profileManageBtn.addEventListener('click', function () {
        // 프로필 관리 페이지로 이동
        window.location.href = '/mypage/my-profile'; // 실제 프로필 페이지 경로로 수정
      });
      console.log('✅ 프로필 관리 버튼 이벤트 등록 완료');
    }

    // 로그아웃 버튼
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function () {
        handleLogout();
      });
      console.log('✅ 로그아웃 버튼 이벤트 등록 완료');
    }
  } catch (error) {
    console.error('❌ 버튼 이벤트 초기화 실패:', error);
  }
}

/**
 * 개선된 로그아웃 처리 - JWT 토큰 삭제 포함!
 */
function handleLogout() {
  console.log('🚪 로그아웃 함수 호출됨!'); // 디버깅 로그 추가

  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('✅ 사용자가 로그아웃 확인함'); // 디버깅 로그 추가

    try {
      // 로딩 상태 표시
      const logoutBtn = document.getElementById('logoutBtn');
      const originalText = logoutBtn.textContent;
      logoutBtn.textContent = '로그아웃 중...';
      logoutBtn.disabled = true;

      console.log('🌐 로그아웃 API 호출 시작...'); // 디버깅 로그 추가

      // 실제 로그아웃 API 호출
      fetch('/api/v1/members/logout', {
        method: 'POST',
        credentials: 'include', // 쿠키 포함
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
          .then(response => {
            console.log('📡 서버 응답 받음:', response.status, response.statusText); // 디버깅 로그

            if (response.ok) {
              console.log('✅ 서버 로그아웃 API 성공');

              // 실제 로그아웃 처리
              performClientSideLogout();

            } else {
              console.error('❌ 서버 응답 오류:', response.status);
              throw new Error(`로그아웃 실패: ${response.status}`);
            }
          })
          .catch(error => {
            console.error('❌ 로그아웃 API 오류:', error);

            // 서버 오류가 나도 클라이언트에서는 로그아웃 처리
            // (JWT는 클라이언트가 주도하므로)
            console.log('⚠️ 서버 오류 발생, 클라이언트에서 강제 로그아웃 처리');
            performClientSideLogout();
          });
    } catch (error) {
      console.error('❌ 로그아웃 처리 실패:', error);
      // 에러가 나도 클라이언트 로그아웃은 수행
      performClientSideLogout();
    }
  } else {
    console.log('❌ 사용자가 로그아웃 취소함'); // 디버깅 로그 추가
  }
}

/**
 * 실제 클라이언트 로그아웃 처리 (JWT 쿠키 + 토큰 삭제)
 */
function performClientSideLogout() {
  console.log('🚪 클라이언트 로그아웃 처리 시작...');

  try {
    // 1️⃣ 강력한 jwtToken 쿠키 삭제 (여러 패턴으로 시도)
    const cookieDeletePatterns = [
      'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;',
      'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;',
      `jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`,
      'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=.localhost;',
      'jwtToken=; max-age=0; path=/;',
      'jwtToken=; max-age=0; path=/; domain=localhost;',
      `jwtToken=; max-age=0; path=/; domain=${window.location.hostname};`
    ];

    cookieDeletePatterns.forEach((pattern, index) => {
      document.cookie = pattern;
      console.log(`🍪 jwtToken 쿠키 삭제 시도 ${index + 1}: ${pattern}`);
    });

    // 2️⃣ 다른 쿠키들도 삭제
    const otherCookies = ['JSESSIONID', 'flowType', 'token', 'accessToken', 'authToken'];
    otherCookies.forEach(cookieName => {
      // 여러 패턴으로 삭제 시도
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;`;
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
      document.cookie = `${cookieName}=; max-age=0; path=/;`;
      console.log(`🍪 ${cookieName} 쿠키 삭제 시도`);
    });

    // 3️⃣ 쿠키 삭제 확인
    setTimeout(() => {
      const remainingCookies = document.cookie;
      console.log('🔍 남은 쿠키들:', remainingCookies);

      if (remainingCookies.includes('jwtToken')) {
        console.warn('⚠️ jwtToken 쿠키가 아직 남아있음!');
        // 한 번 더 강력하게 시도
        document.cookie = 'jwtToken=deleted; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'jwtToken=deleted; max-age=-1; path=/;';
      } else {
        console.log('✅ jwtToken 쿠키 삭제 성공!');
      }
    }, 100);

    // 4️⃣ localStorage에서 토큰 삭제 (혹시 프론트에서 따로 저장했을 수도)
    const storageKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];
    storageKeys.forEach(key => {
      if (localStorage.getItem(key)) {
        localStorage.removeItem(key);
        console.log(`🗑️ localStorage에서 ${key} 삭제 완료`);
      }
    });

    // 5️⃣ sessionStorage에서 토큰 삭제
    storageKeys.forEach(key => {
      if (sessionStorage.getItem(key)) {
        sessionStorage.removeItem(key);
        console.log(`🗑️ sessionStorage에서 ${key} 삭제 완료`);
      }
    });

    // 6️⃣ Authorization 헤더용 토큰도 삭제 (전역 변수가 있다면)
    if (window.authToken) {
      window.authToken = null;
      console.log('🔑 전역 authToken 초기화 완료');
    }

    // 7️⃣ 사용자 정보 초기화
    if (window.currentUser) {
      window.currentUser = null;
      console.log('👤 전역 사용자 정보 초기화 완료');
    }

    // 8️⃣ 성공 메시지
    console.log('✅ 클라이언트 로그아웃 처리 완료!');

    // 9️⃣ 메인페이지로 리다이렉트
    setTimeout(() => {
      console.log('🏠 메인페이지로 이동중...');
      window.location.href = '/';

      // 혹시 안 바뀌면 강제 새로고침
      setTimeout(() => {
        window.location.reload();
      }, 1000);

    }, 500); // 0.5초 후 이동

  } catch (error) {
    console.error('💥 클라이언트 로그아웃 처리 중 오류:', error);

    // 그래도 메인페이지로는 이동
    alert('로그아웃 처리 중 일부 오류가 발생했지만, 로그아웃을 진행합니다.');
    window.location.href = '/';
  }
}

/**
 * 추가 유틸리티: 현재 로그인 상태 확인
 */
function isLoggedIn() {
  const tokenKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];

  // localStorage 또는 sessionStorage에 토큰이 있는지 확인
  const hasToken = tokenKeys.some(key =>
      localStorage.getItem(key) || sessionStorage.getItem(key)
  );

  console.log('🔍 현재 로그인 상태:', hasToken ? '로그인됨' : '로그아웃됨');
  return hasToken;
}

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.MoodTripHeaderAfter = {
  initProfileDropdown: initProfileDropdown,
  initMenuDropdowns: initMenuDropdowns,
  initLoggedInHeaderEmotionSelector: initLoggedInHeaderEmotionSelector,
  handleLogout: handleLogout,
  performClientSideLogout: performClientSideLogout,
  isLoggedIn: isLoggedIn
};
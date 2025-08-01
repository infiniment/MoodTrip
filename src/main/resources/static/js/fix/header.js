document.addEventListener('DOMContentLoaded', function () {
  const header = document.querySelector('header.header');
  const leftNav = header.querySelector('.left-nav');
  const dropdown = header.querySelector('.dropdown-nav-container');
  let openTimeout, closeTimeout;

  // 드롭다운 실제 높이 계산
  function getDropdownHeight() {
    dropdown.style.maxHeight = 'none';
    const height = dropdown.scrollHeight;
    dropdown.style.maxHeight = '0px';
    return height;
  }

  function openDropdown() {
    dropdown.style.maxHeight = getDropdownHeight() + 'px';
    dropdown.style.overflow = 'visible';
    dropdown.style.background = 'linear-gradient(to right, rgba(0,87,146,0.03), rgba(0,26,44,0.03))';
    dropdown.classList.add('active');
  }

  function closeDropdown() {
    dropdown.style.maxHeight = '0px';
    dropdown.style.overflow = 'hidden';
    dropdown.classList.remove('active');
  }

  leftNav.addEventListener('mouseenter', function () {
    clearTimeout(closeTimeout);
    openTimeout = setTimeout(openDropdown, 180);
  });

  leftNav.addEventListener('mouseleave', function () {
    clearTimeout(openTimeout);
    closeTimeout = setTimeout(() => {
      if (!dropdown.matches(':hover')) closeDropdown();
    }, 180);
  });

  dropdown.addEventListener('mouseenter', function () {
    clearTimeout(closeTimeout);
  });

  dropdown.addEventListener('mouseleave', function () {
    closeTimeout = setTimeout(closeDropdown, 180);
  });
});

// 감정 선택기 기능
document.addEventListener('DOMContentLoaded', function () {
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

  // 최근 검색어 관리 (localStorage 대신 메모리 사용)
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
    section.id = 'recent-emotion-section';
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

  function createEmotionSelector() {
    const container = document.createElement('div');
    container.id = 'emotion-selector';
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

    const input = document.querySelector('.search-input');
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
    style.textContent = `#emotion-selector::-webkit-scrollbar { display: none; }`;
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

  // 검색 input이 존재할 때만 감정 선택기 초기화
  const input = document.querySelector('.search-input');
  if (input) {
    const emotionSelector = createEmotionSelector();
    document.body.appendChild(emotionSelector);
    emotionSelector.style.display = 'none';

    input.addEventListener('focus', () => {
      const rect = input.getBoundingClientRect();
      emotionSelector.style.top = rect.bottom + window.scrollY + 4 + 'px';
      emotionSelector.style.left = rect.left + window.scrollX + 'px';
      emotionSelector.style.display = 'flex';
      const newRecent = createRecentSection(function(emotion) {
        selectedEmotions.clear();
        selectedEmotions.add(emotion);
        emotionSelector.updateAllEmotionBtnStyles();
        input.value = Array.from(selectedEmotions).join(', ');
      });
      emotionSelector.replaceChild(newRecent, emotionSelector.firstChild);
      emotionSelector.updateAllEmotionBtnStyles();
    });

    // 외부 클릭 시 닫기
    document.addEventListener('mousedown', function(e) {
      if (
          emotionSelector.style.display !== 'none' &&
          !e.target.closest('#emotion-selector') &&
          e.target !== input
      ) {
        emotionSelector.style.display = 'none';
      }
    }, true);

    input.addEventListener('blur', (e) => {
      setTimeout(() => {
        if (!emotionSelector.contains(document.activeElement)) {
          emotionSelector.style.display = 'none';
        }
      }, 200);
    });
  }
});
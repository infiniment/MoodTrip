document.addEventListener('DOMContentLoaded', function () {
  console.log('header-after.js 로드 시작');

  try {
    // 로그인 상태 확인 후 사용자 정보 로드
    if (isLoggedIn()) {
      loadUserProfile();
    } else {
      console.log('비로그인 상태 → 프로필 API 호출 안 함');
    }

    // 드롭다운 토글 기능
    initProfileDropdown();

    // 버튼 이벤트 리스너 설정
    initButtonEvents();

    // 메뉴 드롭다운 기능 초기화
    initMenuDropdowns();

    console.log('header-after.js 초기화 완료');
  } catch (error) {
    console.error('header-after.js 초기화 실패:', error);
  }
});

/**
 * 메뉴 드롭다운 기능 초기화 (감정여행, 동행매칭, 공지사항) - 디버깅 버전
 */
function initMenuDropdowns() {
  console.log('=== 드롭다운 초기화 시작 ===');

  const header = document.querySelector('header.header');
  console.log('header 요소:', header);

  if (!header) {
    console.error('header 요소를 찾을 수 없습니다!');
    return;
  }

  const leftNav = header.querySelector('.header-left-nav');
  const dropdown = header.querySelector('.header-dropdown-nav-container');
  const menuItems = header.querySelectorAll('.header-left-nav-menu[data-menu]');

  console.log('leftNav 요소:', leftNav);
  console.log('dropdown 요소:', dropdown);
  console.log('menuItems 개수:', menuItems.length);
  console.log('menuItems:', menuItems);

  let openTimeout, closeTimeout;
  let activeMenu = null;

  if (!leftNav || !dropdown || menuItems.length === 0) {
    console.error('필수 요소를 찾을 수 없습니다:', {
      leftNav: !!leftNav,
      dropdown: !!dropdown,
      menuItemsCount: menuItems.length
    });
    return;
  }

  // 드롭다운 메뉴들 확인
  const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
  console.log('드롭다운 메뉴 리스트들:', allDropdowns);

  allDropdowns.forEach((menu, index) => {
    console.log(`드롭다운 메뉴 ${index}:`, menu.id, menu);
  });

  // 드롭다운 실제 높이 계산
  function getDropdownHeight() {
    console.log('높이 계산 시작, activeMenu:', activeMenu);

    const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
    allDropdowns.forEach(menu => {
      menu.style.display = 'flex';
    });

    dropdown.style.maxHeight = 'auto';
    dropdown.style.visibility = 'hidden';
    dropdown.style.display = 'block';

    const height = dropdown.scrollHeight;
    console.log('계산된 높이:', height);

    dropdown.style.maxHeight = '0px';
    dropdown.style.visibility = 'visible';
    dropdown.style.display = '';

    showDropdownForMenu(activeMenu);
    return height;
  }

  function showDropdownForMenu(menuType) {
    console.log('메뉴 표시:', menuType);

    const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
    allDropdowns.forEach(menu => {
      menu.style.display = 'none';
    });

    const targetDropdown = dropdown.querySelector(`#${menuType}-dropdown`);
    console.log('대상 드롭다운:', targetDropdown);

    if (targetDropdown) {
      targetDropdown.style.display = 'flex';
      activeMenu = menuType;
      console.log('메뉴 활성화 완료:', menuType);
    } else {
      console.error('대상 드롭다운을 찾을 수 없습니다:', `#${menuType}-dropdown`);
    }
  }

  function openDropdown() {
    console.log('드롭다운 열기, activeMenu:', activeMenu);

    if (activeMenu) {
      const height = getDropdownHeight();
      dropdown.style.maxHeight = height + 'px';
      dropdown.style.overflow = 'visible';
      dropdown.style.background = '#fff';
      dropdown.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.1)';
      dropdown.classList.add('active');
      console.log('드롭다운 열림 완료');
    }
  }

  function closeDropdown() {
    console.log('드롭다운 닫기');
    dropdown.style.maxHeight = '0px';
    dropdown.style.overflow = 'hidden';
    dropdown.classList.remove('active');
    activeMenu = null;
    console.log('드롭다운 닫힘 완료');
  }

  // 각 메뉴 항목에 호버 이벤트 추가
  menuItems.forEach((menuItem, index) => {
    const menuType = menuItem.getAttribute('data-menu');
    console.log(`메뉴 ${index} 이벤트 등록:`, menuType);

    menuItem.addEventListener('mouseenter', function () {
      console.log('메뉴 hover:', menuType);
      clearTimeout(closeTimeout);

      if (menuType !== activeMenu) {
        showDropdownForMenu(menuType);
        clearTimeout(openTimeout);
        openTimeout = setTimeout(openDropdown, 100);
      }
    });
  });

  // 전체 leftNav에서 마우스가 나갈 때
  leftNav.addEventListener('mouseleave', function () {
    console.log('leftNav 마우스 나감');
    clearTimeout(openTimeout);
    closeTimeout = setTimeout(() => {
      if (!dropdown.matches(':hover')) {
        closeDropdown();
      }
    }, 180);
  });

  // 드롭다운에서 마우스 이벤트
  dropdown.addEventListener('mouseenter', function () {
    console.log('dropdown 마우스 진입');
    clearTimeout(closeTimeout);
  });

  dropdown.addEventListener('mouseleave', function () {
    console.log('dropdown 마우스 나감');
    closeTimeout = setTimeout(closeDropdown, 180);
  });

  console.log('=== 드롭다운 초기화 완료 ===');
}

/**
 * 사용자 프로필 정보 로드
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
        if (response.status === 401) {
          console.log('로그인 안 됨 → 프로필 불러오기 건너뜀');
          return null;
        }
        if (!response.ok) {
          throw new Error('서버 응답 오류');
        }
        return response.json();
      })
      .then(userData => {
        if (!userData) return; // 로그인 안 된 경우 바로 종료

        // 실제 HTML 클래스명에 맞게 수정
        const nicknameElement = document.querySelector('.header-profile-nickname');
        const emailElement = document.querySelector('.header-profile-email');
        const profileImgElement = document.getElementById('profileImg');

        if (nicknameElement) nicknameElement.textContent = userData.nickname || '(알 수 없음)';
        if (emailElement) emailElement.textContent = userData.email || '(이메일 없음)';
        if (profileImgElement) {
          profileImgElement.src = userData.profileImage && userData.profileImage.trim() !== ''
              ? userData.profileImage
              : '/image/fix/moodtrip.png';
        }

        console.log('사용자 프로필 로드 완료');
      })
      .catch(error => {
        console.error('사용자 프로필 로드 실패:', error);
      });
}

/**
 * 프로필 드롭다운 토글 기능 초기화
 */
function initProfileDropdown() {
  console.log('프로필 드롭다운 초기화 시작');

  const profileThumb = document.getElementById('profileThumb');
  const profileDropdown = document.getElementById('profileDropdown');

  if (!profileThumb || !profileDropdown) {
    console.warn('프로필 드롭다운 요소를 찾을 수 없습니다.');
    return;
  }

  try {
    // 프로필 썸네일 클릭 시 드롭다운 토글
    profileThumb.addEventListener('click', function (e) {
      e.stopPropagation();
      const isVisible = profileDropdown.style.display === 'block';
      profileDropdown.style.display = isVisible ? 'none' : 'block';
      console.log(`드롭다운 토글: ${isVisible ? '닫힘' : '열림'}`);
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

    console.log('프로필 드롭다운 초기화 완료');
  } catch (error) {
    console.error('프로필 드롭다운 초기화 실패:', error);
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
        window.location.href = '/mypage/my-profile';
      });
      console.log('프로필 관리 버튼 이벤트 등록 완료');
    }

    // 로그아웃 버튼
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function () {
        handleLogout();
      });
      console.log('로그아웃 버튼 이벤트 등록 완료');
    }
  } catch (error) {
    console.error('버튼 이벤트 초기화 실패:', error);
  }
}

/**
 * 로그아웃 처리
 */
function handleLogout() {
  console.log('로그아웃 함수 호출됨');

  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('사용자가 로그아웃 확인함');

    try {
      // 로딩 상태 표시
      const logoutBtn = document.getElementById('logoutBtn');
      if (logoutBtn) {
        const originalText = logoutBtn.textContent;
        logoutBtn.textContent = '로그아웃 중...';
        logoutBtn.disabled = true;
      }

      console.log('로그아웃 API 호출 시작');

      // 로그아웃 API 호출
      fetch('/api/v1/members/logout', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
          .then(response => {
            console.log('서버 응답 받음:', response.status, response.statusText);

            if (response.ok) {
              console.log('서버 로그아웃 API 성공');
              performClientSideLogout();
            } else {
              console.error('서버 응답 오류:', response.status);
              throw new Error(`로그아웃 실패: ${response.status}`);
            }
          })
          .catch(error => {
            console.error('로그아웃 API 오류:', error);
            console.log('서버 오류 발생, 클라이언트에서 강제 로그아웃 처리');
            performClientSideLogout();
          });
    } catch (error) {
      console.error('로그아웃 처리 실패:', error);
      performClientSideLogout();
    }
  } else {
    console.log('사용자가 로그아웃 취소함');
  }
}

/**
 * 클라이언트 로그아웃 처리
 */
function performClientSideLogout() {
  console.log('클라이언트 로그아웃 처리 시작');

  try {
    // 쿠키 삭제
    const cookieDeletePatterns = [
      'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;',
      'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;',
      `jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`,
      'jwtToken=; max-age=0; path=/;',
      'jwtToken=; max-age=0; path=/; domain=localhost;',
      `jwtToken=; max-age=0; path=/; domain=${window.location.hostname};`
    ];

    cookieDeletePatterns.forEach((pattern, index) => {
      document.cookie = pattern;
      console.log(`jwtToken 쿠키 삭제 시도 ${index + 1}: ${pattern}`);
    });

    // 다른 쿠키들도 삭제
    const otherCookies = ['JSESSIONID', 'flowType', 'token', 'accessToken', 'authToken'];
    otherCookies.forEach(cookieName => {
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;`;
      document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
      document.cookie = `${cookieName}=; max-age=0; path=/;`;
      console.log(`${cookieName} 쿠키 삭제 시도`);
    });

    // localStorage에서 토큰 삭제
    const storageKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];
    storageKeys.forEach(key => {
      if (localStorage.getItem(key)) {
        localStorage.removeItem(key);
        console.log(`localStorage에서 ${key} 삭제 완료`);
      }
    });

    // sessionStorage에서 토큰 삭제
    storageKeys.forEach(key => {
      if (sessionStorage.getItem(key)) {
        sessionStorage.removeItem(key);
        console.log(`sessionStorage에서 ${key} 삭제 완료`);
      }
    });

    // 전역 변수 초기화
    if (window.authToken) {
      window.authToken = null;
      console.log('전역 authToken 초기화 완료');
    }

    if (window.currentUser) {
      window.currentUser = null;
      console.log('전역 사용자 정보 초기화 완료');
    }

    console.log('클라이언트 로그아웃 처리 완료');

    // 메인페이지로 리다이렉트
    setTimeout(() => {
      console.log('메인페이지로 이동중');
      window.location.href = '/';
    }, 500);

  } catch (error) {
    console.error('클라이언트 로그아웃 처리 중 오류:', error);
    alert('로그아웃 처리 중 일부 오류가 발생했지만, 로그아웃을 진행합니다.');
    window.location.href = '/';
  }
}

/**
 * 로그인 상태 확인
 */
function isLoggedIn() {
  const tokenKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];
  const hasToken = tokenKeys.some(key =>
      localStorage.getItem(key) || sessionStorage.getItem(key)
  );
  console.log('현재 로그인 상태:', hasToken ? '로그인됨' : '로그아웃됨');
  return hasToken;
}

// 전역 함수로 노출
window.MoodTripHeaderAfter = {
  initProfileDropdown: initProfileDropdown,
  initMenuDropdowns: initMenuDropdowns,
  handleLogout: handleLogout,
  performClientSideLogout: performClientSideLogout,
  isLoggedIn: isLoggedIn
};
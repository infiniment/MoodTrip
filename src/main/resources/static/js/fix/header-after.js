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
 * 메뉴 드롭다운 기능 초기화 (감정여행, 동행매칭, 공지사항)
 */
function initMenuDropdowns() {
  console.log('=== 드롭다운 초기화 시작 ===');

  const header = document.querySelector('header.header');
  const leftNav = header?.querySelector('.header-left-nav');
  const dropdown = header?.querySelector('.header-dropdown-nav-container');
  const menuItems = header?.querySelectorAll('.header-left-nav-menu[data-menu]');

  if (!header || !leftNav || !dropdown || !menuItems?.length) {
    console.error('필수 요소를 찾을 수 없습니다.');
    return;
  }

  let openTimeout, closeTimeout;
  let activeMenu = null;

  function getDropdownHeight() {
    const allDropdowns = dropdown.querySelectorAll('.header-dropdown-menu-list');
    allDropdowns.forEach(menu => (menu.style.display = 'flex'));
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
    allDropdowns.forEach(menu => (menu.style.display = 'none'));

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
      dropdown.classList.add('active');
    }
  }

  function closeDropdown() {
    dropdown.style.maxHeight = '0px';
    dropdown.style.overflow = 'hidden';
    dropdown.classList.remove('active');
    activeMenu = null;
  }

  menuItems.forEach(menuItem => {
    const menuType = menuItem.getAttribute('data-menu');
    menuItem.addEventListener('mouseenter', function () {
      clearTimeout(closeTimeout);
      if (menuType !== activeMenu) {
        showDropdownForMenu(menuType);
        clearTimeout(openTimeout);
        openTimeout = setTimeout(openDropdown, 100);
      }
    });
  });

  leftNav.addEventListener('mouseleave', function () {
    clearTimeout(openTimeout);
    closeTimeout = setTimeout(() => {
      if (!dropdown.matches(':hover')) closeDropdown();
    }, 180);
  });

  dropdown.addEventListener('mouseenter', () => clearTimeout(closeTimeout));
  dropdown.addEventListener('mouseleave', () => {
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
    headers: { Accept: 'application/json' }
  })
      .then(response => {
        if (response.status === 401) return null;
        if (!response.ok) throw new Error('서버 응답 오류');
        return response.json();
      })
      .then(userData => {
        if (!userData) return;

        const nicknameElement = document.querySelector('.header-profile-nickname');
        const emailElement = document.querySelector('.header-profile-email');
        const profileImgElement = document.getElementById('profileImg');

        if (nicknameElement) nicknameElement.textContent = userData.nickname || '(알 수 없음)';
        if (emailElement) emailElement.textContent = userData.email || '(이메일 없음)';
        if (profileImgElement) {
          profileImgElement.src =
              userData.profileImage && userData.profileImage.trim() !== ''
                  ? userData.profileImage
                  : '/image/fix/moodtrip.png';
        }

        console.log('사용자 프로필 로드 완료');
      })
      .catch(error => console.error('사용자 프로필 로드 실패:', error));
}

/**
 * 헤더 프로필 이미지 즉시 갱신
 */
function updateHeaderProfileImage(newImageUrl) {
  const profileImgElement = document.getElementById('profileImg');
  if (profileImgElement) {
    profileImgElement.src = newImageUrl + '?t=' + new Date().getTime(); // 캐시 방지
    console.log('헤더 프로필 이미지 업데이트 완료:', profileImgElement.src);
  }
}

/**
 * 프로필 드롭다운 토글 기능 초기화
 */
function initProfileDropdown() {
  const profileThumb = document.getElementById('profileThumb');
  const profileDropdown = document.getElementById('profileDropdown');
  if (!profileThumb || !profileDropdown) return;

  profileThumb.addEventListener('click', function (e) {
    e.stopPropagation();
    const isVisible = profileDropdown.style.display === 'block';
    profileDropdown.style.display = isVisible ? 'none' : 'block';
  });

  document.addEventListener('mousedown', function (e) {
    if (!profileDropdown.contains(e.target) && e.target !== profileThumb) {
      profileDropdown.style.display = 'none';
    }
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && profileDropdown.style.display === 'block') {
      profileDropdown.style.display = 'none';
    }
  });
}

/**
 * 버튼 이벤트 리스너 초기화
 */
function initButtonEvents() {
  const profileManageBtn = document.getElementById('profileManageBtn');
  if (profileManageBtn) {
    profileManageBtn.addEventListener('click', () => (window.location.href = '/mypage/my-profile'));
  }

  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
  }
}

/**
 * 로그아웃 처리
 */
function handleLogout() {
  if (!confirm('로그아웃 하시겠습니까?')) return;

  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.textContent = '로그아웃 중...';
    logoutBtn.disabled = true;
  }

  fetch('/api/v1/members/logout', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
      .then(response => {
        if (response.ok) {
          performClientSideLogout();
        } else {
          throw new Error(`로그아웃 실패: ${response.status}`);
        }
      })
      .catch(() => performClientSideLogout());
}

/**
 * 클라이언트 로그아웃 처리
 */
function performClientSideLogout() {
  // 쿠키/스토리지 삭제
  const storageKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];
  storageKeys.forEach(key => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });

  // 메인페이지 이동
  window.location.href = '/';
}

/**
 * 로그인 상태 확인
 */
function isLoggedIn() {
  const tokenKeys = ['token', 'accessToken', 'authToken', 'jwt', 'jwtToken'];
  return tokenKeys.some(key => localStorage.getItem(key) || sessionStorage.getItem(key));
}

// 전역 함수로 노출
window.MoodTripHeaderAfter = {
  initProfileDropdown,
  initMenuDropdowns,
  handleLogout,
  performClientSideLogout,
  isLoggedIn,
  updateHeaderProfileImage // ✅ 새로 추가
};

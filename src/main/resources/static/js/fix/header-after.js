document.addEventListener('DOMContentLoaded', function () {
  console.log('🚀 header-after.js 로드 시작');

  try {
    // 사용자 정보 로드 (실제로는 서버에서 가져와야 함)
    loadUserProfile();

    // 드롭다운 토글 기능
    initProfileDropdown();

    // 버튼 이벤트 리스너 설정
    initButtonEvents();

    console.log('✅ header-after.js 초기화 완료');
  } catch (error) {
    console.error('❌ header-after.js 초기화 실패:', error);
  }
});

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
        // ✅ null 또는 빈 이미지 처리 → 기본 이미지 fallback
        const nicknameElement = document.getElementById('profileNickname');
        const emailElement = document.getElementById('profileEmail');
        const profileImgElement = document.getElementById('profileImg');

        if (nicknameElement) nicknameElement.textContent = userData.nickname || '(알 수 없음)';
        if (emailElement) emailElement.textContent = userData.email || '(이메일 없음)';
        if (profileImgElement) {
          profileImgElement.src = userData.profileImage && userData.profileImage.trim() !== ''
              ? userData.profileImage
              : '/static/image/fix/moodtrip.png'; // ✅ 정확한 경로로 수정
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

    // 🔥 모든 id 요소 출력 (디버깅용)
    const allIds = Array.from(document.querySelectorAll('[id]')).map(el => el.id);
    console.log('📋 페이지의 모든 ID들:', allIds);

    return; // 🔥 에러 던지지 않고 조용히 종료
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
        window.location.href = '/mypage/my-profile'; // 🔥 실제 프로필 페이지 경로로 수정
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
 * 🔥 개선된 로그아웃 처리 - JWT 토큰 삭제 포함!
 */
function handleLogout() {
  console.log('🚪 로그아웃 함수 호출됨!'); // 🔥 디버깅 로그 추가

  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('✅ 사용자가 로그아웃 확인함'); // 🔥 디버깅 로그 추가

    try {
      // 로딩 상태 표시
      const logoutBtn = document.getElementById('logoutBtn');
      const originalText = logoutBtn.textContent;
      logoutBtn.textContent = '로그아웃 중...';
      logoutBtn.disabled = true;

      console.log('🌐 로그아웃 API 호출 시작...'); // 🔥 디버깅 로그 추가

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
            console.log('📡 서버 응답 받음:', response.status, response.statusText); // 🔥 디버깅 로그

            if (response.ok) {
              console.log('✅ 서버 로그아웃 API 성공');

              // 🔥 여기가 핵심! 실제 로그아웃 처리
              performClientSideLogout();

            } else {
              console.error('❌ 서버 응답 오류:', response.status);
              throw new Error(`로그아웃 실패: ${response.status}`);
            }
          })
          .catch(error => {
            console.error('❌ 로그아웃 API 오류:', error);

            // 🔥 서버 오류가 나도 클라이언트에서는 로그아웃 처리
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
    console.log('❌ 사용자가 로그아웃 취소함'); // 🔥 디버깅 로그 추가
  }
}

/**
 * 🔥 실제 클라이언트 로그아웃 처리 (JWT 쿠키 + 토큰 삭제)
 *
 * JwtAuthenticationFilter를 보니 토큰이 다음 위치에 저장됩니다:
 * 1. Authorization 헤더 (Bearer 토큰)
 * 2. jwtToken 쿠키
 */
function performClientSideLogout() {
  console.log('🚪 클라이언트 로그아웃 처리 시작...');

  try {
    // 1️⃣ 🔥 강력한 jwtToken 쿠키 삭제 (여러 패턴으로 시도)
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
 * 🔥 추가 유틸리티: 현재 로그인 상태 확인
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
window.MoodTripHeader = {
  initProfileDropdown: initProfileDropdown,
  handleLogout: handleLogout,
  performClientSideLogout: performClientSideLogout, // 🔥 새로 추가
  isLoggedIn: isLoggedIn // 🔥 새로 추가
};
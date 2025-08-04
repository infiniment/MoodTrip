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
              : '/image/fix/moodtrip.png'; // ✅ fallback 이미지
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
 * 로그아웃 처리
 */
function handleLogout() {
  if (confirm('로그아웃 하시겠습니까?')) {
    try {
      // 로딩 상태 표시
      const logoutBtn = document.getElementById('logoutBtn');
      const originalText = logoutBtn.textContent;
      logoutBtn.textContent = '로그아웃 중...';
      logoutBtn.disabled = true;

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
            if (response.ok) {
              // 로그아웃 성공 시 메인페이지로 리다이렉트
              console.log('✅ 로그아웃 성공');
              window.location.href = '/';
            } else {
              throw new Error('로그아웃 실패');
            }
          })
          .catch(error => {
            console.error('❌ 로그아웃 오류:', error);
            alert('로그아웃 중 오류가 발생했습니다. 다시 시도해주세요.');

            // 버튼 원래 상태로 복원
            logoutBtn.textContent = originalText;
            logoutBtn.disabled = false;
          });
    } catch (error) {
      console.error('❌ 로그아웃 처리 실패:', error);
    }
  }
}

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.MoodTripHeader = {
  initProfileDropdown: initProfileDropdown,
  handleLogout: handleLogout
};
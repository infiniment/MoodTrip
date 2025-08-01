document.addEventListener('DOMContentLoaded', function () {
  // 사용자 정보 로드 (실제로는 서버에서 가져와야 함)
  loadUserProfile();

  // 드롭다운 토글 기능
  initProfileDropdown();

  // 버튼 이벤트 리스너 설정
  initButtonEvents();
});

/**
 * 사용자 프로필 정보 로드
 * 실제로는 서버 API에서 데이터를 가져와야 함
 */
function loadUserProfile() {
  // 현재는 임시 데이터 사용
  // 나중에 실제 API 호출로 교체: /api/v1/profiles/me
  const userData = {
    nickname: 'aktr0204',
    email: 'aktr378@gmail.com',
    profileImage: '/static/image/default-common/profile-default.png'
  };

  // DOM 요소 업데이트
  const nicknameElement = document.getElementById('profileNickname');
  const emailElement = document.getElementById('profileEmail');
  const profileImgElement = document.getElementById('profileImg');

  if (nicknameElement) nicknameElement.textContent = userData.nickname;
  if (emailElement) emailElement.textContent = userData.email;
  if (profileImgElement) profileImgElement.src = userData.profileImage;

  // 실제 구현 시 사용할 코드 (주석 처리)
  /*
  fetch('/api/v1/profiles/me', {
    method: 'GET',
    credentials: 'include' // 쿠키 포함
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('프로필 정보를 가져올 수 없습니다.');
    }
    return response.json();
  })
  .then(data => {
    if (nicknameElement) nicknameElement.textContent = data.nickname || '사용자';
    if (emailElement) emailElement.textContent = data.email || '';
    if (profileImgElement) {
      profileImgElement.src = data.profileImage || '/static/image/default-common/profile-default.png';
    }
  })
  .catch(error => {
    console.error('프로필 로드 실패:', error);
    // 기본값 설정
    if (nicknameElement) nicknameElement.textContent = '사용자';
    if (emailElement) emailElement.textContent = '';
  });
  */
}

/**
 * 프로필 드롭다운 토글 기능 초기화
 */
function initProfileDropdown() {
  const profileThumb = document.getElementById('profileThumb');
  const profileDropdown = document.getElementById('profileDropdown');

  if (!profileThumb || !profileDropdown) {
    console.warn('프로필 드롭다운 요소를 찾을 수 없습니다.');
    return;
  }

  // 프로필 썸네일 클릭 시 드롭다운 토글
  profileThumb.addEventListener('click', function (e) {
    e.stopPropagation();
    const isVisible = profileDropdown.style.display === 'block';
    profileDropdown.style.display = isVisible ? 'none' : 'block';
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
}

/**
 * 버튼 이벤트 리스너 초기화
 */
function initButtonEvents() {
  // 프로필 관리 버튼
  const profileManageBtn = document.getElementById('profileManageBtn');
  if (profileManageBtn) {
    profileManageBtn.addEventListener('click', function () {
      // 프로필 관리 페이지로 이동
      window.location.href = '/profiles/me';
    });
  }

  // 로그아웃 버튼
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', function () {
      handleLogout();
    });
  }
}

/**
 * 로그아웃 처리
 */
function handleLogout() {
  if (confirm('로그아웃 하시겠습니까?')) {
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
            window.location.href = '/';
          } else {
            throw new Error('로그아웃 실패');
          }
        })
        .catch(error => {
          console.error('로그아웃 오류:', error);
          alert('로그아웃 중 오류가 발생했습니다. 다시 시도해주세요.');

          // 버튼 원래 상태로 복원
          logoutBtn.textContent = originalText;
          logoutBtn.disabled = false;
        });

    // 개발 중에는 임시로 바로 메인페이지로 이동
    // window.location.href = '/';
  }
}

/**
 * 프로필 이미지 업로드 처리 (나중에 추가할 기능)
 */
function handleProfileImageUpload(file) {
  const formData = new FormData();
  formData.append('profileImage', file);

  fetch('/api/v1/profiles/me/image', {
    method: 'POST',
    credentials: 'include',
    body: formData
  })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          // 이미지 업데이트
          const profileImg = document.getElementById('profileImg');
          if (profileImg) {
            profileImg.src = data.imageUrl;
          }
          alert('프로필 이미지가 업데이트되었습니다.');
        } else {
          throw new Error(data.message || '이미지 업로드 실패');
        }
      })
      .catch(error => {
        console.error('이미지 업로드 오류:', error);
        alert('이미지 업로드에 실패했습니다.');
      });
}

/**
 * 유틸리티: 사용자 정보 새로고침
 */
function refreshUserProfile() {
  loadUserProfile();
}

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.MoodTripHeader = {
  refreshUserProfile: refreshUserProfile,
  handleLogout: handleLogout
};
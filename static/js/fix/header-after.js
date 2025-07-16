

document.addEventListener('DOMContentLoaded', function () {
  // 예시: 실제로는 서버에서 유저 정보를 받아와야 함
  const userData = {
    nickname: 'aktr0204',
    email: 'aktr378@gmail.com',
    profileImage: '/static/image/default-common/profile-default.png'
    // 실제 이미지 경로로 교체
  };

  // 데이터 바인딩
  document.getElementById('profileNickname').textContent = userData.nickname;
  document.getElementById('profileEmail').textContent = userData.email;
  document.getElementById('profileImg').src = userData.profileImage;

  // 드롭다운 토글
  const profileThumb = document.getElementById('profileThumb');
  const profileDropdown = document.getElementById('profileDropdown');

  profileThumb.addEventListener('click', function (e) {
    e.stopPropagation();
    profileDropdown.style.display = (profileDropdown.style.display === 'block') ? 'none' : 'block';
  });

  // 외부 클릭 시 드롭다운 닫기
  document.addEventListener('mousedown', function (e) {
    if (!profileDropdown.contains(e.target) && e.target !== profileThumb) {
      profileDropdown.style.display = 'none';
    }
  });

  // 프로필 관리 버튼 클릭
  document.getElementById('profileManageBtn').addEventListener('click', function () {
    window.location.href = '/profile/manage/'; // 실제 프로필 관리 페이지로 이동
  });

  // 로그아웃 버튼 클릭
  document.getElementById('logoutBtn').addEventListener('click', function () {
    // 로그아웃 처리 (예: location.href = '/logout/')
    alert('로그아웃 처리');
  });
});

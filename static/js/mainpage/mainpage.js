document.addEventListener("DOMContentLoaded", function () {
  const rollingList = document.querySelector('.rolling-title');
  const items = rollingList.querySelectorAll('li');
  const itemHeight = items[0].offsetHeight;
  const itemCount = items.length;
  let currentIndex = 0;

  // 롤링 함수
  function rollNext() {
    currentIndex++;
    // 마지막 다음엔 처음으로
    if (currentIndex >= itemCount) {
      currentIndex = 0;
    }
    rollingList.style.transition = 'margin-top 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
    rollingList.style.marginTop = `-${itemHeight * currentIndex}px`;
  }

  // transition 끝나고, 마지막에서 처음으로 자연스럽게 점프
  rollingList.addEventListener('transitionend', function () {
    if (currentIndex === 0) {
      rollingList.style.transition = 'none';
      rollingList.style.marginTop = '0px';
    }
  });

  // 일정 시간마다 롤링
  setInterval(rollNext, 2000); // 2초마다 롤링
});









document.addEventListener('DOMContentLoaded', function() {
  const weatherBoxes = document.querySelectorAll('.weather-box');
  const weatherBlock = document.querySelector('.weather-block');
  const rollingSection = document.querySelector('.rolling-weather-section');
  const rollingWeather = document.querySelector('.rolling-weather');
  const rollingList = document.querySelector('.rolling-list');
  const bdFilterLeft = document.querySelector('.bd-filter.left');
  const bdFilterRight = document.querySelector('.bd-filter.right');
  const rollingWeatherContentActive = document.querySelector('.rolling-weather-content.active');
  const weatherInfoSections = document.querySelectorAll('.weather-info-section');
  const containers = document.querySelector('.containers');

  // 날씨별 배경색 매핑
  const weatherColors = {
    '맑음': '#d4edda',  // 연두
    '흐림': '#fff3cd',  // 노란
    '비': '#d1ecf1',    // 하늘
    '눈': '#f0f0f0',    // 연한 회색
    '눈/비': '#cce5ff'  // 연한 파랑
  };

  // weather-icon 스타일로 날씨 타입 추출
  function getWeatherType(box) {
    const icon = box.querySelector('.weather-icon');
    if (!icon) return null;
    const bg = window.getComputedStyle(icon).backgroundImage || window.getComputedStyle(icon).backgroundColor;
    if (bg.indexOf('#ffe066') !== -1) return '맑음';
    if (bg.indexOf('linear-gradient') !== -1 && bg.indexOf('#b2f0ff') !== -1) return '비';
    // 필요시 추가 조건(눈 등) 추가
    return '흐림'; // 기본값
  }

  weatherBoxes.forEach(box => {
    box.addEventListener('mouseenter', () => {
      const weatherType = getWeatherType(box);
      const color = weatherColors[weatherType] || '#fff';

      if (weatherBlock) weatherBlock.style.backgroundColor = color;
      if (rollingSection) rollingSection.style.backgroundColor = color;
      if (rollingWeather) rollingWeather.style.backgroundColor = color;
      if (rollingList) rollingList.style.backgroundColor = color;
      if (bdFilterLeft) bdFilterLeft.style.background = 'linear-gradient(90deg, ' + color + ' 90%, rgba(255,255,255,0) 100%)';
      if (bdFilterRight) bdFilterRight.style.background = 'linear-gradient(270deg, ' + color + ' 90%, rgba(255,255,255,0) 100%)';
      if (rollingWeatherContentActive) rollingWeatherContentActive.style.backgroundColor = color;
      if (containers) containers.style.backgroundColor = color;

      // weather-info-section와 weather-box 모두 흰색으로 고정
      weatherInfoSections.forEach(section => {
        section.style.backgroundColor = '#fff';
      });
      weatherBoxes.forEach(wb => {
        wb.style.backgroundColor = '#fff';
      });
    });
    box.addEventListener('mouseleave', () => {
      if (weatherBlock) weatherBlock.style.backgroundColor = '#fff';
      if (rollingSection) rollingSection.style.backgroundColor = '';
      if (rollingWeather) rollingWeather.style.backgroundColor = '';
      if (rollingList) rollingList.style.backgroundColor = '';
      if (bdFilterLeft) bdFilterLeft.style.background = '';
      if (bdFilterRight) bdFilterRight.style.background = '';
      if (rollingWeatherContentActive) rollingWeatherContentActive.style.backgroundColor = '';
      if (containers) containers.style.backgroundColor = '';

      // weather-info-section와 weather-box 모두 흰색으로 고정
      weatherInfoSections.forEach(section => {
        section.style.backgroundColor = '#fff';
      });
      weatherBoxes.forEach(wb => {
        wb.style.backgroundColor = '#fff';
      });
    });
  });
});

// 날씨 여행 슬라이드(좌우 이동) JS 예제
document.addEventListener('DOMContentLoaded', function () {
  const rollingInner = document.getElementById('rollingPortfolio');
  const weatherContent = rollingInner.querySelector('.rolling-weather-content.active');
  const weatherBoxes = weatherContent.querySelectorAll('.weather-box');
  const boxWidth = 344 + 24; // weather-box width + gap(24px)
  const visibleCount = Math.floor(rollingInner.offsetWidth / boxWidth) || 3; // 한 번에 보이는 카드 수(예: 3)
  const maxIndex = weatherBoxes.length - visibleCount;
  let currentIndex = 0;

  function updateSlide() {
    // margin-left를 음수로 이동
    rollingInner.style.marginLeft = `-${currentIndex * boxWidth}px`;
  }

  document.getElementById('arrowLeft').addEventListener('click', function () {
    if (currentIndex > 0) {
      currentIndex--;
      updateSlide();
    }
  });

  document.getElementById('arrowRight').addEventListener('click', function () {
    if (currentIndex < maxIndex) {
      currentIndex++;
      updateSlide();
    }
  });
});



// 동행매칭 방 리스트를 가져와서 표시하는 함수
// 예시 데이터 (실제 서비스에서는 서버에서 받아옴)

const rooms = [
  {
    profileImg: '/static/image/mainpage/profile.png',
    title: '도봉산 등산 같이 갈 사람',
    host: '노상민',
    memberCount: '4/6',
    location: '도봉역',
    tags: ['#슬픔', '#우울', '#고민']
  },
  {
    profileImg: '/static/image/mainpage/profile.png',
    title: '해운대 같이 갈 사람',
    host: '김구',
    memberCount: '4/6',
    location: '해운대',
    tags: ['#즐거움', '#행복', '#설레임']
  }
   ,{
    profileImg: '/static/image/mainpage/profile.png',
    title: '5호선 근방 글램핑',
    host: '서진유',
    memberCount: '4/6',
    location: '여의나루',
    tags: ['#즐거움', '#행복', '#설레임']
  },
  {
    profileImg: '/static/image/mainpage/profile.png',
    title: '제주도 게하',
    host: '노민상',
    memberCount: '4/6',
    location: '제주도 서귀포',
    tags: ['#즐거움', '#설레임', '#인연']
  }
];

// 카드 HTML 생성 함수
function createRoomCard(room) {
  const tagButtons = room.tags.map(tag =>
    `<button class="tag-btn">${tag}</button>`
  ).join('');

  return `
    <div class="room-card">
      <div class="room-profile">
        <img src="${room.profileImg}" alt="프로필" class="profile-img">
      </div>
      <div class="room-info">
        <div class="room-details">
          <div class="room-title">방 제목: ${room.title}</div>
          <div class="room-host">방장명: ${room.host}</div>
          <div class="room-meta">
            <span class="room-member">
              ${room.memberCount}
            </span>
          </div>
          <div class="room-location">여행지: ${room.location}</div>
          <div class="room-tags">${tagButtons}</div>
        </div>
        <div class="room-buttons">
          <button class="room-enter-btn">방 입장</button>
          <button class="room-out-btn">방 나가기</button>
        </div>
      </div>
    </div>
  `;
}

// 카드 렌더링 함수
function renderRooms(roomArray) {
  const track = document.getElementById('roomSliderTrack');
  if (track) {
    let slides = [];
    for (let i = 0; i < roomArray.length; i += 2) {
      // 2개씩 카드 생성
      const cards = [roomArray[i], roomArray[i + 1]]
        .filter(Boolean)
        .map(createRoomCard)
        .join('');
      slides.push(`<div class="slider-slide">${cards}</div>`);
    }
    track.innerHTML = slides.join('');
  }
}
// DOMContentLoaded 이벤트에 등록
document.addEventListener('DOMContentLoaded', function() {
  renderRooms(rooms);
});

function renderRooms(roomArray) {
  const track = document.getElementById('roomSliderTrack');
  if (track) {
    track.innerHTML = roomArray.map(createRoomCard).join('');
  }
}

document.addEventListener('DOMContentLoaded', function() {
  renderRooms(rooms);

  const track = document.getElementById('roomSliderTrack');
  const leftBtn = document.querySelector('.slider-arrow.left');
  const rightBtn = document.querySelector('.slider-arrow.right');
  const cardWidth = 524; // 카드+gap(px), 카드 min-width + gap

  leftBtn.addEventListener('click', () => {
    track.scrollBy({ left: -cardWidth, behavior: 'smooth' });
  });
  rightBtn.addEventListener('click', () => {
    track.scrollBy({ left: cardWidth, behavior: 'smooth' });
  });
});


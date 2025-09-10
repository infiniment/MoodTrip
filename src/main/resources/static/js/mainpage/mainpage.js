// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
  initScrollAnimations();
  initWeatherHoverEffects();
  initSmoothScroll();
  initializeMessageCounter();
  initEmotionInteractions();
});

// 1. 스크롤 애니메이션
function initScrollAnimations() {
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
      }
    });
  }, observerOptions);

  const elements = document.querySelectorAll('.feature-item, .room-card, .weather-card, .section-header');
  elements.forEach(el => observer.observe(el));
}

// 2. 날씨 호버 효과 - 동적 카드 지원으로 수정
function initWeatherHoverEffects() {
  const weatherCards = document.querySelectorAll('.weather-card');
  const weatherSection = document.querySelector('.weather-travel');

  if (!weatherCards.length || !weatherSection) return;

  const backgroundColors = {
    sunny: 'linear-gradient(180deg, #fef3c7 0%, #fde68a 100%)',
    rainy: 'linear-gradient(180deg, #dbeafe 0%, #bfdbfe 100%)',
    cloudy: 'linear-gradient(180deg, #f3f4f6 0%, #e5e7eb 100%)',
    snowy: 'linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)'
  };

  weatherCards.forEach(card => {
    card.addEventListener('mouseenter', () => {
      // 동적으로 생성된 카드에서 날씨 타입 확인
      const weatherType = [...card.classList].find(cls =>
          ['sunny','rainy','cloudy','snowy'].includes(cls)
      );

      // data-weather 속성에서도 확인
      const dataWeather = card.dataset.weather;
      let bgKey = weatherType;

      // data-weather로 매핑 (한글 → 영문)
      if (!bgKey && dataWeather) {
        switch(dataWeather) {
          case '맑음': bgKey = 'sunny'; break;
          case '비':
          case '이슬비': bgKey = 'rainy'; break;
          case '흐림':
          case '안개': bgKey = 'cloudy'; break;
          case '눈': bgKey = 'snowy'; break;
          default: bgKey = 'sunny';
        }
      }

      weatherSection.style.background = backgroundColors[bgKey] || backgroundColors.sunny;
      weatherSection.style.transition = 'background 0.3s ease';
    });

    card.addEventListener('mouseleave', () => {
      weatherSection.style.background = 'linear-gradient(180deg, #ffffff 0%, #f8fafc 100%)';
    });
  });
}

document.addEventListener('click', function(e) {
  const card = e.target.closest('.weather-card');
  if (card) {
    const contentId = card.dataset.contentId;
    console.log("카드 클릭됨:", contentId);
    if (contentId) {
      window.location.href = `/attractions/detail/${contentId}`;
    }
  }
});

// 날씨 카드 클릭 피드백
function showWeatherCardFeedback(attractionName, weatherType) {
  const feedback = document.createElement('div');
  feedback.className = 'weather-card-feedback';
  feedback.innerHTML = `
    <div style="
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 0, 0, 0.9);
      color: white;
      padding: 20px 40px;
      border-radius: 16px;
      font-size: 1.1rem;
      font-weight: 600;
      z-index: 9999;
      backdrop-filter: blur(10px);
      animation: fadeInScale 0.3s ease-out;
      text-align: center;
    ">
      ${attractionName}<br>
      <small style="font-size: 0.9rem; opacity: 0.8;">${weatherType} 날씨 추천</small>
    </div>
  `;

  document.body.appendChild(feedback);

  // 1.5초 후 제거
  setTimeout(() => {
    feedback.style.animation = 'fadeInScale 0.3s ease-out reverse';
    setTimeout(() => feedback.remove(), 300);
  }, 1500);
}

// 3. 감정 인터랙션 기능 (기존과 동일)
function initEmotionInteractions() {
  const emotionItems = document.querySelectorAll('.emotion-item');
  const emotionCenter = document.querySelector('.emotion-center');
  const emotionBrand = document.querySelector('.emotion-brand');
  const emotionSubtitle = document.querySelector('.emotion-subtitle');

  if (!emotionItems.length || !emotionCenter) return;

  // 감정별 메시지 매핑
  const emotionMessages = {
    '행복': {
      brand: 'HAPPY MOMENT',
      subtitle: '행복의 순간',
      color: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)'
    },
    '설레임': {
      brand: 'EXCITING',
      subtitle: '설레는 마음',
      color: 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)'
    },
    '평온': {
      brand: 'PEACEFUL',
      subtitle: '마음의 평화',
      color: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
    },
    '신남': {
      brand: 'AMAZING',
      subtitle: '신나는 여행',
      color: 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)'
    },
    '힐링': {
      brand: 'HEALING',
      subtitle: '힐링의 시간',
      color: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)'
    },
    '모험': {
      brand: 'ADVENTURE',
      subtitle: '모험의 세계로',
      color: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
    },
    '로맨틱': {
      brand: 'ROMANTIC',
      subtitle: '연인과 함께',
      color: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)'
    },
    '자유': {
      brand: 'FREEDOM',
      subtitle: '자유의 순간',
      color: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)'
    }
  };

  const defaultMessage = {
    brand: 'MOOD TRIP',
    subtitle: '감정을 찾아보세요',
    color: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)'
  };

  // 감정 아이템 호버 이벤트
  emotionItems.forEach(item => {
    const emotion = item.dataset.emotion;

    item.addEventListener('mouseenter', () => {
      const message = emotionMessages[emotion] || defaultMessage;

      // 텍스트 변경
      emotionBrand.textContent = message.brand;
      emotionSubtitle.textContent = message.subtitle;

      // 색상 변경
      emotionBrand.style.background = message.color;
      emotionBrand.style.webkitBackgroundClip = 'text';
      emotionBrand.style.webkitTextFillColor = 'transparent';
      emotionBrand.style.backgroundClip = 'text';

      // 중앙 카드 효과
      emotionCenter.style.transform = 'scale(1.15)';
      emotionCenter.style.boxShadow = '0 25px 50px rgba(0, 0, 0, 0.15)';
    });

    item.addEventListener('mouseleave', () => {
      // 기본 상태로 복원
      emotionBrand.textContent = defaultMessage.brand;
      emotionSubtitle.textContent = defaultMessage.subtitle;
      emotionBrand.style.background = defaultMessage.color;
      emotionBrand.style.webkitBackgroundClip = 'text';
      emotionBrand.style.webkitTextFillColor = 'transparent';
      emotionBrand.style.backgroundClip = 'text';

      emotionCenter.style.transform = 'scale(1.1)';
      emotionCenter.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.1)';
    });

    // 클릭 이벤트 (감정 선택)
    item.addEventListener('click', () => {
      const emotion = item.dataset.emotion;
      console.log(`${emotion} 감정이 선택되었습니다!`);

      // 선택된 감정으로 페이지 이동 (실제 구현 시)
      // window.location.href = `/emotion-search?emotion=${encodeURIComponent(emotion)}`;

      // 임시 피드백
      showEmotionFeedback(emotion);
    });
  });
}

// 감정 선택 피드백 표시
function showEmotionFeedback(emotion) {
  // 기존 피드백 제거
  const existingFeedback = document.querySelector('.emotion-feedback');
  if (existingFeedback) {
    existingFeedback.remove();
  }

  // 새 피드백 생성
  const feedback = document.createElement('div');
  feedback.className = 'emotion-feedback';
  feedback.innerHTML = `
    <div style="
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 0, 0, 0.9);
      color: white;
      padding: 20px 40px;
      border-radius: 16px;
      font-size: 1.2rem;
      font-weight: 600;
      z-index: 9999;
      backdrop-filter: blur(10px);
      animation: fadeInScale 0.3s ease-out;
    ">
      ${emotion} 감정이 선택되었습니다!
    </div>
  `;

  // 애니메이션 스타일 추가
  const style = document.createElement('style');
  style.textContent = `
    @keyframes fadeInScale {
      from {
        opacity: 0;
        transform: translate(-50%, -50%) scale(0.8);
      }
      to {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
      }
    }
  `;
  document.head.appendChild(style);

  document.body.appendChild(feedback);

  // 2초 후 제거
  setTimeout(() => {
    feedback.style.animation = 'fadeInScale 0.3s ease-out reverse';
    setTimeout(() => {
      feedback.remove();
      style.remove();
    }, 300);
  }, 2000);
}

// 4. 부드러운 스크롤
function initSmoothScroll() {
  window.scrollToSection = function(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };
}

// 5. 새로 추가: 모든 날씨 여행지 보기 함수
window.showAllWeatherSpots = function() {
  console.log("모든 날씨 여행지 보기 클릭");

  // 실제 구현시에는 날씨별 여행지 전체 페이지로 이동
  // window.location.href = '/weather-attractions';

  // 임시로 감정 검색 페이지로 이동
  window.location.href = '/emotion-search';
};

// 방 상세보기 모달 열기
window.viewRoomDetail = function(roomId) {
  console.log("상세보기 클릭:", roomId);

  const modal = document.getElementById("detailModal");
  if (!modal) return;

  fetch(`/entering-room/${roomId}/modal-data`)
      .then(res => {
        if (!res.ok) throw new Error("상세보기 요청 실패");
        return res.json();
      })
      .then(data => {
        document.getElementById("detailRoomImage").src = data.image || "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg";
        document.getElementById("detailRoomTitle").textContent = data.title;
        document.getElementById("detailRoomLocation").textContent = data.location || data.category;
        document.getElementById("detailRoomDate").textContent = data.date;
        document.getElementById("detailRoomParticipants").textContent =
            `${data.currentParticipants} / ${data.maxParticipants}`;
        document.getElementById("detailRoomViews").textContent = data.views;
        document.getElementById("detailRoomPeriod").textContent = data.createdDate;
        document.getElementById("detailRoomDesc").textContent = data.description || "소개글이 없습니다.";

        const tagContainer = document.getElementById("detailRoomTags");
        tagContainer.innerHTML = "";
        if (data.emotions && data.emotions.length > 0) {
          data.emotions.forEach(tag => {
            const span = document.createElement("span");
            span.className = "tag";
            span.textContent = `#${tag}`;
            tagContainer.appendChild(span);
          });
        } else {
          tagContainer.innerHTML = '<span class="no-tags">등록된 감정 태그가 없습니다.</span>';
        }

        modal.style.display = "flex";
      })
      .catch(err => {
        console.error("상세보기 오류:", err);
        alert("상세 정보를 불러올 수 없습니다.");
      });
};

// 상세보기 모달 닫기
window.closeDetailModal = function() {
  document.getElementById("detailModal").style.display = "none";
};

// 방 입장 신청하기
window.joinRoom = function(roomId) {
  console.log("입장 신청 클릭:", roomId);

  const modal = document.getElementById("applicationModal");
  if (!modal) return;

  // 방 정보 가져오기
  fetch(`/entering-room/${roomId}/modal-data`)
      .then(res => res.json())
      .then(data => {
        // 방 정보 설정
        document.getElementById("modalRoomTitle").textContent = data.title;
        document.getElementById("modalRoomMeta").textContent = `${data.location} | ${data.createdDate}`;
        modal.dataset.roomId = roomId;

        // 현재 사용자의 최신 프로필 자기소개 가져오기
        loadCurrentUserProfile();

        // 모달 표시
        modal.style.display = "flex";
      })
      .catch(err => {
        console.error("입장 신청 오류:", err);
        alert("방 정보를 불러올 수 없습니다.");
      });
};

// 현재 사용자 프로필 정보 로드
function loadCurrentUserProfile() {
  console.log("현재 사용자의 최신 프로필 정보 조회 중...");

  fetch('/api/v1/profiles/me', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include'
  })
      .then(response => {
        console.log("프로필 API 응답 상태:", response.status);

        if (!response.ok) {
          throw new Error('프로필 조회 실패');
        }
        return response.json();
      })
      .then(profileData => {
        console.log("최신 프로필 데이터 수신:", profileData);

        const profileBioElement = document.getElementById('currentProfileBio');
        if (profileBioElement) {
          const latestBio = profileData.profileBio || '안녕하세요! 여행을 좋아합니다.';
          profileBioElement.textContent = latestBio;

          console.log("자기소개 업데이트 완료:", latestBio);
        } else {
          console.error("currentProfileBio 엘리먼트를 찾을 수 없습니다.");
        }
      })
      .catch(error => {
        console.error('최신 프로필 로드 실패:', error);

        const profileBioElement = document.getElementById('currentProfileBio');
        if (profileBioElement) {
          profileBioElement.textContent = '안녕하세요! 여행을 좋아합니다.';
        }

        console.warn("프로필을 불러올 수 없어 기본 자기소개를 표시합니다.");
      });
}

// 입장 신청 모달 닫기
window.closeApplicationModal = function() {
  document.getElementById("applicationModal").style.display = "none";
};

// 입장 신청 제출
window.submitApplication = function() {
  const modal = document.getElementById("applicationModal");
  const roomId = modal.dataset.roomId;
  const message = document.getElementById("applicationMessage").value.trim();

  if (!message) {
    alert("신청 메시지를 입력해주세요.");
    return;
  }

  fetch(`/api/v1/companion-rooms/${roomId}/join-requests`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message })
  })
      .then(res => {
        if (!res.ok) throw new Error("입장 신청 실패");
        return res.json();
      })
      .then(result => {
        if (result.success) {
          alert(result.resultMessage);
          closeApplicationModal();
        } else {
          alert(result.resultMessage);
        }
      })
      .catch(err => {
        console.error("입장 신청 오류:", err);
        alert("입장 신청 중 오류가 발생했습니다.");
      });
};

// 글자 수 카운터 기능
function initializeMessageCounter() {
  const messageInput = document.getElementById('applicationMessage');
  const counter = document.getElementById('messageLength');

  if (messageInput && counter) {
    messageInput.addEventListener('input', function() {
      const length = this.value.length;
      counter.textContent = length;

      const counterContainer = counter.parentElement;

      if (length > 300) {
        counterContainer.classList.add('warning');
        this.style.borderColor = '#dc3545';
      } else {
        counterContainer.classList.remove('warning');
        this.style.borderColor = '#ced4da';
      }
    });
  }
}

// 신고 모달 열기
window.reportRoom = function(roomId) {
  console.log("신고 클릭:", roomId);

  const modal = document.getElementById("reportModal");
  if (!modal) return;

  fetch(`/entering-room/${roomId}/modal-data`)
      .then(res => res.json())
      .then(data => {
        document.getElementById("reportRoomTitle").textContent = data.title;
        document.getElementById("reportRoomMeta").textContent = `${data.location} | ${data.createdDate}`;
        modal.dataset.roomId = roomId;
        modal.style.display = "flex";
      })
      .catch(err => {
        console.error("신고 모달 오류:", err);
        alert("방 정보를 불러올 수 없습니다.");
      });
};

// 신고 모달 닫기
window.closeReportModal = function() {
  document.getElementById("reportModal").style.display = "none";
};

// 신고 제출
window.submitReport = function() {
  const modal = document.getElementById("reportModal");
  const roomId = modal.dataset.roomId;
  const reason = document.getElementById("reportReason").value;
  const message = document.getElementById("reportMessage").value.trim();

  if (!reason) {
    alert("신고 사유를 선택해주세요.");
    return;
  }

  fetch(`/api/v1/fires/rooms/${roomId}`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      reportReason: reason,
      reportMessage: message
    })
  })
      .then(async res => {
        const data = await res.json().catch(() => null);

        if (!res.ok) {
          const errorMsg = data?.message || data?.error || "신고 실패";
          throw new Error(errorMsg);
        }

        return data;
      })
      .then(result => {
        if (result.success) {
          alert(`${result.message}`);
          closeReportModal();
        } else {
          alert(result.message || "신고 접수 중 오류가 발생했습니다.");
        }
      })
      .catch(err => {
        console.error("신고 오류:", err);
        alert(err.message);
      });
};
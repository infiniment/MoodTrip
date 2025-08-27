// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
  initScrollAnimations();
  initWeatherHoverEffects();
  initSmoothScroll();
  initializeMessageCounter();
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

// 2. 날씨 호버 효과
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
      const weatherType = [...card.classList].find(cls =>
          ['sunny','rainy','cloudy','snowy'].includes(cls)
      );
      weatherSection.style.background = backgroundColors[weatherType] || backgroundColors.sunny;
      weatherSection.style.transition = 'background 0.3s ease';
    });

    card.addEventListener('mouseleave', () => {
      weatherSection.style.background = 'linear-gradient(180deg, #ffffff 0%, #f8fafc 100%)';
    });
  });
}

// 3. 감정 인터랙션 (생략 - 기존 코드 동일)

// 4. 부드러운 스크롤
function initSmoothScroll() {
  window.scrollToSection = function(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };
}

// ... (중간 기능 부분 동일) ...

// 15. 방 상세보기 / 입장 신청 / 신고 모달 기능

// 상세보기 모달 열기
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
        document.getElementById("detailRoomImage").src = data.image || "/image/fix/moodtrip.png";
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

// 입장 신청 모달 열기
window.joinRoom = function(roomId) {
  console.log("입장 신청 클릭:", roomId);

  const modal = document.getElementById("applicationModal");
  if (!modal) return;

  fetch(`/entering-room/${roomId}/modal-data`)
      .then(res => res.json())
      .then(data => {
        document.getElementById("modalRoomTitle").textContent = data.title;
        document.getElementById("modalRoomMeta").textContent = `${data.location} | ${data.createdDate}`;
        modal.dataset.roomId = roomId;
        modal.style.display = "flex";
      })
      .catch(err => {
        console.error("입장 신청 오류:", err);
        alert("방 정보를 불러올 수 없습니다.");
      });
};

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

  fetch(`/api/v1/companion-rooms/${roomId}/join-requests`, {   // ✅ 수정
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

};

// 신고 사유 변경 시 유효성 체크
function validateReportForm() {
  const reasonElement = document.getElementById('reportReason');
  const submitButton = document.querySelector('#reportModal .btn-danger');

  if (reasonElement && submitButton) {
    const reason = reasonElement.value;

    if (reason && reason !== '') {
      submitButton.disabled = false;
      submitButton.style.opacity = '1';
    } else {
      submitButton.disabled = true;
      submitButton.style.opacity = '0.6';
    }
  }
}

// 새로 추가: 글자 수 카운터 기능 추가
function initializeMessageCounter() {
  const messageInput = document.getElementById('applicationMessage');
  const counter = document.getElementById('messageLength');

  if (messageInput && counter) {
    messageInput.addEventListener('input', function() {
      const length = this.value.length;
      counter.textContent = length;

      const counterContainer = counter.parentElement;

      // 300자 넘으면 경고 스타일
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

  fetch(`/api/v1/fires/rooms/${roomId}`, {    // ✅ 수정
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      reportReason: reason,   // ✅ 키 이름 변경
      reportMessage: message
    })
  })
      .then(async res => {
        const data = await res.json().catch(() => null);

        if (!res.ok) {
          // 실패해도 서버에서 준 메시지 있으면 그대로 에러로 던짐
          const errorMsg = data?.message || data?.error || "신고 실패";
          throw new Error(errorMsg);
        }

        return data; // 성공 시 정상 데이터 반환
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
        alert(err.message); // 이제 "신고 실패" 대신 서버 메시지가 그대로 뜸
      });

};
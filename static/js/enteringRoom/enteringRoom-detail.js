// 현재 방 데이터
let currentRoomData = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadRoomData();
    initializeEventListeners();
});

// localStorage에서 방 데이터 로드
function loadRoomData() {
    try {
        const roomData = localStorage.getItem('selectedRoomData');
        if (roomData) {
            currentRoomData = JSON.parse(roomData);
            displayRoomData(currentRoomData);
        } else {
            // 데이터가 없으면 기본 데이터 표시 또는 이전 페이지로 이동
            console.error('방 데이터를 찾을 수 없습니다.');
            alert('방 정보를 불러올 수 없습니다.');
            goBack();
        }
    } catch (error) {
        console.error('방 데이터 로드 중 오류:', error);
        alert('방 정보를 불러오는 중 오류가 발생했습니다.');
        goBack();
    }
}

// 방 데이터를 화면에 표시
function displayRoomData(room) {
    // 상태 배지
    const statusBadge = document.getElementById('statusBadge');
    statusBadge.textContent = room.status || '모집중';
    statusBadge.className = `status-badge ${room.urgent ? 'urgent' : 'recruiting'}`;
    
    // 등록일자
    document.getElementById('registerDate').textContent = formatRegisterDate(new Date());
    
    // 방 제목
    document.getElementById('roomTitle').textContent = room.title;
    
    // 인원 정보 카드
    document.getElementById('peopleCount').textContent = `${room.maxParticipants}명`;
    document.getElementById('dateRange').textContent = room.date;
    document.getElementById('travelDate').textContent = room.createdDate;
    
    // 상세 정보
    document.getElementById('deadline').textContent = formatDeadline(room.dateValue);
    document.getElementById('departureDate').textContent = '모집 이후 채팅으로 협의';
    document.getElementById('currentPeople').textContent = `${room.currentParticipants} / ${room.maxParticipants}명`;
    document.getElementById('regionRole').textContent = '지역 자격은 따로 없고, 모두 다 환영합니다!';
    
    // 감정 태그
    const emotionTagsContainer = document.getElementById('emotionTags');
    emotionTagsContainer.innerHTML = room.tags.map(tag => 
        `<span class="emotion-tag">${tag}</span>`
    ).join('');
    
    // 이미지
    const roomImage = document.getElementById('roomImage');
    roomImage.src = room.image;
    roomImage.alt = room.title;
    roomImage.onerror = function() {
        this.src = 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=400&h=300&fit=crop';
    };
    
    // 설명
    document.getElementById('roomDescription').textContent = room.description;
}

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeApplicationModal();
        }
    });
    
    // 모달 오버레이 클릭으로 닫기
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal-overlay')) {
            closeApplicationModal();
        }
    });
}

// 뒤로가기
function goBack() {
    // 브라우저 히스토리가 있으면 뒤로가기, 없으면 방 찾기 페이지로
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = 'enteringRoom.html';
    }
}

// 신청하기 버튼 클릭
function applyToRoom() {
    if (currentRoomData) {
        openApplicationModal(currentRoomData);
    }
}

// 신청 모달 열기
function openApplicationModal(room) {
    const modal = document.getElementById('applicationModal');
    const modalRoomTitle = document.getElementById('modalRoomTitle');
    const modalRoomMeta = document.getElementById('modalRoomMeta');
    
    modalRoomTitle.textContent = room.title;
    modalRoomMeta.textContent = `${room.location} | ${room.currentParticipants}/${room.maxParticipants}명 | ${room.createdDate}`;
    
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// 신청 모달 닫기
function closeApplicationModal() {
    const modal = document.getElementById('applicationModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
    
    // 폼 초기화
    document.getElementById('applicationMessage').value = '';
}

// 신청 제출
function submitApplication() {
    const message = document.getElementById('applicationMessage').value.trim();
    
    if (!message) {
        alert('신청 메시지를 입력해주세요.');
        return;
    }
    
    if (!currentRoomData) {
        alert('방 정보를 찾을 수 없습니다.');
        return;
    }
    
    // 실제로는 서버에 신청 데이터 전송
    console.log('신청 데이터:', {
        roomId: currentRoomData.id,
        roomTitle: currentRoomData.title,
        message: message,
        timestamp: new Date().toISOString()
    });
    
    alert(`"${currentRoomData.title}" 방에 입장 신청이 완료되었습니다!\n방장의 승인을 기다려주세요.`);
    closeApplicationModal();
    
    // 신청 완료 후 방 찾기 페이지로 이동 (선택사항)
    // setTimeout(() => {
    //     window.location.href = 'enteringRoom.html';
    // }, 1000);
}

// 헬퍼 함수들
function formatRegisterDate(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}.${month}.${day}`;
}

function formatDeadline(dateValue) {
    if (!dateValue) return '2025년 7월 31일';
    
    const deadline = new Date(dateValue);
    deadline.setDate(deadline.getDate() - 7); // 7일 전
    const year = deadline.getFullYear();
    const month = deadline.getMonth() + 1;
    const day = deadline.getDate();
    return `${year}년 ${month}월 ${day}일`;
}

// 방 데이터가 없을 때 사용할 기본 데이터
const defaultRoomData = {
    id: 1,
    title: "경복궁 간장 게장 같이 먹으러 가요~!!",
    location: "서울",
    date: "7월",
    dateValue: new Date('2025-07-21'),
    views: "3명이 봄",
    viewCount: 3,
    description: "안녕하세요! 저희는 세상 최고 멋쟁이 김상우가 다니고 있는 한성대학교 캠퍼스 투어로 떠납니다!! 경복궁에서 간장게장도 먹고 야경도 보면서 즐거운 시간을 보내요!",
    tags: ["설렘", "역사", "야경"],
    currentParticipants: 2,
    maxParticipants: 4,
    createdDate: "2025.07.21 - 2025.07.31",
    image: "/static/image/enteringRoom/hansung.png",
    category: "학교",
    urgent: false,
    status: "모집중"
};
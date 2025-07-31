// 메신저 관련 전역 변수
let isMessengerOpen = false;
let currentUser = generateRandomUser(); // 랜덤 사용자명 생성
let connectedUsers = ['김상우', '노수민']; // 현재 접속자 목록
let stompClient = null; // STOMP 클라이언트
let chattingRoomId = 1; // 채팅방 ID (실제로는 동적으로 설정)

// 날짜 및 일정 관련 전역 변수
let selectedDate = null; // 선택된 날짜
let schedules = {}; // 일정 데이터 저장소 (날짜별)

// 달력 관련 기능
document.addEventListener("DOMContentLoaded", function () {
    const calendarGrid = document.getElementById("calendarGrid");
    const calendarHeader = document.querySelector(".calendar-header h3");

    // 현재 날짜 가져오기
    let currentDate = new Date();
    let currentYear = currentDate.getFullYear();
    let currentMonth = currentDate.getMonth(); // 0: 1월 ~ 11: 12월

    function renderCalendar(year, month) {
        // 기존 날짜 div 초기화 (요일 헤더 제외)
        const allDays = calendarGrid.querySelectorAll(".calendar-day.date");
        allDays.forEach(day => day.remove());

        // 달력 헤더 갱신
        calendarHeader.textContent = `${year}년 ${month + 1}월`;

        // 해당 월의 1일과 마지막 날짜 계산
        const firstDay = new Date(year, month, 1).getDay(); // 0:일 ~ 6:토
        const lastDate = new Date(year, month + 1, 0).getDate(); // 말일

        // 앞쪽 빈칸 채우기
        for (let i = 0; i < firstDay; i++) {
            const emptyDiv = document.createElement("div");
            emptyDiv.className = "calendar-day date empty";
            calendarGrid.appendChild(emptyDiv);
        }

        // 날짜 채우기
        for (let day = 1; day <= lastDate; day++) {
            const dayDiv = document.createElement("div");
            dayDiv.className = "calendar-day date";
            dayDiv.textContent = day;

            // 오늘 날짜 강조
            if (
                day === currentDate.getDate() &&
                month === currentDate.getMonth() &&
                year === currentDate.getFullYear()
            ) {
                dayDiv.style.fontWeight = "bold";
                dayDiv.style.color = "#007BFF";
            }

            // 클릭 이벤트 (일정 불러오기 등)
            dayDiv.addEventListener("click", () => {
                handleDateClick(year, month, day);
            });

            calendarGrid.appendChild(dayDiv);
        }
    }

    // 초기 렌더링
    renderCalendar(currentYear, currentMonth);

    // 이전/다음 달 이동
    const [prevBtn, nextBtn] = document.querySelectorAll(".calendar-nav .nav-btn");
    prevBtn.addEventListener("click", () => {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        renderCalendar(currentYear, currentMonth);
    });

    nextBtn.addEventListener("click", () => {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderCalendar(currentYear, currentMonth);
    });
});

// 중요 : 방 만들기 후
// // 서버에서 현재 로그인한 사용자 정보 받아오기
// let currentUser = null;
//
// // 페이지 로드시 현재 사용자 정보 조회
// async function getCurrentUser() {
//     try {
//         const response = await fetch('/api/user/current');
//         const userData = await response.json();
//         currentUser = userData.username; // 또는 userData.nickname
//         console.log('현재 로그인 사용자:', currentUser);
//         return userData;
//     } catch (error) {
//         console.error('사용자 정보 조회 실패:', error);
//         // 로그인 페이지로 리다이렉트
//         window.location.href = '/login';
//     }
// }
//
// // DOM 로드 후 실행
// document.addEventListener('DOMContentLoaded', async function() {
//     await getCurrentUser(); // 사용자 정보 먼저 가져오기
//     initializeMessenger();
//     updateOnlineUsers();
//     updateUserInterface();
//     connectWebSocket();
// });

// 테스트용 사용자 선택 (더 간단한 방법)
function generateRandomUser() {
    const users = ['김상우', '노수민', '김민규', '서유진'];

    // 세션 스토리지에서 기존 사용자 확인
    let sessionUser = sessionStorage.getItem('currentUser');

    if (!sessionUser) {
        // 첫 번째 창인지 확인 (localStorage 사용)
        const usedUsers = JSON.parse(localStorage.getItem('usedUsers') || '[]');

        // 사용되지 않은 사용자 찾기
        const availableUsers = users.filter(user => !usedUsers.includes(user));

        if (availableUsers.length > 0) {
            sessionUser = availableUsers[0];
        } else {
            // 모든 사용자가 사용 중이면 랜덤 선택
            sessionUser = users[Math.floor(Math.random() * users.length)];
        }

        // 사용자 목록에 추가
        usedUsers.push(sessionUser);
        localStorage.setItem('usedUsers', JSON.stringify(usedUsers));
        sessionStorage.setItem('currentUser', sessionUser);
    }

    console.log('현재 사용자:', sessionUser);
    return sessionUser;
}

// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 현재 사용자 표시
    console.log('현재 접속 사용자:', currentUser);

    initializeMessenger();
    updateOnlineUsers();
    updateUserInterface();
    connectWebSocket(); // WebSocket 연결 초기화
});

// 사용자 인터페이스 업데이트
function updateUserInterface() {
    // 채팅 헤더에 현재 사용자 표시
    const profileInfo = document.querySelector('.profile-info h3');
    if (profileInfo) {
        profileInfo.textContent = `채팅방 - ${currentUser}`;
    }
}

// 메신저 초기화
function initializeMessenger() {
    const messengerWidget = document.getElementById('messengerWidget');
    const floatingBtn = document.getElementById('messengerFloatingBtn');

    // 초기에는 메신저 숨김
    if (messengerWidget) {
        messengerWidget.style.display = 'none';
    }

    // 플로팅 버튼 표시
    if (floatingBtn) {
        floatingBtn.style.display = 'flex';
    }

    // 채팅 입력창 엔터키 이벤트
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }
}

// 메신저 토글 기능
function toggleMessenger() {
    const messengerWidget = document.getElementById('messengerWidget');
    const floatingBtn = document.getElementById('messengerFloatingBtn');

    if (!isMessengerOpen) {
        // 메신저 열기
        messengerWidget.style.display = 'flex';
        messengerWidget.classList.add('show');
        floatingBtn.style.display = 'none';
        isMessengerOpen = true;

        // 스크롤을 최하단으로
        setTimeout(() => {
            scrollToBottom();
        }, 100);
    } else {
        // 메신저 닫기
        messengerWidget.style.display = 'none';
        messengerWidget.classList.remove('show');
        floatingBtn.style.display = 'flex';
        isMessengerOpen = false;
    }
}

// 메시지 전송 기능
function sendMessage() {
    const chatInput = document.getElementById('chatInput');
    const message = chatInput.value.trim();

    if (message === '' || !stompClient || !stompClient.connected) return;

    // Spring Boot ChatMessageRequest 형태로 메시지 전송
    const chatMessage = {
        type: 'TALK',
        chattingRoomId: chattingRoomId,
        sender: currentUser,
        message: message
    };

    // 서버로 메시지 전송
    stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));

    // 입력창 초기화
    chatInput.value = '';
}

// 서버에서 받은 메시지 처리
function addReceivedMessage(chatMessageResponse) {
    const isCurrentUser = chatMessageResponse.sender === currentUser;

    // 시간 포맷팅 (LocalDateTime을 JavaScript Date로 변환)
    const sendTime = new Date(chatMessageResponse.sendTime);
    const timeString = sendTime.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    // 입장/퇴장 메시지인지 확인
    const message = chatMessageResponse.message;
    if (message.includes('입장했습니다') || message.includes('퇴장했습니다')) {
        // 시스템 메시지로 처리
        addSystemMessage(message);
    } else {
        // 일반 채팅 메시지로 처리
        addMessageToUI(chatMessageResponse.sender, message, isCurrentUser, timeString);
    }
}

// 메시지를 UI에 추가하는 함수
function addMessageToUI(sender, content, isCurrentUser = false, timeString = null) {
    const chatMessages = document.getElementById('chatMessages');

    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${isCurrentUser ? 'user' : ''}`;

    const currentTime = timeString || new Date().toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    if (isCurrentUser) {
        messageDiv.innerHTML = `
            <div class="message-content">
                ${content}
                <div class="message-time">${currentTime}</div>
            </div>
        `;
    } else {
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <img src="/static/image/schedule-with-companion/label-logo.jpg" alt="${sender}">
            </div>
            <div class="message-content">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                    <strong>${sender}</strong>
                    <span class="message-time" style="font-size: 11px; color: #999;">${currentTime}</span>
                </div>
                <div>${content}</div>
            </div>
        `;
    }

    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

// 서버에서 기존 채팅 기록 불러오기
function loadChatHistory() {
    // 서버에서 채팅 기록 조회 API 호출
    fetch(`/api/chat/history/${chattingRoomId}`)
        .then(response => response.json())
        .then(messages => {
            // 기존 메시지들을 화면에 표시
            messages.forEach(msg => {
                const isCurrentUser = msg.sender === currentUser;
                const timeString = new Date(msg.sendTime).toLocaleTimeString('ko-KR', {
                    hour: '2-digit',
                    minute: '2-digit'
                });
                addMessageToUI(msg.sender, msg.message, isCurrentUser, timeString);
            });

            // 스크롤을 최하단으로
            setTimeout(() => {
                scrollToBottom();
            }, 100);
        })
        .catch(error => {
            console.error('채팅 기록 로드 실패:', error);
        });
}

// 채팅 스크롤을 최하단으로
function scrollToBottom() {
    const chatMessages = document.getElementById('chatMessages');
    if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}

// 온라인 사용자 업데이트
function updateOnlineUsers() {
    const onlineUsersElement = document.getElementById('onlineUsers');
    const onlineCountElement = document.getElementById('onlineCount');
    const chatRoomUsersElement = document.getElementById('chatRoomUsers');

    if (onlineUsersElement) {
        onlineUsersElement.textContent = connectedUsers.join(', ');
    }

    if (onlineCountElement) {
        onlineCountElement.textContent = `${connectedUsers.length}명 온라인`;
    }

    if (chatRoomUsersElement) {
        chatRoomUsersElement.textContent = `현재 접속자: ${connectedUsers.join(', ')}`;
    }
}

// 새로운 사용자 접속
function userJoined(username) {
    if (!connectedUsers.includes(username)) {
        connectedUsers.push(username);
        updateOnlineUsers();

        // 시스템 메시지 추가
        addSystemMessage(`${username}님이 접속했습니다.`);
    }
}

// 사용자 나감
function userLeft(username) {
    const index = connectedUsers.indexOf(username);
    if (index > -1) {
        connectedUsers.splice(index, 1);
        updateOnlineUsers();

        // 시스템 메시지 추가
        addSystemMessage(`${username}님이 나갔습니다.`);
    }
}

// 시스템 메시지 추가
function addSystemMessage(content) {
    const chatMessages = document.getElementById('chatMessages');

    const systemDiv = document.createElement('div');
    systemDiv.className = 'system-message';
    systemDiv.textContent = content;

    chatMessages.appendChild(systemDiv);
    scrollToBottom();
}

// 실시간 시간 업데이트 (메시지 시간 표시용)
function updateCurrentTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    return timeString;
}

// WebSocket 연결 설정
function connectWebSocket() {
    // SockJS와 STOMP 클라이언트 연결
    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('WebSocket 연결 성공: ' + frame);

        // 채팅방 구독
        stompClient.subscribe(`/sub/chatroom/${chattingRoomId}`, function(message) {
            const chatMessage = JSON.parse(message.body);
            addReceivedMessage(chatMessage);
        });

        // 입장 메시지 전송
        sendEnterMessage();

    }, function(error) {
        console.error('WebSocket 연결 실패:', error);
        // 연결 실패 시 재연결 시도
        setTimeout(connectWebSocket, 5000);
    });
}

// 입장 메시지 전송
function sendEnterMessage() {
    if (stompClient && stompClient.connected) {
        const enterMessage = {
            type: 'ENTER',
            chattingRoomId: chattingRoomId,
            sender: currentUser,
            message: `${currentUser}님이 입장했습니다.`
        };

        stompClient.send("/pub/chat/message", {}, JSON.stringify(enterMessage));
    }
}

// WebSocket 연결 해제
function disconnectWebSocket() {
    if (stompClient && stompClient.connected) {
        // 퇴장 메시지 전송
        const leaveMessage = {
            type: 'LEAVE',
            chattingRoomId: chattingRoomId,
            sender: currentUser,
            message: `${currentUser}님이 퇴장했습니다.`
        };

        stompClient.send("/pub/chat/message", {}, JSON.stringify(leaveMessage));
        stompClient.disconnect();
        console.log('WebSocket 연결 해제');
    }

    // 사용자 목록에서 제거
    const usedUsers = JSON.parse(localStorage.getItem('usedUsers') || '[]');
    const updatedUsers = usedUsers.filter(user => user !== currentUser);
    localStorage.setItem('usedUsers', JSON.stringify(updatedUsers));
}

// 페이지 언로드시 WebSocket 연결 해제
window.addEventListener('beforeunload', function() {
    disconnectWebSocket();
});

// 채팅방 설정 함수
function setChattingRoomId(roomId) {
    chattingRoomId = roomId;
    console.log('채팅방 ID 설정:', chattingRoomId);
}

// 현재 사용자 설정 함수
function setCurrentUser(username) {
    currentUser = username;
    console.log('현재 사용자 설정:', currentUser);
}

// 채팅창 반응형 처리
function handleResponsive() {
    const widget = document.getElementById('messengerWidget');
    const isMobile = window.innerWidth <= 768;

    if (widget && isMessengerOpen) {
        if (isMobile) {
            widget.style.width = 'calc(100vw - 20px)';
            widget.style.height = 'calc(100vh - 40px)';
            widget.style.bottom = '10px';
            widget.style.right = '10px';
        } else {
            widget.style.width = '380px';
            widget.style.height = '600px';
            widget.style.bottom = '30px';
            widget.style.right = '30px';
        }
    }
}

window.addEventListener('resize', handleResponsive);

// 추가 기능 JavaScript 코드 (기존 코드에 추가할 부분)

// 섹션 탭 전환 기능
function showSection(sectionId) {
    // 모든 탭에서 active 클래스 제거
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));

    // 모든 섹션 숨기기
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => section.classList.remove('active'));

    // 클릭된 탭에 active 클래스 추가
    event.target.classList.add('active');

    // 해당 섹션 보이기
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

// 일정 추가 모달 열기
function openScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    if (modal) {
        modal.style.display = 'flex';
        // 모달 애니메이션을 위한 클래스 추가
        setTimeout(() => {
            modal.classList.add('show');
        }, 10);
    }
}

// 일정 추가 모달 닫기
function closeScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    if (modal) {
        modal.classList.remove('show');
        // 애니메이션 완료 후 모달 숨기기
        setTimeout(() => {
            modal.style.display = 'none';
            // 입력 필드 초기화
            clearModalInputs();
        }, 300);
    }
}

// 모달 입력 필드 초기화
function clearModalInputs() {
    document.getElementById('scheduleTime').value = '';
    document.getElementById('scheduleTitle').value = '';
    document.getElementById('scheduleDescription').value = '';
}

// 모달에서 일정 추가하기
function addScheduleFromModal() {
    const time = document.getElementById('scheduleTime').value;
    const title = document.getElementById('scheduleTitle').value;
    const description = document.getElementById('scheduleDescription').value;

    // 입력값 검증
    if (!time || !title.trim()) {
        alert('시간과 일정 제목을 입력해주세요.');
        return;
    }

    // 선택된 날짜가 없으면 오늘 날짜로 설정
    if (!selectedDate) {
        const today = new Date();
        selectedDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    }

    // 일정 객체 생성
    const newSchedule = {
        id: Date.now(), // 임시 ID (실제로는 서버에서 생성)
        time: time,
        title: title.trim(),
        description: description.trim(),
        date: selectedDate
    };

    // 일정 데이터에 추가
    if (!schedules[selectedDate]) {
        schedules[selectedDate] = [];
    }
    schedules[selectedDate].push(newSchedule);

    // 일정 목록 업데이트
    updateScheduleList(selectedDate);

    // 서버에 일정 저장 (실제 구현 시)
    saveScheduleToServer(newSchedule);

    // 성공 메시지
    showNotification('일정이 추가되었습니다.');

    // 모달 닫기
    closeScheduleModal();
}

// 선택된 날짜의 일정 목록 업데이트
function updateScheduleList(dateString) {
    const scheduleList = document.querySelector('.schedule-list');
    const daySchedules = schedules[dateString] || [];

    // 날짜 포맷팅
    const [year, month, day] = dateString.split('-');
    const dateObj = new Date(year, month - 1, day);
    const formattedDate = `${year}년 ${month}월 ${day}일 (${['일', '월', '화', '수', '목', '금', '토'][dateObj.getDay()]})`;

    if (daySchedules.length === 0) {
        scheduleList.innerHTML = `
            <h3 style="margin-bottom: 20px; color: #333;">${formattedDate}</h3>
            <div class="no-schedule">
                <div class="no-schedule-icon">📅</div>
                <p>등록된 일정이 없습니다.<br>새로운 일정을 추가해보세요!</p>
            </div>
            <button class="add-btn" onclick="openScheduleModal()">새 일정 추가</button>
        `;
    } else {
        let scheduleHtml = `<h3 style="margin-bottom: 20px; color: #333;">${formattedDate}</h3>`;

        // 시간순으로 정렬
        daySchedules.sort((a, b) => a.time.localeCompare(b.time));

        daySchedules.forEach(schedule => {
            scheduleHtml += `
                <div class="schedule-item" data-schedule-id="${schedule.id}">
                    <div class="schedule-time">${schedule.time}</div>
                    <div class="schedule-content">
                        <h4>${schedule.title}</h4>
                        ${schedule.description ? `<p>${schedule.description}</p>` : ''}
                    </div>
                    <div class="schedule-actions">
                        <button class="edit-btn" onclick="editSchedule(${schedule.id})">✏️</button>
                        <button class="delete-btn" onclick="deleteSchedule(${schedule.id})">🗑️</button>
                    </div>
                </div>
            `;
        });

        scheduleHtml += `<button class="add-btn" onclick="openScheduleModal()">새 일정 추가</button>`;
        scheduleList.innerHTML = scheduleHtml;
    }
}

// 일정 수정
function editSchedule(scheduleId) {
    // 해당 일정 찾기
    let targetSchedule = null;
    let targetDate = null;

    for (const date in schedules) {
        const schedule = schedules[date].find(s => s.id === scheduleId);
        if (schedule) {
            targetSchedule = schedule;
            targetDate = date;
            break;
        }
    }

    if (!targetSchedule) {
        alert('일정을 찾을 수 없습니다.');
        return;
    }

    // 모달에 기존 데이터 채우기
    document.getElementById('scheduleTime').value = targetSchedule.time;
    document.getElementById('scheduleTitle').value = targetSchedule.title;
    document.getElementById('scheduleDescription').value = targetSchedule.description;

    // 수정 모드로 설정
    const modal = document.getElementById('scheduleModal');
    modal.dataset.editMode = 'true';
    modal.dataset.editId = scheduleId;
    modal.dataset.editDate = targetDate;

    // 모달 제목 변경
    modal.querySelector('.modal-header h2').textContent = '일정 수정';
    modal.querySelector('.btn-confirm').textContent = '일정 수정';

    openScheduleModal();
}

// 일정 삭제
function deleteSchedule(scheduleId) {
    if (!confirm('정말로 이 일정을 삭제하시겠습니까?')) {
        return;
    }

    // 해당 일정 찾아서 삭제
    for (const date in schedules) {
        const index = schedules[date].findIndex(s => s.id === scheduleId);
        if (index !== -1) {
            schedules[date].splice(index, 1);
            updateScheduleList(date);

            // 서버에서도 삭제 (실제 구현 시)
            deleteScheduleFromServer(scheduleId);

            showNotification('일정이 삭제되었습니다.');
            break;
        }
    }
}

// 서버에 일정 저장 (실제 구현 시 사용)
function saveScheduleToServer(schedule) {
    // 실제 구현 예시
    /*
    fetch('/api/schedules', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(schedule)
    })
    .then(response => response.json())
    .then(data => {
        console.log('일정 저장 성공:', data);
    })
    .catch(error => {
        console.error('일정 저장 실패:', error);
    });
    */
}

// 서버에서 일정 삭제 (실제 구현 시 사용)
function deleteScheduleFromServer(scheduleId) {
    // 실제 구현 예시
    /*
    fetch(`/api/schedules/${scheduleId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            console.log('일정 삭제 성공');
        }
    })
    .catch(error => {
        console.error('일정 삭제 실패:', error);
    });
    */
}

// 알림 메시지 표시
function showNotification(message) {
    // 기존 알림이 있으면 제거
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        font-size: 14px;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.3s ease;
    `;

    document.body.appendChild(notification);

    // 애니메이션
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateY(0)';
    }, 10);

    // 3초 후 제거
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateY(-20px)';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// 달력 날짜 클릭 이벤트 수정 (기존 함수 내용 대체)
function handleDateClick(year, month, day) {
    // 이전에 선택된 날짜 스타일 제거
    const previousSelected = document.querySelector('.calendar-day.selected');
    if (previousSelected) {
        previousSelected.classList.remove('selected');
    }

    // 현재 클릭된 날짜에 선택 스타일 추가
    event.target.classList.add('selected');

    // 선택된 날짜 설정
    selectedDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

    // 해당 날짜의 일정 표시
    updateScheduleList(selectedDate);
}

// 모달 외부 클릭시 닫기
document.addEventListener('click', function(event) {
    const modal = document.getElementById('scheduleModal');
    if (event.target === modal) {
        closeScheduleModal();
    }
});

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const modal = document.getElementById('scheduleModal');
        if (modal && modal.style.display === 'flex') {
            closeScheduleModal();
        }
    }
});


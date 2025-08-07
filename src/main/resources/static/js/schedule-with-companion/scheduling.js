// 메신저 관련 전역 변수
let isMessengerOpen = false;
let stompClient = null;
let chattingRoomId = null;
let currentUser = null;
let roomId = null;
// 날짜 및 일정 관련 전역 변수
let selectedDate = null; // 선택된 날짜
let schedules = {}; // 일정 데이터 저장소 (날짜별)
let connectedUsers = [];
let travelStartDate = null;
let travelEndDate = null;


document.addEventListener('DOMContentLoaded', function () {
    const chatDataElement = document.getElementById('chatData');
    if (!chatDataElement) return;

    // 초기 변수 세팅
    const travelStartStr = chatDataElement.dataset.travelStart;
    const travelEndStr = chatDataElement.dataset.travelEnd;
    travelStartDate = new Date(travelStartStr);
    travelEndDate = new Date(travelEndStr);
    chattingRoomId = parseInt(chatDataElement.dataset.roomId);
    currentUser = chatDataElement.dataset.currentUser;
    roomId = chattingRoomId;

    const wrapper = document.querySelector(".time-input-wrapper");
    const timeInput = document.getElementById("scheduleTime");

    if (wrapper && timeInput) {
        wrapper.addEventListener("click", function () {
            timeInput.showPicker(); // 최신 브라우저 지원
        });
    }

    console.log("chattingRoomId:", chattingRoomId);
    console.log("currentUser:", currentUser);

    updateUserInterface();
    connectWebSocket();

    // 달력 렌더링 관련
    const calendarGrid = document.getElementById("calendarGrid");
    const calendarHeader = document.querySelector(".calendar-header h3");

    let currentDate = new Date();
    let currentYear = currentDate.getFullYear();
    let currentMonth = currentDate.getMonth();

    function getDayElement(year, month, day) {
        const allDays = document.querySelectorAll(".calendar-day.date");
        for (const d of allDays) {
            if (
                parseInt(d.textContent) === day &&
                !d.classList.contains("empty") &&
                !d.classList.contains("disabled")
            ) {
                return d;
            }
        }
        return null;
    }

    function renderCalendar(year, month) {
        const allDays = calendarGrid.querySelectorAll(".calendar-day.date");
        allDays.forEach(day => day.remove());

        calendarHeader.textContent = `${year}년 ${month + 1}월`;

        const firstDay = new Date(year, month, 1).getDay();
        const lastDate = new Date(year, month + 1, 0).getDate();

        for (let i = 0; i < firstDay; i++) {
            const emptyDiv = document.createElement("div");
            emptyDiv.className = "calendar-day date empty";
            calendarGrid.appendChild(emptyDiv);
        }

        for (let day = 1; day <= lastDate; day++) {
            const dayDiv = document.createElement("div");
            dayDiv.className = "calendar-day date";
            dayDiv.textContent = day;

            // 현재 날짜를 yyyy-MM-dd 형식 문자열로 변환
            const currentDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

            if (travelStartStr && travelEndStr &&
                (currentDateStr < travelStartStr || currentDateStr > travelEndStr)) {
                // 날짜가 범위 밖이면 비활성화
                dayDiv.classList.add("disabled");
                dayDiv.style.pointerEvents = 'none';
                dayDiv.style.opacity = 0.3;
            } else {
                // 선택 가능 날짜
                if (travelStartStr && travelEndStr &&
                    (currentDateStr < travelStartStr || currentDateStr > travelEndStr)) {
                    // 날짜가 범위 밖이면 비활성화
                    dayDiv.classList.add("disabled");
                    dayDiv.style.pointerEvents = 'none';
                    dayDiv.style.opacity = 0.3;
                } else {
                    // 선택 가능 날짜
                    dayDiv.addEventListener("click", () => {
                        handleDateClick(year, month, day, dayDiv);
                    });
                }


            }

            calendarGrid.appendChild(dayDiv);
        }
    }

    renderCalendar(currentYear, currentMonth);

    fetch(`/api/schedules/room/${roomId}`)
        .then(response => response.json())
        .then(data => {
            data.forEach(schedule => {
                const date = schedule.travelStartDate.split('T')[0];
                if (!schedules[date]) schedules[date] = [];
                schedules[date].push(schedule);
            });

            console.log('전체 일정 데이터:', schedules);
            updateScheduleListAll();
        })
        .catch(error => {
            console.error("일정 데이터 로드 실패:", error);
        });

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

    function setInitialSelectedDate() {
        if (!travelStartDate) return;

        const year = travelStartDate.getFullYear();
        const month = travelStartDate.getMonth();
        const day = travelStartDate.getDate();

        currentYear = year;
        currentMonth = month;

        renderCalendar(year, month); // 초기 렌더링 다시

        const initialElement = getDayElement(year, month, day);
        handleDateClick(year, month, day, initialElement);
        setTimeout(() => {
            const allDays = document.querySelectorAll(".calendar-day.date");
            allDays.forEach(d => {
                if (
                    parseInt(d.textContent) === day &&
                    !d.classList.contains("empty") &&
                    !d.classList.contains("disabled")
                ) {
                    d.classList.add("selected");
                }
            });
        }, 0);
    }

    setInitialSelectedDate();

    // 현재 날씨
    fetch('/api/weather/current')
        .then(res => res.json())
        .then(renderCurrentWeather)
        .catch(err => console.error('현재 날씨 불러오기 실패:', err));

    // 3일 예보
    fetch('/api/weather/daily')
        .then(res => res.json())
        .then(renderDailyForecast)
        .catch(err => console.error('3일 예보 불러오기 실패:', err));
});



// 사용자 인터페이스 업데이트
function updateUserInterface() {
    // 채팅 헤더에 현재 사용자 표시
    const profileInfo = document.querySelector('.profile-info h3');
    if (profileInfo) {
        profileInfo.textContent = `채팅방 - ${currentUser}`;
    }
}

// // 메신저 초기화
// function initializeMessenger() {
//     const messengerWidget = document.getElementById('messengerWidget');
//     const floatingBtn = document.getElementById('messengerFloatingBtn');
//
//     // 초기에는 메신저 숨김
//     if (messengerWidget) {
//         messengerWidget.style.display = 'none';
//     }
//
//     // 플로팅 버튼 표시
//     if (floatingBtn) {
//         floatingBtn.style.display = 'flex';
//     }
//
//     // 채팅 입력창 엔터키 이벤트
//     const chatInput = document.getElementById('chatInput');
//     if (chatInput) {
//         chatInput.addEventListener('keypress', function(e) {
//             if (e.key === 'Enter' && !e.shiftKey) {
//                 e.preventDefault();
//                 sendMessage();
//             }
//         });
//     }
// }

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
                <img th:src="@{/image/schedule-with-companion/label-logo.jpg}" alt="${sender}">
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


// WebSocket 연결 설정
function connectWebSocket() {
    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 연결 성공: ' + frame);

        // 채팅방 메시지 구독
        stompClient.subscribe(`/sub/chatroom/${chattingRoomId}`, function (message) {
            const chatMessage = JSON.parse(message.body);
            addReceivedMessage(chatMessage);
        });

        // 스케줄링 접속자 목록 구독
        stompClient.subscribe(`/sub/schedule/${roomId}`, function (message) {
            console.log("[서버에서 수신함]", message.body);
            const onlineUsers = JSON.parse(message.body);
            updateSchedulingOnlineUsers(onlineUsers);
        });

        stompClient.subscribe(`/sub/schedule/room/${roomId}`, function (message) {
            const payload = JSON.parse(message.body);
            const type = payload.type;
            const data = payload.data;

            switch (type) {
                case 'CREATE':
                    addScheduleToUI(data);
                    break;
                case 'UPDATE':
                    updateScheduleInUI(data);
                    break;
                case 'DELETE':
                    removeScheduleFromUI(data);
                    break;
            }
        })

        // 입장 메시지 전송
        sendEnterMessage();
        sendSchedulingEnterMessage();

    }, function (error) {
        console.error('WebSocket 연결 실패:', error);
        setTimeout(connectWebSocket, 5000); // 재연결 시도
    });
}

function addScheduleToUI(data) {
    const date = data.travelStartDate.split('T')[0];

    if (!schedules[date]) schedules[date] = [];

    const isDuplicate = schedules[date].some(s => s.scheduleId === data.scheduleId);
    if (!isDuplicate) {
        schedules[date].push(data);
        updateScheduleListAll();
    }
}

function updateScheduleInUI(updatedSchedule) {
    if (!updatedSchedule.travelStartDate) {
        console.warn("travelStartDate가 없습니다. UI 업데이트 생략");
        return;
    }

    const newDate = updatedSchedule.travelStartDate.split('T')[0];

    // 모든 날짜 그룹을 순회하면서 일정 ID를 가진 항목을 찾음
    for (const date in schedules) {
        const index = schedules[date].findIndex(item => item.scheduleId === updatedSchedule.scheduleId);
        if (index !== -1) {
            // 날짜가 변경되었으면 기존에서 제거하고 새 날짜에 추가
            if (date !== newDate) {
                const removed = schedules[date].splice(index, 1)[0];
                if (schedules[date].length === 0) delete schedules[date];

                if (!schedules[newDate]) schedules[newDate] = [];
                schedules[newDate].push(updatedSchedule);
            } else {
                // 같은 날짜라면 기존 데이터 교체
                schedules[date][index] = updatedSchedule;
            }

            updateScheduleListAll();
            return;
        }
    }

    // 못 찾았으면 새로 추가 (예외 처리)
    if (!schedules[newDate]) schedules[newDate] = [];
    schedules[newDate].push(updatedSchedule);
    updateScheduleListAll();
}

function removeScheduleFromUI(scheduleId) {
    for (const date in schedules) {
        const index = schedules[date].findIndex(s => s.scheduleId === scheduleId);
        if (index !== -1) {
            schedules[date].splice(index, 1);
            if (schedules[date].length === 0) delete schedules[date];
            break;
        }
    }

    updateScheduleListAll();
}

function updateSchedulingOnlineUsers(users) {
    connectedUsers = users; // 실제 리스트 업데이트

    const onlineUsersElement = document.getElementById('onlineUsers');
    const chatRoomElement = document.getElementById('chatRoomUsers');
    const onlineCountElement = document.getElementById('onlineCount');

    if (onlineUsersElement) {
        onlineUsersElement.textContent = users.join(', ');
    }

    if (onlineCountElement) {
        onlineCountElement.textContent = `${users.length}명 온라인`;
    }

    if (chatRoomElement) {
        chatRoomElement.textContent = `현재 접속자: ${users.join(', ')}`;
    }

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

function sendSchedulingEnterMessage() {
    if (stompClient && stompClient.connected) {
        const schedulingEnterMsg = {
            type: 'ENTER',
            roomId: roomId,
            sender: currentUser
        };
        stompClient.send("/pub/schedule/enter", {}, JSON.stringify(schedulingEnterMsg));
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

    const modal = document.getElementById('scheduleModal');
    modal.dataset.editMode = 'false';
    modal.dataset.editId = '';
    modal.dataset.editDate = '';

    modal.querySelector('.modal-header h2').textContent = '새 일정 추가';
    modal.querySelector('.btn-confirm').textContent = '일정 추가';
}

// 모달에서 일정 추가하기
function addScheduleFromModal() {
    const time = document.getElementById('scheduleTime').value;
    const title = document.getElementById('scheduleTitle').value;
    const description = document.getElementById('scheduleDescription').value;

    if (!time || !title.trim()) {
        alert('시간과 일정 제목을 입력해주세요.');
        return;
    }

    if (!selectedDate) {
        const today = new Date();
        selectedDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    }

    const modal = document.getElementById('scheduleModal');
    const isEditMode = modal.dataset.editMode === 'true';

    const scheduleData = {
        roomId: roomId,
        travelStartDate: `${selectedDate}T${time}`,
        scheduleTitle: title.trim(),
        scheduleDescription: description.trim()
    };

    if (isEditMode) {
        const scheduleId = modal.dataset.editId;
        updateScheduleToServer(scheduleId, scheduleData);
    } else {
        saveScheduleToServer(scheduleData);
    }

    // 모달 닫기 및 초기화
    closeScheduleModal();
}

// 전체 일정 조회
function updateScheduleListAll() {
    const listContainer = document.querySelector('.schedule-list');
    listContainer.innerHTML = '';

    // 정렬된 날짜 기준 전체 일정 표시
    const sortedDates = Object.keys(schedules).sort();

    if (sortedDates.length === 0) {
        listContainer.innerHTML = `
            <h3 style="margin-bottom: 20px; color: #333;">등록된 일정이 없습니다</h3>
            <button class="add-btn" onclick="openScheduleModal()">새 일정 추가</button>
        `;
        return;
    }

    sortedDates.forEach(date => {
        const dateSection = document.createElement('div');
        dateSection.classList.add('date-section');

        const dateHeader = document.createElement('h3');
        dateHeader.textContent = formatDisplayDate(date);
        dateSection.appendChild(dateHeader);

        // ⬇️ 시간 기준으로 정렬 추가
        const sortedSchedules = schedules[date].sort((a, b) => {
            return new Date(a.travelStartDate) - new Date(b.travelStartDate);
        });

        sortedSchedules.forEach(schedule => {
            const scheduleItem = document.createElement('div');
            scheduleItem.classList.add('schedule-item');
            scheduleItem.innerHTML = `
                <div class="time-box">${formatTime(schedule.travelStartDate)}</div>
                <div class="schedule-main">
                    <div class="schedule-header">
                        <strong class="schedule-title">${schedule.scheduleTitle}</strong>
                        <div class="actions">
                            <button class="edit-btn" onclick="editSchedule(${schedule.scheduleId})">✏️</button>
                            <button class="delete-btn" onclick="deleteSchedule(${schedule.scheduleId})">🗑️</button>
                        </div>
                    </div>
                    <p class="schedule-description">${schedule.scheduleDescription || ''}</p>
                </div>
            `;
            dateSection.appendChild(scheduleItem);
        });

        listContainer.appendChild(dateSection);
    });

    // "새 일정 추가" 버튼은 맨 아래 고정
    const addBtn = document.createElement('button');
    addBtn.classList.add('add-btn');
    addBtn.textContent = '새 일정 추가';
    addBtn.onclick = openScheduleModal;
    listContainer.appendChild(addBtn);
}

function fetchAndRenderAllSchedules() {
    fetch(`/api/schedules/room/${roomId}`) // roomId는 전역에 선언되어 있어야 함
        .then(response => response.json())
        .then(data => {
            // 기존 일정 초기화
            schedules = {};

            data.forEach(schedule => {
                const date = schedule.travelStartDate.split('T')[0]; // yyyy-MM-dd 추출
                if (!schedules[date]) schedules[date] = [];
                schedules[date].push(schedule);
            });

            console.log('전체 일정 불러오기 성공:', schedules);
            updateScheduleListAll(); // 렌더링 함수 호출
        })
        .catch(error => {
            console.error("일정 데이터 로드 실패:", error);
        });
}

function handleScheduleCreated(newSchedule) {
    const date = newSchedule.travelStartDate.split('T')[0];

    if (!schedules[date]) {
        schedules[date] = [];
    }

    schedules[date].push(newSchedule);

    updateScheduleListAll(); // 전체 리스트 다시 렌더링
}

function formatTime(isoString) {
    const time = new Date(isoString);
    return time.toTimeString().slice(0, 5); // "HH:MM"
}

function formatDisplayDate(yyyyMMdd) {
    const date = new Date(yyyyMMdd);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'short'
    }); // 예: 2025년 08월 08일 (금)
}


// 일정 수정
function editSchedule(scheduleId) {
    let targetSchedule = null;
    let targetDate = null;

    for (const date in schedules) {
        const schedule = schedules[date].find(s => s.scheduleId === scheduleId);
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

    const localTime = new Date(targetSchedule.travelStartDate);
    const hours = String(localTime.getHours()).padStart(2, '0');
    const minutes = String(localTime.getMinutes()).padStart(2, '0');
    document.getElementById('scheduleTime').value = `${hours}:${minutes}`;
    document.getElementById('scheduleTitle').value = targetSchedule.scheduleTitle;
    document.getElementById('scheduleDescription').value = targetSchedule.scheduleDescription;

    const modal = document.getElementById('scheduleModal');
    modal.dataset.editMode = 'true';
    modal.dataset.editId = scheduleId;
    modal.dataset.editDate = targetDate;

    modal.querySelector('.modal-header h2').textContent = '일정 수정';
    modal.querySelector('.btn-confirm').textContent = '일정 수정';

    openScheduleModal();
}

// 일정 삭제
function deleteSchedule(scheduleId) {
    if (!confirm('정말로 이 일정을 삭제하시겠습니까?')) return;

    for (const date in schedules) {
        const index = schedules[date].findIndex(s => s.scheduleId === scheduleId);
        if (index !== -1) {
            schedules[date].splice(index, 1);
            if (schedules[date].length === 0) delete schedules[date]; // 해당 날짜도 삭제

            deleteScheduleFromServer(scheduleId); // 실제 서버 요청
            updateScheduleListAll(); // 다시 렌더링
            showNotification('일정이 삭제되었습니다.');
            break;
        }
    }
}

// 서버에 일정 저장
function saveScheduleToServer(schedule) {
    fetch(`/api/schedules/room/${roomId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(schedule)
    })
        .then(response => {
            if (!response.ok) throw new Error("서버 응답 실패");
            return response.json();
        })
        .then(data => {
            console.log('일정 저장 성공:', data);
        })
        .catch(error => {
            console.error('일정 저장 실패:', error);
            alert('일정 저장 중 오류가 발생했습니다.');
        });

}

function updateScheduleToServer(scheduleId, updatedSchedule) {
    fetch(`/api/schedules/${scheduleId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedSchedule)
    })
        .then(response => {
            if (!response.ok) throw new Error("서버 응답 실패");
            return response.json();
        })
        .then(data => {
            console.log('일정 수정 성공:', data);
            fetchAndRenderAllSchedules(); // 전체 다시 불러오기
            showNotification('일정이 수정되었습니다.');
        })
        .catch(error => {
            console.error('일정 수정 실패:', error);
            alert('일정 수정 중 오류가 발생했습니다.');
        });
}

// 서버에서 일정 삭제
function deleteScheduleFromServer(scheduleId) {
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
function handleDateClick(year, month, day, element) {
    const previousSelected = document.querySelector('.calendar-day.selected');
    if (previousSelected) {
        previousSelected.classList.remove('selected');
    }

    element.classList.add('selected');

    selectedDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
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



// 현재 날씨 랜더링
function renderCurrentWeather(data) {
    document.querySelector('.weather-icon').textContent = getWeatherEmoji(data.weather);
    document.querySelector('.weather-temp').textContent = `${Math.round(data.temperature)}°C`;
    document.querySelector('.weather-current p:nth-of-type(1)').textContent = data.description;
    document.querySelector('.weather-current p:nth-of-type(2)').textContent = `서울 · ${formatDate(data.date)}`;
    document.querySelector('.weather-current p:nth-of-type(3)').textContent = `습도 ${data.humidity}% · 바람 ${data.windSpeed ?? 2}m/s`;
}


// 일일 날씨 랜더링
function renderDailyForecast(forecasts) {
    const forecastItems = document.querySelectorAll('.forecast-item');
    forecasts.slice(0, 3).forEach((f, i) => {
        const item = forecastItems[i];
        item.querySelector('strong').textContent = formatDateWithWeekday(f.date); // 예: "8월 6일 (화)"
        item.querySelector('p').textContent = f.description;
        item.querySelector('span').textContent = getWeatherEmoji(f.weather);
        item.querySelectorAll('p')[1].innerHTML = `<strong>${Math.round(f.maxTemp)}°C</strong> / ${Math.round(f.minTemp)}°C`;
    });
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function formatDateWithWeekday(dateStr) {
    const date = new Date(dateStr);
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return `${date.getMonth() + 1}월 ${date.getDate()}일 (${days[date.getDay()]})`;
}

function getWeatherEmoji(main) {
    switch (main) {
        case 'Clear': return '☀️';
        case 'Clouds': return '⛅';
        case 'Rain': return '🌧️';
        case 'Snow': return '❄️';
        case 'Thunderstorm': return '⛈️';
        default: return '🌤️';
    }
}

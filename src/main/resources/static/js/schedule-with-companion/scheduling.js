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

// 채팅 프로필
const DEFAULT_AVATAR = '/image/creatingRoom/landscape-placeholder-svgrepo-com.svg';
const AVATAR_CACHE = new Map();

let prevUsers = new Set();

const SEEN_JOIN_KEY  = (id) => `room:${id}:seenJoins`;
const SEEN_LEAVE_KEY = (id) => `room:${id}:seenLeaves`;


document.addEventListener('DOMContentLoaded', function () {
    const chatDataElement = document.getElementById('chatData');
    if (!chatDataElement) return;

    if (window.__chatBooted) return;
    window.__chatBooted = true;

    // 초기 변수 세팅
    const travelStartStr = chatDataElement.dataset.travelStart;
    const travelEndStr = chatDataElement.dataset.travelEnd;
    travelStartDate = new Date(travelStartStr);
    travelEndDate = new Date(travelEndStr);
    chattingRoomId = parseInt(chatDataElement.dataset.roomId);
    currentUser = chatDataElement.dataset.currentUser;
    roomId = chattingRoomId;

    window.destName = chatDataElement.dataset.destName || '';
    const destLat = parseFloat(chatDataElement.dataset.destLat);
    const destLon = parseFloat(chatDataElement.dataset.destLon);

    console.log('destName:', destName);


    const wrapper = document.querySelector(".time-input-wrapper");
    const timeInput = document.getElementById("scheduleTime");

    if (wrapper && timeInput) {
        wrapper.addEventListener("click", function () {
            timeInput.showPicker(); // 최신 브라우저 지원
        });
    }

    console.log("chattingRoomId:", chattingRoomId);
    console.log("currentUser:", currentUser);

    // updateUserInterface();
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

    console.log('[weather] roomId =', roomId);

    const fetchJSON = (url) =>
        fetch(url).then(async (r) => {
            const text = await r.text();
            if (!r.ok) {
                console.error('[weather] HTTP', r.status, '->', text);
                throw new Error(`HTTP ${r.status}`);
            }
            try { return JSON.parse(text); }
            catch (e) {
                console.error('[weather] JSON parse error:', text);
                throw e;
            }
        });

    const coordQuery = (!isNaN(destLat) && !isNaN(destLon))
        ? `lat=${destLat}&lon=${destLon}`
        : `roomId=${roomId}`;

    fetchJSON(`/api/weather/current?${coordQuery}`)
        .then(data => {
            console.log('[weather][current] ↩︎', data);
            renderCurrentWeather(data);
        })
        .catch(err => console.error('현재 날씨 불러오기 실패:', err));


    fetchJSON(`/api/weather/daily?${coordQuery}`)
        .then(list => {
            console.log('[weather][daily] ↩︎', list);
            renderDailyForecast(list);
        })
        .catch(err => console.error('3일 예보 불러오기 실패:', err));

});



function loadSeenSet(key) {
    try { return new Set(JSON.parse(localStorage.getItem(key) || '[]')); }
    catch { return new Set(); }
}
function saveSeenSet(key, set) {
    try { localStorage.setItem(key, JSON.stringify([...set])); } catch {}
}

function shouldShowOnce(kind, roomId, nickname) {
    const key = kind === 'join' ? SEEN_JOIN_KEY(roomId) : SEEN_LEAVE_KEY(roomId);
    const seen = loadSeenSet(key);
    if (seen.has(nickname)) return false;
    seen.add(nickname);
    saveSeenSet(key, seen);
    return true;
}

// “입장했습니다/퇴장했습니다” 메시지에서 닉네임 뽑기
function parseSystemName(msg='') {
    // 예) "jacyo님이 입장했습니다." / "수민님이 퇴장했습니다"
    const m = msg.match(/^(.+?)님이\s*(입장했|퇴장했)/);
    return m ? m[1] : null;
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
    const { sender, message, sendTime, senderProfileImage } = chatMessageResponse;
    if (sender && senderProfileImage) AVATAR_CACHE.set(sender, senderProfileImage);

    const isCurrentUser = chatMessageResponse.sender === currentUser;
    const timeString = new Date(sendTime).toLocaleTimeString('ko-KR', { hour:'2-digit', minute:'2-digit' });

    // 시스템 메시지 필터링
    if (message.includes('입장했습니다') || message.includes('퇴장했습니다')) {
        const name = parseSystemName(message);
        if (!name) return; // 포맷 불일치 시 안전하게 무시

        // ▶ 닉네임당 1회만 노출 (방 별)
        const kind = message.includes('입장') ? 'join' : 'leave';
        if (shouldShowOnce(kind, chattingRoomId, name)) {
            addSystemMessage(message);
        }
        return;
    }

    // 일반 메시지
    addMessageToUI(chatMessageResponse.sender, message, isCurrentUser, timeString);
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

        if (shouldShowOnce('join', chattingRoomId, username)) {
            addSystemMessage(`${username}님이 접속했습니다.`);
        }
    }
}

// 사용자 나감
function userLeft(username) {
    const idx = connectedUsers.indexOf(username);
    if (idx > -1) {
        connectedUsers.splice(idx, 1);
        updateOnlineUsers();

        if (shouldShowOnce('leave', chattingRoomId, username)) {
            addSystemMessage(`${username}님이 나갔습니다.`);
        }
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

let isConnecting = false;
let closedByClient = false;


// WebSocket 연결 설정
function connectWebSocket() {
    if (stompClient?.connected || isConnecting) return;
    isConnecting = true; closedByClient = false;

    const socket = new SockJS('/ws/chat', null, { transports: ['websocket'] });
    stompClient = Stomp.over(socket);

    // 디버그 로그 끄기(원하면 남겨두세요)
    stompClient.debug = () => {};

    // 하트비트(브로커 설정과 맞추세요. 예: 10초)
    stompClient.heartbeat.outgoing = 10000;
    stompClient.heartbeat.incoming = 10000;

    stompClient.connect({},
        () => {
            isConnecting = false;
            console.log('[WS] connected');

            // 입장 직후 1회 내려오는 "개인 히스토리" 구독
            stompClient.subscribe('/user/queue/chat-history', (frame) => {
                const history = JSON.parse(frame.body); // 배열(오래된→최신)
                renderHistory(history);
            });

            // 채팅방 접속자 목록 브로드캐스트 구독
            stompClient.subscribe(`/sub/chatroom/${chattingRoomId}/users`, (message) => {
                const users = JSON.parse(message.body);       // ["닉1", "닉2", ...]
                const current = new Set(users);

                // 1) 최초 스냅샷은 알림 없이 기준만 잡기
                if (prevUsers.size === 0) {
                    prevUsers = new Set(users);
                    connectedUsers = users;
                    updateOnlineUsers();
                    return;
                }

                // 2) diff 로 추가/퇴장 감지
                const joined = users.filter(u => !prevUsers.has(u));
                const left   = [...prevUsers].filter(u => !current.has(u));

                // 3) 감지된 사용자에 대해 알림(once 필터는 userJoined/Left 내부에서 이미 적용)
                joined.forEach(u => userJoined(u));
                left.forEach(u => userLeft(u));

                // 4) 스냅샷 업데이트
                prevUsers = current;
            });


            // 구독은 여기서만 1회
            stompClient.subscribe(`/sub/chatroom/${chattingRoomId}`, (msg) => {
                addReceivedMessage(JSON.parse(msg.body));
            });

            // 접속자 목록(인터셉터 체크 없이 쓰는 공개 채널)
            stompClient.subscribe(`/sub/schedule/${roomId}`, (message) => {
                updateSchedulingOnlineUsers(JSON.parse(message.body));
            });

            // 일정 변경 브로드캐스트(인터셉터 대상: /sub/schedule/room/)
            stompClient.subscribe(`/sub/schedule/room/${roomId}`, (message) => {
                const { type, data } = JSON.parse(message.body);
                if (type === 'CREATE') addScheduleToUI(data);
                if (type === 'UPDATE') updateScheduleInUI(data);
                if (type === 'DELETE') removeScheduleFromUI(data);
            });

            // 입장 알림은 연결 직후 1회만
            sendEnterMessage();
            sendSchedulingEnterMessage();
        },
        (error) => {
            isConnecting = false;
            console.warn('[WS] disconnected:', error);
            // 사용자가 수동으로 끊은 경우가 아니면 재접속
            if (!closedByClient) setTimeout(connectWebSocket, 4000);
        }
    );
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
    prefetchAvatars(users).catch(()=>{});

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


function disconnectWebSocket() {
    try {
        if (stompClient && stompClient.connected) {
            stompClient.send("/pub/chat/message", {}, JSON.stringify({
                type: 'LEAVE', chattingRoomId, sender: currentUser, message: `${currentUser}님이 퇴장했습니다.`
            }));
        }
        navigator.sendBeacon?.('/api/chat/leave', JSON.stringify({
            type: 'LEAVE', chattingRoomId, sender: currentUser, message: `${currentUser}님이 퇴장했습니다.`
        }));
    } catch {}
    if (stompClient && (stompClient.connected || isConnecting)) {
        closedByClient = true;
        try { stompClient.disconnect(() => console.log('[WS] cleanly disconnected')); } catch (_) {}
    }
}

// 페이지 언로드시 WebSocket 연결 해제
window.addEventListener('pagehide', disconnectWebSocket);
window.addEventListener('beforeunload', disconnectWebSocket);

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

async function prefetchAvatars(nicknames = []) {
    const need = nicknames.filter(n => n && !AVATAR_CACHE.has(n));
    if (need.length === 0) return;

    try {
        const res = await fetch('/api/v1/profiles/avatars', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(need) // List<String>
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const data = await res.json(); // { nickname: imageUrl, ... }
        Object.entries(data).forEach(([nick, url]) => {
            AVATAR_CACHE.set(nick, url || DEFAULT_AVATAR);
        });
    } catch (e) {
        need.forEach(n => AVATAR_CACHE.set(n, DEFAULT_AVATAR));
        console.error('prefetchAvatars failed', e);
    }
}

function renderHistory(items) {
    if (!Array.isArray(items) || items.length === 0) return;

    // 서버가 보낸 히스토리에 이미지가 들어있다면 캐시로 선반영
    for (const it of items) {
        if (it.sender && it.senderProfileImage) {
            AVATAR_CACHE.set(it.sender, it.senderProfileImage);
        }
    }

    // 그래도 비어있는 닉네임들은 배치 프리패치 호출
    const need = [...new Set(items
        .filter(it => it.sender && !AVATAR_CACHE.has(it.sender))
        .map(it => it.sender))];
    if (need.length) prefetchAvatars(need).catch(()=>{});

    // 렌더
    for (const it of items) {
        const sender = it.sender ?? '';
        const content = it.message ?? '';
        const t = (typeof it.sentAt === 'number') ? new Date(it.sentAt)
            : (it.sendTime ? new Date(it.sendTime) : new Date());
        const timeString = t.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });

        if (String(it.type).toUpperCase() === 'ENTER' || String(it.type).toUpperCase() === 'LEAVE' ||
            content.includes('입장했습니다') || content.includes('퇴장했습니다')) {
            addSystemMessage(content);
        } else {
            const isCurrentUser = sender === currentUser;
            addMessageToUI(sender, content, isCurrentUser, timeString);
        }
    }
    setTimeout(scrollToBottom, 50);
}

async function getAvatar(nickname) {
    if (!nickname) return DEFAULT_AVATAR;
    if (AVATAR_CACHE.has(nickname)) return AVATAR_CACHE.get(nickname);

    try {
        const res = await fetch('/api/v1/profiles/avatar?nickname=' + encodeURIComponent(nickname));
        if (res.ok) {
            const json = await res.json(); // { nickname, url }
            const url = json.url || DEFAULT_AVATAR;
            AVATAR_CACHE.set(nickname, url);
            return url;
        }
    } catch (e) {
        console.error('getAvatar failed', e);
    }
    AVATAR_CACHE.set(nickname, DEFAULT_AVATAR);
    return DEFAULT_AVATAR;
}

function addMessageToUI(sender, content, isCurrentUser = false, timeString = null) {
    const chatMessages = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${isCurrentUser ? 'user' : ''}`;

    const currentTime = timeString || new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });

    if (isCurrentUser) {
        messageDiv.innerHTML = `
      <div class="message-content">
        ${content}
        <div class="message-time">${currentTime}</div>
      </div>`;
    } else {
        // 아바타는 일단 기본으로 넣고 비동기로 교체
        const avatarUrlPromise = getAvatar(sender);

        messageDiv.innerHTML = `
      <div class="message-avatar">
        <img alt="${sender}">
      </div>
      <div class="message-content">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">
          <strong>${sender}</strong>
          <span class="message-time" style="font-size:11px;color:#999;">${currentTime}</span>
        </div>
        <div>${content}</div>
      </div>`;

        const imgEl = messageDiv.querySelector('.message-avatar img');
        imgEl.src = DEFAULT_AVATAR; // 기본값
        avatarUrlPromise.then(url => { imgEl.src = url; });
    }

    chatMessages.appendChild(messageDiv);
    scrollToBottom();
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
    if (!data) { console.warn('[weather][current] empty'); return; }

    const iconEl = document.querySelector('.weather-icon');
    const tempEl = document.querySelector('.weather-temp');
    const p1 = document.querySelector('.weather-current p:nth-of-type(1)');
    const p2 = document.querySelector('.weather-current p:nth-of-type(2)');
    const p3 = document.querySelector('.weather-current p:nth-of-type(3)');

    if (!iconEl || !tempEl || !p1 || !p2 || !p3) {
        console.warn('[weather][current] elements not found');
        return;
    }

    iconEl.textContent = getWeatherEmoji(data.weather);
    tempEl.textContent = `${Math.round(data.temperature)}°C`;
    p1.textContent = data.description ?? '';
    p2.textContent = `${destName || '여행지'} · ${formatDate(data.date)}`;
    p3.textContent = `습도 ${data.humidity}% · 바람 ${data.windSpeed ?? 2}m/s`; // windSpeed 없으면 2
}



// 일일 날씨 랜더링
function renderDailyForecast(forecasts = []) {
    const items = document.querySelectorAll('.forecast-item');
    if (!items.length) { console.warn('[weather][daily] .forecast-item not found'); return; }
    if (!forecasts.length) { console.warn('[weather][daily] empty'); return; }

    forecasts.slice(0, Math.min(3, items.length)).forEach((f, i) => {
        const item = items[i];
        item.querySelector('strong').textContent = formatDateWithWeekday(f.date);
        item.querySelector('p').textContent = f.description ?? '';
        item.querySelector('span').textContent = getWeatherEmoji(f.weather);
        const pm = item.querySelectorAll('p')[1];
        if (pm) pm.innerHTML = `<strong>${Math.round(f.maxTemp)}°C</strong> / ${Math.round(f.minTemp)}°C`;
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

let map, marker;
let mapInited = false;


function escapeHtml(s = '') {
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function showSection(id) {
    // 1) 탭 active 토글
    document.querySelectorAll('.section-tabs .tab').forEach(t => t.classList.remove('active'));
    const tab = Array.from(document.querySelectorAll('.section-tabs .tab')).find(t => {
        if (id === 'schedule')  return t.textContent.includes('일정');
        if (id === 'weather')   return t.textContent.includes('날씨');
        if (id === 'transport') return t.textContent.includes('교통');
        return false;
    });
    if (tab) tab.classList.add('active');

    // 2) 섹션 표시 토글
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    const section = document.getElementById(id);
    if (section) section.classList.add('active');

    // 3) 교통(지도)
    if (id === 'transport') {
        if (typeof kakao === 'undefined' || !kakao.maps) {
            console.warn('Kakao Maps SDK가 로드되지 않았습니다. (JS 키로 sdk.js 포함 필요)');
            return;
        }
        kakao.maps.load(() => {
            setupSearchUIOnce();
            setupRouteUI();
            if (!mapInited) {
                initKakaoMap();
            } else {
                map.relayout();
                map.setCenter(map.getCenter());
            }
        });
    }
}

window.addEventListener('load', () => {
    const transportActive = document.getElementById('transport')?.classList.contains('active');
    if (transportActive && typeof kakao !== 'undefined' && kakao.maps) {
        kakao.maps.load(() => initKakaoMap());
    }
});

function setupRouteUI() {
    const sInput = document.getElementById('startInput');
    const eInput = document.getElementById('endInput');
    const sRes   = document.getElementById('startResults');
    const eRes   = document.getElementById('endResults');
    const btn    = document.getElementById('routeBtn');
    const swap   = document.getElementById('swapRouteBtn');

    if (!sInput || !eInput || !btn || !swap) return;
    if (btn.dataset.bound === '1') return;     // 중복 방지 (기존 로직)
    btn.dataset.bound = '1';

    // 오토컴플리트(이미 만든 함수 재사용)
    bindAutocomplete(sInput, sRes, (pick) => { startPick = pick; });
    bindAutocomplete(eInput, eRes, (pick) => { endPick   = pick; });

    // 버튼 클릭 바인딩
    btn.addEventListener('click', onRouteClick);
    swap.addEventListener('click', onSwapClick);
}

// 길찾기 swap용 추가
function onSwapClick() {
    const sInput = document.getElementById('startInput');
    const eInput = document.getElementById('endInput');
    if (!sInput || !eInput) return;

    // 입력값 교환
    [sInput.value, eInput.value] = [eInput.value, sInput.value];
    // 선택된 위치 객체도 교환
    [startPick, endPick] = [endPick, startPick];

    if (startPick && endPick) drawRoutePreview(startPick, endPick);
    hideKakaoBtn();
}

function onRouteClick() {
    const sName = document.getElementById('startInput').value.trim();
    const eName = document.getElementById('endInput').value.trim();
    if (!sName || !eName) { alert('출발지와 도착지를 입력해 주세요.'); return; }

    // 좌표 찾아서 프리뷰/교통편 업데이트
    if (!kakao?.maps?.services) return;
    const places = new kakao.maps.services.Places();

    places.keywordSearch(sName, (d1, st1) => {
        if (st1 !== kakao.maps.services.Status.OK || !d1?.[0]) return;
        const s = { name: d1[0].place_name, lon: +d1[0].x, lat: +d1[0].y };

        places.keywordSearch(eName, (d2, st2) => {
            if (st2 !== kakao.maps.services.Status.OK || !d2?.[0]) return;
            const e = { name: d2[0].place_name, lon: +d2[0].x, lat: +d2[0].y };

            startPick = s; endPick = e;
            drawRoutePreview(s, e);
            updateTransportCards(s, e);    // ← 여기만 남김
        });
    });
}
function initKakaoMap() {
    const container = document.getElementById('map');
    if (!container) return;

    const defaultCenter = new kakao.maps.LatLng(37.5665, 126.9780);
    map = new kakao.maps.Map(container, { center: defaultCenter, level: 4 });

    const chatData = document.getElementById('chatData');
    const destName = chatData?.dataset.destName || '';
    const lat = parseFloat(chatData?.dataset.destLat);
    const lon = parseFloat(chatData?.dataset.destLon);

    if (!isNaN(lat) && !isNaN(lon)) {
        setMap(lat, lon, destName);
        mapInited = true;
        return;
    }

    if (destName) {
        const ps = new kakao.maps.services.Places();
        ps.keywordSearch(destName, (data, status) => {
            if (status === kakao.maps.services.Status.OK && data.length > 0) {
                const f = data[0];
                setMap(parseFloat(f.y), parseFloat(f.x), f.place_name || destName);
            } else {
                console.warn('장소 검색 실패 또는 결과 없음:', destName);
            }
            mapInited = true;
        }, { size: 3 });
    } else {
        mapInited = true;
    }

    setupRouteUI();
}

function setMap(lat, lon, title='') {
    const pos = new kakao.maps.LatLng(lat, lon);
    map.setCenter(pos);
    if (marker) marker.setMap(null);
    marker = new kakao.maps.Marker({
        position: pos,
        map,
        title // 브라우저 기본 툴팁만 표시
    });
    setTimeout(() => map.relayout(), 0);
}

// 카카오 검색 결과를 일정으로 저장 (필요 필드 검증 + roomId fallback)
function addFromKakaoResult(doc) {
    const timeEl = document.getElementById('scheduleTime');
    const titleEl = document.getElementById('scheduleTitle');

    const time = timeEl?.value || '';
    const title = (titleEl?.value || '').trim();

    if (!selectedDate) {
        alert('먼저 달력에서 날짜를 선택해주세요.');
        return;
    }
    if (!time) {
        alert('시간을 선택해주세요.');
        return;
    }
    if (!title) {
        alert('일정 제목을 입력해주세요.');
        return;
    }
    if (!doc || !doc.place_name || !doc.x || !doc.y) {
        alert('장소 정보를 확인할 수 없습니다.');
        return;
    }

    // roomId 전역이 없으면 chatData에서 가져오기 (안전장치)
    const rid = (typeof roomId !== 'undefined' && roomId)
        ? roomId
        : parseInt(document.getElementById('chatData')?.dataset.roomId);

    if (!rid) {
        console.error('roomId가 없습니다.');
        alert('방 정보가 유효하지 않습니다.');
        return;
    }

    const scheduleData = {
        roomId: rid,
        travelStartDate: `${selectedDate}T${time}`,
        scheduleTitle: title,
        scheduleDescription: '',
        placeName: doc.place_name,
        placeLat: parseFloat(doc.y), // 위도
        placeLon: parseFloat(doc.x)  // 경도
    };

    saveScheduleToServer(scheduleData);
}


// ===== 장소 검색 UI/로직 =====
let placesService = null;       // kakao.maps.services.Places
let searchDebounceTimer = null; // 디바운스 타이머
let suppressSearch = false;

function ensurePlacesService() {
    if (!placesService) {
        placesService = new kakao.maps.services.Places();
    }
}

function hidePlaceResults() {
    const box = document.getElementById('placeResults');
    if (box) { box.style.display = 'none'; box.innerHTML = ''; }
}

function setupSearchUIOnce() {
    const input = document.getElementById('placeSearchInput');
    const btn = document.getElementById('placeSearchBtn');
    if (!input || !btn) return;
    if (btn.dataset.bound === 'true') return;
    btn.dataset.bound = 'true';

    // 버튼/엔터 → 첫 결과에 포커스(지도 이동)
    btn.addEventListener('click', () => doPlaceSearch(input.value.trim(), { focusFirst: true, hideList: true }));
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') doPlaceSearch(input.value.trim(), { focusFirst: true, hideList: true });
    });

    // 입력 중엔 결과만 갱신(지도는 안 움직임)
    input.addEventListener('input', () => {
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(() => {
            doPlaceSearch(input.value.trim(), { focusFirst: false });
        }, 400);
    });
}

function ensureMapReady(cb) {
    if (map) return cb();
    if (typeof kakao === 'undefined' || !kakao.maps) return;
    kakao.maps.load(() => {
        if (!mapInited) initKakaoMap();
        cb();
    });
}

function doPlaceSearch(query, opts = { focusFirst: false, hideList: false }) {
    const resBox = document.getElementById('placeResults');
    if (!query) { if (resBox) resBox.style.display = 'none'; return; }
    if (typeof kakao === 'undefined' || !kakao.maps) return;

    ensurePlacesService();
    const options = {};
    if (map) options.location = map.getCenter();

    placesService.keywordSearch(query, (data, status) => {
        if (status !== kakao.maps.services.Status.OK || !Array.isArray(data) || data.length === 0) {
            renderSearchResults([]);
            return;
        }

        // 버튼/엔터일 때: 첫 결과 지도 이동 + 리스트 숨김
        if (opts.focusFirst) {
            const d = data[0];
            ensureMapReady(() => setMap(parseFloat(d.y), parseFloat(d.x), d.place_name));
            if (opts.hideList) { hidePlaceResults(); return; }  // ★ 여기서 끝
        }

        // 입력 중인 경우에만 리스트 렌더링
        renderSearchResults(data.slice(0, 8));
    }, options);
}


function renderSearchResults(list) {
    const box = document.getElementById('placeResults');
    if (!box) return;

    if (!list.length) {
        box.innerHTML = `<div style="padding:8px; color:#888;">검색 결과가 없습니다.</div>`;
        box.style.display = 'block';
        return;
    }

    box.innerHTML = list.map((doc, i) => {
        const name = escapeHtml(doc.place_name || '');
        const addr = escapeHtml(doc.road_address_name || doc.address_name || '');
        return `
      <div class="result-item" style="display:flex; align-items:center; justify-content:space-between; gap:8px; padding:8px; border-bottom:1px solid #f3f3f3;">
        <div style="min-width:0;">
          <div style="font-weight:600; margin-bottom:2px;">${i+1}. ${name}</div>
          <div style="font-size:12px; color:#666; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:420px;">${addr}</div>
        </div>
        <div style="flex:0 0 auto; display:flex; gap:6px;">
          <button type="button" class="add-btn" style="padding:6px 10px;" data-act="focus" data-idx="${i}">선택</button>
        </div>
      </div>
    `;
    }).join('');
    box.style.display = 'block';

    // 버튼 핸들링(이벤트 위임)
    box.onclick = (e) => {
        const btn = e.target.closest('button[data-act]');
        if (!btn) return;
        const idx = parseInt(btn.dataset.idx);
        const act = btn.dataset.act;
        const doc = list[idx];
        if (!doc) return;

        if (act === 'focus') {
            // 지도 이동/마커 표시
            const lat = parseFloat(doc.y);
            const lon = parseFloat(doc.x);
            setMap(lat, lon, doc.place_name);

            // 🔹 검색창에 선택한 장소명 넣기
            const searchInput = document.getElementById('placeSearchInput');
            if (searchInput) {
                searchInput.value = doc.place_name || '';
            }

            // 🔹 리스트 숨기기
            hidePlaceResults();
        } else if (act === 'add') {
            addFromKakaoResult(doc);
            hidePlaceResults();
        }
    };
}

// ===== 길찾기 검색 상태 =====
let startPick = null;   // {name, lat, lon}
let endPick   = null;   // {name, lat, lon}
let routeLine = null;   // 미니 프리뷰용 폴리라인

let suppressUntil = 0;                 // 이 시간 전까지는 검색 무시
const SUPPRESS_MS = 300;               // 0.3초면 충분

// === 카카오맵 공용 버튼 제어(전역) ===
let lastStart = null, lastEnd = null;

function showKakaoBtn() {
    const btn = document.getElementById('openKakaoMapBtn');
    if (!btn) return;
    btn.style.display = 'inline-block';
    btn.disabled = false;
    btn.style.opacity = '1';
    btn.style.cursor = 'pointer';
}
function hideKakaoBtn() {
    const btn = document.getElementById('openKakaoMapBtn');
    if (!btn) return;
    btn.style.display = 'none';
}

// 페이지 로드 후 한 번만 버튼 클릭 바인딩
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('openKakaoMapBtn')?.addEventListener('click', () => {
        if (!lastStart || !lastEnd) return;
        const url = buildKakaoRouteUrl(lastStart, lastEnd, 'transit');
        window.open(url, '_blank', 'noopener');
    });

    // 출발/도착 변경 시 버튼 다시 숨김 (선택)
    document.getElementById('startInput')?.addEventListener('input', hideKakaoBtn);
    document.getElementById('endInput')?.addEventListener('input', hideKakaoBtn);
});

function isSuppressed() {
    return Date.now() < suppressUntil;
}

function bindAutocomplete(inputEl, resultsEl, onPick) {
    if (!inputEl || !resultsEl) return;

    // 한글 조합 여부를 따지지 않는다. (space 안 눌러도 바로 뜸)
    // 단, '선택' 직후 억제 시간에는 무시
    inputEl.addEventListener('input', (e) => {
        if (isSuppressed()) return;

        const q = inputEl.value.trim();
        // 반대쪽 리스트 닫기
        document.querySelectorAll('.route-results')?.forEach(bx => {
            if (bx !== resultsEl) { bx.style.display = 'none'; bx.innerHTML = ''; }
        });

        if (!q || !kakao?.maps?.services) { resultsEl.style.display = 'none'; resultsEl.innerHTML=''; return; }

        const places = new kakao.maps.services.Places();
        const opts = (window.map ? { location: map.getCenter() } : {});
        places.keywordSearch(q, (data, status) => {
            if (status !== kakao.maps.services.Status.OK || !data?.length) {
                resultsEl.style.display = 'none'; resultsEl.innerHTML = ''; return;
            }
            resultsEl.innerHTML = data.slice(0, 8).map(d => `
        <div class="result-item" data-x="${d.x}" data-y="${d.y}" data-name="${escapeHtml(d.place_name||'')}">
          <div class="name">${escapeHtml(d.place_name||'')}</div>
          <div class="addr">${escapeHtml(d.road_address_name || d.address_name || '')}</div>
        </div>
      `).join('');
            resultsEl.style.display = 'block';
        }, opts);
    });

    // pointerdown에서 값 세팅 + 리스트 닫기 + 잠깐 억제
    resultsEl.addEventListener('pointerdown', (e) => {
        const item = e.target.closest('.result-item');
        if (!item) return;
        e.preventDefault(); // blur 전에 값 세팅

        const pick = {
            name: item.dataset.name,
            lon : parseFloat(item.dataset.x),
            lat : parseFloat(item.dataset.y),
        };

        suppressUntil = Date.now() + SUPPRESS_MS; // 잠깐 자동검색 억제
        inputEl.value = pick.name || '';
        onPick(pick);
        setMap(pick.lat, pick.lon, pick.name);

        resultsEl.style.display = 'none';
        resultsEl.innerHTML = '';
    });


    // blur 시 살짝 딜레이 후 닫기
    inputEl.addEventListener('blur', () => {
        setTimeout(() => { resultsEl.style.display = 'none'; }, 120);
    });
}

// 간단 폴리라인 프리뷰 + 지도 맞춤
function drawRoutePreview(a, b) {
    if (!map || !a || !b) return;
    if (routeLine) routeLine.setMap(null);
    const path = [
        new kakao.maps.LatLng(a.lat, a.lon),
        new kakao.maps.LatLng(b.lat, b.lon)
    ];
    routeLine = new kakao.maps.Polyline({
        path,
        strokeWeight: 4,
        strokeColor: '#0b6dbe',
        strokeOpacity: 0.8,
        strokeStyle: 'shortdash'
    });
    routeLine.setMap(map);

    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(p => bounds.extend(p));
    map.setBounds(bounds);
}

// 오토컴플리트 바인딩
bindAutocomplete(
    document.getElementById('startInput'),
    document.getElementById('startResults'),
    (pick) => { startPick = pick; }
);
bindAutocomplete(
    document.getElementById('endInput'),
    document.getElementById('endResults'),
    (pick) => { endPick = pick; }
);

// 카카오맵 길찾기 URL (좌표·이름 함께 넘김)
function buildKakaoRouteUrl(s, e, mode = 'car') {
    // 좌표가 확실하면 넣고, 아니면 이름만으로도 길찾기 동작함
    const params = new URLSearchParams({
        sName: s.name, eName: e.name
    });
    if (!isNaN(s.lon) && !isNaN(s.lat)) { params.set('sx', s.lon); params.set('sy', s.lat); }
    if (!isNaN(e.lon) && !isNaN(e.lat)) { params.set('ex', e.lon); params.set('ey', e.lat); }
    if (mode === 'transit') params.set('target', 'transit'); // 자동차면 생략 가능
    return `https://map.kakao.com/?${params.toString()}`;
}

// 도구: 거리(km) 계산
function haversineKm(a, b) {
    const R = 6371;
    const dLat = (b.lat - a.lat) * Math.PI / 180;
    const dLon = (b.lon - a.lon) * Math.PI / 180;
    const la1 = a.lat * Math.PI / 180;
    const la2 = b.lat * Math.PI / 180;
    const h = Math.sin(dLat/2)**2 + Math.cos(la1)*Math.cos(la2)*Math.sin(dLon/2)**2;
    return R * 2 * Math.asin(Math.sqrt(h));
}

// 포맷터
const fmtMin = (m) => (m >= 60 ? `${Math.floor(m/60)}시간 ${m%60}분` : `${m}분`);
const fmtWon = (n) => `₩ ${Number(n || 0).toLocaleString()}`;

//경로 Summary에서 키워드로 추정
function iconFor(r) {
    const s = (r.routeSummary || '').toLowerCase();
    if (s.includes('버스') && s.includes('지하철')) return '🚌';
    if (s.includes('버스')) return '🚌';
    if (s.includes('지하철') || s.includes('전철')) return '🚇';
    return '🚶';
}

// 로딩/에러/빈상태
function renderTransportState(container, type) {
    if (type === 'loading') {
        container.innerHTML = Array.from({length: 2}).map(() => `
      <div class="transport-item" aria-busy="true" style="position:relative;">
        <div class="transport-icon">⌛</div>
        <div class="transport-info">
          <h4 style="height:18px;width:180px;background:rgba(0,87,146,.08);border-radius:8px;margin-bottom:8px;"></h4>
          <p style="height:14px;width:140px;background:rgba(0,87,146,.06);border-radius:6px;"></p>
        </div>
        <div style="position:absolute;inset:0;background:linear-gradient(90deg,transparent,rgba(0,87,146,.08),transparent);animation:mapShimmer 1.8s linear infinite;"></div>
      </div>
    `).join('');
        return;
    }
    if (type === 'empty') {
        container.innerHTML = `
      <div class="transport-item" style="justify-content:center;">
        <div class="transport-info">
          <h4 style="margin:0;color:#1E3A5F;">경로를 찾지 못했습니다.</h4>
          <p style="margin-top:6px;color:#5a7a94;">검색어를 조금 바꾸거나 출발/도착을 다시 선택해 주세요.</p>
        </div>
      </div>`;
        return;
    }
    if (type === 'error') {
        container.innerHTML = `
      <div class="transport-item" style="justify-content:center;border-color:#c33;">
        <div class="transport-icon" style="background:linear-gradient(135deg,#c33,#7a1c1c)">!</div>
        <div class="transport-info">
          <h4 style="margin:0;color:#7a1c1c;">교통 경로 조회 중 오류가 발생했습니다.</h4>
          <p style="margin-top:6px;color:#a54;">잠시 후 다시 시도해 주세요.</p>
        </div>
      </div>`;
    }
}

// 메인: 백엔드 호출 + 렌더
async function updateTransportCards(s, e) {
    const container = document.querySelector('#transport .transport-options #transportList');
    if (!container) return;

    renderTransportState(container, 'loading');
    try {
        const q = new URLSearchParams({ sx:String(s.lon), sy:String(s.lat), ex:String(e.lon), ey:String(e.lat) });
        const res = await fetch(`/api/v1/transport/routes?${q.toString()}`);
        if (!res.ok) throw new Error('ODsay 요청 실패');
        const routes = await res.json();

        if (!Array.isArray(routes) || routes.length === 0) {
            renderTransportState(container, 'empty');
            return;
        }

        // 최신 경로 저장 + 버튼 표시
        lastStart = s;
        lastEnd   = e;
        showKakaoBtn();

        renderTransitOptions(container, routes, s, e);
    } catch (err) {
        console.error(err);
        renderTransportState(container, 'error');
    }
}


function renderTransitOptions(container, routes, s, e) {
    container.innerHTML = routes.map((r, idx) => {
        const segHtml = (r.segments || []).map(seg => `<li>${escapeHtml(seg)}</li>`).join('');
        const transfers = (r.transferCount ?? 0) >= 0 ? `${r.transferCount}회` : '정보없음';

        return `
      <div class="transport-item">
        <div class="transport-icon" style="flex-shrink:0;width:50px;height:50px;display:flex;align-items:center;justify-content:center;font-size:24px;">
          ${iconFor(r)}
        </div>
        <div class="transport-info" style="flex:1;display:flex;flex-direction:column;gap:6px;">
          <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
            <h4 style="margin:0;font-size:16px;font-weight:700;">대중교통 경로 ${idx + 1}</h4>
            <span style="font-size:12px;padding:2px 8px;border-radius:12px;background:rgba(0,87,146,.1);color:#005792;">
              환승 ${transfers}
            </span>
          </div>
          <p style="margin:0;color:#1E3A5F;font-size:14px;">${escapeHtml(r.routeSummary || '')}</p>
          <p style="margin:0;color:#005792;font-weight:600;font-size:14px;">⏱ ${fmtMin(r.totalTime)} · ${fmtWon(r.fare)}</p>
          ${segHtml ? `<ul style="margin:0;padding-left:18px;color:#0a263b;font-size:13px;">${segHtml}</ul>` : ''}
        </div>
      </div>`;
    }).join('');
}

(function initBackNav() {
    if (window.__backNavBound) return;
    window.__backNavBound = true;

    // 쿼리로 들어온 tab 값은 세션에 보관 (fallback 용)
    try {
        const url = new URL(window.location.href);
        const tab = url.searchParams.get('tab');
        if (tab) sessionStorage.setItem('myMatchingActiveTab', tab);
    } catch (_) {}

    // 실제 이동 함수 (HTML의 onclick에서 호출)
    window.goBackToMyMatching = function (e) {
        e?.preventDefault?.();

        // 깔끔한 종료 (선택: 정의돼 있으면 호출)
        try { typeof disconnectWebSocket === 'function' && disconnectWebSocket(); } catch (_) {}

        const ref = document.referrer || '';
        let tab = '';
        try {
            const url = new URL(window.location.href);
            tab = url.searchParams.get('tab') ||
                sessionStorage.getItem('myMatchingActiveTab') || '';
        } catch (_) {}

        // 히스토리가 마이페이지면 그 URL로 (상태/스크롤 복원 가능)
        if (ref.includes('/mypage/my-matching')) {
            window.location.href = ref;
            return false;
        }

        // 아니면 안전하게 기본 경로로 (탭 복원 시도)
        const target = '/mypage/my-matching' + (tab ? `?activeTab=${encodeURIComponent(tab)}` : '');
        window.location.href = target;
        return false;
    };

    // 혹시 onclick을 안 쓰는 버튼도 커버 (아이콘/텍스트 내부 클릭 포함)
    document.addEventListener('click', (ev) => {
        const btn = ev.target.closest('.back-btn, #backToMyMatchingBtn, [data-act="back"]');
        if (btn) return window.goBackToMyMatching(ev);
    });
})();
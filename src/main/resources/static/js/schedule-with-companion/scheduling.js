// 메신저 관련 전역 변수
let isMessengerOpen = false;
let currentUser = generateRandomUser(); // 랜덤 사용자명 생성
let connectedUsers = ['김상우', '노수민']; // 현재 접속자 목록
let stompClient = null; // STOMP 클라이언트
let chattingRoomId = 1; // 채팅방 ID (실제로는 동적으로 설정)

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
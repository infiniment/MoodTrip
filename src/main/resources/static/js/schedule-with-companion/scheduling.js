// 전역 변수
let currentDate = new Date();
let selectedDate = null;
let scheduleData = {}; // 일정 데이터를 저장할 객체
let currentMessengerTab = 'home';
let chatMessages = [];

// 실시간 접속자 관리
let allUsers = ['노수민', '김상우', '김민규', '서유진'];
let onlineUsers = ['김상우', '노수민']; // 현재 접속 중인 사용자들

// 실시간 접속자 업데이트 시뮬레이션
function startRealtimeUpdates() {
    setInterval(() => {
        // 랜덤하게 사용자 접속/퇴장 시뮬레이션
        if (Math.random() < 0.3) { // 30% 확률로 변경
            const randomUser = allUsers[Math.floor(Math.random() * allUsers.length)];
            
            if (onlineUsers.includes(randomUser)) {
                // 사용자 퇴장
                if (onlineUsers.length > 1) { // 최소 1명은 유지
                    onlineUsers = onlineUsers.filter(user => user !== randomUser);
                    updateOnlineUsers();
                    console.log(`${randomUser}님이 퇴장했습니다.`);
                }
            } else {
                // 사용자 접속
                if (onlineUsers.length < allUsers.length) {
                    onlineUsers.push(randomUser);
                    updateOnlineUsers();
                    console.log(`${randomUser}님이 접속했습니다.`);
                }
            }
        }
    }, 8000); // 8초마다 체크
}

// 온라인 사용자 목록 업데이트
function updateOnlineUsers() {
    const onlineUsersElement = document.getElementById('onlineUsers');
    const onlineCountElement = document.getElementById('onlineCount');
    
    if (onlineUsersElement) {
        onlineUsersElement.textContent = onlineUsers.join(', ');
    }
    
    if (onlineCountElement) {
        onlineCountElement.textContent = `${onlineUsers.length}명 온라인`;
        
        // 접속자 수에 따른 색상 변경
        const statusElement = onlineCountElement.parentElement;
        if (onlineUsers.length >= 3) {
            statusElement.style.color = '#FF9800'; // 주황색
        } else if (onlineUsers.length >= 2) {
            statusElement.style.color = '#FF9800'; // 주황색
        } else {
            statusElement.style.color = '#9E9E9E'; // 회색
        }
    }
}

// 탭 전환 기능
function showSection(sectionId) {
    // 모든 탭과 섹션 비활성화
    const tabs = document.querySelectorAll('.tab');
    const sections = document.querySelectorAll('.content-section');
    
    tabs.forEach(tab => tab.classList.remove('active'));
    sections.forEach(section => section.classList.remove('active'));
    
    // 선택된 탭과 섹션 활성화
    event.target.classList.add('active');
    document.getElementById(sectionId).classList.add('active');
}

// 새로 추가된 모달 관련 함수들
function openScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    
    // 첫 번째 입력 필드에 포커스
    setTimeout(() => {
        document.getElementById('scheduleTime').focus();
    }, 300);
}

function closeScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
    
    // 폼 초기화
    document.getElementById('scheduleTime').value = '';
    document.getElementById('scheduleTitle').value = '';
    document.getElementById('scheduleDescription').value = '';
}

function addScheduleFromModal() {
    const time = document.getElementById('scheduleTime').value;
    const title = document.getElementById('scheduleTitle').value;
    const description = document.getElementById('scheduleDescription').value;
    
    // 필수 필드 검증
    if (!time || !title) {
        alert('시간과 제목은 필수 입력사항입니다');
        return;
    }
    
    // 날짜가 선택되지 않았을 때
    if (!selectedDate) {
        alert('먼저 캘린더에서 날짜를 선택해주세요!');
        return;
    }
    
    // 선택된 날짜의 일정 데이터에 새 일정 추가
    const dateKey = `${currentDate.getFullYear()}-${currentDate.getMonth() + 1}-${selectedDate}`;
    
    if (!scheduleData[dateKey]) {
        scheduleData[dateKey] = [];
    }
    
    scheduleData[dateKey].push({
        time: time,
        title: title,
        description: description || ''
    });
    
    // 일정 목록 UI 업데이트
    updateScheduleForDate(selectedDate);
    
    // 모달 닫기
    closeScheduleModal();
    
    console.log('새 일정이 추가되었습니다:', { time, title, description, dateKey });
}

// 메신저 앱 관련 함수들
function openMessenger() {
    const modal = document.getElementById('messengerModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    
    // 기본적으로 HOME 탭을 활성화
    switchMessengerTab('home');
}

function closeMessenger(event) {
    if (event && event.target !== event.currentTarget) return;
    
    const modal = document.getElementById('messengerModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
}

function switchMessengerTab(tabName) {
    // 모든 탭 컨텐츠 숨기기
    document.querySelectorAll('.messenger-tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 모든 네비게이션 아이템 비활성화
    document.querySelectorAll('.nav-item').forEach(nav => {
        nav.classList.remove('active');
    });
    
    // 선택된 탭 활성화
    const tabMap = {
        'home': 'homeTab',
        'messages': 'messagesTab', 
        'settings': 'settingsTab'
    };
    
    document.getElementById(tabMap[tabName]).classList.add('active');
    
    // 해당 네비게이션 아이템 활성화
    const navItems = document.querySelectorAll('.nav-item');
    if (tabName === 'home') navItems[0].classList.add('active');
    else if (tabName === 'messages') navItems[1].classList.add('active');
    else if (tabName === 'settings') navItems[2].classList.add('active');
    
    currentMessengerTab = tabName;
    console.log(`메신저 탭 변경: ${tabName}`);
}

function startChatMode() {
    // HOME 탭을 채팅 모드로 전환
    const homeTab = document.getElementById('homeTab');
    const messengerContent = homeTab.querySelector('.messenger-content');
    
    // 기존 컨텐츠를 채팅 인터페이스로 교체
    messengerContent.innerHTML = `
        <div id="chatMessages" class="chat-messages-container">
            <div class="chat-message company-message">
                <div class="message-avatar">
                    <img src="label-logo.png" alt="무드트립">
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <strong>무드트립</strong>
                        <span class="message-time">방금 전</span>
                    </div>
                    <p>안녕하세요! 무드트립에 문의해주셔서 감사합니다. 어떤 도움이 필요하신가요? 😊</p>
                </div>
            </div>
        </div>
        
        <div class="chat-input-container">
            <input type="text" class="chat-input" placeholder="Enter a message." id="chatInput">
            <button class="send-btn" onclick="sendMessage()">→</button>
        </div>
        
        <div class="auto-reply-notice">
            <span class="clock-icon">⏰</span>
            <span>Back on tomorrow 10:00 AM</span>
        </div>
    `;
    
    // 채팅 입력창에 엔터 키 이벤트 추가
    const newChatInput = document.getElementById('chatInput');
    if (newChatInput) {
        newChatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
        newChatInput.focus(); // 입력창에 포커스
    }
    
    console.log('채팅 모드로 전환되었습니다!');
}

function sendMessage() {
    const chatInput = document.getElementById('chatInput');
    const message = chatInput.value.trim();
    
    if (!message) return;
    
    // 사용자 메시지 추가
    addChatMessage('user', message);
    chatInput.value = '';
    
    // 자동 응답 (실제로는 서버에서 처리)
    setTimeout(() => {
        const responses = [
            "안녕하세요! 무드트립에 문의해주셔서 감사합니다 😊",
            "궁금한 점이 있으시면 언제든지 말씀해주세요!",
            "여행 계획에 도움이 필요하시면 도와드리겠습니다!",
            "더 자세한 상담은 고객센터(1588-0000)로 연락주세요.",
            "좋은 여행 되세요! 🌟",
            "무드트립과 함께 특별한 여행을 만들어보세요! ✈️",
            "어떤 여행지를 계획하고 계신가요?",
            "맞춤형 여행 추천도 가능합니다!",
            "24시간 온라인 상담 서비스를 이용해보세요."
        ];
        
        const randomResponse = responses[Math.floor(Math.random() * responses.length)];
        addChatMessage('company', randomResponse);
    }, 1000);
}

function addChatMessage(sender, message) {
    const messagesContainer = document.getElementById('chatMessages');
    if (!messagesContainer) return;
    
    const messageDiv = document.createElement('div');
    messageDiv.className = 'chat-message';
    
    if (sender === 'user') {
        messageDiv.innerHTML = `
            <div class="message-content" style="margin-left: auto; max-width: 70%;">
                <div class="user-message" style="background: linear-gradient(135deg, #005792 0%, #001A2C 100%);; color: white; padding: 12px 16px; border-radius: 18px 18px 4px 18px; font-size: 14px; line-height: 1.4;">
                    ${message}
                </div>
                <div class="message-time" style="text-align: right; margin-top: 5px; font-size: 11px; color: #8e8e93;">
                    방금 전
                </div>
            </div>
        `;
    } else {
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <img src="label-logo.png" alt="무드트립">
            </div>
            <div class="message-content">
                <div class="message-header">
                    <strong>무드트립</strong>
                    <span class="message-time">방금 전</span>
                </div>
                <p>${message}</p>
            </div>
        `;
    }
    
    messagesContainer.appendChild(messageDiv);
    
    // 스크롤을 맨 아래로
    const messengerContent = messagesContainer.closest('.messenger-content');
    if (messengerContent) {
        messengerContent.scrollTop = messengerContent.scrollHeight;
    }
    
    // 메시지 저장
    chatMessages.push({
        sender: sender,
        message: message,
        timestamp: new Date()
    });
    
    console.log('메시지 전송:', { sender, message });
}

function handleServiceClick(service) {
    console.log('Service clicked:', service);
    
    // 채팅 모드로 전환
    startChatMode();
    
    // 잠시 후 해당 서비스 관련 메시지 추가
    setTimeout(() => {
        if (service === 'pricing') {
            addChatMessage('company', '💰 무드트립의 합리적인 가격 정책을 확인해보세요!\n\n• 투명한 가격 공개\n• 합리적인 여행 비용\n• 다양한 할인 혜택\n\n더 자세한 정보가 필요하시면 말씀해주세요!');
        } else if (service === 'schedule') {
            addChatMessage('company', '⏰ 고객 상담 시간 안내\n\n• 평일: 09:00 ~ 18:00\n• 토요일: 10:00 ~ 15:00\n• 일요일 및 공휴일: 휴무\n\n긴급한 문의사항이 있으시면 언제든 메시지 남겨주세요!');
        }
    }, 500);
}

function makeCall() {
    alert('📞 전화 연결\n\n고객센터: 1588-0000\n운영시간: 평일 09:00~18:00');
}

// DOM 로드 완료 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 초기 일정 데이터 설정
    initializeScheduleData();
    
    // 캘린더 생성
    generateCalendar();
    
    // 캘린더 날짜 클릭 이벤트
    initializeCalendarEvents();
    
    // 애니메이션 효과 초기화
    initializeAnimations();
    
    // 기타 이벤트 리스너 초기화
    initializeEventListeners();
    
    // 메신저 관련 이벤트 리스너 추가
    initializeMessengerEvents();
    
    // 실시간 접속자 업데이트 시작
    updateOnlineUsers(); // 초기 상태 설정
    startRealtimeUpdates(); // 실시간 업데이트 시작
});

// 초기 일정 데이터 설정
function initializeScheduleData() {
    // 예시 일정 데이터 (실제로는 API나 localStorage에서 불러옴)
    scheduleData = {
        '2025-7-12': [
            { time: '09:00', title: '서울역 출발', description: 'KTX 부산행' },
            { time: '12:30', title: '부산역 도착', description: '숙소 체크인' },
            { time: '15:00', title: '해운대 해수욕장', description: '바다 구경 및 휴식' },
            { time: '18:00', title: '자갈치 시장', description: '회 먹기' }
        ],
        '2025-7-13': [
            { time: '10:00', title: '감천문화마을', description: '사진 촬영' },
            { time: '14:00', title: '태종대', description: '산책 및 관광' },
            { time: '19:00', title: '광안리', description: '야경 감상' }
        ],
        '2025-7-14': [
            { time: '11:00', title: '부산역 출발', description: 'KTX 서울행' },
            { time: '14:30', title: '서울역 도착', description: '여행 종료' }
        ]
    };
}

// 실제 캘린더 생성 함수
function generateCalendar() {
    const calendarGrid = document.querySelector('.calendar-grid');
    const calendarHeader = document.querySelector('.calendar-header h3');
    
    // 기존 날짜 셀들 제거 (요일 헤더는 유지)
    const existingDays = calendarGrid.querySelectorAll('.calendar-day:nth-child(n+8)');
    existingDays.forEach(day => day.remove());
    
    // 월 이름 배열
    const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', 
                       '7월', '8월', '9월', '10월', '11월', '12월'];
    
    // 헤더 업데이트
    calendarHeader.textContent = `${currentDate.getFullYear()}년 ${monthNames[currentDate.getMonth()]}`;
    
    // 현재 월의 첫째 날과 마지막 날
    const firstDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    const lastDay = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
    const today = new Date();
    
    // 첫째 주의 빈 칸들 (이전 달)
    const startDay = firstDay.getDay();
    for (let i = startDay - 1; i >= 0; i--) {
        const prevDate = new Date(firstDay);
        prevDate.setDate(prevDate.getDate() - (i + 1));
        
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        dayElement.textContent = prevDate.getDate();
        dayElement.style.color = '#ccc';
        dayElement.style.cursor = 'default';
        calendarGrid.appendChild(dayElement);
    }
    
    // 현재 월의 날짜들
    for (let day = 1; day <= lastDay.getDate(); day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        dayElement.textContent = day;
        
        // 오늘 날짜 표시
        if (currentDate.getFullYear() === today.getFullYear() && 
            currentDate.getMonth() === today.getMonth() && 
            day === today.getDate()) {
            dayElement.style.background = 'linear-gradient(135deg, rgba(46, 91, 140, 0.3) 0%, rgba(30, 58, 95, 0.2) 100%)';
            dayElement.style.fontWeight = '700';
            dayElement.style.color = '#1E3A5F';
        }
        
        calendarGrid.appendChild(dayElement);
    }
    
    // 남은 칸들을 다음 달 날짜로 채우기
    const totalCells = 42; // 6주 * 7일
    const usedCells = startDay + lastDay.getDate();
    
    for (let day = 1; usedCells + day - 1 < totalCells; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        dayElement.textContent = day;
        dayElement.style.color = '#ccc';
        dayElement.style.cursor = 'default';
        calendarGrid.appendChild(dayElement);
    }
    
    // 새로 생성된 날짜들에 이벤트 리스너 추가
    initializeCalendarEvents();
}

// 캘린더 이벤트 초기화
function initializeCalendarEvents() {
    document.querySelectorAll('.calendar-day').forEach(day => {
        if (day.textContent && !isNaN(day.textContent) && day.style.color !== '#ccc') {
            day.addEventListener('click', function() {
                // 기존 선택 제거
                document.querySelectorAll('.calendar-day').forEach(d => d.classList.remove('selected'));
                // 새로운 날짜 선택
                this.classList.add('selected');
                
                // 선택된 날짜에 따른 일정 업데이트
                updateScheduleForDate(this.textContent);
            });
        }
    });
}

// 선택된 날짜의 일정 업데이트
function updateScheduleForDate(selectedDay) {
    const scheduleList = document.querySelector('.schedule-list');
    const scheduleTitle = scheduleList.querySelector('h3');
    
    // 현재 선택된 날짜 저장
    selectedDate = selectedDay;
    
    // 제목 업데이트
    if (scheduleTitle) {
        scheduleTitle.textContent = `${currentDate.getMonth() + 1}월 ${selectedDay}일 일정`;
    }
    
    // 기존 일정 아이템들과 no-schedule 제거
    const existingItems = scheduleList.querySelectorAll('.schedule-item, .no-schedule');
    existingItems.forEach(item => item.remove());
    
    // 해당 날짜의 일정 데이터 가져오기
    const dateKey = `${currentDate.getFullYear()}-${currentDate.getMonth() + 1}-${selectedDay}`;
    const daySchedules = scheduleData[dateKey];
    
    if (daySchedules && daySchedules.length > 0) {
        // 일정이 있는 경우
        const addBtn = scheduleList.querySelector('.add-btn');
        daySchedules.forEach(schedule => {
            const scheduleItem = createScheduleItem(schedule.time, schedule.title, schedule.description);
            scheduleList.insertBefore(scheduleItem, addBtn);
        });
    } else {
        // 일정이 없는 경우
        const noScheduleDiv = document.createElement('div');
        noScheduleDiv.className = 'no-schedule';
        noScheduleDiv.innerHTML = `
            <div class="no-schedule-icon">📝</div>
            <p>이 날짜에는 일정이 없습니다.<br>새로운 일정을 추가해보세요!</p>
        `;
        
        const addBtn = scheduleList.querySelector('.add-btn');
        scheduleList.insertBefore(noScheduleDiv, addBtn);
    }
    
    console.log(`${selectedDay}일 일정을 불러왔습니다.`);
}

// 애니메이션 효과 초기화
function initializeAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // 애니메이션 대상 요소들 설정
    document.querySelectorAll('.schedule-item, .transport-item, .forecast-item').forEach(item => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        item.style.transition = 'all 0.6s ease';
        observer.observe(item);
    });
}

// 기타 이벤트 리스너 초기화
function initializeEventListeners() {
    // 교통편 아이템 클릭 이벤트
    const transportItems = document.querySelectorAll('.transport-item');
    transportItems.forEach(item => {
        item.addEventListener('click', function() {
            handleTransportSelection(this);
        });
    });
    
    // 캘린더 네비게이션 버튼들
    const navBtns = document.querySelectorAll('.nav-btn');
    navBtns.forEach((btn, index) => {
        btn.addEventListener('click', function() {
            if (index === 0) {
                // 이전 달
                navigateCalendar('prev');
            } else {
                // 다음 달
                navigateCalendar('next');
            }
        });
    });
    
    // 모달 외부 클릭 시 닫기
    document.addEventListener('click', function(e) {
        const scheduleModal = document.getElementById('scheduleModal');
        if (e.target === scheduleModal) {
            closeScheduleModal();
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const scheduleModal = document.getElementById('scheduleModal');
            const messengerModal = document.getElementById('messengerModal');
            
            if (scheduleModal && scheduleModal.classList.contains('active')) {
                closeScheduleModal();
            }
            if (messengerModal && messengerModal.classList.contains('active')) {
                closeMessenger();
            }
        }
    });
}

function initializeMessengerEvents() {
    // 토글 스위치 클릭 이벤트
    document.querySelectorAll('.toggle-switch').forEach(toggle => {
        toggle.addEventListener('click', function() {
            this.classList.toggle('active');
        });
    });
    
    // 메시지 아이템 클릭 이벤트
    document.querySelectorAll('.message-item').forEach(item => {
        item.addEventListener('click', function() {
            // 메시지 상세 보기 (실제로는 해당 채팅방으로 이동)
            console.log('메시지 아이템 클릭:', this.querySelector('.message-title').textContent);
            switchMessengerTab('home'); // 홈 탭으로 이동하여 채팅 화면 보여주기
        });
    });
    
    // 언어 설정 클릭 이벤트
    const languageSetting = document.querySelector('.setting-item .setting-value');
    if (languageSetting && languageSetting.textContent.includes('English')) {
        languageSetting.addEventListener('click', function() {
            const languages = ['English', '한국어', '日本語', '中文'];
            const currentLang = this.textContent.replace('>', '').trim();
            const currentIndex = languages.indexOf(currentLang);
            const nextIndex = (currentIndex + 1) % languages.length;
            this.innerHTML = `${languages[nextIndex]} <span class="arrow">></span>`;
        });
    }
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeMessenger();
        }
    });
}

// 기존 일정 추가 핸들러 (이제 사용하지 않음)
function handleAddSchedule() {
    // 이제 모달을 통해 처리하므로 이 함수는 사용하지 않음
    openScheduleModal();
}

// 일정 아이템 생성
function createScheduleItem(time, title, description) {
    const scheduleItem = document.createElement('div');
    scheduleItem.className = 'schedule-item';
    scheduleItem.style.opacity = '0';
    scheduleItem.style.transform = 'translateY(20px)';
    scheduleItem.style.transition = 'all 0.6s ease';
    
    scheduleItem.innerHTML = `
        <div class="schedule-time">${time}</div>
        <div>
            <strong>${title}</strong>
            ${description ? `<p>${description}</p>` : ''}
        </div>
    `;
    
    // 애니메이션 효과
    setTimeout(() => {
        scheduleItem.style.opacity = '1';
        scheduleItem.style.transform = 'translateY(0)';
    }, 100);
    
    return scheduleItem;
}

// 교통편 선택 핸들러
function handleTransportSelection(transportItem) {
    // 모든 교통편 아이템의 선택 상태 제거
    document.querySelectorAll('.transport-item').forEach(item => {
        item.style.borderColor = 'rgba(0, 87, 146, 0.1)';
        item.style.backgroundColor = '';
    });
    
    // 선택된 아이템 스타일 변경
    transportItem.style.borderColor = '#4CAF50';
    transportItem.style.backgroundColor = 'rgba(76, 175, 80, 0.05)';
    
    // 선택된 교통편 정보 가져오기
    const transportName = transportItem.querySelector('h4').textContent;
    const transportDetails = transportItem.querySelector('p').textContent;
    
    console.log('선택된 교통편:', transportName, transportDetails);
}

// 캘린더 네비게이션
function navigateCalendar(direction) {
    if (direction === 'prev') {
        currentDate.setMonth(currentDate.getMonth() - 1);
    } else {
        currentDate.setMonth(currentDate.getMonth() + 1);
    }
    
    // 캘린더 다시 생성
    generateCalendar();
    
    console.log(`캘린더를 ${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월로 변경`);
}

// 초기화 완료 로그
console.log('무드트립 스케줄 플래너 + 메신저 앱이 초기화되었습니다!');
console.log('메신저 팝업: 우하단 💬 버튼 클릭');
console.log('사용 가능한 기능:');
console.log('- 캘린더 날짜 클릭으로 일정 관리');
console.log('- 탭 전환으로 날씨/교통편 확인');
console.log('- 메신저 앱으로 고객 지원');
console.log('- ESC 키로 메신저 앱 닫기');
console.log('- 새로운 모달창으로 일정 추가');
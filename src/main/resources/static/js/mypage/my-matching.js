/**
 * ✅ 기존 my-matching.js에 추가할 실제 API 연동 함수들
 *
 * 📌 기존 코드의 DOMContentLoaded 이벤트 리스너에 추가하세요:
 * document.addEventListener('DOMContentLoaded', function() {
 *     loadRealJoinedRooms();
 *     loadRealCreatedRooms(); // ✅ 새로 추가
 * });
 */

/**
 * 🔥 내가 입장한 방 목록 조회
 */
async function loadRealJoinedRooms() {
    try {
        showLoadingState('received-tab');

        const response = await fetch('/api/v1/mypage/rooms/joined');
        if (!response.ok) throw new Error(`API 실패: ${response.status}`);

        const joinedRooms = await response.json();
        updateJoinedRoomsHTML(joinedRooms);
        hideLoadingState('received-tab');

    } catch (error) {
        console.error('❌ 입장한 방 fetch 실패:', error);
        showErrorState('received-tab', error.message);
    }
}

/**
 * 🔥 내가 만든 방 목록 조회
 */
async function loadRealCreatedRooms() {
    try {
        showLoadingState('created-tab');

        const response = await fetch('/api/v1/mypage/rooms/created');
        if (!response.ok) throw new Error(`API 실패: ${response.status}`);

        const createdRooms = await response.json();
        updateCreatedRoomsHTML(createdRooms);
        hideLoadingState('created-tab');

    } catch (error) {
        console.error('❌ 만든 방 fetch 실패:', error);
        showErrorState('created-tab', error.message);
    }
}

/**
 * 🔄 입장한 방 렌더링
 */
function updateJoinedRoomsHTML(rooms) {
    const wrapper = document.querySelector('#received-tab .matching-list-wrapper');
    if (!wrapper) return;

    wrapper.innerHTML = '';

    if (rooms.length === 0) {
        wrapper.innerHTML = '<p>참여 중인 방이 없습니다.</p>';
        return;
    }

    rooms.forEach(room => wrapper.appendChild(createRealRoomElement(room)));
}

/**
 * 🔄 만든 방 렌더링
 */
function updateCreatedRoomsHTML(rooms) {
    const wrapper = document.querySelector('#created-tab .matching-list-wrapper');
    if (!wrapper) return;

    wrapper.innerHTML = '';

    if (rooms.length === 0) {
        wrapper.innerHTML = '<p>아직 만든 방이 없습니다.</p>';
        return;
    }

    rooms.forEach(room => wrapper.appendChild(createCreatedRoomElement(room)));
}

/**
 * 🧱 입장한 방 엘리먼트 생성
 */
function createRealRoomElement(room) {
    const div = document.createElement('div');
    div.className = 'matching-item';
    div.innerHTML = `
        <h3>${room.roomName}</h3>
        <p>${room.roomDescription}</p>
        <p>${room.destinationCategory} - ${room.destinationName}</p>
        <p>${formatTravelDates(room.travelStartDate, room.travelEndDate)}</p>
        <p>${room.currentCount} / ${room.maxCount}</p>
        <p>내 역할: ${room.myRole}</p>
    `;
    return div;
}

/**
 * 🧱 만든 방 엘리먼트 생성
 */
function createCreatedRoomElement(room) {
    const div = document.createElement('div');
    div.className = 'matching-item';
    div.innerHTML = `
        <h3>${room.roomName}</h3>
        <p>${room.roomDescription}</p>
        <p>${room.destinationCategory} - ${room.destinationName}</p>
        <p>${formatTravelDates(room.travelStartDate, room.travelEndDate)}</p>
        <p>${room.currentCount} / ${room.maxCount}</p>
    `;
    return div;
}

/**
 * 📅 여행 날짜 포맷
 */
function formatTravelDates(start, end) {
    if (!start) return '날짜 미정';
    const s = new Date(start);
    const e = end ? new Date(end) : null;
    const sf = `${s.getMonth()+1}월 ${s.getDate()}일`;
    if (e && start !== end) {
        const ef = `${e.getMonth()+1}월 ${e.getDate()}일`;
        return `${sf} ~ ${ef}`;
    }
    return sf;
}

/**
 * 🧹 로딩 / 에러 상태 관리
 */
function showLoadingState(tabId) {
    const wrapper = document.querySelector(`#${tabId} .matching-list-wrapper`);
    if (wrapper) wrapper.innerHTML = '<p>불러오는 중...</p>';
}
function hideLoadingState(tabId) {
    // 따로 처리 필요 없을 수도 있음
}
function showErrorState(tabId, message) {
    const wrapper = document.querySelector(`#${tabId} .matching-list-wrapper`);
    if (wrapper) wrapper.innerHTML = `<p style="color:red;">${message}</p>`;
}

/**
 * 🧭 탭 클릭 시 API 호출 연결
 */
function enhancedTabSwitching() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.getAttribute('data-tab'); // 'received' or 'created'

            // 1. 모든 탭 버튼에서 active 제거 후 현재 버튼에만 추가
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // 2. 모든 탭 콘텐츠 숨기기
            tabContents.forEach(content => content.classList.remove('active'));

            // 3. 선택된 탭 콘텐츠만 보이기
            const targetContent = document.getElementById(`${target}-tab`);
            if (targetContent) {
                targetContent.classList.add('active');
            }

            // 4. 실제 API 호출
            if (target === 'received') {
                loadRealJoinedRooms();
            } else if (target === 'created') {
                loadRealCreatedRooms();
            }
        });
    });
}

/**
 * 🚀 초기 실행
 */
document.addEventListener('DOMContentLoaded', function () {
    loadRealJoinedRooms();
    enhancedTabSwitching();
});

// 전역 등록
window.loadRealJoinedRooms = loadRealJoinedRooms;
window.loadRealCreatedRooms = loadRealCreatedRooms;
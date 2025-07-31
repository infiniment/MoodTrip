// 최종 방 생성 데이터를 저장할 객체
let finalRoomData = {};

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    loadAllPreviousData();
    initializeInputFields();
    initializeCharacterCount();
    initializeSubmitButton();
});

// 이전 단계들의 모든 데이터 불러오기
function loadAllPreviousData() {
    try {
        // 통합 데이터 먼저 시도
        let roomCreationData = localStorage.getItem('room_creation_data');
        if (roomCreationData) {
            finalRoomData = JSON.parse(roomCreationData);
        } else {
            // 개별 데이터 수집
            finalRoomData = {
                emotions: getEmotionsData(),
                destination: getDestinationData(),
                schedule: getScheduleData(),
                people: getPeopleData(),
                roomName: getRoomNameData(),
                roomIntro: getRoomDescriptionData()
            };
        }

        // UI에 데이터 표시
        displayAllData();

    } catch (error) {
        console.error('이전 데이터 불러오기 실패:', error);
        showErrorMessage('이전 단계의 데이터를 불러오는데 실패했습니다. 처음부터 다시 시작해주세요.');
    }
}

// 개별 데이터 불러오기 함수들
function getEmotionsData() {
    try {
        let emotions = localStorage.getItem('selected_emotions');
        if (emotions) return JSON.parse(emotions);

        emotions = sessionStorage.getItem('selected_emotions');
        if (emotions) return JSON.parse(emotions);

        return [];
    } catch (e) {
        return [];
    }
}

function getDestinationData() {
    try {
        let destination = localStorage.getItem('selected_destination');
        if (destination) return JSON.parse(destination);

        destination = sessionStorage.getItem('selected_destination');
        if (destination) return JSON.parse(destination);

        return null;
    } catch (e) {
        return null;
    }
}

function getScheduleData() {
    try {
        let schedule = localStorage.getItem('travel_date_ranges');
        if (schedule) return JSON.parse(schedule);

        schedule = sessionStorage.getItem('travel_date_ranges');
        if (schedule) return JSON.parse(schedule);

        return null;
    } catch (e) {
        return null;
    }
}

function getPeopleData() {
    return localStorage.getItem('selected_people') || '2명';
}

function getRoomNameData() {
    return localStorage.getItem('room_name') || '';
}

function getRoomDescriptionData() {
    return localStorage.getItem('room_description') || '';
}

// 모든 데이터를 UI에 표시
function displayAllData() {
    displayPeopleInfo();
    displayRoomNameAndIntro();
    displayEmotionTags();
    displayDestination();
    displaySchedule();
}

// 인원 정보 표시
function displayPeopleInfo() {
    const peopleText = document.getElementById('peopleText');
    const peopleIcon = document.getElementById('peopleIcon');

    const people = finalRoomData.people || getPeopleData();

    if (peopleText) {
        peopleText.textContent = people;
    }

    // 인원에 따라 아이콘 변경
    if (peopleIcon) {
        const iconImg = peopleIcon.querySelector('img');
        if (people === '4명') {
            iconImg.src = '/image/creatingRoom/fourpeople.svg';
            iconImg.alt = '4명';
        } else {
            iconImg.src = '/image/creatingRoom/twopeople.svg';
            iconImg.alt = '2명';
        }
    }
}

// 방 이름과 소개 표시
function displayRoomNameAndIntro() {
    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    if (roomNameInput) {
        roomNameInput.value = finalRoomData.roomName || getRoomNameData();
        updateCharCount('roomNameInput', 'currentLength', 30);
    }

    if (roomIntroTextarea) {
        roomIntroTextarea.value = finalRoomData.roomIntro || getRoomDescriptionData();
        updateCharCount('roomIntroTextarea', 'introCurrentLength', 200);
    }
}

// 감정 태그 표시
function displayEmotionTags() {
    const emotionsList = document.getElementById('selectedEmotionsList');

    if (!emotionsList) return;

    const emotions = finalRoomData.emotions || getEmotionsData();

    if (!emotions || emotions.length === 0) {
        emotionsList.innerHTML = '<p style="color: #bdbdbd; text-align: center; padding: 20px;">선택된 감정 태그가 없습니다.</p>';
        return;
    }

    emotionsList.innerHTML = emotions.map(emotion => {
        const emotionText = typeof emotion === 'string' ? emotion : emotion.text;
        const emotionType = typeof emotion === 'object' ? emotion.type : 'preset';

        return `<span class="emotion-tag ${emotionType}">${emotionText}</span>`;
    }).join('');
}

// 관광지 정보 표시
function displayDestination() {
    const destinationDisplay = document.getElementById('destinationCardDisplay');

    if (!destinationDisplay) return;

    const destination = finalRoomData.destination || getDestinationData();

    if (!destination) {
        destinationDisplay.innerHTML = '<p style="color: #bdbdbd; text-align: center; padding: 20px;">선택된 관광지가 없습니다.</p>';
        return;
    }

    destinationDisplay.innerHTML = `
        <div class="destination-card">
            <div class="destination-image">
                <img src="${destination.image || '/image/creatingRoom/default-destination.png'}" alt="${destination.name}">
            </div>
            <div class="destination-details">
                <div class="destination-category">${destination.category || '관광지'}</div>
                <div class="destination-name">${destination.name}</div>
                <div class="destination-description">${destination.description || '설명이 없습니다.'}</div>
            </div>
        </div>
    `;
}

// 일정 정보 표시
function displaySchedule() {
    const scheduleDisplay = document.getElementById('scheduleDisplay');

    if (!scheduleDisplay) return;

    const schedule = finalRoomData.schedule || getScheduleData();

    if (!schedule || !schedule.dateRanges || schedule.dateRanges.length === 0) {
        scheduleDisplay.innerHTML = '<p style="color: #bdbdbd; text-align: center; padding: 20px;">선택된 일정이 없습니다.</p>';
        return;
    }

    const rangesHTML = schedule.dateRanges.map((range, index) => {
        const startDate = range.startDateFormatted || formatDate(new Date(range.startDate));
        const endDate = range.endDateFormatted || formatDate(new Date(range.endDate));
        const dateText = startDate === endDate ? startDate : `${startDate} ~ ${endDate}`;

        return `
            <div class="schedule-range-item">
                <span class="schedule-icon">📅</span>
                <span class="schedule-date-text">${dateText}</span>
            </div>
        `;
    }).join('');

    scheduleDisplay.innerHTML = `
        <div class="schedule-ranges">
            ${rangesHTML}
        </div>
        <div class="schedule-summary">
            총 ${schedule.rangeCount || schedule.dateRanges.length}개 구간, ${schedule.totalDays || calculateTotalDays(schedule.dateRanges)}일
        </div>
    `;
}

// 입력 필드 초기화
function initializeInputFields() {
    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    // 실시간 데이터 업데이트
    if (roomNameInput) {
        roomNameInput.addEventListener('input', function() {
            finalRoomData.roomName = this.value.trim();
            updateCharCount('roomNameInput', 'currentLength', 30);
        });

        roomNameInput.addEventListener('blur', function() {
            localStorage.setItem('room_name', this.value.trim());
        });
    }

    if (roomIntroTextarea) {
        roomIntroTextarea.addEventListener('input', function() {
            finalRoomData.roomIntro = this.value.trim();
            updateCharCount('roomIntroTextarea', 'introCurrentLength', 200);
        });

        roomIntroTextarea.addEventListener('blur', function() {
            localStorage.setItem('room_description', this.value.trim());
        });
    }
}

// 글자 수 카운트 초기화
function initializeCharacterCount() {
    updateCharCount('roomNameInput', 'currentLength', 30);
    updateCharCount('roomIntroTextarea', 'introCurrentLength', 200);
}

// 글자 수 업데이트
function updateCharCount(inputId, countId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(countId);

    if (input && counter) {
        const currentLength = input.value.length;
        counter.textContent = currentLength;

        // 글자 수에 따른 색상 변경
        const countElement = counter.closest('.char-count');
        if (currentLength > maxLength * 0.9) {
            countElement.style.color = '#f44336';
        } else if (currentLength > maxLength * 0.7) {
            countElement.style.color = '#ff9800';
        } else {
            countElement.style.color = '#616161';
        }
    }
}

// 제출 버튼 초기화
function initializeSubmitButton() {
    const submitButton = document.getElementById('submitButton');

    if (submitButton) {
        submitButton.removeEventListener('click', handleSubmitClick);
        submitButton.addEventListener('click', handleSubmitClick);
    }
}

// 방 등록 제출
function submitRoom() {
    const submitButton = document.getElementById('submitButton');

    if (submitButton && (submitButton.disabled || submitButton.classList.contains('loading'))) {
        return;
    }

    if (!validateFinalData()) {
        return;
    }

    setSubmitButtonLoading(true);
    const submitData = prepareFinalSubmitData();
    console.log("최종 submitData:", submitData);
    fetch('/api/v1/companion-rooms', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(submitData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`서버 오류: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('서버 응답:', data);
            showSuccessModal();           // 등록 성공 모달 표시
            clearAllStoredData();         // localStorage 비우기
        })
        .catch(error => {
            console.error('방 등록 실패:', error);
            showErrorMessage('방 등록에 실패했습니다. 다시 시도해주세요.');
        })
        .finally(() => {
            setSubmitButtonLoading(false);
        });
}

// 최종 데이터 유효성 검사
function validateFinalData() {
    const roomName = document.getElementById('roomNameInput').value.trim();
    const roomIntro = document.getElementById('roomIntroTextarea').value.trim();

    if (!roomName) {
        alert('방 이름을 입력해주세요.');
        document.getElementById('roomNameInput').focus();
        return false;
    }

    if (roomName.length < 2) {
        alert('방 이름은 2자 이상 입력해주세요.');
        document.getElementById('roomNameInput').focus();
        return false;
    }

    if (!roomIntro) {
        alert('방 소개를 입력해주세요.');
        document.getElementById('roomIntroTextarea').focus();
        return false;
    }

    if (roomIntro.length < 10) {
        alert('방 소개는 10자 이상 입력해주세요.');
        document.getElementById('roomIntroTextarea').focus();
        return false;
    }

    // 이전 단계 데이터 확인
    if (!finalRoomData.emotions || finalRoomData.emotions.length === 0) {
        alert('감정 태그가 선택되지 않았습니다. 이전 단계로 돌아가서 선택해주세요.');
        return false;
    }

    if (!finalRoomData.destination) {
        alert('여행지가 선택되지 않았습니다. 이전 단계로 돌아가서 선택해주세요.');
        return false;
    }

    if (!finalRoomData.schedule || !finalRoomData.schedule.dateRanges || finalRoomData.schedule.dateRanges.length === 0) {
        alert('일정이 선택되지 않았습니다. 이전 단계로 돌아가서 선택해주세요.');
        return false;
    }

    return true;
}

// 최종 제출 데이터 준비
function prepareFinalSubmitData() {
    // DOM에서 직접 가져온 최신 값 사용
    const roomName = document.getElementById('roomNameInput').value.trim();
    const roomIntro = document.getElementById('roomIntroTextarea').value.trim();
    const people = localStorage.getItem('selected_people') || finalRoomData.people || '2명';

    const submitData = {
        destination: {
            category: finalRoomData.destination?.category || null,
            name: finalRoomData.destination?.name || null
        },
        emotions: finalRoomData.emotions || [],
        schedule: finalRoomData.schedule || null,
        maxParticipants: parseInt(people),
        roomName: roomName,
        roomDescription: roomIntro,
        version: '1.0'
    };

    const jsonData = JSON.stringify(submitData, null, 2);
    localStorage.setItem('final_room_submission', jsonData);

    console.log('저장될 JSON 데이터:', jsonData);

    return submitData;
}



// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    if (window.roomRegistrationInitialized) {
        return;
    }

    window.roomRegistrationInitialized = true;

    loadAllPreviousData();
    initializeInputFields();
    initializeCharacterCount();
    initializeSubmitButton();
});


// 제출 버튼 로딩 상태 변경
function setSubmitButtonLoading(isLoading) {
    const submitButton = document.getElementById('submitButton');

    if (submitButton) {
        if (isLoading) {
            submitButton.classList.add('loading');
            submitButton.disabled = true;
        } else {
            submitButton.classList.remove('loading');
            submitButton.disabled = false;
        }
    }
}


function handleSubmitClick(e) {
    e.preventDefault();

    if (submitButton.disabled || submitButton.classList.contains('loading')) {
        return;
    }

    submitRoom();
}

// 성공 모달 표시
function showSuccessModal() {
    const modal = document.getElementById('successModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

// 에러 메시지 표시
function showErrorMessage(message) {
    alert(message); // 실제로는 더 예쁜 토스트나 모달을 사용
}

// ✨ 수정하기 버튼 - 모달 표시
function editRoom() {
    // 현재 입력 내용 임시 저장
    const roomName = document.getElementById('roomNameInput').value.trim();
    const roomIntro = document.getElementById('roomIntroTextarea').value.trim();

    if (roomName) localStorage.setItem('temp_room_name', roomName);
    if (roomIntro) localStorage.setItem('temp_room_description', roomIntro);

    // 수정 선택 모달 열기
    openEditSelectionModal();
}

// ✨ 수정 선택 모달 열기
function openEditSelectionModal() {
    const modal = document.getElementById('editSelectionModal');
    if (modal) {
        // 모달 표시 전에 미리보기 데이터 업데이트
        updateEditModalPreviews();
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

// ✨ 수정 모달의 미리보기 데이터 업데이트
function updateEditModalPreviews() {
    // 인원 미리보기
    const peoplePreview = document.getElementById('editPeoplePreview');
    if (peoplePreview) {
        const people = finalRoomData.people || getPeopleData();
        peoplePreview.textContent = people;
    }

    // 감정 태그 미리보기
    const emotionsPreview = document.getElementById('editEmotionsPreview');
    if (emotionsPreview) {
        const emotions = finalRoomData.emotions || getEmotionsData();
        if (emotions && emotions.length > 0) {
            emotionsPreview.textContent = `${emotions.length}개 선택됨`;
        } else {
            emotionsPreview.textContent = '선택 안됨';
        }
    }

    // 여행지 미리보기
    const destinationPreview = document.getElementById('editDestinationPreview');
    if (destinationPreview) {
        const destination = finalRoomData.destination || getDestinationData();
        if (destination && destination.name) {
            destinationPreview.textContent = destination.name;
        } else {
            destinationPreview.textContent = '선택 안됨';
        }
    }

    // 일정 미리보기
    const schedulePreview = document.getElementById('editSchedulePreview');
    if (schedulePreview) {
        const schedule = finalRoomData.schedule || getScheduleData();
        if (schedule && schedule.totalDays) {
            const nights = schedule.totalDays - 1;
            schedulePreview.textContent = `${nights}박 ${schedule.totalDays}일`;
        } else if (schedule && schedule.dateRanges && schedule.dateRanges.length > 0) {
            const totalDays = calculateTotalDays(schedule.dateRanges);
            const nights = totalDays - 1;
            schedulePreview.textContent = `${nights}박 ${totalDays}일`;
        } else {
            schedulePreview.textContent = '선택 안됨';
        }
    }
}

// ✨ 수정 선택 모달 닫기
function closeEditSelectionModal() {
    const modal = document.getElementById('editSelectionModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// ✨ 특정 단계로 이동
function goToEditStep(stepNumber) {
    const stepUrls = {
        1: '/companion-rooms/create',  // 기본 정보
        2: '/companion-rooms/emotion',        // 감정 선택
        3: '/companion-rooms/attraction',     // 여행지 선택
        4: '/companion-rooms/schedule'        // 일정 선택
    };

    const stepNames = {
        1: '기본 정보',
        2: '감정 선택',
        3: '여행지 선택',
        4: '일정 선택'
    };

    if (stepUrls[stepNumber]) {
        // 사용자에게 확인
        if (confirm(`${stepNames[stepNumber]} 수정 페이지로 이동하시겠습니까?\n현재 입력한 내용은 임시 저장됩니다.`)) {
            // 현재 입력 내용 저장
            saveCurrentInputs();

            // 해당 단계로 이동
            window.location.href = stepUrls[stepNumber];
        }

        // 모달 닫기
        closeEditSelectionModal();
    } else {
        alert('잘못된 단계입니다.');
    }
}

// ✨ 현재 입력 내용 저장
function saveCurrentInputs() {
    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    if (roomNameInput && roomNameInput.value.trim()) {
        localStorage.setItem('temp_room_name', roomNameInput.value.trim());
    }

    if (roomIntroTextarea && roomIntroTextarea.value.trim()) {
        localStorage.setItem('temp_room_description', roomIntroTextarea.value.trim());
    }

    console.log('현재 입력 내용이 임시 저장되었습니다.');
}

// 이전 페이지로 돌아가기
function goToPreviousPage() {
    // 현재 입력 내용 임시 저장
    const roomName = document.getElementById('roomNameInput').value.trim();
    const roomIntro = document.getElementById('roomIntroTextarea').value.trim();

    if (roomName) localStorage.setItem('temp_room_name', roomName);
    if (roomIntro) localStorage.setItem('temp_room_description', roomIntro);

    // 이전 페이지로 이동
    window.location.href = '/companion-rooms/schedule.html';
}

// 내 방 보기로 이동 (나중에 href 수정해야됨)
function goToMyRooms() {
    window.location.href = "/mypage/my-rooms"; // 실제 내 방 목록 페이지로 이동
}

// 새 방 만들기
function createNewRoom() {
    if (confirm('새로운 방을 만드시겠습니까?\n현재 입력된 모든 정보가 삭제됩니다.')) {
        // 모든 데이터 완전 초기화
        clearAllStoredData();

        // 추가로 다른 가능한 저장소도 정리
        clearAdditionalStorageData();

        // 성공 모달 닫기 (혹시 열려있다면)
        const successModal = document.getElementById('successModal');
        if (successModal) {
            successModal.style.display = 'none';
        }

        // 페이지 이동 전 로딩 표시 (선택사항)
        showNewRoomCreationLoading();

        // 새 방 만들기 플래그 설정
        sessionStorage.setItem('from_new_room_creation', 'true');

        setTimeout(() => {
            // 첫 번째 단계(기본 정보 입력)로 이동 - URL 파라미터 추가
            window.location.href = '/companion-rooms/create?new=true';
        }, 500); // 약간의 지연으로 사용자가 초기화를 인지할 수 있도록
    }
}

// ✨ 추가 저장소 데이터 정리 (더 완전한 초기화)
function clearAdditionalStorageData() {
    // 혹시 놓친 임시 데이터들도 정리
    const additionalKeys = [
        // 각 단계별 임시 데이터
        'temp_people_selection',
        'temp_emotion_selection',
        'temp_destination_selection',
        'temp_schedule_selection',

        // 폼 상태 관련
        'form_step_status',
        'current_form_step',
        'form_validation_state',

        // 사용자 입력 히스토리
        'room_name_history',
        'room_description_history',

        // 기타 캐시 데이터
        'cached_destination_list',
        'cached_emotion_tags',
        'user_preferences',

        // 진행 상태 관련
        'room_creation_progress',
        'step_completion_status'
    ];

    additionalKeys.forEach(key => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });

    // IndexedDB 정리 (만약 사용 중이라면)
    clearIndexedDBData();

    console.log('모든 추가 저장소 데이터가 정리되었습니다.');
}

// ✨ IndexedDB 데이터 정리 (고급 기능)
function clearIndexedDBData() {
    try {
        // IndexedDB 정리 (비동기)
        const dbNames = ['roomCreationDB', 'userPreferencesDB', 'tempDataDB'];

        dbNames.forEach(dbName => {
            const deleteReq = indexedDB.deleteDatabase(dbName);
            deleteReq.onsuccess = () => {
                console.log(`${dbName} 데이터베이스가 삭제되었습니다.`);
            };
            deleteReq.onerror = () => {
                console.log(`${dbName} 데이터베이스 삭제 중 오류가 발생했습니다.`);
            };
        });
    } catch (error) {
        console.log('IndexedDB 정리 중 오류:', error);
    }
}

// ✨ 새 방 생성 로딩 표시
function showNewRoomCreationLoading() {
    // 기존 모달들 모두 숨기기
    const modals = document.querySelectorAll('.success-modal, .edit-selection-modal, .help-modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });

    // 로딩 오버레이 생성
    const loadingOverlay = document.createElement('div');
    loadingOverlay.id = 'newRoomLoadingOverlay';
    loadingOverlay.innerHTML = `
        <div class="loading-overlay-background"></div>
        <div class="loading-content">
            <div class="loading-spinner-new">
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
            </div>
            <div class="loading-text">
                <h3>새로운 방 만들기 준비 중...</h3>
                <p>모든 정보를 초기화하고 있습니다</p>
            </div>
        </div>
    `;

    // 스타일 추가
    loadingOverlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 9999;
        display: flex;
        align-items: center;
        justify-content: center;
    `;

    // 페이지에 추가
    document.body.appendChild(loadingOverlay);
    document.body.style.overflow = 'hidden';
}

// ✨ 개선된 모든 저장된 데이터 정리
function clearAllStoredData() {
    // 기본 방 생성 관련 데이터
    const basicKeys = [
        'selected_people',
        'room_name',
        'room_description',
        'selected_emotions',
        'selected_destination',
        'travel_date_ranges',
        'room_creation_data',
        'final_room_submission'
    ];

    // 임시 저장 데이터
    const tempKeys = [
        'temp_room_name',
        'temp_room_description',
        'temp_selected_people',
        'temp_selected_emotions',
        'temp_selected_destination',
        'temp_selected_schedule'
    ];

    // 모든 키 합치기
    const allKeys = [...basicKeys, ...tempKeys];

    // localStorage와 sessionStorage에서 삭제
    allKeys.forEach(key => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });

    // 메모리상의 데이터도 초기화
    finalRoomData = {};

    // 쿠키 정리 (필요시)
    clearRoomCreationCookies();

    console.log('=== 데이터 초기화 완료 ===');
    console.log('삭제된 localStorage 항목:', allKeys.length);
    console.log('초기화된 메모리 데이터: finalRoomData');
    console.log('========================');
}

// ✨ 방 생성 관련 쿠키 정리
function clearRoomCreationCookies() {
    const cookiesToClear = [
        'room_creation_session',
        'user_preferences',
        'form_auto_save',
        'step_progress'
    ];

    cookiesToClear.forEach(cookieName => {
        // 쿠키 삭제 (만료일을 과거로 설정)
        document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
        document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
    });
}

// ✨ 초기화 확인 함수 (디버깅용)
function verifyDataClearing() {
    console.log('=== 데이터 초기화 검증 ===');

    // localStorage 확인
    const remainingLocalStorage = [];
    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && (key.includes('room') || key.includes('temp') || key.includes('selected'))) {
            remainingLocalStorage.push(key);
        }
    }

    // sessionStorage 확인
    const remainingSessionStorage = [];
    for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i);
        if (key && (key.includes('room') || key.includes('temp') || key.includes('selected'))) {
            remainingSessionStorage.push(key);
        }
    }

    console.log('남은 localStorage 항목:', remainingLocalStorage);
    console.log('남은 sessionStorage 항목:', remainingSessionStorage);
    console.log('finalRoomData 상태:', finalRoomData);
    console.log('========================');

    return remainingLocalStorage.length === 0 && remainingSessionStorage.length === 0;
}

// 모든 저장된 데이터 정리
function clearAllStoredData() {
    // 각 단계별 데이터 삭제
    const keysToRemove = [
        'selected_people',
        'room_name',
        'room_description',
        'selected_emotions',
        'selected_destination',
        'travel_date_ranges',
        'room_creation_data',
        'final_room_submission',
        'temp_room_name',
        'temp_room_description',
        'temp_selected_emotions',
        'temp_selected_destination',
        'temp_selected_schedule'
    ];

    keysToRemove.forEach(key => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });

    console.log('모든 저장된 데이터가 정리되었습니다.');
}

// 도움말 모달 열기
function openHelpModal() {
    const modal = document.getElementById('helpModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

// 도움말 모달 닫기
function closeHelpModal() {
    const modal = document.getElementById('helpModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeHelpModal();
        closeEditSelectionModal(); // ✨ 수정 모달도 ESC로 닫기
    }
});

// 유틸리티 함수들

// 날짜 포맷팅
function formatDate(date) {
    if (!date) return '';

    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');

    return `${year}.${month}.${day}`;
}

// 총 일수 계산
function calculateTotalDays(dateRanges) {
    if (!dateRanges || dateRanges.length === 0) return 0;

    return dateRanges.reduce((total, range) => {
        const startDate = new Date(range.startDate);
        const endDate = new Date(range.endDate);
        const days = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
        return total + days;
    }, 0);
}

// 페이지 떠날 때 데이터 저장
window.addEventListener('beforeunload', function() {
    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    if (roomNameInput && roomNameInput.value.trim()) {
        localStorage.setItem('temp_room_name', roomNameInput.value.trim());
    }

    if (roomIntroTextarea && roomIntroTextarea.value.trim()) {
        localStorage.setItem('temp_room_description', roomIntroTextarea.value.trim());
    }
});

// 페이지 로드 시 임시 저장된 데이터 복원
function restoreTemporaryData() {
    const tempRoomName = localStorage.getItem('temp_room_name');
    const tempRoomIntro = localStorage.getItem('temp_room_description');

    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    if (tempRoomName && roomNameInput && !roomNameInput.value) {
        roomNameInput.value = tempRoomName;
        finalRoomData.roomName = tempRoomName;
        updateCharCount('roomNameInput', 'currentLength', 30);
        localStorage.removeItem('temp_room_name');
    }

    if (tempRoomIntro && roomIntroTextarea && !roomIntroTextarea.value) {
        roomIntroTextarea.value = tempRoomIntro;
        finalRoomData.roomIntro = tempRoomIntro;
        updateCharCount('roomIntroTextarea', 'introCurrentLength', 200);
        localStorage.removeItem('temp_room_description');
    }
}

// 입력 필드 유효성 검사 실시간 피드백
function initializeRealTimeValidation() {
    const roomNameInput = document.getElementById('roomNameInput');
    const roomIntroTextarea = document.getElementById('roomIntroTextarea');

    if (roomNameInput) {
        roomNameInput.addEventListener('input', function() {
            validateRoomNameRealTime(this);
        });

        roomNameInput.addEventListener('blur', function() {
            validateRoomNameFinal(this);
        });
    }

    if (roomIntroTextarea) {
        roomIntroTextarea.addEventListener('input', function() {
            validateRoomIntroRealTime(this);
        });

        roomIntroTextarea.addEventListener('blur', function() {
            validateRoomIntroFinal(this);
        });
    }
}

// 방 이름 실시간 유효성 검사
function validateRoomNameRealTime(input) {
    const value = input.value.trim();
    const maxLength = 30;

    // 글자 수 시각화
    updateCharCount('roomNameInput', 'currentLength', maxLength);

    if (value.length === 0) {
        input.style.borderColor = '#ef5350'; // 빨간색
    } else if (value.length < 2) {
        input.style.borderColor = '#ff9800'; // 주황색
    } else if (value.length > maxLength) {
        input.style.borderColor = '#ef5350'; // 최대 길이 초과
    } else {
        input.style.borderColor = '#66bb6a'; // 초록색
    }
}

// 방 소개 실시간 유효성 검사
function validateRoomIntroRealTime(textarea) {
    const value = textarea.value.trim();
    const maxLength = 200;

    // 글자 수 시각화 (이미 있다면 updateCharCount와 연동)
    updateCharCount('roomIntroTextarea', 'introCurrentLength', maxLength);

    if (value.length === 0) {
        textarea.style.borderColor = '#ef5350'; // 빨간색
    } else if (value.length < 10) {
        textarea.style.borderColor = '#ff9800'; // 주황색
    } else if (value.length > maxLength) {
        textarea.style.borderColor = '#ef5350'; // 최대 길이 초과
    } else {
        textarea.style.borderColor = '#66bb6a'; // 초록색
    }
}

// 방 이름 최종 유효성 검사
function validateRoomNameFinal(input) {
    const value = input.value.trim();

    if (value.length === 0) {
        input.style.borderColor = '#ef5350';
        return false;
    } else if (value.length < 2) {
        input.style.borderColor = '#ef5350';
        return false;
    } else {
        input.style.borderColor = '#66bb6a';
        return true;
    }
}

// 방 소개 최종 유효성 검사
function validateRoomIntroFinal(textarea) {
    const value = textarea.value.trim();

    if (value.length === 0) {
        textarea.style.borderColor = '#ef5350';
        return false;
    } else if (value.length < 10) {
        textarea.style.borderColor = '#ef5350';
        return false;
    } else {
        textarea.style.borderColor = '#66bb6a';
        return true;
    }
}

// 데이터 무결성 검사
function validateDataIntegrity() {
    const issues = [];

    if (!finalRoomData.emotions || finalRoomData.emotions.length === 0) {
        issues.push('감정 태그 데이터가 없습니다.');
    }

    if (!finalRoomData.destination) {
        issues.push('여행지 데이터가 없습니다.');
    }

    if (!finalRoomData.schedule || !finalRoomData.schedule.dateRanges || finalRoomData.schedule.dateRanges.length === 0) {
        issues.push('일정 데이터가 없습니다.');
    }

    if (issues.length > 0) {
        console.warn('데이터 무결성 문제:', issues);
        return false;
    }

    return true;
}

// DOM 로드 완료 후 추가 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 기존 초기화 함수들 실행 후
    setTimeout(() => {
        restoreTemporaryData();
        initializeRealTimeValidation();

        // 데이터 무결성 검사
        if (!validateDataIntegrity()) {
            console.warn('일부 데이터가 누락되었습니다. 사용자에게 알림을 표시해야 할 수 있습니다.');
        }
    }, 100);
});

/*
// 실제 서버 연동 시 에러 처리
function submitRoom() {
    if (!validateFinalData()) {
        return;
    }

    setSubmitButtonLoading(true);
    const submitData = prepareFinalSubmitData();

    // 실제 서버 요청
    fetch('/api/rooms/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRFToken': getCsrfToken() // Django인 경우
        },
        body: JSON.stringify(submitData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`서버 오류: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('서버 응답:', data);
        showSuccessModal();
        clearAllStoredData();
    })
    .catch(error => {
        console.error('제출 실패:', error);

        // 사용자 친화적인 에러 메시지
        if (error.message.includes('Failed to fetch')) {
            showErrorMessage('서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.');
        } else {
            showErrorMessage('방 등록에 실패했습니다. 다시 시도해주세요.');
        }
    })
    .finally(() => {
        setSubmitButtonLoading(false);
    });
}
*/
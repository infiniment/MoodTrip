// 다중 날짜 범위를 저장할 배열
let selectedDateRanges = [];
let selectedStartDate = null;
let selectedEndDate = null;
let previousData = {};

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    loadPreviousData();
    initializeCurrentMonthView(); // 현재 날짜 기준 3개월 표시
    initializeDateSelection();
    initializeMonthSelector();
    initializeAddDateButton(); // 추가 버튼 초기화
    initializeNextButton();
    initializeBackButton();
});

// 현재 날짜 기준 3개월 표시 초기화
function initializeCurrentMonthView() {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth() + 1; // 0부터 시작하므로 +1
    const currentYear = currentDate.getFullYear();

    // 현재 월부터 3개월 계산 (예: 현재가 7월이면 7,8,9월)
    const months = [];
    for (let i = 0; i < 3; i++) {
        const targetMonth = currentMonth + i;
        const targetYear = currentYear + Math.floor((targetMonth - 1) / 12);
        const adjustedMonth = ((targetMonth - 1) % 12) + 1;

        months.push({
            year: targetYear,
            month: adjustedMonth,
            monthValue: `${targetYear}-${String(adjustedMonth).padStart(2, '0')}`
        });
    }

    // 드롭다운 옵션 업데이트
    updateMonthSelectOptions(months);

    // 달력 그리드 업데이트
    updateCalendarGrid(months);
}

// 월 선택 드롭다운 옵션 업데이트
function updateMonthSelectOptions(months) {
    const monthSelect = document.getElementById('monthSelect');
    const monthNames = [
        '', '1월', '2월', '3월', '4월', '5월', '6월',
        '7월', '8월', '9월', '10월', '11월', '12월'
    ];

    // 기본 옵션은 유지하고 현재 월 기준으로 추가
    let optionsHTML = '<option value="">3개월 보기</option>';

    // 현재 월부터 6개월 정도 옵션 추가
    for (let i = 0; i < 6; i++) {
        const targetMonth = months[0].month + i;
        const targetYear = months[0].year + Math.floor((targetMonth - 1) / 12);
        const adjustedMonth = ((targetMonth - 1) % 12) + 1;
        const monthValue = `${targetYear}-${String(adjustedMonth).padStart(2, '0')}`;
        optionsHTML += `<option value="${monthValue}">${targetYear}년 ${monthNames[adjustedMonth]}</option>`;
    }

    monthSelect.innerHTML = optionsHTML;
}

// 달력 그리드 업데이트 (현재 날짜 기준 3개월)
function updateCalendarGrid(months) {
    const calendarGrid = document.getElementById('calendarGrid');
    let gridHTML = '';

    months.forEach(monthInfo => {
        gridHTML += generateMonthCalendarHTML(monthInfo);
    });

    calendarGrid.innerHTML = gridHTML;
}

// 월별 달력 HTML 생성
function generateMonthCalendarHTML(monthInfo) {
    const { year, month, monthValue } = monthInfo;
    const monthNames = [
        '', '1월', '2월', '3월', '4월', '5월', '6월',
        '7월', '8월', '9월', '10월', '11월', '12월'
    ];

    // 해당 월의 정보 계산
    const firstDay = new Date(year, month - 1, 1);
    const lastDay = new Date(year, month, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    // 이전 달 마지막 날들
    const prevMonth = new Date(year, month - 2, 0);
    const prevMonthDays = prevMonth.getDate();

    let calendarHTML = `
  <div class="calendar-month" data-month="${monthValue}">
    <div class="calendar-header">${monthNames[month]}</div>
    <div class="calendar-weekdays">
      <div class="weekday">일</div><div class="weekday">월</div><div class="weekday">화</div>
      <div class="weekday">수</div><div class="weekday">목</div><div class="weekday">금</div><div class="weekday">토</div>
    </div>
    <div class="calendar-days">
`;

    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
        calendarHTML += `<div class="day prev-month">${prevMonthDays - i}</div>`;
    }
    for (let day = 1; day <= daysInMonth; day++) {
        calendarHTML += `<div class="day">${day}</div>`;
    }
    const totalCells = 42;
    const usedCells = startingDayOfWeek + daysInMonth;
    for (let day = 1; day <= totalCells - usedCells; day++) {
        calendarHTML += `<div class="day next-month">${day}</div>`;
    }

    calendarHTML += `
    </div>
  </div>
    `;
    return calendarHTML;

}

// 이전 페이지들에서 선택된 데이터 불러오기
function loadPreviousData() {
    try {
        // 전체 방 생성 데이터 불러오기
        let roomData = localStorage.getItem('room_creation_data');
        if (roomData) {
            previousData = JSON.parse(roomData);
            console.log('이전 단계 데이터:', previousData);
            return;
        }

        // 개별 데이터 불러오기
        const emotions = localStorage.getItem('selected_emotions');
        const destination = localStorage.getItem('selected_destination');

        if (emotions) previousData.emotions = JSON.parse(emotions);
        if (destination) previousData.destination = JSON.parse(destination);

        console.log('이전 단계 데이터 (개별):', previousData);
    } catch (e) {
        console.error('이전 데이터 불러오기 실패:', e);
    }
}

// 날짜 선택 초기화
function initializeDateSelection() {
    const days = document.querySelectorAll('.day:not(.prev-month):not(.next-month)');

    days.forEach(day => {
        day.addEventListener('click', function() {
            // 이미 추가된 날짜는 클릭 무시
            if (this.classList.contains('added-date')) return;

            const dayNumber = parseInt(this.textContent);
            const monthElement = this.closest('.calendar-month');
            const monthValue = monthElement.getAttribute('data-month');
            const [year, month] = monthValue.split('-');

            const clickedDate = new Date(year, month - 1, dayNumber);

            handleDateSelection(clickedDate, this);
        });
    });
}

// 날짜 선택 처리
function handleDateSelection(clickedDate, dayElement) {
    if (!selectedStartDate || (selectedStartDate && selectedEndDate)) {
        // 새로운 선택 시작
        clearCurrentSelection();
        selectedStartDate = clickedDate;
        selectedEndDate = null;
        dayElement.classList.add('range-start', 'selected');
    } else if (selectedStartDate && !selectedEndDate) {
        // 종료 날짜 선택
        if (clickedDate < selectedStartDate) {
            // 시작일보다 이른 날짜를 선택한 경우 시작일로 설정
            clearCurrentSelection();
            selectedStartDate = clickedDate;
            selectedEndDate = null;
            dayElement.classList.add('range-start', 'selected');
        } else {
            // 정상적인 종료일 선택
            selectedEndDate = clickedDate;
            dayElement.classList.add('range-end', 'selected');

            // 범위 표시
            highlightCurrentRange();
        }
    }

    updateCurrentSelectionDisplay();
}

// 현재 선택 초기화 (전체가 아닌 현재 선택만)
function clearCurrentSelection() {
    const allDays = document.querySelectorAll('.day');
    allDays.forEach(day => {
        // 이미 추가된 날짜는 유지하고 현재 선택만 제거
        if (!day.classList.contains('added-date')) {
            day.classList.remove('selected', 'in-range', 'range-start', 'range-end');
        }
    });
}

// 현재 범위 하이라이트
function highlightCurrentRange() {
    if (!selectedStartDate || !selectedEndDate) return;

    const allDays = document.querySelectorAll('.day:not(.prev-month):not(.next-month)');

    allDays.forEach(day => {
        if (day.classList.contains('added-date')) return; // 이미 추가된 날짜는 건드리지 않음

        const dayNumber = parseInt(day.textContent);
        const monthElement = day.closest('.calendar-month');
        const monthValue = monthElement.getAttribute('data-month');
        const [year, month] = monthValue.split('-');

        const dayDate = new Date(year, month - 1, dayNumber);

        if (dayDate > selectedStartDate && dayDate < selectedEndDate) {
            day.classList.add('in-range');
        }
    });
}

// 추가 버튼 초기화
function initializeAddDateButton() {
    // 추가 버튼을 달력 아래에 동적으로 생성
    const calendarGrid = document.getElementById('calendarGrid');
    const addButtonContainer = document.createElement('div');
    addButtonContainer.className = 'add-date-container';
    addButtonContainer.innerHTML = `
      <button type="button" id="addDateRangeButton" class="btn btn-add-date" disabled>
        선택한 날짜 추가
      </button>
      <p class="add-date-guide">날짜 범위를 선택한 후 '추가' 버튼을 눌러주세요</p>
    `;

    // 달력 그리드 다음에 추가
    calendarGrid.parentNode.insertBefore(addButtonContainer, calendarGrid.nextSibling);

    // 추가 버튼 이벤트 리스너
    const addButton = document.getElementById('addDateRangeButton');
    addButton.addEventListener('click', function() {
        addDateRange();
    });
}

// 날짜 범위 추가
function addDateRange() {
    // 시작일이 없으면 무시
    if (!selectedStartDate) return;

    // 종료일이 없으면 당일로 간주 (end = start)
    const start = new Date(selectedStartDate);
    const end   = selectedEndDate ? new Date(selectedEndDate) : new Date(selectedStartDate);
    const newRange = {
        startDate: start,
        endDate: end,
        startDateFormatted: formatDate(start),
        endDateFormatted: formatDate(end)
    };

    // 기존 범위와 겹치는지 확인
    const hasOverlap = selectedDateRanges.some(range => {
        return (newRange.startDate <= range.endDate && newRange.endDate >= range.startDate);
    });

    if (hasOverlap) {
        alert('이미 선택된 날짜와 겹칩니다. 다른 날짜를 선택해주세요.');
        return;
    }

    // 범위 추가
    selectedDateRanges.push(newRange);

    // 추가된 날짜들을 달력에 표시
    markAddedDatesOnCalendar(newRange);

    // 현재 선택 초기화
    clearCurrentSelection();
    selectedStartDate = null;
    selectedEndDate = null;

    // UI 업데이트
    updateSelectedDateDisplay();
    updateAddButton();

    console.log('추가된 날짜 범위:', selectedDateRanges);
}

// 추가된 날짜들을 달력에 표시
function markAddedDatesOnCalendar(dateRange) {
    const allDays = document.querySelectorAll('.day:not(.prev-month):not(.next-month)');
    const isSingle = dateRange.startDate.getTime() === dateRange.endDate.getTime();

    allDays.forEach(day => {
        const dayNumber = parseInt(day.textContent);
        const monthElement = day.closest('.calendar-month');
        const monthValue = monthElement.getAttribute('data-month');
        const [year, month] = monthValue.split('-');

        const dayDate = new Date(year, month - 1, dayNumber);

        if (dayDate >= dateRange.startDate && dayDate <= dateRange.endDate) {
            day.classList.add('added-date');
            day.classList.remove('selected', 'in-range', 'range-start', 'range-end');

            // 추가된 날짜 스타일
            if (isSingle) {
                day.classList.add('added-single-day');  // 당일 표시
                } else if (dayDate.getTime() === dateRange.startDate.getTime()) {
                day.classList.add('added-range-start');
                } else if (dayDate.getTime() === dateRange.endDate.getTime()) {
                day.classList.add('added-range-end');
                } else {
                day.classList.add('added-in-range');
            }
        }
    });
}

// 현재 선택 표시 업데이트 (추가 버튼 활성화/비활성화)
function updateCurrentSelectionDisplay() {
    updateAddButton();
}

// 추가 버튼 상태 업데이트
function updateAddButton() {
    const addButton = document.getElementById('addDateRangeButton');
    const guideText = document.querySelector('.add-date-guide');

    if (selectedStartDate && selectedEndDate) {
        addButton.disabled = false;
        addButton.textContent = `${formatDate(selectedStartDate)} ~ ${formatDate(selectedEndDate)} 추가`;
        guideText.textContent = '선택한 날짜 범위를 추가하시려면 버튼을 눌러주세요';
    } else if (selectedStartDate) {
        // 종료일 선택 전에도 "당일"로 바로 추가 가능
        addButton.disabled = false;
        addButton.textContent = `${formatDate(selectedStartDate)} (당일) 추가`;
        guideText.textContent = '종료일을 고르지 않으면 당일 일정으로 추가됩니다';
    } else {
        addButton.disabled = true;
        addButton.textContent = '선택한 날짜 추가';
        guideText.textContent = '날짜 범위를 선택한 후 \'추가\' 버튼을 눌러주세요';
    }
}

// 선택된 날짜 표시 업데이트 (다중 범위 표시)
function updateSelectedDateDisplay() {
    const selectedDateSection   = document.getElementById('selectedDateSection');
    const selectedDateContainer = document.getElementById('selectedDateContainer');
    if (!selectedDateSection || !selectedDateContainer) return;

    if (selectedDateRanges.length === 0) {
        selectedDateSection.style.display = 'none';
        selectedDateContainer.innerHTML = '';
        return;
    }

    // 선택된 날짜 섹션 표시
    selectedDateSection.style.display = 'block';

    // 범위 아이템 HTML
    const rangeItemsHTML = selectedDateRanges.map((range, index) => {
        const rangeText =
            range.startDateFormatted === range.endDateFormatted
                ? range.startDateFormatted
                : `${range.startDateFormatted} ~ ${range.endDateFormatted}`;

        return `
      <div class="date-range-item">
        <span class="range-text">${rangeText}</span>
        <button type="button" class="delete-range-btn" data-index="${index}">삭제</button>
      </div>
    `;
    }).join('');

    // 총 일수는 먼저 계산
    const totalDays = calculateTotalDays();

    // 컨테이너 업데이트 (한 번만)
    selectedDateContainer.innerHTML = `
    <div class="date-range-display">
      ${rangeItemsHTML}
    </div>
    <div class="date-info-text">
      총 ${selectedDateRanges.length}개 구간, ${totalDays}일이 선택되었습니다!
    </div>
  `;

    // 삭제 버튼에 이벤트 연결
    selectedDateContainer.querySelectorAll('.delete-range-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const index = Number(e.currentTarget.dataset.index);
            deleteDateRange(index);
        });
    });
}

// 총 선택 일수 계산
function calculateTotalDays() {
    return selectedDateRanges.reduce((total, range) => {
        const days = Math.ceil((range.endDate - range.startDate) / (1000 * 60 * 60 * 24)) + 1;
        return total + days;
    }, 0);
}

// 특정 날짜 범위 삭제
function deleteDateRange(index) {
    if (index < 0 || index >= selectedDateRanges.length) return;

    const deletedRange = selectedDateRanges[index];

    // 배열에서 제거
    selectedDateRanges.splice(index, 1);

    // 달력에서 해당 날짜 마크 제거
    removeAddedDatesFromCalendar(deletedRange);

    // UI 업데이트
    updateSelectedDateDisplay();

    console.log('삭제된 날짜 범위:', deletedRange);
    console.log('남은 날짜 범위들:', selectedDateRanges);
}

// 달력에서 추가된 날짜 마크 제거
function removeAddedDatesFromCalendar(dateRange) {
    const allDays = document.querySelectorAll('.day');

    allDays.forEach(day => {
        const dayNumber = parseInt(day.textContent);
        const monthElement = day.closest('.calendar-month');
        if (!monthElement) return;

        const monthValue = monthElement.getAttribute('data-month');
        const [year, month] = monthValue.split('-');

        const dayDate = new Date(year, month - 1, dayNumber);

        if (dayDate >= dateRange.startDate && dayDate <= dateRange.endDate) {
            day.classList.remove('added-date', 'added-range-start', 'added-range-end', 'added-in-range');
        }
    });
}

// 월 선택 드롭다운 초기화
function initializeMonthSelector() {
    const monthSelect = document.getElementById('monthSelect');

    monthSelect.addEventListener('change', function() {
        const selectedMonth = this.value;

        if (selectedMonth === '' || selectedMonth === '3month') {
            // 3개월 보기로 전환
            showThreeMonthView();
        } else {
            // 단일 월 보기로 전환
            showSingleMonthView(selectedMonth);
        }
    });
}

// 3개월 보기 표시
function showThreeMonthView() {
    const calendarGrid = document.getElementById('calendarGrid');
    const singleCalendarContainer = document.getElementById('singleCalendarContainer');

    calendarGrid.style.display = 'grid';
    singleCalendarContainer.style.display = 'none';

    // 기존 이벤트 리스너 재연결
    setTimeout(() => {
        initializeDateSelection();
        restoreAddedDatesDisplay();
    }, 100);
}

// 단일 월 보기 표시
function showSingleMonthView(monthValue) {
    const calendarGrid = document.getElementById('calendarGrid');
    const singleCalendarContainer = document.getElementById('singleCalendarContainer');

    calendarGrid.style.display = 'none';
    singleCalendarContainer.style.display = 'block';

    // 선택된 월의 달력 생성
    generateSingleMonthCalendar(monthValue, singleCalendarContainer);
}

// 단일 월 달력 생성
function generateSingleMonthCalendar(monthValue, container) {
    const [yStr, mStr] = monthValue.split('-');  // 예: "2025-08"
    const year  = Number(yStr);
    const month = Number(mStr); // 1..12

    const monthNames = {
        '01': '1월', '02': '2월', '03': '3월', '04': '4월', '05': '5월', '06': '6월',
        '07': '7월', '08': '8월', '09': '9월', '10': '10월', '11': '11월', '12': '12월'
    };

    // 해당 월 정보
    const firstDay = new Date(year, month - 1, 1);
    const daysInMonth = new Date(year, month, 0).getDate();
    const startingDayOfWeek = firstDay.getDay();

    // 이전 달 마지막 날짜 (✔️ month-1, day=0)
    const prevMonthDays = new Date(year, month - 1, 0).getDate();

    let calendarHTML = `
    <div class="calendar-month" data-month="${monthValue}">
      <div class="calendar-header">${monthNames[mStr]}</div>
      <div class="calendar-weekdays">
        <div class="weekday">일</div>
        <div class="weekday">월</div>
        <div class="weekday">화</div>
        <div class="weekday">수</div>
        <div class="weekday">목</div>
        <div class="weekday">금</div>
        <div class="weekday">토</div>
      </div>
      <div class="calendar-days">
  `;

    // 이전 달 날짜(회색)
    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
        calendarHTML += `<div class="day prev-month">${prevMonthDays - i}</div>`;
    }

    // 현재 달 날짜
    for (let day = 1; day <= daysInMonth; day++) {
        calendarHTML += `<div class="day">${day}</div>`;
    }

    // 다음 달 날짜(회색) — 총 42칸(6주) 채우기
    const totalCells = 42;
    const filledCells = startingDayOfWeek + daysInMonth;
    const remainingCells = totalCells - filledCells;

    for (let day = 1; day <= remainingCells; day++) {
        calendarHTML += `<div class="day next-month">${day}</div>`;
    }

    calendarHTML += `
      </div>
    </div>
  `;

    container.innerHTML = calendarHTML;

    // 새로 생성된 달력에 이벤트 연결 및 기존 추가 범위 복원
    setTimeout(() => {
        initializeDateSelection();
        restoreAddedDatesDisplay();
    }, 0);
}

// 추가된 날짜들을 달력에 다시 표시
function restoreAddedDatesDisplay() {
    selectedDateRanges.forEach(range => {
        markAddedDatesOnCalendar(range);
    });
}

// 날짜 포맷팅 (YYYY.MM.DD)
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

// 날짜 정보 포맷팅 (YY년 M월 D일)
function formatDateInfo(date) {
    const year = String(date.getFullYear()).slice(-2);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${year}년 ${month}월 ${day}일`;
}

// 다음 버튼 초기화
function initializeNextButton() {
    const nextButton = document.getElementById('nextButton');
    if (nextButton) {
        nextButton.addEventListener('click', function(e) {
            e.preventDefault();

            // 유효성 검사 - 다중 범위 체크
            if (selectedDateRanges.length === 0) {
                alert('최소 하나 이상의 여행 날짜를 추가해주세요.');
                return;
            }

            // 데이터 저장 및 다음 페이지로 이동
            saveScheduleForNextPage();
            goToNextPage();
        });
    }
}

// 뒤로 가기 버튼 초기화
function initializeBackButton() {
    // 뒤로 가기 버튼은 HTML에서 onclick으로 처리됨
}

// 뒤로 가기 함수
function goToPreviousPage() {
    // 현재 선택된 일정 임시 저장
    if (selectedDateRanges.length > 0) {
        localStorage.setItem('temp_selected_schedule', JSON.stringify(selectedDateRanges));
    }

    // 이전 페이지로 이동
    window.location.href = "/companion-rooms/attraction";
}

// 다음 페이지로 전달할 일정 데이터 저장
function saveScheduleForNextPage() {
    const scheduleData = {
        dateRanges: selectedDateRanges,
        totalDays: calculateTotalDays(),
        rangeCount: selectedDateRanges.length
    };

    const combinedData = {
        ...previousData,
        schedule: scheduleData,
        timestamp: new Date().toISOString()
    };

    // 로컬 스토리지에 저장
    localStorage.setItem('travel_date_ranges', JSON.stringify(scheduleData));
    localStorage.setItem('room_creation_data', JSON.stringify(combinedData));

    // 세션 스토리지에도 백업 저장
    sessionStorage.setItem('travel_date_ranges', JSON.stringify(scheduleData));
    sessionStorage.setItem('room_creation_data', JSON.stringify(combinedData));

    console.log('최종 방 생성 데이터 저장 완료:', combinedData);
}

// 다음 페이지로 이동 (최종 완료 페이지)
function goToNextPage() {
    // 실제 완료 페이지 URL로 변경해주세요
    window.location.href = "/companion-rooms/final";

    // 또는 최종 등록 처리
    // submitRoomCreation();
}

// 폼 유효성 검사 (HTML에서 onsubmit으로 호출됨)
function validationPhase(form) {
    if (selectedDateRanges.length === 0) {
        alert('최소 하나 이상의 여행 날짜를 추가해주세요.');
        return false;
    }

    // 폼 제출 준비
    prepareFormSubmission();
    saveScheduleForNextPage();

    return true;
}

// 폼 제출 시 선택된 데이터를 hidden input에 추가
function prepareFormSubmission() {
    const form = document.getElementById('temporary_room_phase_4');

    // 기존 hidden input들 제거
    const existingInputs = form.querySelectorAll('input[name="selected_schedule"], input[name="room_creation_data"]');
    existingInputs.forEach(input => input.remove());

    // 선택된 일정을 hidden input으로 추가
    if (selectedDateRanges.length > 0) {
        const scheduleInput = document.createElement('input');
        scheduleInput.type = 'hidden';
        scheduleInput.name = 'selected_schedule';
        scheduleInput.value = JSON.stringify({
            dateRanges: selectedDateRanges,
            totalDays: calculateTotalDays(),
            rangeCount: selectedDateRanges.length
        });
        form.appendChild(scheduleInput);
    }

    // 전체 방 생성 데이터도 hidden input으로 추가
    const roomDataInput = document.createElement('input');
    roomDataInput.type = 'hidden';
    roomDataInput.name = 'room_creation_data';
    roomDataInput.value = JSON.stringify(previousData);
    form.appendChild(roomDataInput);
}

// 다른 페이지에서 저장된 일정 데이터 불러오기
function getSelectedScheduleFromPreviousPage() {
    try {
        let schedule = localStorage.getItem('travel_date_ranges');
        if (schedule) {
            return JSON.parse(schedule);
        }

        schedule = sessionStorage.getItem('travel_date_ranges');
        if (schedule) {
            return JSON.parse(schedule);
        }

        return null;
    } catch (e) {
        console.error('저장된 일정 데이터 불러오기 실패:', e);
        return null;
    }
}

// 최종 방 생성 데이터 불러오기
function getFinalRoomCreationData() {
    try {
        let data = localStorage.getItem('room_creation_data');
        if (data) {
            return JSON.parse(data);
        }

        data = sessionStorage.getItem('room_creation_data');
        if (data) {
            return JSON.parse(data);
        }

        return null;
    } catch (e) {
        console.error('최종 방 생성 데이터 불러오기 실패:', e);
        return null;
    }
}

// 임시 저장된 일정 복원
function restoreTemporarySchedule() {
    const tempSchedule = localStorage.getItem('temp_selected_schedule');
    if (tempSchedule) {
        try {
            const schedule = JSON.parse(tempSchedule);
            selectedDateRanges = schedule;

            // UI 업데이트
            updateSelectedDateDisplay();
            restoreAddedDatesDisplay();

            localStorage.removeItem('temp_selected_schedule');
        } catch (e) {
            console.error('임시 저장된 일정 데이터 복원 실패:', e);
        }
    }
}

// 최종 방 생성 제출
function submitRoomCreation() {
    const finalData = getFinalRoomCreationData();

    if (!finalData) {
        alert('방 생성 데이터가 없습니다. 처음부터 다시 시작해주세요.');
        return;
    }

    console.log('방 생성 제출:', finalData);

    // 실제 서버 제출 로직
    // fetch('/api/rooms/create', {
    //     method: 'POST',
    //     headers: { 'Content-Type': 'application/json' },
    //     body: JSON.stringify(finalData)
    // });

    // 성공 후 데이터 정리
    clearAllData();
}

// 모든 저장된 데이터 정리
function clearAllData() {
    localStorage.removeItem('selected_emotions');
    localStorage.removeItem('selected_destination');
    localStorage.removeItem('travel_date_ranges');
    localStorage.removeItem('room_creation_data');
    localStorage.removeItem('temp_selected_schedule');

    sessionStorage.removeItem('selected_emotions');
    sessionStorage.removeItem('selected_destination');
    sessionStorage.removeItem('travel_date_ranges');
    sessionStorage.removeItem('room_creation_data');
}

// 도움말 모달 열기
function openHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// 도움말 모달 닫기
function closeHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeHelpModal();
    }
});
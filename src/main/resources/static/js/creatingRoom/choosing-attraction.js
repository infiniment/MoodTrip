let selectedDestination = null;
let previousEmotions = [];

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    loadPreviousEmotions();
    initializeDestinationSelection();
    initializeDestinationSearch();
    initializeNextButton();
    initializeBackButton();

    // 나중에 관광지 상세 페이지에서 방만들기 버튼을 눌렀을 때 해당 관광지에 대한 정보 localstorage로 받았을 때 처리
    preselectDestinationIfNeeded();
});

// 이전 페이지에서 선택된 감정들 불러오기
function loadPreviousEmotions() {
    try {
        // 로컬 스토리지에서 먼저 시도
        let emotions = localStorage.getItem('selected_emotions');
        if (emotions) {
            previousEmotions = JSON.parse(emotions);
            console.log('이전 단계에서 선택된 감정들:', previousEmotions);
            return;
        }
        
        // 세션 스토리지에서 백업 데이터 시도
        emotions = sessionStorage.getItem('selected_emotions');
        if (emotions) {
            previousEmotions = JSON.parse(emotions);
            console.log('이전 단계에서 선택된 감정들 (세션):', previousEmotions);
            return;
        }
        
        console.log('이전 단계 감정 데이터가 없습니다.');
    } catch (e) {
        console.error('이전 감정 데이터 불러오기 실패:', e);
    }
}

// 관광지 선택 초기화(나중에 관광지 테이블에서 데이터를 가져오면 그 관광지의 아이디가 넘어가게 해야됨)
function initializeDestinationSelection() {
    const destinationRadios = document.querySelectorAll('.destination-radio');
    
    destinationRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                const destinationName = this.value;
                const destinationCard = this.closest('.destination-card');
                const destinationInfo = destinationCard.querySelector('.destination-info');
                
                // 선택된 관광지 정보 저장
                selectedDestination = {
                    name: destinationName,
                    category: destinationInfo.querySelector('.destination-category').textContent,
                    description: destinationInfo.querySelector('.destination-description').textContent,
                    image: destinationCard.querySelector('.destination-image img').src
                };
                
                // UI 업데이트
                updateSelectedDestinationDisplay();
            }
        });
        
        // 라벨 클릭 시 선택 취소 기능 추가
        const label = radio.nextElementSibling;
        if (label && label.classList.contains('destination-label')) {
            label.addEventListener('click', function(e) {
                // 이미 선택된 라디오 버튼을 다시 클릭한 경우
                if (radio.checked) {
                    e.preventDefault(); // 기본 라벨 동작 방지
                    
                    // 라디오 버튼 해제
                    radio.checked = false;
                    
                    // 선택된 관광지 초기화
                    selectedDestination = null;
                    
                    // UI 업데이트
                    updateSelectedDestinationDisplay();
                }
            });
        }
    });
}

// 관광지 검색 기능
function initializeDestinationSearch() {
    const searchInput = document.getElementById('destinationInput');
    const filterSelect = document.getElementById('regionFilter');
    
    // 검색 입력 이벤트
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.trim().toLowerCase();
            filterDestinations(searchTerm);
        });
    }
    
    // 필터 변경 이벤트
    if (filterSelect) {
        filterSelect.addEventListener('change', function() {
            const filterType = this.value;
            applyFilter(filterType);
        });
    }
}

// 관광지 필터링
function filterDestinations(searchTerm) {
    const destinationCards = document.querySelectorAll('.destination-card');
    
    destinationCards.forEach(card => {
        const destinationName = card.dataset.destination.toLowerCase();
        const categoryText = card.querySelector('.destination-category').textContent.toLowerCase();
        const descriptionText = card.querySelector('.destination-description').textContent.toLowerCase();
        
        const isMatch = destinationName.includes(searchTerm) || 
                       categoryText.includes(searchTerm) || 
                       descriptionText.includes(searchTerm);
        
        if (isMatch || searchTerm === '') {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// 필터 적용
function applyFilter(filterType) {
    const destinationCards = document.querySelectorAll('.destination-card');
    const cardsArray = Array.from(destinationCards);
    const container = document.querySelector('.destination-results-grid');
    
    // 정렬 로직 (실제로는 서버에서 데이터를 받아와야 함)
    switch(filterType) {
        case 'popular':
            console.log('인기순 정렬 적용');
            break;
        case 'distance':
            console.log('거리순 정렬 적용');
            break;
        case 'recent':
            console.log('최신순 정렬 적용');
            break;
        default:
            console.log('추천순 정렬 적용');
            break;
    }
}

// 선택된 관광지 표시 업데이트
function updateSelectedDestinationDisplay() {
    const container = document.getElementById('selectedDestinationContainer');
    const infoContainer = document.getElementById('selectedDestinationInfo');
    
    if (!selectedDestination) {
        if (container) container.style.display = 'none';
        return;
    }
    
    if (!infoContainer) return;
    
    // 선택된 관광지 정보 HTML 생성
    infoContainer.innerHTML = `
        <div class="destination-image">
            <img src="${selectedDestination.image}" alt="${selectedDestination.name}">
        </div>
        <div class="destination-details">
            <div class="destination-name">${selectedDestination.name}</div>
            <div class="destination-category">${selectedDestination.category}</div>
            <div class="destination-description">${selectedDestination.description}</div>
        </div>
    `;
    
    if (container) container.style.display = 'block';
}

// 다음 버튼 초기화
function initializeNextButton() {
    const nextButton = document.getElementById('nextButton');
    if (nextButton) {
        nextButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 유효성 검사
            if (!selectedDestination) {
                alert('관광지를 선택해주세요.');
                return;
            }
            
            // 데이터 저장 및 다음 페이지로 이동
            saveDestinationForNextPage();
            goToNextPage();
        });
    }
}

// 뒤로 가기 버튼 초기화
function initializeBackButton() {
    // 뒤로 가기 버튼은 HTML에서 onclick으로 처리됨
}

// 뒤로 가기 함수 - 확인 메시지 없이 바로 이동
function goToPreviousPage() {
    // 현재 선택된 관광지 임시 저장 (조용히 저장)
    if (selectedDestination) {
        localStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
    }
    
    // 바로 이전 페이지로 이동 (확인 메시지 없음)
    window.location.href = '/companion-rooms/emotion';
}

// 다음 페이지로 전달할 관광지 데이터 저장
function saveDestinationForNextPage() {
    const combinedData = {
        emotions: previousEmotions,
        destination: selectedDestination,
        timestamp: new Date().toISOString()
    };
    
    // 로컬 스토리지에 저장
    localStorage.setItem('selected_destination', JSON.stringify(selectedDestination));
    localStorage.setItem('room_creation_data', JSON.stringify(combinedData));
    
    // 세션 스토리지에도 백업 저장
    sessionStorage.setItem('selected_destination', JSON.stringify(selectedDestination));
    sessionStorage.setItem('room_creation_data', JSON.stringify(combinedData));
    
    console.log('다음 페이지로 전달할 데이터 저장 완료:', combinedData);
}

// 다음 페이지로 이동
function goToNextPage() {
    window.location.href = '/companion-rooms/schedule';
}

// 폼 유효성 검사
function validationPhase(form) {
    if (!selectedDestination) {
        alert('관광지를 선택해주세요.');
        return false;
    }
    
    prepareFormSubmission();
    saveDestinationForNextPage();
    
    return true;
}

// 폼 제출 시 선택된 데이터를 hidden input에 추가
function prepareFormSubmission() {
    const form = document.getElementById('temporary_room_phase_3');
    
    if (!form) return;
    
    // 기존 hidden input들 제거
    const existingInputs = form.querySelectorAll('input[name="selected_destination"], input[name="previous_emotions"]');
    existingInputs.forEach(input => input.remove());
    
    // 선택된 관광지를 hidden input으로 추가
    if (selectedDestination) {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'selected_destination';
        hiddenInput.value = JSON.stringify(selectedDestination);
        form.appendChild(hiddenInput);
    }
    
    // 이전 단계 감정들도 hidden input으로 추가
    if (previousEmotions.length > 0) {
        const emotionInput = document.createElement('input');
        emotionInput.type = 'hidden';
        emotionInput.name = 'previous_emotions';
        emotionInput.value = JSON.stringify(previousEmotions);
        form.appendChild(emotionInput);
    }
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
    }
});

// 빠른 분위기별 필터링
function filterByMood(mood) {
    const destinationCards = document.querySelectorAll('.destination-card');
    
    // 모든 카드 표시
    destinationCards.forEach(card => {
        card.style.display = 'block';
    });
    
    // 분위기별 필터링 로직 (실제로는 서버에서 데이터를 받아와야 함)
    switch(mood) {
        case 'energetic':
            console.log('에너지 넘치는 곳 필터링');
            break;
        case 'peaceful':
            console.log('평화로운 곳 필터링');
            break;
        case 'cultural':
            console.log('문화적인 곳 필터링');
            break;
        case 'trendy':
            console.log('트렌디한 곳 필터링');
            break;
    }
    
    // 모달 닫기
    closeHelpModal();
}

// 데이터 정리
function clearAllData() {
    localStorage.removeItem('selected_emotions');
    localStorage.removeItem('selected_destination');
    localStorage.removeItem('room_creation_data');
    localStorage.removeItem('temp_selected_destination');
    
    sessionStorage.removeItem('selected_emotions');
    sessionStorage.removeItem('selected_destination');
    sessionStorage.removeItem('room_creation_data');
}

function preselectDestinationIfNeeded() {
    const preselectedName = localStorage.getItem('preselected_destination_name');
    if (!preselectedName) return;

    const radios = document.querySelectorAll('.destination-radio');
    for (const radio of radios) {
        if (radio.value === preselectedName) {
            radio.checked = true;
            radio.dispatchEvent(new Event('change')); // 선택 UI 업데이트
            console.log('사전 선택된 관광지 자동 적용:', preselectedName);
            break;
        }
    }

    // 자동 선택 후에는 다시 저장하지 않도록 삭제
    localStorage.removeItem('preselected_destination_name');
}

// 페이지 떠날 때 자동 저장 (사용자가 모르게)
window.addEventListener('beforeunload', function() {
    if (selectedDestination) {
        localStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
    }
});
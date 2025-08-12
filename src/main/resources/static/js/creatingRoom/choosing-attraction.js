let selectedDestination = null;
let previousEmotions = [];
let searchTimer;
let originalGridHTML = '';

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    loadPreviousEmotions();
    initializeDestinationSelection();
    initializeDestinationSearch();
    initializeNextButton();
    initializeBackButton();

    // 나중에 관광지 상세 페이지에서 방만들기 버튼을 눌렀을 때 해당 관광지에 대한 정보 localstorage로 받았을 때 처리
    preselectDestinationIfNeeded();

    const grid = document.getElementById('destinationResults');
    if (grid) originalGridHTML = grid.innerHTML;
    // 페이징 섹션
    setupPaginationFromGrid();
});

function restoreOriginalGrid() {
    const grid = document.getElementById('destinationResults');
    if (!grid || !originalGridHTML) return;

    grid.innerHTML = originalGridHTML;            // 초기 카드 복원
    initializeDestinationSelection();             // 이벤트 다시 연결
    setupPaginationFromGrid();                    // 페이징 다시 계산
}

function getActiveFilters() {
    const usp = new URLSearchParams(location.search);
    return {
        areaCode: usp.get('areaCode'),
        sigunguCode: usp.get('sigunguCode'),
        contentTypeId: usp.get('contentTypeId'),
    };
}

// hidden 필드 레퍼런스
function hiddenRefs() {
    return {
        id:  document.getElementById('destContentId'),
        ttl: document.getElementById('destTitle'),
        addr:document.getElementById('destAddr'),
        img: document.getElementById('destImage'),
        lat: document.getElementById('destLat'),
        lon: document.getElementById('destLon'),
        cat: document.getElementById('destCategory'),
    };
}

// hidden 필드 채우기
function fillHiddenFromMeta(meta) {
    const h = hiddenRefs();
    if (!meta || !h.id) return;

    const d = meta.dataset; // <div class="hidden" ... data-*> 에서 가져옴
    h.id.value  = d.contentId || '';
    h.ttl.value = d.title || '';
    h.addr.value= ((d.addr1 || '') + ' ' + (d.addr2 || '')).trim();
    h.img.value = d.img || '';
    // TourAPI: mapY=위도, mapX=경도
    h.lat.value = d.mapy || '';
    h.lon.value = d.mapx || '';
    h.cat.value = 'ATTRACTION';
}

// 해제 시 hidden 초기화
function clearHidden() {
    const h = hiddenRefs();
    [h.id,h.ttl,h.addr,h.img,h.lat,h.lon].forEach(el => el && (el.value=''));
}

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

    // 카드가 0개면 조용히 종료 (콘솔 깨끗하게)
    if (!destinationRadios || destinationRadios.length === 0) {
        return;
    }
    
    destinationRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                const destinationCard = this.closest('.destination-card');
                const destinationInfo = destinationCard.querySelector('.destination-info');
                const meta = destinationCard.querySelector('.hidden'); // ← 타임리프로 심어둔 data-*

                selectedDestination = {
                    // 화면 표시용
                    name: this.value,
                    category: destinationInfo.querySelector('.destination-category')?.textContent || '',
                    description: destinationInfo.querySelector('.destination-description')?.textContent || '',
                    image: destinationCard.querySelector('.destination-image img')?.src || '',
                    // 서버 전송/다음단계용(있으면)
                    contentId: meta?.dataset.contentId || null,
                    addr1: meta?.dataset.addr1 || '',
                    addr2: meta?.dataset.addr2 || '',
                    mapX: meta?.dataset.mapx || null, // 경도
                    mapY: meta?.dataset.mapy || null, // 위도
                    contentTypeId: meta?.dataset.type || null,
                };

                // hidden 필드 채우기
                fillHiddenFromMeta(meta);

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

                    // hidden 초기화
                    clearHidden();
                    
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

    if (searchInput) {
        searchInput.addEventListener('input', function () {
            clearTimeout(searchTimer);
            const q = this.value.trim();
            searchTimer = setTimeout(() => runSearch(q), 350);
        });

        // 브라우저 X 버튼(clear) or ESC 로 비웠을 때도 즉시 복원
        searchInput.addEventListener('search', () => runSearch(''));
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                searchInput.value = '';
                runSearch('');
            }
        });
    }

    if (filterSelect) {
        filterSelect.addEventListener('change', function () {
            // 필요 시 정렬 로직
        });
    }
}

async function runSearch(q) {
    const grid = document.getElementById('destinationResults');
    if (!grid) return;

    const { areaCode, sigunguCode, contentTypeId } = getActiveFilters();

    // 검색어가 비고, 활성화된 필터도 없으면 초기 상태로 복귀
    if (!q || q.length === 0) {
        if (!areaCode && !sigunguCode && !contentTypeId) {
            restoreOriginalGrid();
            return;
        }
        // 필터만 있는 경우에는 서버 검색 계속 수행
    }

    try {
        const params = new URLSearchParams();
        if (q) params.set('q', q);
        if (areaCode) params.set('areaCode', areaCode);
        if (sigunguCode) params.set('sigunguCode', sigunguCode);
        if (contentTypeId) params.set('contentTypeId', contentTypeId);

        const res = await fetch('/api/attractions/search?' + params.toString());
        if (!res.ok) throw new Error('검색 실패');
        const items = await res.json();

        renderAttractions(items);
        initializeDestinationSelection();
        setupPaginationFromGrid();
    } catch (e) {
        console.error(e);
        grid.innerHTML = `<div class="empty">검색 중 오류가 발생했습니다.</div>`;
    }
}

function renderAttractions(items) {
    const grid = document.getElementById('destinationResults');
    if (!grid) return;

    if (!items || items.length === 0) {
        grid.innerHTML = `<div class="empty">검색 결과가 없습니다. 다른 키워드로 검색해보세요.</div>`;
        return;
    }

    const html = items.map((a, idx) => {
        const imgEmpty = !a.firstImage || a.firstImage.trim() === '';
        const imgTag = imgEmpty
            ? `
        <div class="destination-image is-empty">
          <div class="image-placeholder">
            <img src="/image/creatingRoom/landscape-placeholder-svgrepo-com.svg" alt="">
          </div>
        </div>`
            : `
        <div class="destination-image">
          <img src="${a.firstImage}" alt="${a.title || ''}">
        </div>`;

        const desc = (a.tel && a.tel !== 'null') ? a.tel
            : (a.addr2 && a.addr2 !== 'null') ? a.addr2
                : '';

        return `
      <div class="destination-card" data-destination="${a.title || ''}">
        <input type="radio" class="destination-radio" name="destination"
               id="dest-${idx}" value="${a.title || ''}">
        <label class="destination-label" for="dest-${idx}">
          ${imgTag}
          <div class="destination-info">
            <div class="destination-category">${a.addr1 || '주소 정보 없음'}</div>
            <div class="destination-name">${a.title || ''}</div>
            ${desc ? `<div class="destination-description">${desc}</div>` : ``}
          </div>
        </label>
        <div class="hidden"
             data-content-id="${a.contentId || ''}"
             data-title="${a.title || ''}"
             data-addr1="${a.addr1 || ''}"
             data-addr2="${a.addr2 || ''}"
             data-img="${a.firstImage || ''}"
             data-mapx="${a.mapX || ''}"
             data-mapy="${a.mapY || ''}"
             data-type="${a.contentTypeId || ''}">
        </div>
      </div>
    `;
    }).join('');

    grid.innerHTML = html;
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

    if (!selectedDestination || !infoContainer) {
        if (container) container.style.display = 'none';
        return;
    }

    const imgSrc   = selectedDestination.image || '/image/creatingRoom/landscape-placeholder-svgrepo-com.svg';
    const category = selectedDestination.category && selectedDestination.category !== 'null'
        ? selectedDestination.category : '';
    const desc     = selectedDestination.description && selectedDestination.description !== 'null'
        ? selectedDestination.description : '';

    infoContainer.innerHTML = `
    <div class="destination-image">
      <img src="${imgSrc}" alt="${selectedDestination.name}"
           onerror="this.onerror=null;this.src='/image/creatingRoom/landscape-placeholder-svgrepo-com.svg';">
    </div>
    <div class="destination-details">
      <div class="destination-name">${selectedDestination.name}</div>
      ${category ? `<div class="destination-category">${category}</div>` : ``}
      ${desc ? `<div class="destination-description">${desc}</div>` : ``}
    </div>
  `;

    container.style.display = 'block';
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

// ===== Pagination =====
let currentPage = 1;
const pageSize = 9; // 한번에 보여줄 카드 수 (그리드 3열이면 9가 보기 좋아요)

/** 현재 그리드 상태로 페이지네이션(번호/범위/버튼)을 다시 구성 */
function setupPaginationFromGrid() {
    const container = document.getElementById('paginationContainer');
    const grid = document.getElementById('destinationResults');
    if (!container || !grid) return;

    const cards = Array.from(grid.querySelectorAll('.destination-card'));
    const totalItems = cards.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

    // 아이템 없거나 한 페이지만 있으면 감추기
    if (totalItems === 0 || totalPages <= 1) {
        container.hidden = true;
        // 모두 보이도록 (검색 직후 0개일 때도 안전)
        cards.forEach(c => (c.style.display = 'block'));
        return;
    }
    container.hidden = false;

    // 번호 버튼 생성
    const numbers = container.querySelector('.pagination-numbers');
    numbers.innerHTML = '';
    currentPage = Math.min(currentPage, totalPages); // 범위 보정
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'pagination-number' + (i === currentPage ? ' active' : '');
        btn.textContent = i;
        btn.dataset.page = i;
        btn.addEventListener('click', () => renderPage(i));
        numbers.appendChild(btn);
    }

    // 이전/다음 핸들러
    const prevBtn = container.querySelector('.pagination-button.prev');
    const nextBtn = container.querySelector('.pagination-button.next');
    prevBtn.onclick = () => currentPage > 1 && renderPage(currentPage - 1);
    nextBtn.onclick = () => currentPage < totalPages && renderPage(currentPage + 1);

    // 첫 렌더
    renderPage(currentPage);
}

/** 주어진 페이지의 카드만 보이게 */
function renderPage(page) {
    const container = document.getElementById('paginationContainer');
    const grid = document.getElementById('destinationResults');
    if (!container || !grid) return;

    const cards = Array.from(grid.querySelectorAll('.destination-card'));
    const totalItems = cards.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

    currentPage = Math.max(1, Math.min(page, totalPages));

    const start = (currentPage - 1) * pageSize;
    const end = Math.min(start + pageSize, totalItems);

    cards.forEach((card, idx) => {
        card.style.display = (idx >= start && idx < end) ? 'block' : 'none';
    });

    // 번호 active 상태
    container.querySelectorAll('.pagination-number').forEach(btn => {
        btn.classList.toggle('active', Number(btn.dataset.page) === currentPage);
    });

    // prev/next disabled
    container.querySelector('.pagination-button.prev').disabled = currentPage === 1;
    container.querySelector('.pagination-button.next').disabled = currentPage === totalPages;

    // 범위 텍스트
    const range = container.querySelector('.pagination-info .range');
    const totalEl = container.querySelector('.pagination-info .total');
    if (range) range.textContent = `${start + 1} - ${end} / `;
    if (totalEl) totalEl.textContent = String(totalItems);

    // 뷰 상단으로 살짝 스크롤
    grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
}
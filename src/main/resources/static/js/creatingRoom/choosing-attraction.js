let selectedDestination = null;
let previousEmotions = [];
let searchTimer;

let navIntent = 'unknown';

// ===== Pagination =====
let currentPage = 1;
let serverPage = 0;
const serverSize = 9;  // (3열 × 3행)
let lastQuery = '';

const PREFILL_TTL_MS =  60 * 10 * 1000;

// DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 새 방문(초진입) 판별
    const nav = performance.getEntriesByType && performance.getEntriesByType('navigation')[0];
    const isFreshVisit = !nav || nav.type === 'navigate';    // 주소 입력/새 탭/일반 링크 등
    const wantsRestore = sessionStorage.getItem('restore_attraction') === '1';
    const hasPrefill   = !!getRoomPrefill({ consume: false }); // 상세 -> 프리필 진입인지

    // 새 방문이면서 내부 복원/프리필이 아니라면, 남아 있던 선택값을 정리
    if (isFreshVisit && !wantsRestore && !hasPrefill) {
        ['selected_destination','temp_selected_destination','room_creation_data'].forEach(k => {
            sessionStorage.removeItem(k);
            localStorage.removeItem(k); // 혹시 다른 페이지가 쓴 흔적도 제거
        });
    }
    purgeExpiredPrefillAndSelection();
    applyPrefillIfAny();
    if (!__prefillApplied) {
        restoreSelectionOnLoad();
    }
    loadPreviousEmotions();
    initializeDestinationSelection();
    initializeDestinationSearch();
    initializeNextButton();
    initializeBackButton();


    runSearchPaged('', 0); // 전체 목록 1페이지(9개) 서버에서 받아오기
    // 첫 렌더 직후 프리필 한 번 시도
});

function updateResultCount(total) {
    const el = document.getElementById('searchCount');
    if (el) el.textContent = `${total}개 검색어 추천`;
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
    h.id.value  = d.attractionId || '';
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
                    attractionId: meta?.dataset.attractionId
                        ? Number(meta.dataset.attractionId) : null,
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
                sessionStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));

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
            searchTimer = setTimeout(() => runSearchPaged(q, 0), 350); // ← 변경
        });

        searchInput.addEventListener('search', () => runSearchPaged('', 0));  // ← 변경
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                searchInput.value = '';
                runSearchPaged('', 0); // ← 변경
            }
        });
    }

    if (filterSelect) {
        filterSelect.addEventListener('change', function () {
            // 필요 시 정렬 로직
        });
    }
}

// 화면폭에 따라 모드/버튼 수 결정
function paginationMode() {
    const w = window.innerWidth;
    if (w <= 600) return { mode: 'compact', max: 0 }; // 모바일
    if (w <= 1024) return { mode: 'normal',  max: 5 }; // 태블릿
    return { mode: 'normal', max: 7 };                 // 데스크톱
}

let __pageState = { totalPages: 0, current: 0, totalElements: 0 };

function setupServerPagination(totalPages, current, totalElements) {
    __pageState = { totalPages, current, totalElements };

    const container = document.getElementById('paginationContainer');
    if (!container) return;

    if (!totalPages || totalPages <= 1) { container.hidden = true; return; }
    container.hidden = false;

    current = Math.max(0, Math.min(current, totalPages - 1));

    const { mode, max } = paginationMode();

    const numbers = container.querySelector('.pagination-numbers');
    numbers.innerHTML = ''; // 초기화

    // 이전/다음 버튼
    const prevBtn = container.querySelector('.pagination-button.prev');
    const nextBtn = container.querySelector('.pagination-button.next');
    if (prevBtn) {
        prevBtn.disabled = current <= 0;
        prevBtn.onclick = () => current > 0 && runSearchPaged(lastQuery, current - 1);
    }
    if (nextBtn) {
        nextBtn.disabled = current >= totalPages - 1;
        nextBtn.onclick = () => current < totalPages - 1 && runSearchPaged(lastQuery, current + 1);
    }

    // --- 모바일: 콤팩트 표시 (예: 12 / 623) ---
    if (mode === 'compact') {
        const compact = document.createElement('div');
        compact.className = 'pagination-compact';
        compact.innerHTML = `<strong class="current">${current + 1}</strong> / <span class="total">${totalPages}</span>`;
        numbers.appendChild(compact);
    } else {
        // --- 데스크톱/태블릿: 숫자 버튼 ---
        const maxButtons = Math.max(3, max);
        let start = Math.max(0, current - Math.floor(maxButtons / 2));
        let end   = start + maxButtons - 1;

        if (end > totalPages - 1) {
            end = totalPages - 1;
            start = Math.max(0, end - (maxButtons - 1));
        }

        const makePageBtn = (pageIdx, cur) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'pagination-number' + (pageIdx === cur ? ' active' : '');
            btn.textContent = String(pageIdx + 1);
            btn.dataset.page = pageIdx;
            btn.addEventListener('click', () => runSearchPaged(lastQuery, pageIdx));
            return btn;
        };
        const makeEllipsis = () => {
            const span = document.createElement('span');
            span.className = 'pagination-ellipsis';
            span.textContent = '…';
            return span;
        };

        if (start > 0) {
            numbers.appendChild(makePageBtn(0, current));     // 첫 페이지
            if (start > 1) numbers.appendChild(makeEllipsis());
        }
        for (let i = start; i <= end; i++) numbers.appendChild(makePageBtn(i, current));
        if (end < totalPages - 1) {
            if (end < totalPages - 2) numbers.appendChild(makeEllipsis());
            numbers.appendChild(makePageBtn(totalPages - 1, current)); // 마지막
        }
    }

    // 하단 범위/총개수
    const size = serverSize;
    const startIdx = current * size + 1;
    const endIdx   = Math.min((current + 1) * size, totalElements || 0);
    const range = container.querySelector('.pagination-info .range');
    const totalEl = container.querySelector('.pagination-info .total');
    if (range)  range.textContent = `${startIdx} - ${endIdx} / `;
    if (totalEl) totalEl.textContent = String(totalElements ?? 0);
}

// 창 크기 변경 시 자동 재렌더
window.addEventListener('resize', () => {
    if (__pageState.totalPages > 0) {
        setupServerPagination(__pageState.totalPages, __pageState.current, __pageState.totalElements);
    }
});

async function runSearchPaged(q, page = 0) {
    const grid = document.getElementById('destinationResults');
    if (!grid) return;
    const { areaCode, sigunguCode, contentTypeId } = getActiveFilters();

    lastQuery = q;
    serverPage = page;


    try {
        const params = new URLSearchParams();
        if (q) params.set('q', q);
        if (areaCode) params.set('areaCode', areaCode);
        if (sigunguCode) params.set('sigunguCode', sigunguCode);
        if (contentTypeId) params.set('contentTypeId', contentTypeId);
        params.set('page', page);
        params.set('size', serverSize);

        const res = await fetch('/api/attractions/search-paged?' + params.toString());
        if (!res.ok) throw new Error('검색 실패');

        const data = await res.json(); // { content, page, size, totalElements, totalPages }
        const totalElements = Number(data.totalElements ?? 0);
        const totalPagesCalc = totalElements === 0 ? 0 : Math.ceil(totalElements / serverSize);
        const safePage = Math.max(0, Math.min(Number(data.page ?? 0), Math.max(0, totalPagesCalc - 1)));
        renderAttractions(data.content);
        initializeDestinationSelection();
        updateResultCount(totalElements);
        const container = document.getElementById('paginationContainer');
        if (totalElements === 0) {
            if (container) {
                container.hidden = true;
                const nums = container.querySelector('.pagination-numbers');
                if (nums) nums.innerHTML = '';
                const prev = container.querySelector('.pagination-button.prev');
                const next = container.querySelector('.pagination-button.next');
                if (prev) prev.disabled = true;
                if (next) next.disabled = true;
                const range = container.querySelector('.pagination-info .range');
                const total = container.querySelector('.pagination-info .total');
                if (range) range.textContent = `0 - 0 / `;
                if (total) total.textContent = `0`;
            }
            return; // ← 여기서 끝
        }

        setupServerPagination(totalPagesCalc, safePage, totalElements);
        applyPrefillIfAny();
        recheckRadioFromSelected();
    } catch (e) {
        console.error(e);
        grid.innerHTML = `<div class="empty">검색 중 오류가 발생했습니다.</div>`;
        // 페이징 영역 숨김
        const container = document.getElementById('paginationContainer');
        if (container) container.hidden = true;
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
             data-attraction-id="${a.attractionId || ''}"
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

    const imgSrc = selectedDestination.image || '/image/creatingRoom/landscape-placeholder-svgrepo-com.svg';

    // ① address/tel 을 우선 fallback 으로 사용
    const address =
        selectedDestination.address ||
        [selectedDestination.addr1, selectedDestination.addr2].filter(Boolean).join(' ').trim();

    const category =
        (selectedDestination.category && selectedDestination.category !== 'null')
            ? selectedDestination.category
            : address || '';                           // ← addr 없으면 빈값

    const desc =
        (selectedDestination.description && selectedDestination.description !== 'null')
            ? selectedDestination.description
            : (selectedDestination.tel || '');         // ← tel 을 설명으로 fallback

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

            navIntent = 'next';
            sessionStorage.removeItem('restore_attraction');


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
        sessionStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
        sessionStorage.setItem('restore_attraction', '1');
    }

    // 바로 이전 페이지로 이동 (확인 메시지 없음)
    window.location.href = '/companion-rooms/emotion';
}

// 다음 페이지로 전달할 관광지 데이터 저장
function saveDestinationForNextPage() {
    const payload = {
        emotions: previousEmotions,
        destination: {
            ...selectedDestination,
            // 안전하게 address/tel이 비어 있으면 다시 구성
            address:
                selectedDestination.address ||
                [selectedDestination.addr1, selectedDestination.addr2].filter(Boolean).join(' ').trim(),
            tel: selectedDestination.tel || ''
        },
        timestamp: new Date().toISOString()
    };

    sessionStorage.setItem('selected_destination', JSON.stringify(payload.destination));
    sessionStorage.setItem('room_creation_data', JSON.stringify(payload));
}

// 다음 페이지로 이동
function goToNextPage() {
    sessionStorage.removeItem('temp_selected_destination');
    sessionStorage.removeItem('restore_attraction');
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


function applyPrefillIfAny() {
    if (__prefillApplied) return;

    const p = getRoomPrefill({ consume: false }); // ← 여기서 소비(지움)
    if (!p) return;

    __prefillApplied = true;
    const cid = Number(p.contentId ?? p.attraction?.contentId);
    if (!Number.isFinite(cid)) return;

    // 1) 현재 페이지 그리드에 카드가 이미 있으면 그걸 체크
    const metaEl = document.querySelector(`.destination-card .hidden[data-content-id="${cid}"]`);
    if (metaEl) {
        const d = metaEl.dataset;
        const cardInfo = metaEl.closest('.destination-card')?.querySelector('.destination-info');
        const cardDesc = cardInfo?.querySelector('.destination-description')?.textContent?.trim() || '';
        const radio    = metaEl.closest('.destination-card')?.querySelector('.destination-radio'); // ← 누락 버그 수정

        if (radio) radio.checked = true;

        selectedDestination = {
            attractionId: d.attractionId ? Number(d.attractionId) : null,
            contentId: cid,
            name: d.title || '',
            image: d.img || '',
            addr1: d.addr1 || '',
            addr2: d.addr2 || '',
            address: [d.addr1, d.addr2].filter(Boolean).join(' ').trim(),
            category: d.addr1 || '',
            description: cardDesc || d.addr2 || '',
            tel: cardDesc || '',
            mapX: d.mapx ? Number(d.mapx) : null,
            mapY: d.mapy ? Number(d.mapy) : null,
            contentTypeId: d.type ? Number(d.type) : null,
            origin: 'prefill',
            originTs: Date.now()
        };
        fillHiddenFromMeta(metaEl);
        updateSelectedDestinationDisplay();
        try {
            sessionStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
            sessionStorage.setItem('restore_attraction', '1');
        } catch (_) {}
        schedulePrefillCleanup();
        return;
    }

    // 2) 현재 페이지에 카드가 없으면 상세 API로 채워서 표시만 함(라디오는 없음)
    fetch(`/api/attractions/content/${cid}/detail`, { headers: { Accept: 'application/json' } })
        .then(r => r.ok ? r.json() : null)
        .then(d => {
            if (!d) return;
            selectedDestination = {
                attractionId: null,
                contentId: cid,
                name: d.title || '',
                image: d.image || '',
                address: d.addr || '',
                tel: d.tel || '',
                category: d.addr || '',
                description: d.tel || '',
                origin: 'prefill',
                originTs: Date.now()
            };
            const dummy = document.createElement('div');
            dummy.dataset.contentId = String(cid);
            dummy.dataset.title = selectedDestination.name;
            dummy.dataset.addr1 = selectedDestination.address;
            dummy.dataset.addr2 = '';
            dummy.dataset.img = selectedDestination.image;
            fillHiddenFromMeta(dummy);
            updateSelectedDestinationDisplay();
            try {
                sessionStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
                sessionStorage.setItem('restore_attraction', '1');
            } catch (_) {}
            schedulePrefillCleanup();
        });
}


// function restoreSelectionOnLoad() {
//     // 1) 상세 프리필이 유효하면 복원하지 않고 프리필에 맡김
//     const pf = getRoomPrefill({ consume: false });
//     if (pf) return;
//     // 2) 사용자가 고른 임시 선택이 있으면 조건없이 복원 시도
//     const raw =
//     sessionStorage.getItem('temp_selected_destination') ||
//     sessionStorage.getItem('selected_destination');
//
//     if (!raw) return;
//     try {
//         const sel = JSON.parse(raw);
//         if (sel?.origin === 'prefill') return; // prefill 유래는 복원 금지
//         selectedDestination = sel;
//         updateSelectedDestinationDisplay();
//     } catch (e) {
//         console.warn('restoreSelectionOnLoad parse fail', e);
//     }
// }

// 교체 후
function restoreSelectionOnLoad() {
    const nav = performance.getEntriesByType && performance.getEntriesByType('navigation')[0];
    const isReloadOrBF = !!nav && (nav.type === 'reload' || nav.type === 'back_forward');

    // 유효한 프리필이 남아있거나, 리로드/뒤로오기이거나, 복원 플래그가 있으면 복원
    const hasValidPrefill = !!getRoomPrefill({ consume: false });

    const hasSavedSelection =
        !!sessionStorage.getItem('selected_destination') ||
        !!sessionStorage.getItem('temp_selected_destination');

    const allowRestore =
        isReloadOrBF ||
        hasValidPrefill ||
        sessionStorage.getItem('restore_attraction') === '1' ||
        hasSavedSelection;                   // ← 추가

    if (!allowRestore) return;

    const raw =
        sessionStorage.getItem('temp_selected_destination') ||
        sessionStorage.getItem('selected_destination');

    if (!raw) return;
    try {
        const sel = JSON.parse(raw);
        // ★ 프리필 유래라도 복원 허용
        selectedDestination = sel;
        updateSelectedDestinationDisplay();
        recheckRadioFromSelected();
    } catch (e) {
        console.warn('restoreSelectionOnLoad parse fail', e);
    }
}

function recheckRadioFromSelected() {
    if (!selectedDestination) return;

    const { contentId, attractionId } = selectedDestination;
    let metaEl = null;
    if (contentId) {
        metaEl = document.querySelector(`.destination-card .hidden[data-content-id="${contentId}"]`);
    }
    if (!metaEl && attractionId) {
        metaEl = document.querySelector(`.destination-card .hidden[data-attraction-id="${attractionId}"]`);
    }
    if (metaEl) {
        const radio = metaEl.closest('.destination-card')?.querySelector('.destination-radio');
        if (radio) radio.checked = true;
    }
}


let __prefillApplied = false;
let __prefillTimer = null;

function getRoomPrefill({ consume = true, ttlMs = PREFILL_TTL_MS } = {}) {
    const raw = sessionStorage.getItem('room_prefill'); // ← 세션만 사용
    if (!raw) return null;

    let p; try { p = JSON.parse(raw); } catch { return null; }

    const fromDetail = sessionStorage.getItem('prefill_from_detail') === '1';
    const okSource   = p?.source === 'attraction-detail';
    const now = Date.now();
    const exp = Number(p.exp || 0);
    const ts  = Number(p.ts || 0);
    const fresh = exp ? (now <= exp) : (ts ? (now - ts) <= ttlMs : true);

    if (!fromDetail || !okSource || !fresh) {
        // 만료/출처불일치면 깨끗이 정리
        sessionStorage.removeItem('room_prefill');
        sessionStorage.removeItem('prefill_from_detail');
        return null;
    }

    if (consume) {
        sessionStorage.removeItem('room_prefill');
        sessionStorage.removeItem('prefill_from_detail');
    }
    return p;
}

function schedulePrefillCleanup() {
    clearTimeout(__prefillTimer);
    if (!selectedDestination || selectedDestination.origin !== 'prefill') return;

    const remain = Math.max(0, selectedDestination.originTs + PREFILL_TTL_MS - Date.now());
    __prefillTimer = setTimeout(() => {
        if (selectedDestination && selectedDestination.origin === 'prefill') {
            selectedDestination = null;
            clearHidden();
            updateSelectedDestinationDisplay();
        }
    }, remain);
}

function purgeExpiredPrefillAndSelection() {
    const nav = performance.getEntriesByType && performance.getEntriesByType('navigation')[0];
    const isBF = !!nav && nav.type === 'back_forward';
    if (isBF || sessionStorage.getItem('restore_attraction') === '1') {
        return; // 뒤로 오기: 기존 선택 보존
    }

    const raw = sessionStorage.getItem('room_prefill') || localStorage.getItem('room_prefill');
    let pf = null;
    try { pf = raw ? JSON.parse(raw) : null; } catch {}

    const now = Date.now();
    const ts  = Number(pf?.ts  || 0);   // 상세에서 저장할 때 넣은 ts
    const exp = Number(pf?.exp || 0);   // 상세에서 저장할 때 넣은 exp(선택)
    const fromDetail = sessionStorage.getItem('prefill_from_detail') === '1';

    // ts/exp가 없으면 만료로 간주(테스트 편의)
    const fresh = pf && (exp ? now <= exp : (ts ? (now - ts) <= PREFILL_TTL_MS : false));
    const isValidPrefill = pf && fromDetail && fresh;

    if (!isValidPrefill) {
        // 1) 프리필 제거
        sessionStorage.removeItem('room_prefill');
        sessionStorage.removeItem('prefill_from_detail');
        localStorage.removeItem('room_prefill');

        // 2) 프리필 기반으로 만들어진 선택 저장값도 제거
        const saved =
            sessionStorage.getItem('selected_destination') ||
            localStorage.getItem('selected_destination')  ||
            localStorage.getItem('temp_selected_destination');

        if (saved) {
            try {
                const sel = JSON.parse(saved);
                const shouldDelete =
                    sel?.origin === 'prefill' &&
                    !isBF &&
                    sessionStorage.getItem('restore_attraction') !== '1';

                if (shouldDelete) {
                    sessionStorage.removeItem('selected_destination');
                    localStorage.removeItem('selected_destination');
                    localStorage.removeItem('temp_selected_destination');
                }
            } catch {}
        }

        // 3) 메모리에 들고 있는 것도 프리필 유래면 지우기
        if (window.selectedDestination?.origin === 'prefill'  &&
            !isBF &&
            sessionStorage.getItem('restore_attraction') !== '1') {
            window.selectedDestination = null;
            if (typeof updateSelectedDestinationDisplay === 'function') {
                updateSelectedDestinationDisplay();
            }
        }
    }
}


// function preselectDestinationIfNeeded() {
//     const preselectedName = localStorage.getItem('preselected_destination_name');
//     if (!preselectedName) return;
//
//     const radios = document.querySelectorAll('.destination-radio');
//     for (const radio of radios) {
//         if (radio.value === preselectedName) {
//             radio.checked = true;
//             radio.dispatchEvent(new Event('change')); // 선택 UI 업데이트
//             console.log('사전 선택된 관광지 자동 적용:', preselectedName);
//             break;
//         }
//     }
//
//     // 자동 선택 후에는 다시 저장하지 않도록 삭제
//     localStorage.removeItem('preselected_destination_name');
// }

// 페이지 떠날 때 자동 저장 (사용자가 모르게)
// window.addEventListener('beforeunload', function() {
//     if (selectedDestination) {
//         localStorage.setItem('temp_selected_destination', JSON.stringify(selectedDestination));
//     }
// });

document.addEventListener('click', (e) => {
    const a = e.target.closest('a[href]');
    if (!a) return;
    const href = a.getAttribute('href') || '';
    if (href.startsWith('/companion-rooms/schedule')) {
        navIntent = 'next';
        sessionStorage.removeItem('restore_attraction');
    } else if (href.startsWith('/companion-rooms/emotion')) {
        navIntent = 'back';
        sessionStorage.setItem('restore_attraction', '1');
    } else {
        navIntent = 'leave';
    }
});


window.addEventListener('beforeunload', () => {
    if (navIntent === 'next') {
        // 다음 단계로 이동: 유지(이미 sessionStorage에 있음)
        sessionStorage.removeItem('temp_selected_destination');
        sessionStorage.removeItem('restore_attraction');
        // 프리필 관련만 정리
        sessionStorage.removeItem('room_prefill');
        sessionStorage.removeItem('prefill_from_detail');

    } else if (navIntent === 'back') {
        // 이전 단계로 돌아갈 때: 복원 플래그만
        sessionStorage.setItem('restore_attraction', '1');

    } else if (navIntent === 'leave') {
        // ★ 사이트 이탈: 선택값까지 싹 정리 (로컬/세션 모두)
        ['selected_destination','temp_selected_destination','room_creation_data'].forEach(k => {
            sessionStorage.removeItem(k);
            localStorage.removeItem(k);
        });
        sessionStorage.removeItem('restore_attraction');
        sessionStorage.removeItem('room_prefill');
        sessionStorage.removeItem('prefill_from_detail');

    } else {
        // 리로드 등: 유지(그대로 두어야 새로고침 후 복원됨)
        sessionStorage.removeItem('temp_selected_destination');
        sessionStorage.removeItem('restore_attraction');
    }
});

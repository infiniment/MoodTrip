// // 방 데이터 (실제로는 서버에서 가져올 데이터)
// let roomsData = [
//     {
//         id: 1,
//         title: "한성대학교 캠퍼스 투어!!!",
//         location: "서울",
//         date: "7월 셋째주",
//         dateValue: new Date('2025-07-21'),
//         views: "3명이 봄",
//         viewCount: 3,
//         description: "안녕하세요! 저희는 세상 최고 멋쟁이 김상우가 다니고 있는 한성대학교 캠퍼스 투어로 떠납니다!!",
//         tags: ["설렘", "역사", "야경"],
//         currentParticipants: 2,
//         maxParticipants: 8,
//         createdDate: "25/07/21 ~ 25/07/25",
//         image: "/static/image/enteringRoom/hansung.png",
//         category: "학교",
//         urgent: false
//     },
//     {
//         id: 2,
//         title: "고양 종합 운동장",
//         location: "경기",
//         date: "11월 둘째주",
//         dateValue: new Date('2025-11-10'),
//         views: "11명이 봄",
//         viewCount: 11,
//         description: "최근에 방탄소년단이 전부 전역했다는 소식 다 들었죠!!! 방탄소년단 콘서트가 11월 둘쨰주에 있다고 해요! 저랑 떠나실 분들 떠나요!!",
//         tags: ["행복", "떨림", "설렘"],
//         currentParticipants: 3,
//         maxParticipants: 4,
//         createdDate: "25/11/21 ~ 25/11/27",
//         image: "/static/image/enteringRoom/stadium.png",
//         category: "스타디움",
//         urgent: false
//     },
//     {
//         id: 3,
//         title: "고양 킨텍스",
//         location: "경기",
//         date: "9월 첫째주",
//         dateValue: new Date('2025-09-01'),
//         views: "31명이 봄",
//         viewCount: 31,
//         description: "킨텍스에서 싸이 워터밤이 열립니다!!!!!!!!!!!!!시원하게 노실 분 구해요!",
//         tags: ["힐링", "행복", "설렘"],
//         currentParticipants: 1,
//         maxParticipants: 2,
//         createdDate: "25/09/01 ~ 25/09/07",
//         image: "/static/image/enteringRoom/kintex.png",
//         category: "스타디움",
//         urgent: true
//     }
// ];
//
// // 현재 날짜
// const currentDate = new Date('2025-07-02');
//
// // 현재 필터 및 정렬 상태
// let currentFilter = 'all';
// let currentSort = 'default';
// let currentPage = 1;
// let currentPeopleFilter = 'all';
// let currentRegionFilter = 'all';
// let filteredRooms = [...roomsData];
// let currentDetailRoomId = null;
// let currentReportRoomId = null;
//
// // 방 상태 계산 함수
// function calculateRoomStatus(room) {
//     const occupancyRate = room.currentParticipants / room.maxParticipants;
//     if (occupancyRate >= 0.5) {
//         return { status: '마감임박', urgent: true };
//     } else {
//         return { status: '모집중', urgent: false };
//     }
// }

// 방 데이터 상태 업데이트
function updateRoomStatuses() {
    roomsData.forEach(room => {
        const { status, urgent } = calculateRoomStatus(room);
        room.status = status;
        room.urgent = urgent;
    });
}

// DOM 로드 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    updateRoomStatuses();
    initializeEventListeners();
    renderRooms();
    updateResultsCount();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 검색 기능
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    
    if (searchButton && searchInput) {
        searchButton.addEventListener('click', handleSearch);
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }

    // 필터 탭
    const filterTabs = document.querySelectorAll('.filter-tab');
    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const filter = this.getAttribute('data-filter');
            setActiveFilter(filter);
            applyFilters();
        });
    });

    // 지역별 서브 필터
    const regionFilterTabs = document.querySelectorAll('.region-filter-tab');
    regionFilterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const regionFilter = this.getAttribute('data-region');
            setActiveRegionFilter(regionFilter);
            applyFilters();
        });
    });

    // 인원별 서브 필터
    const peopleFilterTabs = document.querySelectorAll('.people-filter-tab');
    peopleFilterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const peopleFilter = this.getAttribute('data-people');
            setActivePeopleFilter(peopleFilter);
            applyFilters();
        });
    });

    // 정렬 드롭다운
    const sortButton = document.getElementById('sortButton');
    const sortMenu = document.getElementById('sortMenu');
    const sortOptions = document.querySelectorAll('.sort-option');

    if (sortButton && sortMenu) {
        sortButton.addEventListener('click', function() {
            sortMenu.classList.toggle('show');
        });

        sortOptions.forEach(option => {
            option.addEventListener('click', function() {
                const sort = this.getAttribute('data-sort');
                setActiveSort(sort);
                applySorting();
                renderRooms();
                updatePagination();
                sortMenu.classList.remove('show');
            });
        });

        // 외부 클릭 시 드롭다운 닫기
        document.addEventListener('click', function(e) {
            if (!sortButton.contains(e.target) && !sortMenu.contains(e.target)) {
                sortMenu.classList.remove('show');
            }
        });
    }

    // 마감 임박 체크박스
    const urgentOnly = document.getElementById('urgentOnly');
    if (urgentOnly) {
        urgentOnly.addEventListener('change', applyFilters);
    }

    // 페이지네이션
    initializePagination();
}

// 방 상세보기 모달 열기
function viewRoomDetail(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (room) {
        currentDetailRoomId = roomId;
        openDetailModal(room);
        
        // 조회수 증가
        room.viewCount += 1;
        room.views = `${room.viewCount}명이 봄`;
    }
}

// 상세보기 모달 열기
function openDetailModal(room) {
    const modal = document.getElementById('detailModal');
    if (!modal) return;
    
    // 모달 내용 업데이트
    const elements = {
        detailRoomImage: document.getElementById('detailRoomImage'),
        detailRoomStatus: document.getElementById('detailRoomStatus'),
        detailRoomTitle: document.getElementById('detailRoomTitle'),
        detailRoomLocation: document.getElementById('detailRoomLocation'),
        detailRoomDate: document.getElementById('detailRoomDate'),
        detailRoomParticipants: document.getElementById('detailRoomParticipants'),
        detailRoomViews: document.getElementById('detailRoomViews'),
        detailRoomPeriod: document.getElementById('detailRoomPeriod'),
        detailRoomDesc: document.getElementById('detailRoomDesc'),
        detailRoomTags: document.getElementById('detailRoomTags')
    };

    if (elements.detailRoomImage) {
        elements.detailRoomImage.src = room.image;
        elements.detailRoomImage.alt = room.title;
    }
    if (elements.detailRoomStatus) {
        elements.detailRoomStatus.textContent = room.status;
        elements.detailRoomStatus.className = `detail-room-status ${room.urgent ? 'urgent' : ''}`;
    }
    if (elements.detailRoomTitle) elements.detailRoomTitle.textContent = room.title;
    if (elements.detailRoomLocation) elements.detailRoomLocation.textContent = room.location;
    if (elements.detailRoomDate) elements.detailRoomDate.textContent = room.date;
    if (elements.detailRoomParticipants) elements.detailRoomParticipants.textContent = `${room.currentParticipants} / ${room.maxParticipants}명`;
    if (elements.detailRoomViews) elements.detailRoomViews.textContent = room.views;
    if (elements.detailRoomPeriod) elements.detailRoomPeriod.textContent = room.createdDate;
    if (elements.detailRoomDesc) elements.detailRoomDesc.textContent = room.description;
    
    // 태그 업데이트
    if (elements.detailRoomTags) {
        elements.detailRoomTags.innerHTML = room.tags.map(tag => `<span class="tag"># ${tag}</span>`).join('');
    }
    
    // 모달 표시
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
}

// 상세보기 모달 닫기
function closeDetailModal() {
    const modal = document.getElementById('detailModal');
    if (modal) {
        modal.style.display = 'none';
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = 'auto';
        currentDetailRoomId = null;
    }
}

// 모달에서 입장 신청 (상세보기 모달에서 바로 입장 신청 모달 열기)
function applyFromModal() {
    if (currentDetailRoomId) {
        const room = roomsData.find(r => r.id === currentDetailRoomId);
        if (room) {
            closeDetailModal();
            openApplicationModal(room);
        }
    }
}

// 검색 처리
function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;
    
    const searchTerm = searchInput.value.toLowerCase().trim();
    
    if (searchTerm === '') {
        filteredRooms = [...roomsData];
    } else {
        filteredRooms = roomsData.filter(room => 
            room.title.toLowerCase().includes(searchTerm) ||
            room.description.toLowerCase().includes(searchTerm) ||
            room.location.toLowerCase().includes(searchTerm) ||
            room.tags.some(tag => tag.toLowerCase().includes(searchTerm))
        );
    }
    
    currentPage = 1;
    applySorting();
    renderRooms();
    updateResultsCount();
    updatePagination();
}

// 필터 적용
function applyFilters() {
    const urgentOnly = document.getElementById('urgentOnly');
    const urgentChecked = urgentOnly ? urgentOnly.checked : false;
    
    filteredRooms = roomsData.filter(room => {
        // 마감 임박 보기 필터
        if (urgentChecked && !room.urgent) {
            return false;
        }
        
        // 카테고리 필터
        if (currentFilter === 'all') {
            return true;
        } else if (currentFilter === 'nearby') {
            if (currentRegionFilter === 'all') {
                return true;
            } else {
                return room.location.includes(currentRegionFilter);
            }
        } else if (currentFilter === 'popular') {
            if (currentPeopleFilter === 'all') {
                return true;
            } else if (currentPeopleFilter === '2') {
                return room.maxParticipants === 2;
            } else if (currentPeopleFilter === '4') {
                return room.maxParticipants === 4;
            } else if (currentPeopleFilter === 'other') {
                return room.maxParticipants > 4;
            }
        }
        
        return true;
    });
    
    currentPage = 1;
    applySorting();
    renderRooms();
    updateResultsCount();
    updatePagination();
}

// 정렬 적용
function applySorting() {
    switch (currentSort) {
        case 'nearest':
            filteredRooms.sort((a, b) => {
                const aDiff = Math.abs(a.dateValue.getTime() - currentDate.getTime());
                const bDiff = Math.abs(b.dateValue.getTime() - currentDate.getTime());
                return aDiff - bDiff;
            });
            break;
        case 'popular':
            filteredRooms.sort((a, b) => {
                return b.viewCount - a.viewCount;
            });
            break;
        default:
            filteredRooms.sort((a, b) => {
                if (a.urgent !== b.urgent) {
                    return a.urgent ? 1 : -1;
                }
                return a.id - b.id;
            });
    }
    
    currentPage = 1;
}

// 활성 필터 설정
function setActiveFilter(filter) {
    currentFilter = filter;
    
    // UI 업데이트
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-filter="${filter}"]`);
    if (activeTab) activeTab.classList.add('active');
    
    // 지역별 필터 섹션 표시/숨김
    const regionFilterSection = document.getElementById('regionFilterSection');
    if (regionFilterSection) {
        if (filter === 'nearby') {
            regionFilterSection.style.display = 'block';
        } else {
            regionFilterSection.style.display = 'none';
            currentRegionFilter = 'all';
            setActiveRegionFilter('all');
        }
    }
    
    // 인원별 필터 섹션 표시/숨김
    const peopleFilterSection = document.getElementById('peopleFilterSection');
    if (peopleFilterSection) {
        if (filter === 'popular') {
            peopleFilterSection.style.display = 'block';
        } else {
            peopleFilterSection.style.display = 'none';
            currentPeopleFilter = 'all';
            setActivePeopleFilter('all');
        }
    }
}

// 활성 지역별 필터 설정
function setActiveRegionFilter(regionFilter) {
    currentRegionFilter = regionFilter;
    
    document.querySelectorAll('.region-filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-region="${regionFilter}"]`);
    if (activeTab) activeTab.classList.add('active');
}

// 활성 인원별 필터 설정
function setActivePeopleFilter(peopleFilter) {
    currentPeopleFilter = peopleFilter;
    
    document.querySelectorAll('.people-filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`[data-people="${peopleFilter}"]`);
    if (activeTab) activeTab.classList.add('active');
}

// 활성 정렬 설정
function setActiveSort(sort) {
    currentSort = sort;
    
    document.querySelectorAll('.sort-option').forEach(option => {
        option.classList.remove('active');
    });
    const activeOption = document.querySelector(`[data-sort="${sort}"]`);
    if (activeOption) activeOption.classList.add('active');
    
    // 버튼 텍스트 업데이트
    const sortTexts = {
        'default': '기본 정렬 순',
        'nearest': '가까운 날짜 순',
        'popular': '인기순'
    };
    
    const sortButton = document.getElementById('sortButton');
    if (sortButton && sortButton.firstChild) {
        sortButton.firstChild.textContent = sortTexts[sort] + ' ';
    }
}

// 방 목록 렌더링
function renderRooms() {
    const roomList = document.getElementById('roomList');
    if (!roomList) return;
    
    if (filteredRooms.length === 0) {
        roomList.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #64748b;">
                <h3>검색 결과가 없습니다</h3>
                <p>다른 키워드로 검색해보세요.</p>
            </div>
        `;
        return;
    }
    
    // 페이지네이션 적용
    const itemsPerPage = 5;
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageRooms = filteredRooms.slice(startIndex, endIndex);
    
    roomList.innerHTML = pageRooms.map(room => `
        <div class="room-card" data-room-id="${room.id}">
            <div class="room-image">
                <img src="${room.image}" alt="${room.title}" onerror="this.src='https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=300&h=200&fit=crop'">
                <div class="room-status ${room.urgent ? 'urgent' : ''}">${room.status}</div>
            </div>
            <div class="room-content">
                <div class="room-header">
                    <h3 class="room-title">${room.title}</h3>
                    <div class="room-meta">
                        <span class="room-location">${room.location}</span>
                        <span class="room-date">${room.date}</span>
                        <span class="room-views">${room.views}</span>
                    </div>
                </div>
                <div class="room-description">${room.description}</div>
                <div class="room-tags">
                    ${room.tags.map(tag => `<span class="tag"># ${tag}</span>`).join('')}
                </div>
                <div class="room-footer">
                    <div class="room-participants">
                        <span class="participants-label">인원현재</span>
                        <span class="participants-count">${room.currentParticipants} / ${room.maxParticipants}</span>
                    </div>
                    <div class="room-date-info">
                        <span class="created-date">${room.createdDate}</span>
                    </div>
                    <div class="room-actions">
                        <button class="btn-detail" onclick="viewRoomDetail(${room.id})" aria-label="방 상세보기">상세보기</button>
                        <button class="btn-apply" onclick="applyRoom(${room.id})" aria-label="방 입장 신청">입장 신청</button>
                        <button class="btn-report-card" onclick="reportRoomFromCard(${room.id})" aria-label="방 신고하기">신고</button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// 결과 개수 업데이트
function updateResultsCount() {
    const resultsCount = document.getElementById('resultsCount');
    if (resultsCount) {
        resultsCount.textContent = filteredRooms.length.toLocaleString();
    }
}

// 페이지네이션 초기화
function initializePagination() {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                renderRooms();
                updatePagination();
            }
        });
    }
    
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            const totalPages = Math.ceil(filteredRooms.length / 5);
            if (currentPage < totalPages) {
                currentPage++;
                renderRooms();
                updatePagination();
            }
        });
    }
    
    // 페이지 번호 클릭
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('page-btn') && e.target.getAttribute('data-page')) {
            const page = parseInt(e.target.getAttribute('data-page'));
            currentPage = page;
            renderRooms();
            updatePagination();
        }
    });
}

// 페이지네이션 업데이트
function updatePagination() {
    const totalPages = Math.ceil(filteredRooms.length / 5);
    const pagination = document.getElementById('pagination');
    
    if (!pagination) return;
    
    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }
    
    pagination.style.display = 'flex';
    
    // 페이지 버튼 업데이트
    const pageButtons = document.querySelectorAll('.page-btn[data-page]');
    pageButtons.forEach(btn => {
        const page = parseInt(btn.getAttribute('data-page'));
        btn.classList.toggle('active', page === currentPage);
    });
    
    // 이전/다음 버튼 상태
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    
    if (prevBtn) prevBtn.disabled = currentPage === 1;
    if (nextBtn) nextBtn.disabled = currentPage === totalPages;
}

// 방 입장 신청하기
function applyRoom(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (room) {
        openApplicationModal(room);
    }
}

// 신청 모달 열기
function openApplicationModal(room) {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;
    
    const modalRoomTitle = document.getElementById('modalRoomTitle');
    const modalRoomMeta = document.getElementById('modalRoomMeta');
    
    if (modalRoomTitle) modalRoomTitle.textContent = room.title;
    if (modalRoomMeta) modalRoomMeta.textContent = `${room.location} | ${room.currentParticipants}/${room.maxParticipants}명 | ${room.createdDate}`;
    
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    modal.setAttribute('data-room-id', room.id);
    document.body.style.overflow = 'hidden';
}

// 신청 모달 닫기
function closeApplicationModal() {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;
    
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = 'auto';
    
    // 폼 초기화
    const applicationMessage = document.getElementById('applicationMessage');
    if (applicationMessage) applicationMessage.value = '';
}

// 신청 제출
function submitApplication() {
    const modal = document.getElementById('applicationModal');
    if (!modal) return;
    
    const roomId = parseInt(modal.getAttribute('data-room-id'));
    const messageElement = document.getElementById('applicationMessage');
    
    if (!messageElement) return;
    
    const message = messageElement.value.trim();
    
    if (!message) {
        alert('신청 메시지를 입력해주세요.');
        return;
    }
    
    const room = roomsData.find(r => r.id === roomId);
    if (!room) return;
    
    // 실제로는 서버에 신청 데이터 전송
    console.log('신청 데이터:', {
        roomId: roomId,
        roomTitle: room.title,
        message: message,
        timestamp: new Date().toISOString()
    });
    
    alert(`"${room.title}" 방에 입장 신청이 완료되었습니다!\n방장의 승인을 기다려주세요.`);
    closeApplicationModal();
}

// 방 신고하기 (카드에서만)
function reportRoomFromCard(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (room) {
        openReportModal(room);
    }
}

// 신고 모달 열기
function openReportModal(room) {
    const modal = document.getElementById('reportModal');
    if (!modal) return;
    
    currentReportRoomId = room.id;
    
    const reportRoomTitle = document.getElementById('reportRoomTitle');
    const reportRoomMeta = document.getElementById('reportRoomMeta');
    
    if (reportRoomTitle) reportRoomTitle.textContent = room.title;
    if (reportRoomMeta) reportRoomMeta.textContent = `${room.location} | ${room.currentParticipants}/${room.maxParticipants}명`;
    
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
}

// 신고 모달 닫기
function closeReportModal() {
    const modal = document.getElementById('reportModal');
    if (!modal) return;
    
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = 'auto';
    currentReportRoomId = null;
    
    // 폼 초기화
    const reportReason = document.getElementById('reportReason');
    const reportMessage = document.getElementById('reportMessage');
    
    if (reportReason) reportReason.value = '';
    if (reportMessage) reportMessage.value = '';
}

// 신고 제출
function submitReport() {
    const reasonElement = document.getElementById('reportReason');
    const messageElement = document.getElementById('reportMessage');
    
    if (!reasonElement) return;
    
    const reason = reasonElement.value;
    const message = messageElement ? messageElement.value.trim() : '';
    
    if (!reason) {
        alert('신고 사유를 선택해주세요.');
        return;
    }
    
    const room = roomsData.find(r => r.id === currentReportRoomId);
    if (!room) return;
    
    // 실제로는 서버에 신고 데이터 전송
    console.log('신고 데이터:', {
        roomId: currentReportRoomId,
        roomTitle: room.title,
        reason: reason,
        message: message,
        timestamp: new Date().toISOString()
    });
    
    alert('신고가 접수되었습니다.\n검토 후 적절한 조치를 취하겠습니다.');
    closeReportModal();
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeDetailModal();
        closeApplicationModal();
        closeReportModal();
    }
});

// 모달 오버레이 클릭으로 닫기
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        closeDetailModal();
        closeApplicationModal();
        closeReportModal();
    }
});

// localStorage에서 사용자 생성 방 데이터 불러오기
function loadUserCreatedRooms() {
    try {
        const finalSubmission = localStorage.getItem('final_room_submission');
        if (finalSubmission) {
            const roomData = JSON.parse(finalSubmission);
            
            const newRoom = {
                id: roomsData.length + 1,
                title: roomData.roomName || '새로운 여행',
                location: roomData.destination?.name || '미정',
                date: formatScheduleForDisplay(roomData.schedule),
                dateValue: roomData.schedule?.dateRanges?.[0]?.start ? new Date(roomData.schedule.dateRanges[0].start) : new Date(),
                views: '0명이 봄',
                viewCount: 0,
                description: roomData.roomIntro || '여행 소개가 없습니다.',
                tags: roomData.emotions?.map(e => typeof e === 'string' ? e : e.text) || ['여행'],
                currentParticipants: 1,
                maxParticipants: convertPeopleToNumber(roomData.people),
                createdDate: formatDate(new Date()),
                image: roomData.destination?.image || 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=300&h=200&fit=crop',
                category: '사용자 생성',
                urgent: false
            };
            
            if (!roomsData.find(room => room.id === newRoom.id)) {
                roomsData.unshift(newRoom);
                console.log('사용자 생성 방이 목록에 추가되었습니다:', newRoom);
            }
        }
    } catch (error) {
        console.error('사용자 생성 방 로드 중 오류:', error);
    }
}

// 헬퍼 함수들
function formatScheduleForDisplay(schedule) {
    if (!schedule || !schedule.dateRanges || schedule.dateRanges.length === 0) {
        return '일정 미정';
    }
    
    const totalDays = schedule.totalDays || 1;
    const nights = Math.max(0, totalDays - 1);
    return `${nights}박 ${totalDays}일`;
}

function convertPeopleToNumber(peopleText) {
    if (peopleText === '2명') return 2;
    if (peopleText === '4명') return 4;
    if (peopleText === '기타') return 6;
    return 2;
}

function formatDate(date) {
    const year = date.getFullYear().toString().slice(-2);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}/${month}/${day}`;
}

// 페이지 로드 시 사용자 생성 방 확인
document.addEventListener('DOMContentLoaded', function() {
    loadUserCreatedRooms();
});

// 상세보기 페이지 관련 함수들 (기존 유지)
function closeDetailPage() {
    const detailPage = document.getElementById('detailPage');
    if (detailPage) {
        detailPage.style.display = 'none';
        document.body.style.overflow = 'auto';
        currentDetailRoomId = null;
    }
}

function applyFromDetailPage() {
    if (currentDetailRoomId) {
        closeDetailPage();
        applyRoom(currentDetailRoomId);
    }
}
// 인원 선택 값 변환 함수들 추가
function convertPeopleValueToKorean(englishValue) {
    const peopleMap = {
        'twopeople': '2',
        'fourpeople': '4',
        'etc': '기타'
    };
    return peopleMap[englishValue] || englishValue;
}

function convertPeopleValueToEnglish(koreanValue) {
    const reverseMap = {
        '2': 'twopeople',
        '4': 'fourpeople',
        '기타': 'etc'
    };
    return reverseMap[koreanValue] || koreanValue;
}

// ✨ 인원수에서 최대 인원 수 추출하는 함수
function extractMaxPeopleNumber(peopleValue) {
    // 한글 인원 값에서 숫자만 추출
    if (typeof peopleValue === 'string') {
        if (peopleValue.includes('명')) {
            const number = peopleValue.replace('명', '').trim();
            return parseInt(number);
        }
        // 영어 값인 경우 한글로 변환 후 추출
        const koreanValue = convertPeopleValueToKorean(peopleValue);
        if (koreanValue.includes('명')) {
            const number = koreanValue.replace('명', '').trim();
            return parseInt(number);
        }
    }
    return null;
}

document.addEventListener("DOMContentLoaded", function () {
    // 새로고침 감지 및 처리
    handlePageLoad();

    // 새 방 만들기 체크
    checkForNewRoomCreation();

    const categoryCards = document.querySelectorAll(".category-card");
    const customInput = document.getElementById("customPeopleInput");
    const customInputWrapper = document.getElementById("customPeopleInputWrapper");
    const hiddenInput = document.querySelector('input[name="selected_people"]');

    // 카드 클릭 이벤트 처리 (수정됨)
    categoryCards.forEach(card => {
        card.addEventListener("click", function () {
            // 1. 모든 카드 초기화
            categoryCards.forEach(c => {
                const checkImg = c.querySelector(".check-mark img");
                if (checkImg) {
                    checkImg.src = "/image/creatingRoom/checkbox.svg";
                }
                c.classList.remove("selected");
            });

            // 2. 현재 카드만 선택
            const checkImg = this.querySelector(".check-mark img");
            if (checkImg) {
                checkImg.src = "/image/creatingRoom/checkbox-checked.svg";
            }
            this.classList.add("selected");

            const selectedValue = this.getAttribute("data-target");
            console.log("선택된 인원:", selectedValue);

            // 3. 기타 선택 시 직접 입력 필드 표시/숨김
            if (selectedValue === 'etc') {
                if (customInputWrapper) {
                    customInputWrapper.style.display = "block";
                }
                if (hiddenInput) {
                    hiddenInput.value = ""; // 직접 입력 대기
                }
                // 직접 입력 필드에 포커스
                if (customInput) {
                    setTimeout(() => customInput.focus(), 100);
                }
            } else {
                if (customInputWrapper) {
                    customInputWrapper.style.display = "none";
                }
                if (customInput) {
                    customInput.value = "";
                    customInput.classList.remove('error', 'success');
                }
                if (hiddenInput) {
                    hiddenInput.value = selectedValue;
                }
            }
        });
    });

    // 직접 입력 처리 (이벤트 리스너는 한 번만 등록)
    if (customInput && hiddenInput) {
        customInput.addEventListener("input", function () {
            const value = parseInt(this.value);

            // 클래스 초기화
            this.classList.remove('error', 'success');

            if (this.value === '') {
                // 빈 값일 때는 기본 상태
                hiddenInput.value = "";
            } else if (!isNaN(value) && value >= 2 && value <= 99) {
                // 유효한 값
                this.classList.add('success');
                hiddenInput.value = value + "명";
                console.log("직접 입력된 인원:", value + "명");
            } else {
                // 무효한 값
                this.classList.add('error');
                hiddenInput.value = "";
            }
        });
    }

    // 임시 데이터 복원
    restoreTemporaryData();
});

// ✨ 페이지 로드 방식 감지 및 처리
function handlePageLoad() {
    // performance.navigation API 사용 (구형 브라우저 지원)
    const navigationType = performance.navigation ? performance.navigation.type : null;

    // 또는 최신 Navigation API 사용
    const navigationEntries = performance.getEntriesByType('navigation');
    const navigationType2 = navigationEntries.length > 0 ? navigationEntries[0].type : null;

    // Referrer 확인
    const referrer = document.referrer;

    console.log('페이지 로드 방식:', {
        navigationType,
        navigationType2,
        referrer
    });

    // 새로고침인지 확인
    const isRefresh = navigationType === 1 || // TYPE_RELOAD
        navigationType2 === 'reload' ||
        !referrer; // referrer가 없으면 직접 접근 또는 새로고침

    // URL에 특별한 파라미터가 없고 새로고침이라면
    const urlParams = new URLSearchParams(window.location.search);
    const hasSpecialParams = urlParams.has('new') || urlParams.has('edit') || urlParams.has('return');

    if (isRefresh && !hasSpecialParams) {
        console.log('새로고침 또는 직접 접근 감지: 임시 데이터를 삭제합니다.');
        clearTemporaryDataOnRefresh();
    } else {
        console.log('정상적인 페이지 이동으로 감지됨');
    }
}

// ✨ 새로고침 시 임시 데이터 삭제
function clearTemporaryDataOnRefresh() {
    const tempKeys = [
        'temp_selected_people',
        'temp_room_name',
        'temp_room_description',
        'temp_max_people' // ✨ 최대 인원수 임시 데이터도 삭제
    ];

    tempKeys.forEach(key => {
        const value = localStorage.getItem(key);
        if (value) {
            console.log(`삭제된 임시 데이터: ${key} = ${value}`);
            localStorage.removeItem(key);
        }
    });

    // 세션 플래그들도 정리
    sessionStorage.removeItem('from_new_room_creation');
    sessionStorage.removeItem('form_navigation_state');

    console.log('새로고침으로 인한 임시 데이터 정리 완료');
}

// 새 방 만들기에서 요청이 오면  임시 방 draft를 생성해서 draftId를 생성한다.
function checkForNewRoomCreation() {
    const urlParams = new URLSearchParams(window.location.search);
    const isNewRoom = urlParams.get('new') === 'true';
    const isFromFinalRegistration = sessionStorage.getItem('from_new_room_creation') === 'true';

    if (isNewRoom || isFromFinalRegistration) {
        console.log('새 방 만들기 모드: 모든 폼 데이터를 초기화합니다.');

        // 강제로 폼 초기화
        forceResetForm();

        // 세션 플래그 제거
        sessionStorage.removeItem('from_new_room_creation');

        // URL에서 new 파라미터 제거
        if (isNewRoom) {
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
        }

        return true;
    }
    return false;
}

// 강제 폼 초기화
function forceResetForm() {
    // 모든 입력 필드 초기화
    const roomNameInput = document.querySelector('.roomName-search-typing-input');
    const roomIntroInput = document.querySelector('.roomName-search-typing-input2');
    const hiddenInput = document.querySelector('input[name="selected_people"]');

    if (roomNameInput) {
        roomNameInput.value = '';
        roomNameInput.style.borderColor = '#e0e0e0'; // 기본 테두리 색상 복원
    }
    if (roomIntroInput) {
        roomIntroInput.value = '';
        roomIntroInput.style.borderColor = '#e0e0e0'; // 기본 테두리 색상 복원
    }
    if (hiddenInput) hiddenInput.value = '';

    // 모든 체크박스 초기화
    document.querySelectorAll('.category-card').forEach(card => {
        const checkImg = card.querySelector('.check-mark img');
        if (checkImg) {
            checkImg.src = '/image/creatingRoom/checkbox.svg';
        }
        card.classList.remove('selected');
    });

    // 관련 localStorage 항목들도 정리
    const keysToRemove = [
        'selected_people',
        'room_name',
        'room_description',
        'max_people',
        'temp_selected_people',
        'temp_room_name',
        'temp_room_description',
        'temp_max_people'
    ];

    keysToRemove.forEach(key => {
        localStorage.removeItem(key);
    });

    console.log('폼이 강제로 초기화되었습니다.');
}

// 다음 버튼 눌렀을 경우, 1. 인원 선택 여부, 방 이름 여부, 방 소개 여부 유효성 검사
function validationPhase(form) {
    const selectedPeople = form.querySelector('input[name="selected_people"]').value;
    const roomNameInput = form.querySelector('.roomName-search-typing-input');
    const roomIntroInput = form.querySelector('.roomName-search-typing-input2');

    if (!selectedPeople) {
        alert("인원을 선택해주세요.");
        return false;
    }

    if (!roomNameInput || roomNameInput.value.trim() === "") {
        alert("방 이름을 입력해주세요.");
        roomNameInput.focus();
        return false;
    }

    if (!roomIntroInput || roomIntroInput.value.trim() === "") {
        alert("방 소개를 입력해주세요.");
        roomIntroInput.focus();
        return false;
    }

    // ✨ 한글로 변환해서 저장
    const koreanPeopleValue = convertPeopleValueToKorean(selectedPeople);

    // ✨ 최대 인원수 추출 및 저장
    const maxPeople = extractMaxPeopleNumber(koreanPeopleValue);

    localStorage.setItem("selected_people", koreanPeopleValue); // 2명, 4명, 기타로 저장
    localStorage.setItem("room_name", roomNameInput.value.trim());
    localStorage.setItem("room_description", roomIntroInput.value.trim());

    // 최대 인원수 저장 (데이터베이스용)
    if (maxPeople) {
        localStorage.setItem("max_people", maxPeople.toString());
        console.log("최대 인원수 저장:", maxPeople);
    }

    // 디버깅용 콘솔 출력
    console.log("저장된 데이터:", {
        인원: koreanPeopleValue,
        방이름: roomNameInput.value.trim(),
        방소개: roomIntroInput.value.trim(),
        최대인원수: maxPeople
    });

    window.location.href = '/companion-rooms/emotion';

    // const draftId = localStorage.getItem("draft_room_id");
    // if (!draftId) {
    //     alert("방 생성 ID가 존재하지 않습니다. 다시 시작해주세요.");
    //     return false;
    // }
    // window.location.href = `/companion-rooms/emotion?draftId=${draftId}`;

    return false; // form 제출 막기
}

// 뒤로가기 함수 - 확인 메시지 없이 바로 이동 (구체적인 URL로 이동)
function exitWithSubmit(formId, value) {
    console.log('뒤로가기 버튼 클릭됨');

    // 현재 입력 내용 임시 저장 (조용히 저장)
    const selectedPeople = document.querySelector('input[name="selected_people"]')?.value;
    const roomNameInput = document.querySelector('.roomName-search-typing-input');
    const roomIntroInput = document.querySelector('.roomName-search-typing-input2');

    if (selectedPeople) {
        const koreanPeopleValue = convertPeopleValueToKorean(selectedPeople);
        localStorage.setItem("temp_selected_people", koreanPeopleValue);

        const maxPeople = extractMaxPeopleNumber(koreanPeopleValue);
        if (maxPeople) {
            localStorage.setItem("temp_max_people", maxPeople.toString());
        }
    }
    if (roomNameInput && roomNameInput.value.trim()) {
        localStorage.setItem("temp_room_name", roomNameInput.value.trim());
    }
    if (roomIntroInput && roomIntroInput.value.trim()) {
        localStorage.setItem("temp_room_description", roomIntroInput.value.trim());
    }
    window.location.href = "/companion-rooms/start";
}

function goToPreviousPage() {
    console.log('뒤로가기 버튼 클릭됨');

    // 현재 입력 내용 임시 저장
    const selectedPeople = document.querySelector('input[name="selected_people"]')?.value;
    const roomNameInput = document.querySelector('.roomName-search-typing-input');
    const roomIntroInput = document.querySelector('.roomName-search-typing-input2');

    if (selectedPeople) {
        const koreanPeopleValue = convertPeopleValueToKorean(selectedPeople);
        localStorage.setItem("temp_selected_people", koreanPeopleValue);

        const maxPeople = extractMaxPeopleNumber(koreanPeopleValue);
        if (maxPeople) {
            localStorage.setItem("temp_max_people", maxPeople.toString());
        }
    }
    if (roomNameInput && roomNameInput.value.trim()) {
        localStorage.setItem("temp_room_name", roomNameInput.value.trim());
    }
    if (roomIntroInput && roomIntroInput.value.trim()) {
        localStorage.setItem("temp_room_description", roomIntroInput.value.trim());
    }

    // 바로 메인 페이지나 방 목록으로 이동 (확인 메시지 없음)
    window.location.href = "/companion-rooms/start"; // 메인 페이지로 이동
}

// 다음 버튼을 눌렀을 때 해당 정보들을 가지고 다음 페이지로 넘어가는 JS 코드
window.addEventListener("DOMContentLoaded", function () {
    const name = localStorage.getItem("room_name");
    const intro = localStorage.getItem("room_description");

    // 예시: 화면에 표시
    const previewName = document.getElementById("preview-name");
    const previewIntro = document.getElementById("preview-intro");

    if (previewName && name) {
        previewName.textContent = name;
    }
    if (previewIntro && intro) {
        previewIntro.textContent = intro;
    }
});

function restoreTemporaryData() {
    // 먼저 새 방 만들기 모드인지 확인
    const urlParams = new URLSearchParams(window.location.search);
    const isNewRoom = urlParams.get('new') === 'true';
    const isFromFinalRegistration = sessionStorage.getItem('from_new_room_creation') === 'true';

    // 새 방 만들기로 왔다면 복원하지 않고 바로 리턴
    if (isNewRoom || isFromFinalRegistration) {
        console.log('새 방 만들기 모드: 데이터 복원을 건너뜁니다.');
        return;
    }

    // Referrer가 있고, 특정 페이지에서 온 경우에만 복원
    const referrer = document.referrer;
    const validReferrers = [
        'choosing-emotion.html',
        'choosing-destination.html',
        'choosing-schedule.html',
        'final-registration.html'
    ];

    const isFromValidPage = validReferrers.some(page => referrer.includes(page));

    if (!isFromValidPage && referrer) {
        console.log('유효하지 않은 이전 페이지에서 온 요청: 데이터 복원을 건너뜁니다.');
        console.log('Referrer:', referrer);
        return;
    }

    // 직접 접근이나 새로고침의 경우 복원하지 않음
    if (!referrer) {
        console.log('직접 접근 또는 새로고침: 데이터 복원을 건너뜁니다.');
        return;
    }

    const tempPeople = localStorage.getItem("temp_selected_people");
    const tempName = localStorage.getItem("temp_room_name");
    const tempIntro = localStorage.getItem("temp_room_description");
    const tempMaxPeople = localStorage.getItem("temp_max_people"); // ✨ 임시 최대 인원수도 복원

    // 임시 데이터가 있을 때만 복원
    if (tempPeople || tempName || tempIntro) {
        console.log('임시 저장된 데이터 복원 중...');
        console.log('Referrer:', referrer);

        // 인원 복원 (한글 → 영어로 변환해서 UI 복원)
        if (tempPeople) {
            const englishValue = convertPeopleValueToEnglish(tempPeople);
            const hiddenInput = document.querySelector('input[name="selected_people"]');
            const customInput = document.getElementById("customPeopleInput");
            const customInputWrapper = document.getElementById("customPeopleInputWrapper");

            if (hiddenInput) {
                hiddenInput.value = englishValue; // UI용으로는 영어 값 유지
            }

            // 해당 카드 선택 상태 복원
            const targetCard = document.querySelector(`[data-target="${englishValue}"]`);
            if (targetCard) {
                const checkImg = targetCard.querySelector(".check-mark img");
                if (checkImg) {
                    checkImg.src = "/image/creatingRoom/checkbox-checked.svg";
                }
                targetCard.classList.add("selected");

                // 기타 선택인 경우 직접 입력 필드 표시
                if (englishValue === 'etc') {
                    if (customInputWrapper) {
                        customInputWrapper.style.display = "block";
                    }
                    // 저장된 숫자 값이 있다면 복원
                    const numericValue = tempPeople.replace('명', '');
                    if (customInput && !isNaN(parseInt(numericValue))) {
                        customInput.value = numericValue;
                        customInput.classList.add('success');
                    }
                }
            }

            localStorage.removeItem("temp_selected_people");
        }

        // 임시 최대 인원수 복원 (정식 데이터로 이동)
        if (tempMaxPeople) {
            localStorage.setItem("max_people", tempMaxPeople);
            localStorage.removeItem("temp_max_people");
            console.log('임시 최대 인원수 복원:', tempMaxPeople);
        }

        // 방 이름 복원
        if (tempName) {
            const roomNameInput = document.querySelector('.roomName-search-typing-input');
            if (roomNameInput && !roomNameInput.value) {
                roomNameInput.value = tempName;
                roomNameInput.dispatchEvent(new Event('input'));
            }
            localStorage.removeItem("temp_room_name");
        }

        // 방 소개 복원
        if (tempIntro) {
            const roomIntroInput = document.querySelector('.roomName-search-typing-input2');
            if (roomIntroInput && !roomIntroInput.value) {
                roomIntroInput.value = tempIntro;
                roomIntroInput.dispatchEvent(new Event('input'));
            }
            localStorage.removeItem("temp_room_description");
        }

        console.log('임시 데이터 복원 완료');
    } else {
        console.log('복원할 임시 데이터가 없습니다.');
    }
}

// 도움말 모달 열기
function openHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden'; // 배경 스크롤 방지
}

// 도움말 모달 닫기
function closeHelpModal() {
    const modal = document.getElementById('helpModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto'; // 배경 스크롤 복원
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeHelpModal();
    }
});

// 입력 필드 실시간 가이드 (선택사항)
document.addEventListener('DOMContentLoaded', function() {
    const roomNameInput = document.querySelector('.roomName-search-typing-input');
    const roomIntroInput = document.querySelector('.roomName-search-typing-input2');

    // 방 이름 입력 가이드
    if (roomNameInput) {
        roomNameInput.addEventListener('input', function() {
            const value = this.value.trim();
            const length = value.length;
        });
    }

    // 방 소개 입력 가이드
    if (roomIntroInput) {
        roomIntroInput.addEventListener('input', function() {
            const value = this.value.trim();
            const length = value.length;
        });
    }
});

// 추천 문구 자동 완성 기능 (선택사항)
function insertSampleText(type) {
    const sampleTexts = {
        roomName: [
            "제주도 힐링 여행",
            "부산 바다 투어",
            "고양시로 가는고양",
            "떠나자! 여행멘토들",
            "힐링이 필요해"
        ],
        roomIntro: [
            "안녕하세요! 함께 여행할 동행자를 찾고 있어요. 힐링과 맛집 투어가 목적입니다. 새로운 친구들과 즐거운 추억 만들어요! 🌟",
            "20대 직장인입니다! 스트레스 해소차 여행 떠나려고 해요. 사진 찍기 좋아하고 맛집 탐방 좋아하는 분들 환영!",
            "혼자 여행도 좋지만 함께 하면 더 재밌잖아요? 계획보단 즉흥적인 여행 스타일이에요. 편하게 대화하며 여행해요! 😊"
        ]
    };

    const randomIndex = Math.floor(Math.random() * sampleTexts[type].length);
    const sampleText = sampleTexts[type][randomIndex];

    if (type === 'roomName') {
        const input = document.querySelector('.roomName-search-typing-input');
        if (input) {
            input.value = sampleText;
            input.dispatchEvent(new Event('input')); // 실시간 가이드 트리거
        }
    } else if (type === 'roomIntro') {
        const input = document.querySelector('.roomName-search-typing-input2');
        if (input) {
            input.value = sampleText;
            input.dispatchEvent(new Event('input')); // 실시간 가이드 트리거
        }
    }
}

// 폼 제출 전 최종 확인
function finalValidation() {
    const selectedPeople = document.querySelector('input[name="selected_people"]').value;
    const roomName = document.querySelector('.roomName-search-typing-input').value.trim();
    const roomIntro = document.querySelector('.roomName-search-typing-input2').value.trim();

    // ✨ 사용자에게는 한글로 표시
    const koreanPeople = convertPeopleValueToKorean(selectedPeople);

    // 최종 확인 메시지
    const confirmMessage = `
입력하신 정보를 확인해주세요:

• 인원: ${koreanPeople}
• 방 이름: ${roomName}
• 방 소개: ${roomIntro.length > 50 ? roomIntro.substring(0, 50) + '...' : roomIntro}

이대로 진행하시겠습니까?
    `;

    return confirm(confirmMessage);
}

// 선택된 인원 텍스트 변환 (이제 convertPeopleValueToKorean 함수 사용)
function getSelectedPeopleText(value) {
    return convertPeopleValueToKorean(value);
}

// 페이지 떠날 때 자동 저장 (사용자가 모르게)
window.addEventListener('beforeunload', function() {
    // 새 방 만들기 모드가 아닐 때만 자동 저장
    const urlParams = new URLSearchParams(window.location.search);
    const isNewRoom = urlParams.get('new') === 'true';

    if (!isNewRoom) {
        const selectedPeople = document.querySelector('input[name="selected_people"]')?.value;
        const roomNameInput = document.querySelector('.roomName-search-typing-input');
        const roomIntroInput = document.querySelector('.roomName-search-typing-input2');

        if (selectedPeople) {
            // ✨ 자동 저장도 한글로
            const koreanPeopleValue = convertPeopleValueToKorean(selectedPeople);
            localStorage.setItem("temp_selected_people", koreanPeopleValue);

            // ✨ 최대 인원수도 자동 저장
            const maxPeople = extractMaxPeopleNumber(koreanPeopleValue);
            if (maxPeople) {
                localStorage.setItem("temp_max_people", maxPeople.toString());
            }
        }
        if (roomNameInput && roomNameInput.value.trim()) {
            localStorage.setItem("temp_room_name", roomNameInput.value.trim());
        }
        if (roomIntroInput && roomIntroInput.value.trim()) {
            localStorage.setItem("temp_room_description", roomIntroInput.value.trim());
        }
    }
});

// ✨ 추가된 유틸리티 함수들

// 저장된 데이터 확인용 (개발자 도구에서 사용)
function checkSavedData() {
    console.log("=== 현재 저장된 데이터 ===");
    console.log("인원:", localStorage.getItem("selected_people"));
    console.log("최대 인원수:", localStorage.getItem("max_people")); // ✨ 추가
    console.log("방 이름:", localStorage.getItem("room_name"));
    console.log("방 소개:", localStorage.getItem("room_description"));
    console.log("========================");
}

// 전체 폼 데이터를 객체로 관리 (향후 확장성을 위해)
function saveFormDataAsObject() {
    const selectedPeople = document.querySelector('input[name="selected_people"]')?.value;
    const roomNameInput = document.querySelector('.roomName-search-typing-input');
    const roomIntroInput = document.querySelector('.roomName-search-typing-input2');

    if (selectedPeople && roomNameInput?.value.trim() && roomIntroInput?.value.trim()) {
        const koreanPeopleValue = convertPeopleValueToKorean(selectedPeople);
        const maxPeople = extractMaxPeopleNumber(koreanPeopleValue);

        const formData = {
            people: koreanPeopleValue,
            maxPeople: maxPeople, // ✨ 최대 인원수 추가
            roomName: roomNameInput.value.trim(),
            roomIntro: roomIntroInput.value.trim(),
            step: 'basic_info_completed'
        };

        localStorage.setItem("room_creation_data", JSON.stringify(formData));
        console.log("폼 데이터 객체로 저장 완료:", formData);
        return formData;
    }
    return null;
}

// 데이터 불러오기
function loadFormDataAsObject() {
    const savedData = localStorage.getItem("room_creation_data");
    return savedData ? JSON.parse(savedData) : null;
}

// 임시 저장 데이터 정리 (필요할 때 사용)
function clearTemporaryData() {
    localStorage.removeItem("temp_selected_people");
    localStorage.removeItem("temp_room_name");
    localStorage.removeItem("temp_room_description");
    localStorage.removeItem("temp_max_people"); // ✨ 임시 최대 인원수도 정리
    console.log("임시 저장 데이터 정리 완료");
}

// ✨ 방 생성 시 데이터베이스에 전송할 데이터 준비 함수
function prepareRoomDataForDatabase() {
    const roomData = {
        roomName: localStorage.getItem("room_name"),
        roomDescription: localStorage.getItem("room_description"),
        selectedPeople: localStorage.getItem("selected_people"), // "2명", "4명", "7명" 등
        maxPeople: parseInt(localStorage.getItem("max_people")), // 숫자로 변환 (2, 4, 7 등)
        // 나중에 추가될 다른 데이터들
        emotion: localStorage.getItem("selected_emotion"),
        destination: localStorage.getItem("selected_destination"),
        schedule: localStorage.getItem("selected_schedule")
    };

    console.log("데이터베이스 전송 준비된 데이터:", roomData);
    return roomData;
}

// ✨ 현재 저장된 최대 인원수 가져오기
function getCurrentMaxPeople() {
    const maxPeople = localStorage.getItem("max_people");
    return maxPeople ? parseInt(maxPeople) : null;
}

// ✨ 페이지 새로고침 감지 및 처리
window.addEventListener('load', function() {
    // 페이지가 로드된 후 새 방 만들기 체크
    const urlParams = new URLSearchParams(window.location.search);
    const isNewRoom = urlParams.get('new') === 'true';

    if (isNewRoom) {
        console.log('새 방 만들기 페이지 로드 완료');

        // URL 정리 (뒤로가기 시 깨끗한 URL)
        setTimeout(() => {
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
        }, 1000);
    }
});
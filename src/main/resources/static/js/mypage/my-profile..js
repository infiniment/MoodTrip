// ========================================
// 🎯 프로필 페이지 완전한 JavaScript (최종 버전)
// ========================================

let originalNickname = ''; // 전역 변수로 원래 닉네임 저장
let originalSelfIntro = ''; // 원래 자기소개 저장용
let originalProfileImageSrc = ''; // 원래 프로필 이미지 저장용

document.addEventListener('DOMContentLoaded', function() {

    // ========================================
    // 🎯 닉네임 수정 기능
    // ========================================

    // 닉네임 수정 버튼 클릭 시
    const modifyNicknameBtn = document.querySelector('.modify-nickname');
    if (modifyNicknameBtn) {
        modifyNicknameBtn.addEventListener('click', function () {
            console.log('닉네임 수정 버튼 클릭됨');

            document.querySelector('.modify-nickname-wrapper').hidden = true;
            document.querySelector('.complete-nickname-wrapper').hidden = false;

            // input 활성화 및 스타일 변경
            const inputSection = document.querySelector('.name-input-section');
            const input = document.querySelector('#username');

            // 현재 이름 저장
            originalNickname = input.value;

            inputSection.style.backgroundColor = '#ffffff';
            input.disabled = false;
            input.style.color = '#000000';
            input.focus(); // 포커스 추가
        });
    }

    // 취소 버튼 클릭 시
    const cancelNicknameBtn = document.querySelector('.modify-nickname-cancel');
    if (cancelNicknameBtn) {
        cancelNicknameBtn.addEventListener('click', function () {
            console.log('닉네임 수정 취소 버튼 클릭됨');

            // input 다시 비활성화 + 스타일 원래대로 복구
            const inputSection = document.querySelector('.name-input-section');
            inputSection.style.backgroundColor = '';

            const input = document.querySelector('#username');
            input.disabled = true;
            input.style.color = '';

            // 원래 닉네임 복원
            input.value = originalNickname;

            document.querySelector('.modify-nickname-wrapper').hidden = false;
            document.querySelector('.complete-nickname-wrapper').hidden = true;
        });
    }

    // ✅ 닉네임 저장 버튼 클릭 시 (백엔드 연결)
    const saveNicknameBtn = document.querySelector('.save-nickname');
    if (saveNicknameBtn) {
        saveNicknameBtn.addEventListener('click', function () {
            console.log('🚀 닉네임 저장 버튼 클릭됨');

            const input = document.querySelector('#username');
            const inputSection = document.querySelector('.name-input-section');
            const newNickname = input.value.trim();

            // 유효성 검사
            if (!newNickname) {
                showErrorMessage('닉네임을 입력해주세요.');
                return;
            }

            if (newNickname.length < 2) {
                showErrorMessage('닉네임은 2자 이상 입력해주세요.');
                return;
            }

            if (newNickname.length > 30) {
                showErrorMessage('닉네임은 30자 이내로 입력해주세요.');
                return;
            }

            const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
            if (!nicknameRegex.test(newNickname)) {
                showErrorMessage('닉네임은 한글, 영문, 숫자만 입력 가능합니다.');
                return;
            }

            // 로딩 상태 표시
            const originalBtnText = saveNicknameBtn.textContent;
            saveNicknameBtn.textContent = '저장 중...';
            saveNicknameBtn.disabled = true;

            // ✅ 백엔드 API 호출
            fetch('/api/v1/profiles/me/nickname', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({nickname: newNickname})
            })
                .then(response => {
                    console.log('📡 닉네임 수정 응답 상태:', response.status);

                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                })
                .then(data => {
                    console.log('✅ 닉네임 수정 성공:', data);

                    // 저장 완료 UI 전환
                    input.disabled = true;
                    inputSection.style.backgroundColor = '';
                    input.style.color = '';
                    document.querySelector('.modify-nickname-wrapper').hidden = false;
                    document.querySelector('.complete-nickname-wrapper').hidden = true;

                    // 원래 닉네임 갱신
                    originalNickname = newNickname;

                    // 성공 메시지 표시
                    showSuccessMessage('닉네임이 성공적으로 수정되었습니다!');
                })
                .catch(error => {
                    console.error('❌ 닉네임 수정 실패:', error);

                    let errorMessage = '닉네임 수정 중 오류가 발생했습니다.';
                    if (error.message.includes('400')) {
                        errorMessage = '잘못된 닉네임 형식입니다. 다시 확인해주세요.';
                    } else if (error.message.includes('409')) {
                        errorMessage = '이미 사용 중인 닉네임입니다.';
                    } else if (error.message.includes('500')) {
                        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                    }

                    showErrorMessage(errorMessage);

                    // 실패 시 원래 닉네임으로 복원
                    input.value = originalNickname;
                })
                .finally(() => {
                    // 로딩 상태 해제
                    saveNicknameBtn.textContent = originalBtnText;
                    saveNicknameBtn.disabled = false;
                });
        });
    }

    // ========================================
    // 🎯 자기소개 수정 기능
    // ========================================

    // 자기소개 수정 버튼 클릭
    const editSelfIntroBtn = document.querySelector('.edit-self-introduction');
    if (editSelfIntroBtn) {
        editSelfIntroBtn.addEventListener('click', function () {
            console.log('자기소개 수정 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const completeWrapper = document.querySelector('.complete-self-introduction-wrapper');
            const editWrapper = document.querySelector('.edit-self-introduction-wrapper');

            originalSelfIntro = textarea.value; // 현재 내용 저장
            textarea.disabled = false;
            textarea.style.backgroundColor = '#ffffff';
            textarea.style.color = '#000000';
            textarea.style.border = '1px solid #cccccc';
            textarea.focus();

            editWrapper.hidden = true;
            completeWrapper.hidden = false;
        });
    }

    // 자기소개 취소 버튼 클릭
    const cancelSelfIntroBtn = document.querySelector('.complete-self-introduction-wrapper .modify-nickname-cancel');
    if (cancelSelfIntroBtn) {
        cancelSelfIntroBtn.addEventListener('click', function () {
            console.log('자기소개 취소 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const completeWrapper = document.querySelector('.complete-self-introduction-wrapper');
            const editWrapper = document.querySelector('.edit-self-introduction-wrapper');

            textarea.value = originalSelfIntro; // 원래대로 복원
            textarea.disabled = true;
            textarea.style.backgroundColor = '';
            textarea.style.color = '';
            textarea.style.border = '';

            editWrapper.hidden = false;
            completeWrapper.hidden = true;
        });
    }

    // ✅ 자기소개 저장 버튼 클릭 (백엔드 연결)
    const saveSelfIntroBtn = document.querySelector('.complete-self-introduction-wrapper .save-nickname');
    if (saveSelfIntroBtn) {
        saveSelfIntroBtn.addEventListener('click', function () {
            console.log('🚀 자기소개 저장 버튼 클릭됨');

            const textarea = document.querySelector('#selfIntroduction');
            const newSelfIntro = textarea.value.trim();
            const saveButton = this; // 현재 클릭된 버튼

            // ✅ 유효성 검사
            if (newSelfIntro.length === 0) {
                showErrorMessage("자기소개를 입력해주세요.");
                return;
            }

            if (newSelfIntro.length > 500) {
                showErrorMessage("자기소개는 500자 이내로 입력해주세요.");
                return;
            }

            // ✅ 로딩 상태 표시
            saveButton.disabled = true;
            const originalButtonText = saveButton.textContent;
            saveButton.textContent = '저장 중...';

            // ✅ API 호출
            fetch('/api/v1/profiles/me/introduce', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({profileBio: newSelfIntro})
            })
                .then(response => {
                    console.log('📡 자기소개 수정 응답 상태:', response.status);

                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                })
                .then(data => {
                    console.log('✅ 자기소개 수정 성공:', data);

                    // ✅ API 성공 후에 UI 처리
                    textarea.disabled = true;
                    textarea.style.backgroundColor = '';
                    textarea.style.color = '';
                    textarea.style.border = '';

                    document.querySelector('.edit-self-introduction-wrapper').hidden = false;
                    document.querySelector('.complete-self-introduction-wrapper').hidden = true;

                    originalSelfIntro = newSelfIntro;

                    showSuccessMessage("자기소개가 성공적으로 저장되었습니다!");
                })
                .catch(error => {
                    console.error('❌ 자기소개 수정 실패:', error);

                    // ✅ 에러 메시지 더 구체적으로
                    let errorMessage = "자기소개 저장 중 오류가 발생했습니다.";

                    if (error.message.includes('500')) {
                        errorMessage = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
                    } else if (error.message.includes('400')) {
                        errorMessage = "입력 내용을 확인해주세요. (500자 이내로 작성)";
                    }

                    showErrorMessage(errorMessage);

                    // ✅ 실패시 원래 내용으로 복원
                    textarea.value = originalSelfIntro;
                })
                .finally(() => {
                    // ✅ 로딩 상태 해제 (성공/실패 모두)
                    saveButton.disabled = false;
                    saveButton.textContent = originalButtonText;
                });
        });
    }

    // ========================================
    // 🎯 프로필 이미지 변경 기능
    // ========================================

    // 프로필 이미지 변경 파일 input 이벤트
    const profileImageInput = document.getElementById('file-input');
    if (profileImageInput) {
        profileImageInput.addEventListener('change', function(event) {
            console.log('프로필 이미지 파일 선택됨');

            const selectedFile = event.target.files[0];

            // 파일이 선택되지 않은 경우
            if (!selectedFile) {
                console.log('파일 선택 취소됨');
                return;
            }

            console.log('선택된 파일:', {
                name: selectedFile.name,
                size: selectedFile.size,
                type: selectedFile.type
            });

            // 파일 유효성 검사
            if (!validateImageFile(selectedFile)) {
                // validateImageFile 함수에서 에러 메시지 처리됨
                profileImageInput.value = ''; // input 초기화
                return;
            }

            // 현재 이미지 src 저장 (실패 시 복원용)
            const profileImage = document.querySelector('.profile-image');
            if (profileImage) {
                originalProfileImageSrc = profileImage.src;
            }

            // 즉시 미리보기 표시
            showImagePreview(selectedFile, profileImage);

            // 프로필 변경 버튼을 로딩 상태로 변경
            const profileBtn = document.querySelector('.profile-btn');
            const originalBtnHTML = profileBtn ? profileBtn.innerHTML : '';
            if (profileBtn) {
                profileBtn.innerHTML = '<span class="profile-btn-text">업로드 중...</span>';
                profileBtn.disabled = true;
            }

            // 서버에 업로드 및 DB 저장
            uploadAndSaveProfileImage(selectedFile)
                .then(() => {
                    console.log('✅ 프로필 이미지 변경 완료');
                    showSuccessMessage('프로필 사진이 성공적으로 변경되었습니다!');
                })
                .catch((error) => {
                    console.error('❌ 프로필 이미지 변경 실패:', error);

                    // 실패 시 원래 이미지로 복원
                    if (profileImage && originalProfileImageSrc) {
                        profileImage.src = originalProfileImageSrc;
                    }

                    showErrorMessage('프로필 사진 변경에 실패했습니다. 다시 시도해주세요.');
                })
                .finally(() => {
                    // 버튼 상태 복원
                    if (profileBtn) {
                        profileBtn.innerHTML = originalBtnHTML;
                        profileBtn.disabled = false;
                    }

                    // input 초기화 (같은 파일 재선택 가능하게)
                    profileImageInput.value = '';
                });
        });
    }

    // ========================================
    // 🎯 회원 탈퇴 모달 기능
    // ========================================

    // 모달 관련 요소들
    const withdrawModal = document.getElementById('withdrawModal');
    const cancelWithdrawBtn = document.getElementById('cancelWithdraw');
    const confirmWithdrawBtn = document.getElementById('confirmWithdraw');

    // 모달 열기 함수
    function openWithdrawModal() {
        console.log('탈퇴 모달 열기');
        if (withdrawModal) {
            withdrawModal.style.display = 'flex';
            setTimeout(() => {
                withdrawModal.classList.add('show');
                withdrawModal.classList.remove('hide');
            }, 10);
            document.body.style.overflow = 'hidden';
        }
    }

    // 모달 닫기 함수
    function closeWithdrawModal() {
        console.log('탈퇴 모달 닫기');
        if (withdrawModal) {
            withdrawModal.classList.add('hide');
            withdrawModal.classList.remove('show');

            setTimeout(() => {
                withdrawModal.classList.remove('hide');
                withdrawModal.style.display = 'none';
                document.body.style.overflow = '';
            }, 300);
        }
    }

    // 탈퇴하기 버튼 클릭 이벤트
    const withdrawBtn = document.querySelector('.withdraw-btn');
    if (withdrawBtn) {
        withdrawBtn.addEventListener('click', function (e) {
            console.log('탈퇴하기 버튼 클릭됨');
            e.preventDefault();
            openWithdrawModal();
        });
    }

    // 취소 버튼 클릭 이벤트
    if (cancelWithdrawBtn) {
        cancelWithdrawBtn.addEventListener('click', function () {
            closeWithdrawModal();
        });
    }

    // ✅ 탈퇴 확인 버튼 클릭 이벤트 (완성 버전)
    if (confirmWithdrawBtn) {
        confirmWithdrawBtn.addEventListener('click', function () {
            console.log('🚀 탈퇴 확인 버튼 클릭됨');

            // 로딩 상태 표시
            const originalBtnText = confirmWithdrawBtn.textContent;
            confirmWithdrawBtn.textContent = '탈퇴 처리 중...';
            confirmWithdrawBtn.disabled = true;

            // ✅ 실제 백엔드 API 호출
            fetch('/api/v1/members/me', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    console.log('📡 회원 탈퇴 응답 상태:', response.status);

                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                })
                .then(data => {
                    console.log('✅ 회원 탈퇴 성공:', data);

                    // 모달 닫기
                    closeWithdrawModal();

                    setTimeout(() => {
                        // 백엔드에서 받은 메시지 표시
                        const message = data.message || '탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.';
                        alert(message);

                        // 사용자 데이터 정리
                        clearUserData();

                        // 메인 페이지로 이동
                        window.location.href = '/';
                    }, 300);
                })
                .catch(error => {
                    console.error('❌ 회원 탈퇴 실패:', error);

                    // 에러 메시지 표시
                    let errorMessage = '탈퇴 처리 중 오류가 발생했습니다. 고객센터에 문의해 주세요.';

                    if (error.message.includes('400')) {
                        errorMessage = '이미 탈퇴된 계정이거나 잘못된 요청입니다.';
                    } else if (error.message.includes('401')) {
                        errorMessage = '로그인이 필요합니다. 다시 로그인 후 시도해주세요.';
                    } else if (error.message.includes('500')) {
                        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                    }

                    alert('❌ ' + errorMessage);
                    closeWithdrawModal();
                })
                .finally(() => {
                    // 로딩 상태 해제 (성공/실패 관계없이)
                    confirmWithdrawBtn.textContent = originalBtnText;
                    confirmWithdrawBtn.disabled = false;
                });
        });
    }

    // 모달 오버레이 클릭 시 닫기 (배경 클릭 시)
    if (withdrawModal) {
        withdrawModal.addEventListener('click', function(e) {
            if (e.target === withdrawModal) {
                closeWithdrawModal();
            }
        });
    }

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && withdrawModal && withdrawModal.classList.contains('show')) {
            closeWithdrawModal();
        }
    });

    // 모달 내부 클릭 시 이벤트 버블링 방지
    const modalContainer = document.querySelector('.modal-container');
    if (modalContainer) {
        modalContainer.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    console.log('✅ 모든 기능 초기화 완료');

    // ========================================
    // 🛠️ 유틸리티 함수들
    // ========================================

    /**
     * ✅ 성공 메시지 표시 함수
     */
    function showSuccessMessage(message) {
        // 기존 메시지 제거
        removeMessage();

        // 새 성공 메시지 생성
        const messageDiv = document.createElement('div');
        messageDiv.className = 'success-message';
        messageDiv.style.cssText = `
            background: #d4edda; 
            color: #155724; 
            padding: 12px 15px; 
            border-radius: 5px; 
            margin-bottom: 20px;
            border: 1px solid #c3e6cb;
            animation: fadeIn 0.3s ease-in;
            font-weight: 500;
        `;
        messageDiv.innerHTML = `<p style="margin: 0;">✅ ${message}</p>`;

        // 메시지 삽입
        const infoTitle = document.querySelector('.info-title');
        if (infoTitle) {
            infoTitle.after(messageDiv);
        } else {
            document.body.prepend(messageDiv);
        }

        // 3초 후 자동 제거
        setTimeout(() => {
            if (messageDiv && messageDiv.parentNode) {
                messageDiv.style.animation = 'fadeOut 0.3s ease-out';
                setTimeout(() => {
                    if (messageDiv && messageDiv.parentNode) {
                        messageDiv.remove();
                    }
                }, 300);
            }
        }, 3000);
    }

    /**
     * ✅ 에러 메시지 표시 함수
     */
    function showErrorMessage(message) {
        // 기존 메시지 제거
        removeMessage();

        // 새 에러 메시지 생성
        const messageDiv = document.createElement('div');
        messageDiv.className = 'error-message';
        messageDiv.style.cssText = `
            background: #f8d7da; 
            color: #721c24; 
            padding: 12px 15px; 
            border-radius: 5px; 
            margin-bottom: 20px;
            border: 1px solid #f5c6cb;
            animation: fadeIn 0.3s ease-in;
            font-weight: 500;
        `;
        messageDiv.innerHTML = `<p style="margin: 0;">❌ ${message}</p>`;

        // 메시지 삽입
        const infoTitle = document.querySelector('.info-title');
        if (infoTitle) {
            infoTitle.after(messageDiv);
        } else {
            document.body.prepend(messageDiv);
        }

        // 5초 후 자동 제거 (에러는 조금 더 오래)
        setTimeout(() => {
            if (messageDiv && messageDiv.parentNode) {
                messageDiv.style.animation = 'fadeOut 0.3s ease-out';
                setTimeout(() => {
                    if (messageDiv && messageDiv.parentNode) {
                        messageDiv.remove();
                    }
                }, 300);
            }
        }, 5000);
    }

    /**
     * ✅ 기존 메시지 제거 함수
     */
    function removeMessage() {
        const existingMessages = document.querySelectorAll('.success-message, .error-message');
        existingMessages.forEach(msg => {
            if (msg && msg.parentNode) {
                msg.remove();
            }
        });
    }

    /**
     * ✅ 사용자 데이터 완전 정리
     */
    function clearUserData() {
        console.log('🧹 사용자 데이터 정리 시작');

        try {
            // 로컬 스토리지 정리
            localStorage.clear();

            // 세션 스토리지 정리
            sessionStorage.clear();

            // 쿠키 정리 (인증 관련)
            clearAuthCookies();

            console.log('✅ 사용자 데이터 정리 완료');

        } catch (error) {
            console.error('⚠️ 데이터 정리 중 오류:', error);
        }
    }

    /**
     * ✅ 인증 관련 쿠키 삭제
     */
    function clearAuthCookies() {
        // 일반적인 인증 쿠키 이름들
        const cookiesToClear = [
            'authToken',
            'refreshToken',
            'sessionId',
            'JSESSIONID',
            'accessToken'
        ];

        cookiesToClear.forEach(cookieName => {
            // 쿠키 삭제 (만료일을 과거로 설정)
            document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
            document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`;
        });

        console.log('🍪 인증 쿠키 정리 완료');
    }

    // ========================================
    // 🎯 프로필 이미지 관련 유틸리티 함수들
    // ========================================

    /**
     * 이미지 파일 유효성 검사
     */
    function validateImageFile(file) {
        // 파일 크기 확인 (5MB 제한)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            showErrorMessage('파일 크기는 5MB를 초과할 수 없습니다.');
            return false;
        }

        // 파일 타입 확인
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
        if (!allowedTypes.includes(file.type)) {
            showErrorMessage('지원하지 않는 파일 형식입니다. (JPG, PNG, GIF만 허용)');
            return false;
        }

        return true;
    }

    /**
     * 선택한 이미지를 즉시 화면에 표시 (미리보기)
     */
    function showImagePreview(file, imgElement) {
        if (!file || !imgElement) return;

        const reader = new FileReader();

        reader.onload = function(e) {
            imgElement.src = e.target.result;
            console.log('이미지 미리보기 적용 완료');
        };

        reader.onerror = function() {
            console.error('파일 읽기 실패');
            showErrorMessage('파일을 읽는 중 오류가 발생했습니다.');
        };

        reader.readAsDataURL(file);
    }

    /**
     * 서버에 파일 업로드하고 DB에 URL 저장
     */
    async function uploadAndSaveProfileImage(file) {
        try {
            console.log('🚀 파일 업로드 시작...');

            // 1️⃣ 파일 업로드 API 호출
            const formData = new FormData();
            formData.append('file', file);

            const uploadResponse = await fetch('/api/v1/profiles/me/profileImage/upload', {
                method: 'POST',
                body: formData
            });

            if (!uploadResponse.ok) {
                const errorText = await uploadResponse.text();
                throw new Error(`파일 업로드 실패: ${uploadResponse.status} - ${errorText}`);
            }

            const uploadResult = await uploadResponse.json();
            const imageUrl = uploadResult.imageUrl;

            console.log('✅ 파일 업로드 성공! URL:', imageUrl);

            // 2️⃣ DB에 이미지 URL 저장
            console.log('💾 DB에 이미지 URL 저장 시작...');

            const updateResponse = await fetch('/api/v1/profiles/me/profileImage', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    profileImage: imageUrl
                })
            });

            if (!updateResponse.ok) {
                const errorText = await updateResponse.text();
                throw new Error(`프로필 업데이트 실패: ${updateResponse.status} - ${errorText}`);
            }

            const updateResult = await updateResponse.json();
            console.log('✅ DB 저장 성공!', updateResult);

            // 3️⃣ 화면의 모든 프로필 이미지 업데이트 (캐시 방지)
            const finalImageUrl = imageUrl + '?t=' + new Date().getTime();
            updateAllProfileImages(finalImageUrl);

            // 원본 이미지 URL 업데이트
            originalProfileImageSrc = finalImageUrl;

            return updateResult;

        } catch (error) {
            console.error('❌ 프로필 이미지 변경 중 오류:', error);
            throw error; // 상위로 에러 전파
        } finally {
            // ✅ 파일 input 초기화 (연속 업로드 가능하게)
            const fileInput = document.querySelector('input[type="file"]');
            if (fileInput) {
                fileInput.value = '';
            }
        }
    }

    /**
     * ✅ 모든 프로필 이미지 업데이트 함수
     */
    function updateAllProfileImages(imageUrl) {
        console.log('🔄 모든 프로필 이미지 업데이트 시작:', imageUrl);

        // 페이지에서 프로필 이미지로 사용될 수 있는 모든 요소 찾기
        const selectors = [
            '.profile-image',
            '#profileImage',
            '.user-avatar',
            '.profile-picture',
            '.user-profile-image',
            '[data-profile-image]',
            '.profile img',
            '.header-profile img'
        ];

        let updatedCount = 0;

        selectors.forEach(selector => {
            const elements = document.querySelectorAll(selector);
            elements.forEach(element => {
                if (element.tagName === 'IMG') {
                    element.src = imageUrl;
                    updatedCount++;
                    console.log(`이미지 업데이트 (${selector}):`, imageUrl);
                } else if (element.style) {
                    // div 등의 배경 이미지인 경우
                    element.style.backgroundImage = `url(${imageUrl})`;
                    updatedCount++;
                    console.log(`배경 이미지 업데이트 (${selector}):`, imageUrl);
                }
            });
        });

        console.log(`✅ 총 ${updatedCount}개의 프로필 이미지 업데이트 완료`);
    }

    /**
     * ✅ 안전한 프로필 이미지 요소 찾기
     */
    function getProfileImageElement() {
        // 여러 방법으로 프로필 이미지 요소 찾기
        const possibleSelectors = [
            '.profile-image',
            '#profileImage',
            '.user-avatar',
            '.profile-picture',
            '[data-profile-image]'
        ];

        for (const selector of possibleSelectors) {
            const element = document.querySelector(selector);
            if (element) {
                console.log('프로필 이미지 요소 발견:', selector);
                return element;
            }
        }

        console.warn('프로필 이미지 요소를 찾을 수 없습니다.');
        return null;
    }

    // ========================================
    // 🎯 CSS 애니메이션 스타일 추가
    // ========================================

    // 페이지 로드 시 CSS 애니메이션 스타일 자동 추가
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { 
                opacity: 0; 
                transform: translateY(-10px); 
            }
            to { 
                opacity: 1; 
                transform: translateY(0); 
            }
        }
        
        @keyframes fadeOut {
            from { 
                opacity: 1; 
                transform: translateY(0); 
            }
            to { 
                opacity: 0; 
                transform: translateY(-10px); 
            }
        }
        
        .success-message, .error-message {
            animation: fadeIn 0.3s ease-in;
            z-index: 1000;
            position: relative;
        }
        
        /* 로딩 상태 스타일 */
        .loading {
            opacity: 0.7;
            pointer-events: none;
        }
        
        /* 업로드 중 스타일 */
        .uploading {
            cursor: not-allowed;
        }
    `;
    document.head.appendChild(style);

    // ========================================
    // 🔧 추가 개선 기능들
    // ========================================

    /**
     * ✅ 디버깅용 함수들 (개발 중에만 사용)
     */
    window.debugProfile = {
        // 닉네임 API 테스트
        testNickname: function(nickname) {
            console.log('🧪 닉네임 API 테스트:', nickname);
            fetch('/api/v1/profiles/me/nickname', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({nickname: nickname})
            })
                .then(response => response.json())
                .then(data => console.log('테스트 결과:', data))
                .catch(error => console.error('테스트 실패:', error));
        },

        // 자기소개 API 테스트
        testIntro: function(intro) {
            console.log('🧪 자기소개 API 테스트:', intro);
            fetch('/api/v1/profiles/me/introduce', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({profileBio: intro})
            })
                .then(response => response.json())
                .then(data => console.log('테스트 결과:', data))
                .catch(error => console.error('테스트 실패:', error));
        },

        // 탈퇴 API 테스트
        testWithdraw: function() {
            console.log('🧪 탈퇴 API 테스트');
            fetch('/api/v1/members/me', {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            })
                .then(response => response.json())
                .then(data => console.log('테스트 결과:', data))
                .catch(error => console.error('테스트 실패:', error));
        },

        // 현재 상태 확인
        getStatus: function() {
            console.log('📊 현재 상태:', {
                originalNickname,
                originalSelfIntro,
                originalProfileImageSrc,
                닉네임입력: document.querySelector('#username')?.value,
                자기소개입력: document.querySelector('#selfIntroduction')?.value
            });
        }
    };

    /**
     * ✅ 전역 에러 핸들러
     */
    window.addEventListener('error', function(event) {
        console.error('💥 전역 에러 발생:', event.error);
        // 프로덕션에서는 에러 로깅 서비스로 전송
    });

    /**
     * ✅ 네트워크 상태 확인
     */
    window.addEventListener('online', function() {
        console.log('🌐 네트워크 연결됨');
        showSuccessMessage('네트워크가 연결되었습니다.');
    });

    window.addEventListener('offline', function() {
        console.log('📵 네트워크 연결 끊김');
        showErrorMessage('네트워크 연결이 끊어졌습니다. 연결을 확인해주세요.');
    });

    /**
     * ✅ 페이지 떠나기 전 확인 (편집 중일 때)
     */
    function checkUnsavedChanges() {
        const nicknameInput = document.querySelector('#username');
        const introTextarea = document.querySelector('#selfIntroduction');

        const hasNicknameChanges = nicknameInput && !nicknameInput.disabled &&
            nicknameInput.value.trim() !== originalNickname;
        const hasIntroChanges = introTextarea && !introTextarea.disabled &&
            introTextarea.value.trim() !== originalSelfIntro;

        if (hasNicknameChanges || hasIntroChanges) {
            return '변경사항이 저장되지 않았습니다. 정말 페이지를 떠나시겠습니까?';
        }
    }

    window.addEventListener('beforeunload', function(event) {
        const message = checkUnsavedChanges();
        if (message) {
            event.preventDefault();
            event.returnValue = message;
            return message;
        }
    });

    console.log('🎉 프로필 페이지 JavaScript 초기화 완료!');
    console.log('🛠️ 디버깅 함수: window.debugProfile 사용 가능');

}); // DOMContentLoaded 이벤트 끝
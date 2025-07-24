// DOM이 완전히 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 타이핑 효과
    const typingText = document.getElementById('typing-text');
    const messages = ['혼자가는 여행은 끝!', '새로운 동행자를 찾아보세요', '무드트립과 함께하세요'];
    let messageIndex = 0;
    let charIndex = 0;
    let isDeleting = false;

    function typeWriter() {
        const currentMessage = messages[messageIndex];
        
        if (isDeleting) {
            typingText.textContent = currentMessage.substring(0, charIndex - 1);
            charIndex--;
        } else {
            typingText.textContent = currentMessage.substring(0, charIndex + 1);
            charIndex++;
        }

        let typeSpeed = isDeleting ? 50 : 100;

        if (!isDeleting && charIndex === currentMessage.length) {
            typeSpeed = 2000; // 완성된 후 잠시 멈춤
            isDeleting = true;
        } else if (isDeleting && charIndex === 0) {
            isDeleting = false;
            messageIndex = (messageIndex + 1) % messages.length;
            typeSpeed = 500; // 다음 메시지 시작 전 잠시 멈춤
        }

        setTimeout(typeWriter, typeSpeed);
    }

    // 타이핑 효과 즉시 시작
    typeWriter();

    // 스크롤 애니메이션
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

    // 모든 fade-in 요소들을 관찰
    document.querySelectorAll('.fade-in, .fade-in-delay-1, .fade-in-delay-2, .fade-in-delay-3').forEach(el => {
        observer.observe(el);
    });

    // 버튼 클릭 이벤트
    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            // "방 만들러 가기"와 "방 찾으러 가기" 버튼은 링크로 이동하므로 기본 동작 유지
            if (btn.textContent.includes('방 만들러 가기') || btn.textContent.includes('방 찾으러 가기')) {
                return; // 기본 링크 동작 허용
            }
            
            // 다른 버튼들은 경고창 표시
            e.preventDefault();
            const text = btn.textContent;
            alert(`${text} 기능이 준비 중입니다! 🎉`);
        });
    });

    // 부드러운 스크롤
    const scrollIndicator = document.querySelector('.scroll-indicator');
    if (scrollIndicator) {
        scrollIndicator.addEventListener('click', () => {
            document.querySelector('.companies-section').scrollIntoView({
                behavior: 'smooth'
            });
        });
    }

    // 마우스 움직임에 따른 패럴랙스 효과
    document.addEventListener('mousemove', (e) => {
        const mouseX = e.clientX / window.innerWidth;
        const mouseY = e.clientY / window.innerHeight;
        
        const circles = document.querySelectorAll('.floating-circle');
        circles.forEach((circle, index) => {
            const speed = (index + 1) * 0.5;
            const x = (mouseX - 0.5) * speed * 50;
            const y = (mouseY - 0.5) * speed * 50;
            circle.style.transform = `translate(${x}px, ${y}px)`;
        });
    });
});
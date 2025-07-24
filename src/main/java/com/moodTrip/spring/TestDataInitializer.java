package com.moodTrip.spring;


import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) { // 테이블 비어있을 때만 실행
            Member member = Member.builder()
                    .name("테스트회원")
                    .email("test@example.com")
                    .build();
            memberRepository.save(member);

            System.out.println("✅ 테스트용 Member 데이터 삽입 완료!");
        }
    }
}

package com.moodTrip.spring.domain.member.controller;


import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "회원가입", description = "회원 관리 관련 API")
@Controller
@RequiredArgsConstructor  // final 필드 생성자 주입 생성
public class SignUpController {


    private final MemberService memberService;




    @Operation(summary = "회원가입 폼", description = "회원가입 화면을 반환한다.")
    @GetMapping("/api/signup")
    public String signupForm(Model model) {
        // 타임리프에서 th:object="${memberRequest}"를 사용하므로 반드시 넣어야 함
        model.addAttribute("memberRequest", new MemberRequest());
        return "signup/signup"; // templates/signup/signup.html
    }

    @Operation(summary = "회원가입 처리", description = "회원정보를 받아서 가입 처리한다.")
    @PostMapping("/api/signup")
    public String signupSubmit(@ModelAttribute("memberRequest") MemberRequest memberRequest,Model model) {
        try {
            //요청 객체 서비스 로직으로
            memberService.register(memberRequest); // 서비스 호출(예외 던짐)

            model.addAttribute("message", "회원가입이 성공적으로 완료되었습니다!");
            return "signup/success";  // 성공 시 보여줄 뷰 이름
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("memberRequest", memberRequest); // 폼 데이터 유지
            return "signup/signup";  // 실패 시 다시 회원가입 폼 보여주기
        }
    }

    @GetMapping("/signup/success")
    public String signupSuccess() {
        return "signup/success"; // templates/signup/success.html이 렌더링됨
    }
//회원 가입 에러
    @GetMapping("/signup")
    public String signupForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) model.addAttribute("errorMessage", error);
        model.addAttribute("memberRequest", new MemberRequest());
        return "signup/signup"; // templates/signup/signup.html
    }

}

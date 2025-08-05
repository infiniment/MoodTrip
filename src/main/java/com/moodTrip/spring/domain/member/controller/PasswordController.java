package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.PasswordForm;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MailService;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.common.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/password")
class PasswordController {
    @Autowired
    MemberService memberService;
    @Autowired
    MailService mailService;
    @Autowired
    PasswordEncoder passwordEncoder;

//    private final UserService userService;
//    private final MailService mailService;
//
//    public PasswordController(UserService userService, MailService mailService) {
//        this.userService = userService;
//        this.mailService = mailService;
//    }

    // 비밀번호 찾기 페이지 렌더링 (GET)
    @GetMapping("/find")
    public String showPasswordFindForm(Model model) {
        model.addAttribute("mailSent", false);
        return "find-password/find-password"; // templates/find-password.html
    }


    @PostMapping("/send-temp")
    public String sendTempPassword(@RequestParam String email, Model model) {
        // 1. 유저 조회
        Member member = memberService.findByEmail(email);
        if (member == null) {
            model.addAttribute("mailSent", false); // 반드시 추가!
            model.addAttribute("error", "등록된 이메일이 없습니다.");
            model.addAttribute("email", email);  // 이 부분을 반드시 추가!
            return "find-password/find-password";
        }

        // 2. 임시비밀번호 생성
        String tempPassword = PasswordUtil.generateTempPassword();

        // 3. 임시비밀번호로 유저 비밀번호 업데이트 (암호화 필수!)
        memberService.updatePassword(member, passwordEncoder.encode(tempPassword));

        // 4. 이메일로 임시비밀번호 발송
        mailService.sendTempPasswordMail(email, tempPassword);

        model.addAttribute("mailSent", true);
        model.addAttribute("email", email);
        return "find-password/find-password";

    }


//임시 비밀번호 인증

    @PostMapping("/validate-temp")
    public String validateTempPassword(@RequestParam String email,
                                       @RequestParam String extra_input,
                                       Model model, HttpSession session) {
        // 1. DB에서 해당 이메일 계정의 변경된 임시 비밀번호 조회
        Member member = memberService.findByEmail(email);

        if (member == null) {
            log.info("회원 조회 실패: 존재하지 않는 이메일 - {}", email);
            model.addAttribute("mailSent", false);
            model.addAttribute("error", "존재하지 않는 이메일입니다.");
            return "find-password/find-password";
        }

        // 2. 입력받은 임시 비밀번호와 비교
        if (!passwordEncoder.matches(extra_input, member.getMemberPw())) {
            log.info("임시 비밀번호 불일치: 이메일={}, 입력값={}", email, extra_input);
            model.addAttribute("mailSent", true); // 사용자가 메일을 이미 받았다고 알림
            model.addAttribute("error", "임시 비밀번호가 일치하지 않습니다.");
            return "find-password/find-password";
        }


        session.setAttribute("emailVerified", email);
        return "redirect:/password/new-password";

    }

//비밀번호 변경페이지
@GetMapping("/new-password")
public String showNewPasswordForm(Model model) {
    model.addAttribute("passwordForm", new PasswordForm());
    return "find-password/new-password";
}


    @PostMapping("/reset")
    public String resetPassword(
            @ModelAttribute("passwordForm") PasswordForm form,
            HttpSession session, RedirectAttributes redirectAttributes,
            Model model) {

        String email = (String) session.getAttribute("emailVerified"); // 이메일 인증 세션값 확인

        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "인증된 이메일 정보가 없습니다.");
            return "redirect:/password/find";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "redirect:/password/new-password";
        }

        Member member = memberService.findByEmail(email);
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "회원 정보가 존재하지 않습니다.");
            return "redirect:/password/find";
        }

        memberService.updatePassword(member, passwordEncoder.encode(form.getNewPassword()));
        session.removeAttribute("emailVerified");
        redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
        return "login/login"; // 성공 메시지 포함 동일 페이지 or 성공페이지

    }



}
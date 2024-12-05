package com.worthyi.worthyi_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 홈 페이지 또는 로그인 페이지
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 대시보드 페이지 (로그인 후 접근 가능)
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("user", authentication.getPrincipal());
        return "dashboard";
    }
}
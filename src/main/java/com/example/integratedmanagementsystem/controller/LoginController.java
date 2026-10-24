package com.example.integratedmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.integratedmanagementsystem.model.LoginDTO;

@Controller
public class LoginController {
  @GetMapping("/")
  public String welcomePage() {
    return "welcome";
  }

  @GetMapping("/staff/login")
  public String staffLoginPage(Model model) {
    LoginDTO form = new LoginDTO();
    model.addAttribute("login", form);
    return "stafflogin";
  }

  @GetMapping("/admin/login")
  public String adminLoginPage(Model model) {
    LoginDTO form = new LoginDTO();
    model.addAttribute("login", form);
    return "adminlogin";
  }
}

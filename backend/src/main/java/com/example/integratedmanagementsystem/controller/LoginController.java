package com.example.integratedmanagementsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.integratedmanagementsystem.model.LoginDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
  @GetMapping("/")
  public String welcomePage() {
    return "welcome";
  }

  // destination for controllers and JavaScript when user is no longer found in the repository (due to cases like admin removal while they are still logged in)
  @GetMapping("/logout/accountnotfound")
  public String redirectWelcomePage(HttpServletRequest request, HttpServletResponse response, RedirectAttributes ra) {
 
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // check for active logged-in session from Spring Security
    if (auth != null) {
      // clear SecurityContextHolder memory, terminate user session and auto logout
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }

    ra.addFlashAttribute("errormsg", "User No Longer Exist: Your Account might have been deleted by an administrator. Please contact your manager / administrator for more details.");

    return "redirect:/";
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

package com.example.integratedmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
  @GetMapping("/about")
  public String aboutUsPage() {
    return "about";
  }

}

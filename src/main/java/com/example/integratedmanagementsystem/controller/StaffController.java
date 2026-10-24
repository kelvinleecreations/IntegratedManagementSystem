package com.example.integratedmanagementsystem.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.integratedmanagementsystem.model.ProfileDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/staff")
public class StaffController {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public StaffController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/index")
  public String showStaffIndex() {
    return "index";
  }

  @GetMapping("/profile")
  public String showStaffProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model) {

    User loggedInUser = principalUser.getUser();

    ProfileDTO form = new ProfileDTO();

    // Automatic prefill of user details to matching fields of the form
    BeanUtils.copyProperties(loggedInUser, form);
    model.addAttribute("profileForm", form);
    return "profile";
  }

  @GetMapping("/myteam")
  public String showMyTeam(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra) {
    
    User loggedInUser = principalUser.getUser();

    User loggedInUserManager = loggedInUser.getManager();

    List<User> teamMembers = new ArrayList<>();

    // if there is a manager, get all his/her subordinates as well
    if (loggedInUserManager != null) {
      teamMembers = userRepository.findByManagerUserIdAndUserIdNot(loggedInUserManager.getUserId(), loggedInUser.getUserId());
      }

    // check if the logged in user is a manager, if yes, return his/her subordinates in another section
    List<User> subordinatesList = new ArrayList<>();
    
    try {
      // very unlikely for this exception to happen, because Spring Security would have checked before it reaches the controller

      // find and load the subordinates of logged in user into the user object
      User loggedInUserWithSubordinatesLoaded = userRepository.findWithSubordinatesByUserId(loggedInUser.getUserId()).orElseThrow(() -> new RuntimeException("Logged In User not found"));

      // populate the empty list
      subordinatesList = loggedInUserWithSubordinatesLoaded.getSubordinates();
    }
    catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/index";
    }
    
    model.addAttribute("subordinates", subordinatesList);
    model.addAttribute("manager", loggedInUserManager);
    model.addAttribute("otherMembers", teamMembers);
    return "myteam";
  }

  @PostMapping("/saveprofile")
  public String saveStaffProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("profileForm") ProfileDTO form, BindingResult result, RedirectAttributes ra) {

    User loggedInUser = principalUser.getUser();

    boolean hasCurrentPassword = form.getCurrentPassword() != null && !form.getCurrentPassword().trim().isEmpty();
    boolean hasNewPassword = form.getNewPassword() != null && !form.getNewPassword().trim().isEmpty();

    // When both fields are not filled
    if (!hasCurrentPassword && !hasNewPassword) {
      ra.addFlashAttribute("errormsg", "No changes saved. You are redirected back to your home page.");
      return "redirect:/staff/index";
    }

    // Apply NotBlank Validation only when either field is filled
    if (hasCurrentPassword || hasNewPassword) {
      // If current password field is not filled while new password is filled,
      if (!hasCurrentPassword) {
        result.rejectValue("currentPassword", "currentPasswordEmptyError", "Please enter your current password");
      }

      // If current password field is filled while new password is not filled,
      if (!hasNewPassword) {
        result.rejectValue("newPassword", "newPasswordEmptyError", "Please enter your new password");
      }

      // If current password is filled and it does not match with the existing password from the database,
      if (hasCurrentPassword && !passwordEncoder.matches(form.getCurrentPassword(), loggedInUser.getPassword())) {
        result.rejectValue("currentPassword", "currentPasswordMatchingError", "The current password entered does not match with your existing one");
      }

      // If password is filled but does not fulfil the password change constraints,
      if (hasNewPassword && (form.getNewPassword().trim().length() < 5 || form.getNewPassword().trim().length() > 20)) {
        result.rejectValue("newPassword", "newPasswordLengthError", "New Password should only be between 5 to 20 characters, inclusive");
      } 
    }

    if (result.hasErrors()) {
      return "profile";
    }

    // Update Password
    // Encrypt password for Spring Security during login
    if (hasNewPassword){
      String encryptedPassword = passwordEncoder.encode(form.getNewPassword());
      loggedInUser.setPassword(encryptedPassword);
    }
    
    userRepository.save(loggedInUser);

    // Flash Attribute
    ra.addFlashAttribute("successmsg", "Password updated successfully!");
    return "redirect:/staff/index";
  }

  @GetMapping("/hr")
  public String showHR() {
    return "hr";
  }

  @GetMapping("/finance")
  public String showFinance() {
    return "finance";
  }
}

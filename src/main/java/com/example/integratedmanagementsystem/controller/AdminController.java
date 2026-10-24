package com.example.integratedmanagementsystem.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.integratedmanagementsystem.model.ProfileDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/index")
  public String showAdminIndex() {
    return "index";
  }

  @GetMapping("/profile")
  public String showAdminProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model) {

    User loggedInUser = principalUser.getUser();

    ProfileDTO form = new ProfileDTO();

    // Automatic prefill of user details to matching fields of the form
    BeanUtils.copyProperties(loggedInUser, form);
    model.addAttribute("profileForm", form);
    return "profile";
  }

  @PostMapping("/saveprofile")
  public String saveStaffProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("profileForm") ProfileDTO form, BindingResult result, RedirectAttributes ra) {

    User loggedInUser = principalUser.getUser();

    boolean hasCurrentPassword = form.getCurrentPassword() != null && !form.getCurrentPassword().trim().isEmpty();
    boolean hasNewPassword = form.getNewPassword() != null && !form.getNewPassword().trim().isEmpty();
    
    // When both fields are not filled
    if (!hasCurrentPassword && !hasNewPassword) {
      ra.addFlashAttribute("errormsg", "No changes saved. You are redirected back to your home page.");
      return "redirect:/admin/index";
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
    return "redirect:/admin/index";
  }

  @GetMapping("/administration")
  public String showAdministration(@RequestParam(defaultValue = "0") int page, @RequestParam(required=false) String search, Model model) {

    Pageable pageable = PageRequest.of(page,10, Sort.by("userId").ascending());

    Page<User> userPage;

    // if Admin enters on the search field,
    if (search != null && !search.trim().isEmpty()) {
      userPage = userRepository.findByUsernameContainingIgnoreCase(search.trim(), pageable);
    }
    // display all
    else {
      userPage = userRepository.findAllWithSubordinatesAndManager(pageable);
    }
    
    if (page > 0 && userPage.getContent().isEmpty()) {
      return "redirect:/administration";
    }

    // return user list
    model.addAttribute("users", userPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", userPage.getTotalPages());
    model.addAttribute("filter", search);
    return "administration";
  }

  @GetMapping("/createuser")
  public String createUser(Model model){
    User form = new User();

    model.addAttribute("userForm", form);

    // return all managers in the current user repository
    List<User> managers = userRepository.findAllManagers();
    model.addAttribute("managers", managers);

    return "userform";
  }

  @PostMapping("/createuser")
  public String saveUser(@Valid @ModelAttribute("userForm") User form, BindingResult result, Model model, RedirectAttributes ra) {

    // during registration of new users,
    if (form.getUserId() == null) {
      // check if the username already exists
      if (userRepository.existsByUsername(form.getUsername())){
        result.rejectValue("username", "usernameerror", "Username already exists");
      }
    }

    // during editing of existing users
    else {
      // 'Not' is to exclude ownself from the check of duplicate username, allowing editing and updating to pass through

      // Example when the admin is trying to edit Cass's details, it will pass even though the username already exist, just because it is hers to begin with

      // If the username is found to exist within a user's details when editing another user details, reject
      if (userRepository.existsByUsernameAndUserIdNot(form.getUsername(), form.getUserId())){
        result.rejectValue("username", "usernameerror", "Username already exists");
      }
    }

    if (result.hasErrors()) {
      // repopulate the manager list
      List<User> managers = userRepository.findAllManagers();
      model.addAttribute("managers", managers);
      return "userform";
    }

    // check if it is a new user or an existing user
    boolean isNewUser = (form.getUserId()==null);

    // for existing user, extract original pw
    if (!isNewUser) {
      User originalUserDetails = userRepository.findById(form.getUserId()).orElse(null);
      form.setPassword(originalUserDetails.getPassword());
    }
    // for new users, give them default pw to login
    else {
      form.setPassword("DefaultPassword");

      // Encrypt password for Spring Security during login
      String encryptedPassword = passwordEncoder.encode(form.getPassword());
      form.setPassword(encryptedPassword);
    }

    userRepository.save(form);

    // Flash Attributes
    if (isNewUser) {
      ra.addFlashAttribute("successmsg", "User '" + form.getUserId() + "' created successfully!");
    } else {
      ra.addFlashAttribute("successmsg", "User '" + form.getUserId() + "' updated successfully!");
    }

    return "redirect:/admin/administration";

  }

  @GetMapping("/edituser")
  public String editUser(@RequestParam(name="userId", required=true) Integer id, Model model, RedirectAttributes ra) {
    // check if the user to be edited exists
    User editedUser = userRepository.findById(id).orElse(null);
    if (editedUser == null) {
      return "redirect:/admin/administration";
    }

    // prevent editing of CEO
    if (id == 1) {
      ra.addFlashAttribute("errormsg", "You are not authorised to edit the details of this person");
      return "redirect:/admin/administration";
    }

    // Prefill the form
    model.addAttribute("userForm", editedUser);

    // Prefill manager list
    List<User> managers = userRepository.findAllManagers();

    model.addAttribute("managers", managers);
    return "userform";
  }

  @PostMapping("/deleteuser")
  public String deleteUser(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="userId", required=true) Integer id, Model model, RedirectAttributes ra) {

    User loggedInUser = principalUser.getUser();
    // check if admin is trying to delete himself/herself
    if (loggedInUser.getUserId().equals(id)) {
      ra.addFlashAttribute("errormsg", "You are not authorised to delete yourself");
      return "redirect:/admin/administration";
    }

    // check if user to be deleted is the CEO
    if (id == 1) {
      ra.addFlashAttribute("errormsg", "You are not authorised to remove this person");
      return "redirect:/admin/administration";
    }

    // check if the user to be deleted exists
    User deletedUser = userRepository.findById(id).orElse(null);
    if (deletedUser == null) {
      return "redirect:/admin/administration";
    }

    // unlink subordinates and manager related to the user
    userRepository.unlinkSubordinates(id);
    userRepository.unlinkManager(id);

    userRepository.deleteById(id);
    
    // Flash Attribute
    ra.addFlashAttribute("successmsg", "User '" + deletedUser.getUserId() + "' deleted successfully!");

    return "redirect:/admin/administration";
  
  }
}

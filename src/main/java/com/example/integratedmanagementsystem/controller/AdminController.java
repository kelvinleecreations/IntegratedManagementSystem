package com.example.integratedmanagementsystem.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.integratedmanagementsystem.exception.SvcValidationException;
import com.example.integratedmanagementsystem.model.LeaveDTO;
import com.example.integratedmanagementsystem.model.ProfileDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.LeaveApplicationService;
import com.example.integratedmanagementsystem.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final UserRepository userRepository;
  private final UserService userService;
  private final LeaveApplicationService leaveApplicationService;

  public AdminController(UserRepository userRepository, UserService userService, LeaveApplicationService leaveApplicationService) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.leaveApplicationService = leaveApplicationService;
  }

  @GetMapping("/index")
  public String showAdminIndex(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) Integer yearSearch, Model model, RedirectAttributes ra) {

  // additional check whether user exists
  User loggedInUser = principalUser.getUser();
  
  // get from database
  User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

  if (currentUser == null) {
    return "redirect:/logout/accountnotfound";
  }

  try {
    model.addAttribute("user", loggedInUser);

    // add in the leave dto to show dynamic changes based on year selection to user-statistics
    Integer selectedYear = (yearSearch != null) ? yearSearch : LocalDate.now().getYear();
    LeaveDTO leaveDTO = leaveApplicationService.getLeaveFiguresBasedOnSelectedYear(loggedInUser.getUserId(), selectedYear);

    model.addAttribute("leave", leaveDTO);
    model.addAttribute("yearSelection", List.of(2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029));
    return "index";

  } catch (RuntimeException error) {
    ra.addFlashAttribute("errormsg", error.getMessage());
    return "redirect:/admin/index";
    }
  }

  @GetMapping("/profile")
  public String showAdminProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra) {

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

    ProfileDTO form = new ProfileDTO();

    // Automatic prefill of user details to matching fields of the form
    BeanUtils.copyProperties(loggedInUser, form);
    model.addAttribute("profileForm", form);
    return "profile";
  }

  @PostMapping("/saveprofile")
  public String saveAdminProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("profileForm") ProfileDTO form, BindingResult result, Model model, RedirectAttributes ra) {

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

    // valid annotation check
    if (result.hasErrors()) {
      return "profile";
    }

    // try-catch block with Service Layer
    try {
      String status = userService.saveUserProfile(form, loggedInUser);

      if ("NO_CHANGES".equals(status)) {
        ra.addFlashAttribute("errormsg", "No changes saved. You are redirected back to your home page.");
      }

      if ("UPDATED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Password updated successfully!");
      }

      return "redirect:/admin/index";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      
      return "profile";
    } 
  }

  @GetMapping("/administration")
  public String showAdministration(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(defaultValue = "0") int page, @RequestParam(required=false) String search, Model model, RedirectAttributes ra) {

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

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
  public String createUser(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra){

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

    User form = new User();

    model.addAttribute("userForm", form);

    // return all managers in the current user repository
    List<User> managers = userRepository.findAllManagers();
    model.addAttribute("managers", managers);

    return "userform";
  }

  @PostMapping("/createuser")
  public String saveUser(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("userForm") User form, BindingResult result, Model model, RedirectAttributes ra) {

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

    // valid annotation check
    if (result.hasErrors()) {
      // repopulate the manager list
      List<User> managers = userRepository.findAllManagers();
      model.addAttribute("managers", managers);
      return "userform";
    }

    // try-catch block with Service Layer
    try {
      String status = userService.saveUser(form);

      if ("NO_CHANGES".equals(status)) {
        ra.addFlashAttribute("errormsg", "No changes saved. You are redirected back to the administration page.");
      }

      if ("CREATED".equals(status)) {
        ra.addFlashAttribute("successmsg", "User '" + form.getUserId() + "' created successfully!");
      }

      if ("UPDATED".equals(status)) {
        ra.addFlashAttribute("successmsg", "User '" + form.getUserId() + "' updated successfully!");
      }
      
      return "redirect:/admin/administration";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      List<User> managers = userRepository.findAllManagers();
      model.addAttribute("managers", managers);

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      return "userform";
    }
  }

  @GetMapping("/edituser")
  public String editUser(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="userId", required=true) Integer id, Model model, RedirectAttributes ra) {

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }
    
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

    // additional check whether user exists
    User loggedInUser = principalUser.getUser();
  
    // get from database
    User currentUser = userRepository.findById(loggedInUser.getUserId()).orElse(null);

    if (currentUser == null) {
      return "redirect:/logout/accountnotfound";
    }

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

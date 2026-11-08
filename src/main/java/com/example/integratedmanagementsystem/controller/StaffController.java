package com.example.integratedmanagementsystem.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.integratedmanagementsystem.exception.SvcValidationException;
import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.ApplicationStatus;
import com.example.integratedmanagementsystem.model.LeaveDTO;
import com.example.integratedmanagementsystem.model.ProfileDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.LeaveApplicationService;
import com.example.integratedmanagementsystem.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staff")
public class StaffController {

  private final UserRepository userRepository;
  private final UserService userService;
  private final LeaveApplicationRepository leaveApplicationRepository;
  private final LeaveApplicationService leaveApplicationService;

  public StaffController(UserRepository userRepository, UserService userService, LeaveApplicationRepository leaveApplicationRepository, LeaveApplicationService leaveApplicationService) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.leaveApplicationRepository = leaveApplicationRepository;
    this.leaveApplicationService = leaveApplicationService;
  }

  @GetMapping("/index")
  public String showStaffIndex(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) Integer yearSearch, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    try {
      model.addAttribute("user", loggedInUser);

      // filtering by year
      Integer selectedYear = (yearSearch != null) ? yearSearch : LocalDate.now().getYear();

      // display applications that have not been reviewed below the user statistics
      List<ApplicationStatus> beforeReviewStatus = List.of(ApplicationStatus.APPLIED, ApplicationStatus.UPDATED, ApplicationStatus.DELETED);
      List<Application> leaveApplications = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(loggedInUser.getUserId(), beforeReviewStatus);
    
      // display reviewed applications in a separate table
      List<ApplicationStatus> postReviewStatus = List.of(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED);
      List<Application> reviewedLeaveApplications = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(loggedInUser.getUserId(), postReviewStatus);

      // display cancelled applications in a separate table
      List<ApplicationStatus> cancelledStatus = List.of(ApplicationStatus.CANCELLED);
      List<Application> cancelledLeaveApplications = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(loggedInUser.getUserId(), cancelledStatus);

      // displayed locked applications in a separate table
      List<ApplicationStatus> lockedStatus = List.of(ApplicationStatus.LOCKED);
      List<Application> lockedLeaveApplications = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(loggedInUser.getUserId(), lockedStatus);

      // filtering
      if (selectedYear != null) {
        leaveApplications = leaveApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        reviewedLeaveApplications = reviewedLeaveApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        cancelledLeaveApplications = cancelledLeaveApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        lockedLeaveApplications = lockedLeaveApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
      }

      // add in the leave dto to show dynamic changes based on year selection to user-statistics
      LeaveDTO leaveDTO = leaveApplicationService.getLeaveFiguresBasedOnSelectedYear(loggedInUser.getUserId(), selectedYear);

      model.addAttribute("applications", leaveApplications);
      model.addAttribute("reviewedApplications", reviewedLeaveApplications);
      model.addAttribute("cancelledApplications", cancelledLeaveApplications);
      model.addAttribute("lockedApplications", lockedLeaveApplications);
      model.addAttribute("leave", leaveDTO);
      model.addAttribute("yearSelection", List.of(2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029));
      return "index";

    } catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/index";
    }
  }

  @GetMapping("/profile")
  public String showStaffProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    ProfileDTO form = new ProfileDTO();

    // Automatic prefill of user details to matching fields of the form
    BeanUtils.copyProperties(loggedInUser, form);
    model.addAttribute("profileForm", form);
    return "profile";
  }

  @GetMapping("/myteam")
  public String showMyTeam(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra) {
    
    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

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

      model.addAttribute("subordinates", subordinatesList);
      model.addAttribute("manager", loggedInUserManager);
      model.addAttribute("otherMembers", teamMembers);
      return "myteam";
    }
    catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/index";
    }
  }

  @PostMapping("/saveprofile")
  public String saveStaffProfile(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("profileForm") ProfileDTO form, BindingResult result, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // valid annotation check
    if (result.hasErrors()) {
      return "profile";
    }

    // try-catch block with Service Layer
    try {
      String status = userService.saveUserProfile(form, loggedInUser);

      if ("READONLY_FIELDS_COMPROMISED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Form Name, Username or Email no longer matches your profile. Due to Security Issue, updating user profile process is stopped. You are redirected back to your home page.");
      }

      if ("NO_CHANGES".equals(status)) {
        ra.addFlashAttribute("errormsg", "No changes saved. You are redirected back to your home page.");
      }

      if ("UPDATED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Password updated successfully!");
      }

      return "redirect:/staff/index";

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

  @GetMapping("/hr")
  public String showHR(@AuthenticationPrincipal SecurityUserDetails principalUser) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    return "hr";
  }

  @GetMapping("/finance")
  public String showFinance(@AuthenticationPrincipal SecurityUserDetails principalUser) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }
    
    return "finance";
  }

  @GetMapping("/calendar")
  public String showCalendar(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="year", required=false) Integer yearSearch, @RequestParam(name="month", required=false) String monthSearch, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // If no month and year are provided, display current month and year
    YearMonth selectedYearMonth;
    if (yearSearch == null || monthSearch == null || monthSearch.trim().isEmpty()) {
      LocalDate currentDate = LocalDate.now();
      selectedYearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
    }
    else {
      // display what was selected
      // java.time.Month is a built-in enum containing all months in caps
      // .valueOf searches that enum for matches
      // converts to upper case to match
      // YearMonth.of combines the year and month and stores them together, to be used for subsequent code
      selectedYearMonth = YearMonth.of(yearSearch, java.time.Month.valueOf(monthSearch.toUpperCase()));
    }

    String nameOfMonth = selectedYearMonth.getMonth().name();
    int numOfDaysInMonth = selectedYearMonth.lengthOfMonth();
    int currentYear = selectedYearMonth.getYear();

    // Find the 1st day of the month
    LocalDate firstDayOfMonth = selectedYearMonth.atDay(1);
    // and its corresponding day of the week
    int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

    // Default is 1 - Monday, 2 - Tuesday to 7 - Sunday
    // Adjust to ensure that Sunday is 1, Monday is 2 and so on
    int reassignedDayOfWeek;

    if (dayOfWeek == 7) {
      reassignedDayOfWeek = 1; // For Sunday
    } else {
      reassignedDayOfWeek = dayOfWeek + 1; // Monday to Saturday
    }

    // create list for blank days
    List<Integer> blankDays = new ArrayList<>();
    for (int i = 1; i < reassignedDayOfWeek; i++) {
      blankDays.add(0);
    }

    // create list for days
    List<Integer> days = new ArrayList<>();
    for (int i = 1; i <= numOfDaysInMonth; i++) {
      days.add(i);
    }

    model.addAttribute("currentMonth", nameOfMonth);
    model.addAttribute("currentYear", currentYear);
    model.addAttribute("blanks", blankDays);
    model.addAttribute("days", days);

    // create list for years
    List<Integer> years = List.of(2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029);
    model.addAttribute("years", years);

    // create list for months
    List<String> months = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
    model.addAttribute("months", months);
    return "calendar";
  }

  @GetMapping("/pendingapproval")
  public String showPendingApprovals(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) String search, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    List<User> subordinatesList = new ArrayList<>();

    List<Application> subordinatesLeaveApplications = new ArrayList<>();

    try {
      // very unlikely for this exception to happen, because Spring Security would have checked before it reaches the controller

      // find and load the subordinates of logged in user into the user object
      User loggedInUserWithSubordinatesLoaded = userRepository.findWithSubordinatesByUserId(loggedInUser.getUserId()).orElseThrow(() -> new RuntimeException("Logged In User not found"));

      // populate the empty subordinates list
      subordinatesList = loggedInUserWithSubordinatesLoaded.getSubordinates();

      // populate the empty subordinates leave applications list
      // only allow "APPLIED" and "UPDATED" statuses to be shown in manager view
      List<ApplicationStatus> status = List.of(ApplicationStatus.APPLIED, ApplicationStatus.UPDATED);
      // if Manager enters on the search field by username,
      if (search != null && !search.trim().isEmpty()) {
        subordinatesLeaveApplications = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(subordinatesList, status, search.trim());
      }
      // else, just display all
      else {
        subordinatesLeaveApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(subordinatesList, status);
      }

      model.addAttribute("filter", search);
      model.addAttribute("subordinatesApplications", subordinatesLeaveApplications);
      model.addAttribute("subordinates", subordinatesList);
      return "pendingapproval";
    }
    catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/index";
    }
  }

  @GetMapping("/reviewedleave")
  public String reviewedLeave(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) String searchLocked, @RequestParam(required=false) String searchApproved, @RequestParam(required=false) String searchRejected, @RequestParam(required=false) String searchCancelled, @RequestParam(required=false) Integer yearSearch, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    List<User> subordinatesList = new ArrayList<>();

    List<Application> subordinatesLockedApplications = new ArrayList<>();
    List<Application> subordinatesApprovedApplications = new ArrayList<>();
    List<Application> subordinatesRejectedApplications = new ArrayList<>();
    List<Application> subordinatesCancelledApplications = new ArrayList<>();

    // filtering by year
    Integer selectedYear = (yearSearch != null) ? yearSearch : LocalDate.now().getYear();

    try {
      // very unlikely for this exception to happen, because Spring Security would have checked before it reaches the controller

      // find and load the subordinates of logged in user into the user object
      User loggedInUserWithSubordinatesLoaded = userRepository.findWithSubordinatesByUserId(loggedInUser.getUserId()).orElseThrow(() -> new RuntimeException("Logged In User not found"));

      // populate the empty subordinates list
      subordinatesList = loggedInUserWithSubordinatesLoaded.getSubordinates();

      // display all subordinates leave that has been locked in the first table
      List<ApplicationStatus> lockedStatus = List.of(ApplicationStatus.LOCKED);

      // if Manager enters on the search field by username for locked table,
      if (searchLocked != null && !searchLocked.trim().isEmpty()) {
        subordinatesLockedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(subordinatesList, lockedStatus, searchLocked.trim());
      }

      // else, just display all
      else {
        subordinatesLockedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(subordinatesList, lockedStatus);
      }

      // display all subordinates leave that has been approved in the second table
      List<ApplicationStatus> approvedStatus = List.of(ApplicationStatus.APPROVED);

      // if Manager enters on the search field by username for approved table,
      if (searchApproved != null && !searchApproved.trim().isEmpty()) {
        subordinatesApprovedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(subordinatesList, approvedStatus, searchApproved.trim());
      }
      // else, just display all
      else {
        subordinatesApprovedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(subordinatesList, approvedStatus);
      }

      // display all subordinates leave that has been rejected in the third table
      List<ApplicationStatus> rejectedStatus = List.of(ApplicationStatus.REJECTED);

      // if Manager enters on the search field by username for rejected table,
      if (searchRejected != null && !searchRejected.trim().isEmpty()) {
        subordinatesRejectedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(subordinatesList, rejectedStatus, searchRejected.trim());
      }
      // else, just display all
      else {
        subordinatesRejectedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(subordinatesList, rejectedStatus);
      }

      // display all subordinates leave that has been cancelled in the fourth table
      List<ApplicationStatus> cancelledStatus = List.of(ApplicationStatus.CANCELLED);

      // if Manager enters on the search field by username for cancelled table,
      if (searchCancelled != null && !searchCancelled.trim().isEmpty()) {
        subordinatesCancelledApplications = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(subordinatesList, cancelledStatus, searchCancelled.trim());
      }
      // else, just display all
      else {
        subordinatesCancelledApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(subordinatesList, cancelledStatus);
      }

      // filtering
      if (selectedYear != null) {
        subordinatesLockedApplications = subordinatesLockedApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        subordinatesApprovedApplications = subordinatesApprovedApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        subordinatesRejectedApplications = subordinatesRejectedApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
        subordinatesCancelledApplications = subordinatesCancelledApplications.stream().filter(a -> a.getStartDate().getYear() == selectedYear).toList();
      }

      model.addAttribute("subordinatesLockedApplications", subordinatesLockedApplications);
      model.addAttribute("filterLocked", searchLocked);
      model.addAttribute("subordinatesApprovedApplications", subordinatesApprovedApplications);
      model.addAttribute("filterApproved", searchApproved);
      model.addAttribute("subordinatesRejectedApplications", subordinatesRejectedApplications);
      model.addAttribute("filterRejected", searchRejected);
      model.addAttribute("subordinatesCancelledApplications", subordinatesCancelledApplications);
      model.addAttribute("filterCancelled", searchCancelled);
      model.addAttribute("yearSelection", List.of(2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029));
      model.addAttribute("selectedYear", selectedYear);

      return "reviewedleave";
 
    } catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/index";

    }
  }
}

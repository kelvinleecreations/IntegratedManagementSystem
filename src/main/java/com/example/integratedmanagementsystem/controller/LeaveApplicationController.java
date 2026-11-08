package com.example.integratedmanagementsystem.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.LeaveApplicationService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staff")
public class LeaveApplicationController {

  private final LeaveApplicationRepository leaveApplicationRepository;
  private final LeaveApplicationService leaveApplicationService;
  private final UserRepository userRepository;

  public LeaveApplicationController(LeaveApplicationRepository leaveApplicationRepository, LeaveApplicationService leaveApplicationService, UserRepository userRepository) {
    this.leaveApplicationRepository = leaveApplicationRepository;
    this.leaveApplicationService = leaveApplicationService;
    this.userRepository = userRepository;
  }

  @GetMapping("/applyleave")
  public String showLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // additional check if user has a manager first
    if (loggedInUser.getManager() == null) {
      return "redirect:/staff/index";
    }

    // create a new application object
    Application app = new Application();
    // set the user
    app.setUser(loggedInUser);

    model.addAttribute("leaveApplicationForm", app);
    return "applyleave";
  }

  @PostMapping("/applyleave")
  public String submitLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("leaveApplicationForm") Application form, BindingResult result, Model model, RedirectAttributes ra) {
    
    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // valid annotation check for first time or updating application, before review
    if (form.getApplicationStatus() == ApplicationStatus.APPLIED || form.getApplicationStatus() == ApplicationStatus.UPDATED) {
      if (result.hasErrors()) {
        return "reviewleave";
      }
    }

    // try-catch block with Service Layer
    try {
      String status = leaveApplicationService.saveApplication(form, loggedInUser);

      if ("READONLY_FIELDS_COMPROMISED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Leave Application Form Name or Email no longer matches your profile. Due to Security Issue, application process is stopped. You are redirected back to your home page.");
      }

      if ("NO_CHANGES".equals(status)) {
        ra.addFlashAttribute("errormsg", "No changes made to the leave application. You are redirected back to your home page.");
      }

      if ("APPLIED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Leave Application submitted successfully!");
      }

      if ("UPDATED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Leave Application updated successfully!");
      }

      if ("NOT_FOUND".equals(status)) {
        ra.addFlashAttribute("errormsg", "Leave Application not found");
      }

      if ("ALREADY_DELETED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Edit Operation Failed: Leave Application has already been deleted");
      }

      if ("ALREADY_APPROVED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Edit Operation Failed: Leave Application has already been approved by your manager");
      }

      if ("ALREADY_REJECTED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Edit Operation Failed: Leave Application has already been rejected by your manager");
      }

      if ("ALREADY_CANCELLED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Edit Operation Failed: Leave Application has already been cancelled");
      }

      if ("ALREADY_LOCKED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Edit Operation Failed: Leave Application has already been locked by your manager");
      }

      return "redirect:/staff/index";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      model.addAttribute("leaveApplicationForm", form);
      
      return "applyleave";
    }
  }

  @GetMapping("/updateleave")
  public String editLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="applicationId", required=true) Integer id, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    Optional<Application> application = leaveApplicationRepository.findByUser_UserIdAndApplicationId(loggedInUser.getUserId(), id);

    // prevent url manipulation and accessing other staff's leave applications
    if (application.isEmpty()) {
      ra.addFlashAttribute("errormsg", "Leave Application not found / You are trying to access other staff applications. You are redirected back to your home page.");
      return "redirect:/staff/index";
    }

    application.get().setUser(loggedInUser);

    model.addAttribute("leaveApplicationForm", application.get());
    return "applyleave";  
  }

  @PostMapping("/deleteleave")
  public String deleteLeave(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("leaveApplicationForm") Application form, BindingResult result, @RequestParam(name="applicationId", required=true) Integer id, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // no need for valid annotation check
  
    // try-catch block with Service Layer
    try {
      String status = leaveApplicationService.deleteApplication(form, id, loggedInUser);

      if ("NOT_FOUND".equals(status)) {
        ra.addFlashAttribute("errormsg", "Leave Application not found");
      }

      if ("NO_CHANGES_ALLOWED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Delete Operation Failed: No changes to existing application fields are allowed");
      }

      if ("DELETED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Leave Application deleted successfully!");
      }

      if ("ALREADY_DELETED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Delete Operation Failed: Leave Application has already been deleted previously");
      }

      if ("ALREADY_APPROVED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Delete Operation Failed: Leave Application has been approved by your manager");
      }

      if ("ALREADY_REJECTED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Delete Operation Failed: Leave Application has been rejected by your manager");
      }

      if ("ALREADY_LOCKED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Delete Operation Failed: Leave Application has been locked by your manager");
      }

      return "redirect:/staff/index";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      model.addAttribute("leaveApplicationForm", form);
      
      return "applyleave"; 
    }
  }

  @GetMapping("/reviewleave")
  public String reviewLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="applicationId", required=true) Integer aId, @RequestParam(name="userId", required=true) Integer uId, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    List<User> subordinatesList = new ArrayList<>();

    try {
      // very unlikely for this exception to happen, because Spring Security would have checked before it reaches the controller

      // find and load the subordinates of logged in user into the user object
      User loggedInUserWithSubordinatesLoaded = userRepository.findWithSubordinatesByUserId(loggedInUser.getUserId()).orElseThrow(() -> new RuntimeException("Logged In User not found"));

      // populate the empty subordinates list
      subordinatesList = loggedInUserWithSubordinatesLoaded.getSubordinates();

      // prevent url manipulation and accessing other managers' subordinate leave applications
      boolean isSubordinate = subordinatesList.stream().anyMatch(s -> s.getUserId().equals(uId));

      if (!isSubordinate) {
        ra.addFlashAttribute("errormsg", "This user is not your subordinate. You are redirected back to pending approval.");
        return "redirect:/staff/pendingapproval";
      }

      Optional<Application> application = leaveApplicationRepository.findByUser_UserIdAndApplicationId(uId, aId);

      if (application.isEmpty()) {
      ra.addFlashAttribute("errormsg", "Leave Application not found");
      return "redirect:/staff/pendingapproval";
      }

      model.addAttribute("subordinateLeaveApplicationForm", application.get());
      
      return "reviewleave";

    } catch (RuntimeException error) {
      ra.addFlashAttribute("errormsg", error.getMessage());
      return "redirect:/staff/pendingapproval";
    }

  }

  @PostMapping("/reviewleave")
  public String submitReviewLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("subordinateLeaveApplicationForm") Application form, BindingResult result, @RequestParam(name="action", required=true) String submit, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // valid annotation check for first time review
    if (form.getApplicationStatus() == ApplicationStatus.APPLIED || form.getApplicationStatus() == ApplicationStatus.UPDATED) {
      if (result.hasErrors()) {
        return "reviewleave";
      }
    }

    // try-catch block with Service Layer
    try {
      String status = leaveApplicationService.reviewApplication(form, submit);

      if ("ALREADY_APPROVED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Review Operation Failed: You have already approved this application");
        return "redirect:/staff/reviewedleave";
      }

      if ("ALREADY_REJECTED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Review Operation Failed: You have already rejected this application");
        return "redirect:/staff/reviewedleave";
      }

      if ("ALREADY_CANCELLED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Review Operation Failed: Your Subordinate has already cancelled this reviewed application");
        return "redirect:/staff/reviewedleave";
      }

      if ("ALREADY_LOCKED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Review Operation Failed: You have already locked this application");
        return "redirect:/staff/reviewedleave";
      }

      if ("NOT_FOUND".equals(status)) {
        ra.addFlashAttribute("errormsg", "Application not found");
      }

      if ("READONLY_FIELDS_COMPROMISED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Leave Application Form Name or Email no longer matches the original application owner. Due to Security Issue, review process is stopped. You are redirected back to your pending approvals page.");
      }

      if ("NO_CHANGES_ALLOWED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Review Operation Failed: No changes to existing application fields are allowed");
      }

      if ("APPROVED".equals(status)) {
        ra.addFlashAttribute("successmsg", "You have approved this application successfully!");
      }

      if ("REJECTED".equals(status)) {
        ra.addFlashAttribute("successmsg", "You have rejected this application successfully!");
      }

      return "redirect:/staff/pendingapproval";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      model.addAttribute("subordinateLeaveApplicationForm", form);
      
      return "reviewleave";
    }
  }

  @PostMapping("/cancelleave")
  public String cancelLeave(@AuthenticationPrincipal SecurityUserDetails principalUser, @Valid @ModelAttribute("leaveApplicationForm") Application form, BindingResult result, @RequestParam(name="applicationId", required=true) Integer id, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }

    // no need for valid annotation check

    // try-catch block with Service Layer
    try {
      String status = leaveApplicationService.cancelApplication(form, id, loggedInUser);

      if ("NOT_FOUND".equals(status)) {
        ra.addFlashAttribute("errormsg", "Leave application not found");
      }

      if ("ALREADY_CANCELLED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Cancel Operation Failed: You have already cancelled this application");
      }

      if ("ALREADY_LOCKED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Cancel Operation Failed: Leave Application has been locked by your manager");
      }

      if ("NO_CHANGES_ALLOWED".equals(status)) {
        ra.addFlashAttribute("errormsg", "Cancel Operation Failed: No changes are allowed after approval");
      }

      if ("CANCELLED".equals(status)) {
        ra.addFlashAttribute("successmsg", "Leave application cancelled successfully!");
      }

      return "redirect:/staff/index";

    } catch (SvcValidationException e) {
      // display individual field error messages beside the respective fields
      e.getErrors().forEach((fieldName, message) -> {
        result.rejectValue(fieldName, e.getErrorCode(), message);
      });

      // restore the application from the database
      Application originalApplication = leaveApplicationRepository.findByUser_UserIdAndApplicationId(loggedInUser.getUserId(), id).orElse(null);

      // check for null
      if (originalApplication == null) {
        ra.addFlashAttribute("errormsg", "Retrieval Failed: Leave Application cannot be found");

        return "redirect:/staff/index";
      }

      // add back fields, specifically fields that have higher chance of getting lost during an error, into the form by referencing to the original application data
      form.setManagerReason(originalApplication.getManagerReason());

      // differentiate between valid annotation check and service check
      model.addAttribute("svcvalidationerror", e.getMessage());
      model.addAttribute("leaveApplicationForm", form);
      
      return "applyleave";
    }
  }

  @PostMapping("/lockleave")
  public String lockReviewLeaveForm(@AuthenticationPrincipal SecurityUserDetails principalUser, Application form, @RequestParam(name="applicationId", required=true) Integer id, Model model, RedirectAttributes ra) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return "redirect:/logout/accountnotfound";
    }
 
    // no need for valid annotation check since this is the final phase of the leave application lifecycle
    // check for status of the leave application and only allow locking if the application is APPROVED and no changes are being made to the fields
    // for all other application states, redirect with error message
    String status = leaveApplicationService.lockApplication(form, id);

    if ("ALREADY_REJECTED".equals(status)) {
      ra.addFlashAttribute("errormsg", "Lock Operation Failed: You have already rejected this application");
      return "redirect:/staff/reviewedleave";
    }

    if ("ALREADY_CANCELLED".equals(status)) {
      ra.addFlashAttribute("errormsg", "Lock Operation Failed: Your Subordinate has already cancelled this reviewed application");
      return "redirect:/staff/reviewedleave";
    }

    if ("ALREADY_LOCKED".equals(status)) {
      ra.addFlashAttribute("errormsg", "Lock Operation Failed: You have already locked this application");
      return "redirect:/staff/reviewedleave";
    }

    if ("NOT_FOUND".equals(status)) {
      ra.addFlashAttribute("errormsg", "Leave application not found");
    }

    if ("NO_CHANGES_ALLOWED".equals(status)) {
      ra.addFlashAttribute("errormsg", "Lock Operation Failed: No changes to existing application fields are allowed");
    }

    if ("ALREADY_APPROVED".equals(status)) {
      ra.addFlashAttribute("successmsg", "You have locked this application successfully!");
    }

    return "redirect:/staff/index";
  }

}

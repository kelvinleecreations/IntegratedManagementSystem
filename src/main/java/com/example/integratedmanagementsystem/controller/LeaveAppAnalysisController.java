package com.example.integratedmanagementsystem.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.LeaveAppAnalysisDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.LeaveAppAnalysisService;

@RestController
@RequestMapping("/api/leave-app-data")
public class LeaveAppAnalysisController {

  private final LeaveApplicationRepository leaveApplicationRepository;
  private final LeaveAppAnalysisService leaveAppAnalysisService;
  private final UserRepository userRepository;

  public LeaveAppAnalysisController (LeaveApplicationRepository leaveApplicationRepository, LeaveAppAnalysisService leaveAppAnalysisService, UserRepository userRepository) {
    this.leaveApplicationRepository = leaveApplicationRepository;
    this.leaveAppAnalysisService = leaveAppAnalysisService;
    this.userRepository = userRepository;
  }

  @GetMapping("/analysis")
  public ResponseEntity<?> getAnalysis(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(name="applicationId", required=true) Integer aId, @RequestParam(name="userId", required=true) Integer uId) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User no longer exists in the system.");
    }

    Optional<Application> application = leaveApplicationRepository.findByUser_UserIdAndApplicationId(uId, aId);

    if (application.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Access Denied or Leave Application not found");
    }

    // get the manager id
    Integer managerId = application.get().getUser().getManager().getUserId();

    // let service layer handle
    List<LeaveAppAnalysisDTO> subordinateClashes = leaveAppAnalysisService.filterClashingSubordinatesApp(application.get(), managerId);

    return ResponseEntity.ok(subordinateClashes);
  }
}

package com.example.integratedmanagementsystem.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.ResumeReviewService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/resume")
public class ResumeReviewController {
  private final ResumeReviewService resumeReviewService;
  private final UserRepository userRepository;

  public ResumeReviewController (ResumeReviewService resumeReviewService, UserRepository userRepository) {
    this.resumeReviewService = resumeReviewService;
    this.userRepository = userRepository;
  }

  @PostMapping("/analysis")
  public ResponseEntity<?> reviewedResumes(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) String jobDescription, @RequestParam(required=false) List<MultipartFile> files) {

    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User no longer exists in the system.");
    }

    try {
      return ResponseEntity.ok(resumeReviewService.rankResume(jobDescription, files, currentUser.get().getUsername()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of("error", e.getMessage()));

    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));

    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", "AI Service Down."));
    }
  }
}

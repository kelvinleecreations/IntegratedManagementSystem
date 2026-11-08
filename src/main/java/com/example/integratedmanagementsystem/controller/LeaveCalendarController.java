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

import com.example.integratedmanagementsystem.model.CalendarDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;
import com.example.integratedmanagementsystem.security.SecurityUserDetails;
import com.example.integratedmanagementsystem.service.CalendarService;

@RestController
@RequestMapping("/api/leave-cal-data")
public class LeaveCalendarController {
  private final CalendarService calendarService;
  private final UserRepository userRepository;

  public LeaveCalendarController(CalendarService calendarService, UserRepository userRepository) {
    this.calendarService = calendarService;
    this.userRepository = userRepository;
  }

  @GetMapping("/analysis")
  public ResponseEntity<?> approvedLeaves(@AuthenticationPrincipal SecurityUserDetails principalUser, @RequestParam(required=false) String month, @RequestParam(required=false) Integer year) {
    
    // check whether user exists (for cases when user is still logged in and admin removes them from the system)
    User loggedInUser = principalUser.getUser();

    Optional<User> currentUser = userRepository.findById(loggedInUser.getUserId());
    
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User no longer exists in the system.");
    }

    List<CalendarDTO> leaves = calendarService.getApprovedLeave(currentUser.get().getUserId(), month, year);

    return ResponseEntity.ok(leaves); 
  }
}

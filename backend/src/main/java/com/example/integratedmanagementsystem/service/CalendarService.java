package com.example.integratedmanagementsystem.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.ApplicationStatus;
import com.example.integratedmanagementsystem.model.CalendarDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;
import com.example.integratedmanagementsystem.repository.UserRepository;

@Service
public class CalendarService {
  private final UserRepository userRepository;
  private final LeaveApplicationRepository leaveApplicationRepository;
  private final LeaveApplicationService leaveApplicationService;

  public CalendarService(UserRepository userRepository, LeaveApplicationRepository leaveApplicationRepository, LeaveApplicationService leaveApplicationService) {
    this.userRepository = userRepository;
    this.leaveApplicationRepository = leaveApplicationRepository;
    this.leaveApplicationService = leaveApplicationService;
  }

  public List<CalendarDTO> getApprovedLeave(Integer userId, String month, Integer year) {
    User loggedInUser = userRepository.findById(userId).orElse(null);

    if (loggedInUser == null) {
      return new ArrayList<>();
    }

    List<CalendarDTO> leaveCalendar = new ArrayList<>();

    // Get user from the database
    Optional<User> userWithSubordinates = userRepository.findWithSubordinatesByUserId(loggedInUser.getUserId());

    // If user not found
    if (userWithSubordinates.isEmpty()) {
      return new ArrayList<>();
    }

    User currentUser = userWithSubordinates.get();
    User manager = currentUser.getManager();
    List<User> subordinates = currentUser.getSubordinates();
    List<User> teamMembers = new ArrayList<>();

    // if the user is a manager, has subordinates and also has a reporting manager (directors/managers)
    if (subordinates != null && !subordinates.isEmpty() && manager != null) {
      // add subordinates
      teamMembers.addAll(subordinates);

      // retrieve other team mates reporting to the same manager as the user
      List<User> teamMates = userRepository.findByManagerUserIdAndUserIdNot(manager.getUserId(), currentUser.getUserId());
      teamMembers.addAll(teamMates);

      // include manager approved leaves
      teamMembers.add(manager);

      // include own approved leaves as well
      teamMembers.add(currentUser);
    }
    // if the user is a manager, has subordinates but no reporting manager (ceo)
    else if (subordinates != null && !subordinates.isEmpty() && manager == null) {
      // add subordinates
      teamMembers.addAll(subordinates);
    }
    // if the user is not a manager, but has a reporting manager (subordinate)
    else if ((subordinates == null || subordinates.isEmpty()) && manager != null) {
      // retrieve other team mates reporting to the same manager as the user
      List<User> teamMates = userRepository.findByManagerUserIdAndUserIdNot(manager.getUserId(), currentUser.getUserId());
      teamMembers.addAll(teamMates);

      // include manager approved leaves
      teamMembers.add(manager);

      // include own approved leaves as well
      teamMembers.add(currentUser);
    }
    // additional safeguard - if no subordinates and no reporting manager (newly created users without manager assigned)
    else {
      return new ArrayList<>();
    }
  
    // gather all applications that belongs to the list of user objects and application status 'APPROVED'
    List<ApplicationStatus> approvedStatus = List.of(ApplicationStatus.APPROVED);
    List<Application> approvedApplications = leaveApplicationRepository.findByUserInAndApplicationStatusIn(teamMembers, approvedStatus);

    for (Application a : approvedApplications) {
      LocalDate start = a.getStartDate();
      LocalDate end = a.getEndDate();

      // calculate leave duration using the calculateNumOfDays method in LeaveApplicationService
      Double totalDays = leaveApplicationService.calculateNumOfDays(a);

      // assign
      String durationStr = (totalDays == 0.5) ? "0.5 day" : totalDays + " day(s)";

      // for multiple days leave, indicate them individually on each day
      for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

        // exclude weekends
        if (!leaveApplicationService.isWorkingDay(date)) {
          continue;
        }

        // only display based on the month and year selected by the user
        if (date.getMonth().name().equalsIgnoreCase(month) && date.getYear() == year) {
          // convert to DTO format
          CalendarDTO calendarDTO = new CalendarDTO(date.getDayOfMonth(), a.getUser().getName(), a.getLeaveType(), a.getLeaveDuration(), durationStr);
          // add the DTO into the empty list, to be returned to JS
          leaveCalendar.add(calendarDTO);
        }
      }

    }
   
    return leaveCalendar;
  }
}

package com.example.integratedmanagementsystem.service;

import com.example.integratedmanagementsystem.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.integratedmanagementsystem.exception.SvcValidationException;
import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.ApplicationStatus;
import com.example.integratedmanagementsystem.model.LeaveDTO;
import com.example.integratedmanagementsystem.model.LeaveDuration;
import com.example.integratedmanagementsystem.model.LeaveType;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;

@Service
public class LeaveApplicationService {
  private final UserRepository userRepository;
  private final UserService userService;
  private final LeaveApplicationRepository leaveApplicationRepository;

  public LeaveApplicationService (LeaveApplicationRepository leaveApplicationRepository, UserService userService, UserRepository userRepository) {
    this.leaveApplicationRepository = leaveApplicationRepository;
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String saveApplication(Application form, User loggedInUser) {

    // additional safety check for originally readonly fields such as name and email against logged in user
    // there is still another check using noChangeValidator
    // if any changes made to prefilled fields, exit early
    String result = readOnlyFieldValidator(form, loggedInUser);
    if ("FORM_NAME_NULL_OR_BLANK".equals(result) || "FORM_EMAIL_NULL_OR_BLANK".equals(result) || "FORM_FIELDS_LOGGED_IN_USER_MISMATCH".equals(result)) {
      return "READONLY_FIELDS_COMPROMISED";
    }
    
    Map<String, String> appSvcValidationErrors = new HashMap<>();

    List<Application> leaveApplications = leaveApplicationRepository.findByUser_UserId(loggedInUser.getUserId());

    // Check if it is existing or a new application
    boolean isNew = (form.getApplicationId()==null);

    // set up original application variable
    Application originalApplication = null;

    // If it is an existing application,
    if (!isNew) {

      // Fetch original application
      originalApplication = leaveApplicationRepository.findById(form.getApplicationId()).orElse(null);

      if (originalApplication == null) {
        return "NOT_FOUND";
      }

      // check if the application is already deleted
      if (originalApplication.getApplicationStatus() == ApplicationStatus.DELETED) {
        return "ALREADY_DELETED";
      }

      // check if the application is already approved
      if (originalApplication.getApplicationStatus() == ApplicationStatus.APPROVED) {
        return "ALREADY_APPROVED";
      }

      // check if the application is already rejected
      if (originalApplication.getApplicationStatus() == ApplicationStatus.REJECTED) {
        return "ALREADY_REJECTED";
      }

      // check if the application is already cancelled
      if (originalApplication.getApplicationStatus() == ApplicationStatus.CANCELLED) {
        return "ALREADY_CANCELLED";
      }

      // check if the application is already locked
      if (originalApplication.getApplicationStatus() == ApplicationStatus.LOCKED) {
        return "ALREADY_LOCKED";
      }

      // check if there are any changes made
      if (noChangeValidator(originalApplication, form)) {
        return "NO_CHANGES";
      }
    }
  
    // if Start Date is after End Date
    if (form.getStartDate().isAfter(form.getEndDate())) {
      appSvcValidationErrors.put("startDate", "Start Date cannot be after End Date");
    }

    // if Start date falls on a weekend
    if (!isWorkingDay(form.getStartDate())) {
      appSvcValidationErrors.put("startDate", "Start Date cannot fall on a weekend");
    }

    // if End date falls on a weekend
    if (!isWorkingDay(form.getEndDate())) {
      appSvcValidationErrors.put("endDate", "End Date cannot fall on a weekend");
    }

    // if Leave Duration is set as half day, only allow start and end dates to be the same day
    if (form.getLeaveDuration() != LeaveDuration.FULLDAY) {
      // if both fields are filled,
      if (form.getStartDate() != null && form.getEndDate() != null) {
        // if both fields do not share the same date,
        if (!form.getStartDate().equals(form.getEndDate())) {
          appSvcValidationErrors.put("endDate", "End Date must be same as Start Date");
        }
      }
    }

    // if the leave duration exceeds budgeted annual leave
    Double numOfDays = calculateNumOfDays(form);
    String leaveType = form.getLeaveType().toString().toUpperCase();

    switch (leaveType) {
      case "ANNUAL":
        if (numOfDays > loggedInUser.getBudgetedAnnualLeaveDays()) {
        appSvcValidationErrors.put("leaveType", "You do not have sufficient AL days to apply for this leave");
        }
        break;

      case "HOSPITALISATION":
        // if the selected leave duration exceeds budgeted hospitalisation leave
        if (numOfDays > loggedInUser.getBudgetedHospitalizationLeaveDays()) {
        appSvcValidationErrors.put("leaveType", "You do not have sufficient HL days to apply for this leave");
        }
        break;

      case "CHILDCARE":    
        // if the selected leave duration exceeds budgeted childcare leave
        if (numOfDays > loggedInUser.getBudgetedChildcareLeaveDays()) {
        appSvcValidationErrors.put("leaveType", "You do not have sufficient CL days to apply for this leave");
        }
        break;        
    }

    // if the current application clashes with existing applications in terms of dates
    // if there are existing applications made by the user,
    if (leaveApplications != null) {
      boolean isClashing = leaveApplications.stream()
        // keeps applications that are either APPLIED, UPDATED, or APPROVED
        .filter(app -> app.getApplicationStatus() == ApplicationStatus.APPLIED || app.getApplicationStatus() == ApplicationStatus.UPDATED || app.getApplicationStatus() == ApplicationStatus.APPROVED)
        // keeps applications that do not share the same id as the current one
        .filter(app -> !app.getApplicationId().equals(form.getApplicationId()))
        // check if the dates overlap with the new form 
        .anyMatch(app -> {
          // clash as long as start date of current is not after the end date of existing AND end date of current is not before the start date of existing
          boolean overlap = !form.getStartDate().isAfter(app.getEndDate()) && !form.getEndDate().isBefore(app.getStartDate());

          // if no overlap,
          if (!overlap) {
            return false;
          }

          // check for clash between 2 half day leave (AM/PM)
          // if current and existing are both half days,
          if (form.getLeaveDuration() != LeaveDuration.FULLDAY && app.getLeaveDuration() != LeaveDuration.FULLDAY) {
            // if current and existing do not share the same half day slot
            if (form.getLeaveDuration() != app.getLeaveDuration()) {
              // no clash
              return false;
            }
          }
          
          return true;
        });

        if (isClashing) {
          appSvcValidationErrors.put("startDate", "Clash with existing application(s) found");
          appSvcValidationErrors.put("endDate", "Clash with existing application(s) found");

        }
    }

    // compile all the validation errors into an exception
    if (!appSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(appSvcValidationErrors, "allappSvcValidationErrors", "From Application Service Validator:");
    }

    // process refund only after the form passes all validation
    if (!isNew) {
      // Refund the budgeted AL / CL / HL (based on what was applied) back to user first
      refundBudgetedLeaveDays(originalApplication, loggedInUser, appSvcValidationErrors);
    }

    // subtract budgeted leave statistics before initial application or updating application
    switch (leaveType) {
      case "ANNUAL":
        loggedInUser.setBudgetedAnnualLeaveDays(loggedInUser.getBudgetedAnnualLeaveDays() - numOfDays);
        break;

      case "HOSPITALISATION":
        loggedInUser.setBudgetedHospitalizationLeaveDays(loggedInUser.getBudgetedHospitalizationLeaveDays() - numOfDays);
        break;

      case "CHILDCARE":
        loggedInUser.setBudgetedChildcareLeaveDays(loggedInUser.getBudgetedChildcareLeaveDays() - numOfDays);
        break;
    }
    
    userRepository.save(loggedInUser);

    // assign statuses accordingly
    // for existing applications,
    if (!isNew) {
      form.setApplicationStatus(ApplicationStatus.UPDATED);
      leaveApplicationRepository.save(form);
      return "UPDATED";
    } else {
    // for new applications,
    form.setApplicationStatus(ApplicationStatus.APPLIED);
    leaveApplicationRepository.save(form);
    return "APPLIED";
    }
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String deleteApplication(Application form, Integer id, User loggedInUser) {
    Map<String, String> appSvcValidationErrors = new HashMap<>();

    // get the actual application
    Application actualApplication = leaveApplicationRepository.findByUser_UserIdAndApplicationId(loggedInUser.getUserId(), id).orElse(null);

    if (actualApplication == null) {
      return "NOT_FOUND";
    }

    // ensure no changes are made to other fields before approving or rejecting, aside from manager reason
    if (!noChangeValidator(actualApplication, form)) {
      return "NO_CHANGES_ALLOWED";
    }

    // if the application has been approved,
    if (actualApplication.getApplicationStatus() == ApplicationStatus.APPROVED) {
      return "ALREADY_APPROVED";
    }

    // if the application has been rejected,
    if (actualApplication.getApplicationStatus() == ApplicationStatus.REJECTED) {
      return "ALREADY_REJECTED";
    }

    // if the application has been locked,
    if (actualApplication.getApplicationStatus() == ApplicationStatus.LOCKED) {
      return "ALREADY_LOCKED";
    }

    // if the application has already been deleted,
    if (actualApplication.getApplicationStatus() == ApplicationStatus.DELETED) {
      return "ALREADY_DELETED";
    }

    // refund back the leave
    refundBudgetedLeaveDays(actualApplication, loggedInUser, appSvcValidationErrors);

    // compile all the validation errors into an exception
    if (!appSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(appSvcValidationErrors, "allappSvcValidationErrors", "From Application Service Validator:");
    }

    form.setApplicationStatus(ApplicationStatus.DELETED);
    leaveApplicationRepository.save(form);
    userRepository.save(loggedInUser);
    return "DELETED";
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String reviewApplication(Application form, @RequestParam(name="action", required=true) String submit) {

    // get the actual application
    Application actualApplication = leaveApplicationRepository.findById(form.getApplicationId()).orElse(null);

    if (actualApplication == null) {
      return "NOT_FOUND";
    }

    // additional safety check for originally readonly fields such as name and email against logged in user's subordinate
    // there is still another check using noChangeValidator
    // if any changes made to prefilled fields, exit early
    String result = readOnlyFieldValidator(form, actualApplication.getUser());
    if ("FORM_NAME_NULL_OR_BLANK".equals(result) || "FORM_EMAIL_NULL_OR_BLANK".equals(result) || "FORM_FIELDS_LOGGED_IN_USER_MISMATCH".equals(result)) {
      return "READONLY_FIELDS_COMPROMISED";
    }

    Map<String, String> appSvcValidationErrors = new HashMap<>();

    // check if the application is already approved
    if (actualApplication.getApplicationStatus() == ApplicationStatus.APPROVED) {
      return "ALREADY_APPROVED";
    }

    // check if the application is already rejected
    if (actualApplication.getApplicationStatus() == ApplicationStatus.REJECTED) {
      return "ALREADY_REJECTED";
    }

    // check if the application is already cancelled
    if (actualApplication.getApplicationStatus() == ApplicationStatus.CANCELLED) {
      return "ALREADY_CANCELLED";
    }

    // check if the application is already locked
    if (actualApplication.getApplicationStatus() == ApplicationStatus.LOCKED) {
      return "ALREADY_LOCKED";
    }

    // ensure no changes are made to other fields before approving or rejecting, aside from manager reason
    if (!noChangeValidator(actualApplication, form)) {
      return "NO_CHANGES_ALLOWED";
    }
    
    // if manager reason field is not filled
    if (form.getManagerReason() == null || form.getManagerReason().trim().isEmpty()) {
      appSvcValidationErrors.put("managerReason", "Manager reason is required before approving / rejecting");
    }

    // compile all the validation errors into an exception
    if (!appSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(appSvcValidationErrors, "allappSvcValidationErrors", "From Application Service Validator:");
    }

    if ("approve".equals(submit)) {
      form.setApplicationStatus(ApplicationStatus.APPROVED);
      leaveApplicationRepository.save(form);
      return "APPROVED";
    }

    // if application is rejected,
    else {
      // refund back the leave to the user tagged to the application
      refundBudgetedLeaveDays(actualApplication, actualApplication.getUser(), appSvcValidationErrors);
      form.setApplicationStatus(ApplicationStatus.REJECTED);
      leaveApplicationRepository.save(form);
      userRepository.save(actualApplication.getUser());
      return "REJECTED";
    }
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String cancelApplication (Application form, Integer id, User loggedInUser) {
    Map<String, String> appSvcValidationErrors = new HashMap<>();

    // get the actual application
    Application actualApplication = leaveApplicationRepository.findByUser_UserIdAndApplicationId(loggedInUser.getUserId(), id).orElse(null);

    if (actualApplication == null) {
      return "NOT_FOUND";
    }

    // check if the application is already cancelled
    if (actualApplication.getApplicationStatus() == ApplicationStatus.CANCELLED) {
      return "ALREADY_CANCELLED";
    }

    // check if the application is already locked
    if (actualApplication.getApplicationStatus() == ApplicationStatus.LOCKED) {
      return "ALREADY_LOCKED";
    }

    // ensure no changes are made to other fields before cancellation, aside from user cancellation reason
    if (!noChangeValidator(actualApplication, form)) {
      return "NO_CHANGES_ALLOWED";
    }

    // if user cancellation reason field is not filled
    if (form.getUserCancellationReason() == null || form.getUserCancellationReason().trim().isEmpty()) {
      appSvcValidationErrors.put("userCancellationReason", "Your reason for cancellation is required");
    }

    // compile all the validation errors into an exception
    if (!appSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(appSvcValidationErrors, "allappSvcValidationErrors", "From Application Service Validator:");
    } 

    // refund back the leave
    refundBudgetedLeaveDays(actualApplication, loggedInUser, appSvcValidationErrors);

    // manually set back the manager reason before saving changes, because this field is not within the apply leave form
    form.setManagerReason(actualApplication.getManagerReason());
    form.setApplicationStatus(ApplicationStatus.CANCELLED);
    leaveApplicationRepository.save(form);
    userRepository.save(loggedInUser);
    return "CANCELLED";

  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String lockApplication (Application form, Integer id) {

    // get the actual application
    Application actualApplication = leaveApplicationRepository.findById(form.getApplicationId()).orElse(null);

    if (actualApplication == null) {
      return "NOT_FOUND";
    }

    // check if the application is already cancelled
    if (actualApplication.getApplicationStatus() == ApplicationStatus.CANCELLED) {
      return "ALREADY_CANCELLED";
    }

    // ensure no changes are made to other fields before cancellation, together with manager reason
    if (!noChangeValidator(actualApplication, form) || !Objects.equals(form.getManagerReason(), actualApplication.getManagerReason())) {
      return "NO_CHANGES_ALLOWED";
    }

    // check if the application is already locked
    if (actualApplication.getApplicationStatus() == ApplicationStatus.LOCKED) {
      return "ALREADY_LOCKED";
    }

    // check if the application is already rejected
    if (actualApplication.getApplicationStatus() == ApplicationStatus.REJECTED) {
      return "ALREADY_REJECTED";
    }

    form.setApplicationStatus(ApplicationStatus.LOCKED);
    leaveApplicationRepository.save(form);
    return "ALREADY_APPROVED";
  }

  // method for refunding leave days
  private void refundBudgetedLeaveDays(Application a, User u, Map<String, String> e) {
    Double retrieveNumOfDays = calculateNumOfDays(a);

    // obtain leave type from the original application
    String leaveType = a.getLeaveType().toString().toUpperCase();

    switch (leaveType) {
      case "ANNUAL":
        u.setBudgetedAnnualLeaveDays(u.getBudgetedAnnualLeaveDays() + retrieveNumOfDays);
        break;

      case "HOSPITALISATION":
        u.setBudgetedHospitalizationLeaveDays(u.getBudgetedHospitalizationLeaveDays() + retrieveNumOfDays);
        break;

      case "CHILDCARE":
        u.setBudgetedChildcareLeaveDays(u.getBudgetedChildcareLeaveDays() + retrieveNumOfDays);
        break;

      default:
        e.put("leaveType", "Invalid Leave Type, unable to refund");
    } 
  }
  
  // method for calculating leave days
  public Double calculateNumOfDays(Application a) {
    Double numOfDays = 0.0;
    if (a.getLeaveDuration() != LeaveDuration.FULLDAY) {
      return numOfDays = 0.5;
    }
    else {
      LocalDate start = a.getStartDate();
      LocalDate end = a.getEndDate();

      while (!start.isAfter(end)) {
        if (isWorkingDay(start)) {
          numOfDays++;
        }
        start = start.plusDays(1);
      }
    }
    return numOfDays;
  }

  // Method Overloading to be used for Dynamic Year Filtering Function in Leave Application Service
  private Double calculateNumOfDays(Application a, LocalDate startDateOfSelectedYear, LocalDate endDateOfSelectedYear) {
    Double numOfDays = 0.0;
    // for half-days
    if (a.getLeaveDuration() != LeaveDuration.FULLDAY) {
      // only deduct if the application belongs to the selected year
      if (a.getStartDate().getYear() == startDateOfSelectedYear.getYear()) {
        return numOfDays = 0.5;
      }
      return numOfDays = 0.5;
    }

    // for full days,
    // differentiate between non-crossover and crossover applications
    // allocate number of days into their respective years (for crossover)
    // if the application start date falls before 1st January, re-assign as 1st January, else no change
    LocalDate assignStart = a.getStartDate().isBefore(startDateOfSelectedYear) ? startDateOfSelectedYear : a.getStartDate();

    // if the application end date falls after 31st Decemeber, re-assign as 31st December, else no change
    LocalDate assignEnd = a.getEndDate().isAfter(endDateOfSelectedYear) ? endDateOfSelectedYear : a.getEndDate();

    while (!assignStart.isAfter(assignEnd)) {
      if (isWorkingDay(assignStart)) {
        numOfDays++;
      }
      assignStart = assignStart.plusDays(1);
    }
    return numOfDays;
  }

  // method for checking whether the date chosen is a working day
  public boolean isWorkingDay(LocalDate d) {
    return !(d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY);
  }

  // method for checking whether there are any changes made
  private boolean noChangeValidator(Application actual, Application form) {
    boolean noChanges = Objects.equals(actual.getUser().getName(), form.getUser().getName()) && Objects.equals(actual.getUser().getEmail(), form.getUser().getEmail()) && Objects.equals(actual.getLeaveType(), form.getLeaveType()) && Objects.equals(actual.getLeaveDuration(), form.getLeaveDuration()) && Objects.equals(actual.getStartDate(), form.getStartDate()) && Objects.equals(actual.getEndDate(), form.getEndDate()) && Objects.equals(actual.getUserReason(), form.getUserReason());
   
    if (noChanges) {
      return true;
    }

    return false;
  }

  // method for dynamic year filtering leave statistics
  public LeaveDTO getLeaveFiguresBasedOnSelectedYear(Integer userId, Integer selectedYear) {
    User loggedInUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    // establish actual AL and CL dynamically, using calculation methods from user service
    // HL default as 60.0
    Double entitledActualAL = userService.calculateAnnualLeave(loggedInUser, selectedYear);

    Double entitledActualHL = 60.0;

    Double entitledActualCL = userService.adjustChildcareLeave(loggedInUser, loggedInUser.getChildrenCount());

    // for ceo and admin
    if (entitledActualAL == null || entitledActualCL == null) {
      // set the dto fields to null and exit early
      return new LeaveDTO (selectedYear, null, null, null, null, null, null);
    }

    // establish yearly scope
    LocalDate startDateOfSelectedYear = LocalDate.of(selectedYear, 1, 1);
    LocalDate endDateOfSelectedYear = LocalDate.of(selectedYear, 12, 31);

    // get applications that are either 'APPLIED' or 'UPDATED' or 'APPROVED' that have start and end dates in between year as well as crossing over between years
    List<Application> applications = leaveApplicationRepository.findAnyDeductingCrossYearOrWithinYearApplications(userId, startDateOfSelectedYear, endDateOfSelectedYear);

    // initialise counters to deduct budgeted figures
    Double annualLeaveCounter = 0.0;
    Double hospitalisationLeaveCounter = 0.0;
    Double childcareLeaveCounter = 0.0;

    // for each application in the user's application list
    for (Application a : applications) {
      
      // unlikely scenario: if the application happens to have null value for start date and end date, ignore this application and move on to the next immediately
      if (a.getStartDate() == null || a.getEndDate() == null) {
        continue;
      }

      // call the method to calculate half days and full days (differentiate between non-crossover and crossover applications)
      Double appDaysWithinTheSelectedYear = calculateNumOfDays(a, startDateOfSelectedYear, endDateOfSelectedYear);

      // increment the respective counters with the application days
      if (a.getLeaveType() == LeaveType.ANNUAL) {
        annualLeaveCounter += appDaysWithinTheSelectedYear;
      }
      if (a.getLeaveType() == LeaveType.HOSPITALISATION) {
        hospitalisationLeaveCounter += appDaysWithinTheSelectedYear;
      }
      if (a.getLeaveType() == LeaveType.CHILDCARE) {
        childcareLeaveCounter += appDaysWithinTheSelectedYear;
      }
    }

    // calculate budgeted leave
    Double budgetedAL = entitledActualAL - annualLeaveCounter;
    Double budgetedHL = entitledActualHL - hospitalisationLeaveCounter;
    Double budgetedCL = entitledActualCL - childcareLeaveCounter;

    return new LeaveDTO(selectedYear, entitledActualAL, budgetedAL, entitledActualHL, budgetedHL, entitledActualCL, budgetedCL);
  }

  // method for validating originally readonly fields
  public String readOnlyFieldValidator(Application a, User u) {
    String name = a.getUser().getName();
    String email = a.getUser().getEmail();

    if (name == null || name.trim().isEmpty()) {
      return "FORM_NAME_NULL_OR_BLANK";
    }

    if (email == null || email.trim().isEmpty()) {
      return "FORM_EMAIL_NULL_OR_BLANK";
    }

    // if not null, check against logged in user's profile
    if (!Objects.equals(u.getName(), name) || !Objects.equals(u.getEmail(), email)) {
      return "FORM_FIELDS_LOGGED_IN_USER_MISMATCH";
    }
    
    return "NO_ERROR";
  }


}

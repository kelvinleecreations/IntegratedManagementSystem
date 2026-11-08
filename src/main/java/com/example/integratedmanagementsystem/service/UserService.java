package com.example.integratedmanagementsystem.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.integratedmanagementsystem.exception.SvcValidationException;
import com.example.integratedmanagementsystem.model.MaritalStatus;
import com.example.integratedmanagementsystem.model.ProfileDTO;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.model.Role;
import com.example.integratedmanagementsystem.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService (UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String saveUserProfile(ProfileDTO form, User loggedInUser) {

    // safety check for originally readonly fields such as name, username and email against logged in user
    // if any changes made to prefilled fields, exit early
    String result = readOnlyProfileFieldValidator(form, loggedInUser);
    if ("PROFILE_NAME_NULL_OR_BLANK".equals(result) || "PROFILE_USERNAME_NULL_OR_BLANK".equals(result) || "PROFILE_EMAIL_NULL_OR_BLANK".equals(result) || "PROFILE_FIELDS_LOGGED_IN_USER_MISMATCH".equals(result)) {
      return "READONLY_FIELDS_COMPROMISED";
    }

    Map<String, String> userSvcValidationErrors = new HashMap<>();

    boolean hasCurrentPassword = form.getCurrentPassword() != null && !form.getCurrentPassword().trim().isEmpty();
    boolean hasNewPassword = form.getNewPassword() != null && !form.getNewPassword().trim().isEmpty();
    
    // When both fields are not filled
    if (!hasCurrentPassword && !hasNewPassword) {
      return "NO_CHANGES";
    }

    // Apply NotBlank Validation only when either field is filled
    if (hasCurrentPassword || hasNewPassword) {
      // If current password field is not filled while new password is filled,
      if (!hasCurrentPassword) {
        userSvcValidationErrors.put("currentPassword", "Please enter your current password");
      }
      // If current password field is filled while new password is not filled,
      if (!hasNewPassword) {
        userSvcValidationErrors.put("newPassword", "Please enter your new password");
      }
      // If current password is filled and it does not match with the existing password from the database,
      if (hasCurrentPassword && !passwordEncoder.matches(form.getCurrentPassword(), loggedInUser.getPassword())) {
        userSvcValidationErrors.put("currentPassword", "The current password entered does not match with your existing one");
      }
      // If password is filled but does not fulfil the password change constraints,
      if (hasNewPassword && (form.getNewPassword().trim().length() < 5 || form.getNewPassword().trim().length() > 20)) {
        userSvcValidationErrors.put("newPassword", "New Password should only be between 5 to 20 characters, inclusive");
      }
    }

    // compile all the validation errors into an exception
    if(!userSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(userSvcValidationErrors, "allUserSvcValidationErrors", "From User Service Validator:");
    }

    // Update Password
    // Encrypt password for Spring Security during login
    if (hasNewPassword){
      String encryptedPassword = passwordEncoder.encode(form.getNewPassword());
      loggedInUser.setPassword(encryptedPassword);
    }

    userRepository.save(loggedInUser);

    return "UPDATED";
  }

  @Transactional (
    propagation = Propagation.REQUIRED,
    isolation = Isolation.SERIALIZABLE,
    rollbackFor = Exception.class,
    timeout = 30
  )
  public String saveUser(User form) {

    Map<String, String> userSvcValidationErrors = new HashMap<>();

    // during registration of new users,
    if (form.getUserId() == null) {
      // check if the username already exists
      if (userRepository.existsByUsername(form.getUsername())){
        userSvcValidationErrors.put("username", "Username already exists");
      }
    }

    // during editing of existing users
    else {
      // 'Not' is to exclude ownself from the check of duplicate username, allowing editing and updating to pass through

      // Example when the admin is trying to edit Cass's details, it will pass even though the username already exist, just because it is hers to begin with

      // If the username is found to exist within a user's details when editing another user details, reject
      if (userRepository.existsByUsernameAndUserIdNot(form.getUsername(), form.getUserId())){
         userSvcValidationErrors.put("username", "Username already exists");
      }
    }

    // ensure that date of join, marital status and children count fields are filled for users except ceo and admin roles
    // dynamically changes AL, CL and set default HL
    if (form.getRole() != Role.ROLE_ADMIN && form.getRole() != Role.ROLE_CEO) {

      // if date of join is missing,
      if (form.getDateOfJoin() == null) {
        userSvcValidationErrors.put("dateOfJoin", "Date of Joining required");
      }

      // if marital status is missing,
      if (form.getMaritalStatus() == null) {
        userSvcValidationErrors.put("maritalStatus", "Marital Status required");
      }

      // if marital status is set as MARRIED or DIVORCED,
      if (form.getMaritalStatus() == MaritalStatus.MARRIED || form.getMaritalStatus() == MaritalStatus.DIVORCED) {
        // reject if children count is not filled,
        if (form.getChildrenCount() == null) {
          userSvcValidationErrors.put("childrenCount", "Children Count required");
        }
      }

      // if martial status is set as SINGLE, just set as 0
      if (form.getMaritalStatus() == MaritalStatus.SINGLE) {
        form.setChildrenCount(0);
      }

      // set AL based on the number of years the user had been with the company, only when date of join is filled
      // if there is no date of join key found within the hashmap,
      if (!userSvcValidationErrors.containsKey("dateOfJoin")){
        form.setActualAnnualLeaveDays(calculateAnnualLeave(form));
      }

      // set static HL
      form.setActualHospitalizationLeaveDays(60.0);

      // adjust the entitlement field for CL based on admin's selection of children count for users except ceo and admin roles
      adjustChildcareLeave(form);
    }

    // for admins and ceo, set those as null, together with the dependent leave type field
    else {
      form.setMaritalStatus(null);
      form.setChildrenCount(null);
      form.setActualChildcareLeaveDays(null);

      // set the other 2 independent leave types as null too
      form.setActualAnnualLeaveDays(null);
      form.setActualHospitalizationLeaveDays(null);

      // set date of join as null too
      form.setDateOfJoin(null);
    }

    // compile all the validation errors into an exception
    if(!userSvcValidationErrors.isEmpty()) {
      throw new SvcValidationException(userSvcValidationErrors, "allUserSvcValidationErrors", "From User Service Validator:");
    }

    // check if it is a new user or an existing user
    boolean isNewUser = (form.getUserId()==null);

    // for existing user, extract original pw
    if (!isNewUser) {
      User originalUserDetails = userRepository.findById(form.getUserId()).orElse(null);

      boolean noChanges = Objects.equals(originalUserDetails.getName(), form.getName()) && Objects.equals(originalUserDetails.getUsername(), form.getUsername()) && Objects.equals(originalUserDetails.getEmail(), form.getEmail()) && Objects.equals(originalUserDetails.getRole(), form.getRole()) && Objects.equals(originalUserDetails.getManager(), form.getManager()) && Objects.equals(originalUserDetails.getMaritalStatus(), form.getMaritalStatus()) && Objects.equals(originalUserDetails.getChildrenCount(), form.getChildrenCount()) && Objects.equals(originalUserDetails.getActualAnnualLeaveDays(), form.getActualAnnualLeaveDays()) && Objects.equals(originalUserDetails.getActualHospitalizationLeaveDays(), form.getActualHospitalizationLeaveDays()) && Objects.equals(originalUserDetails.getActualChildcareLeaveDays(), form.getActualChildcareLeaveDays()) && Objects.equals(originalUserDetails.getDateOfJoin(), form.getDateOfJoin());

      if (noChanges) {
        return "NO_CHANGES";
      }

      // retain the pw details within the form
      form.setPassword(originalUserDetails.getPassword());

      // set all budgeted entitlements (AL, HL, CL) to be equal to the actual entitlements
      syncBudgetedWithActualLeaveStatistic(form);
    }
    // for new users, give them default pw to login
    else {
      form.setPassword("DefaultPassword");

      // Encrypt password for Spring Security during login
      String encryptedPassword = passwordEncoder.encode(form.getPassword());
      form.setPassword(encryptedPassword);

      // set all budgeted entitlements (AL, HL, CL) to be equal to the actual entitlements for new users as well
      syncBudgetedWithActualLeaveStatistic(form);
    }

    User savedUser = userRepository.save(form);
    form.setUserId(savedUser.getUserId());

    return isNewUser ? "CREATED" : "UPDATED"; 
  }

  // Method to sync Budgeted Leave Statistics with Actual Leave Statistics
  private void syncBudgetedWithActualLeaveStatistic(User form) {
    form.setBudgetedAnnualLeaveDays(form.getActualAnnualLeaveDays());
    form.setBudgetedHospitalizationLeaveDays(form.getActualHospitalizationLeaveDays());
    form.setBudgetedChildcareLeaveDays(form.getActualChildcareLeaveDays());
  }

  // Method to calculate Annual Leave based on Date of Join against current year
  public Double calculateAnnualLeave(User form) {
    // calculate years of service - difference between current year and year of join

    // for ceo and admin
    if (form.getDateOfJoin() == null) {
      return null;
    }

    Double yearsOfService = (Double) (double) ChronoUnit.YEARS.between(form.getDateOfJoin(), LocalDate.now());
    return fetchAnnualLeaveTier(yearsOfService);
  }

  // Method Overloading to be used for Leave Application Service
  public Double calculateAnnualLeave(User user, Integer selectedYear) {

    // for ceo and admin
    if (user.getDateOfJoin() == null) {
      return null;
    }

    LocalDate startOfYearDate = LocalDate.of(selectedYear,1,1);
    Double yearsOfService = (Double) (double) ChronoUnit.YEARS.between(user.getDateOfJoin(), startOfYearDate);
    return fetchAnnualLeaveTier(yearsOfService);
  }

  // Formula
  private Double fetchAnnualLeaveTier(Double yearsOfService) {
    // assume AL starts at 8 and increment by 1 day for each year, capped at 14 at year 6 onwards
    if (yearsOfService < 1) {
      return 8.0;
    } else if (yearsOfService >= 1 && yearsOfService < 2) {
      return 9.0;
    } else if (yearsOfService >= 2 && yearsOfService < 3) {
      return 10.0;
    } else if (yearsOfService >= 3 && yearsOfService < 4) {
      return 11.0;
    } else if (yearsOfService >= 4 && yearsOfService < 5) {
      return 12.0;
    } else if (yearsOfService >= 5 && yearsOfService < 6) {
      return 13.0;
    } else  {
      return 14.0;
    }

  }

  // Method for adjusting Childcare Leave
  public void adjustChildcareLeave(User user) {

    if (user.getChildrenCount() != null && user.getChildrenCount() != 0) {
        if (user.getChildrenCount() == 1) {
          user.setActualChildcareLeaveDays(8.0);
        }
        if (user.getChildrenCount() == 2) {
          user.setActualChildcareLeaveDays(10.0);
        }
        if (user.getChildrenCount() >= 3) {
          user.setActualChildcareLeaveDays(12.0);
        }
      } 
      else {
        user.setActualChildcareLeaveDays(0.0);
      }
  }

  // Method Overloading to be used for Leave Application Service
  public Double adjustChildcareLeave(User user, Integer childrenCount) {

    // for ceo and admin
    if (user.getDateOfJoin() == null) {
      return null;
    }

    if (childrenCount != null && childrenCount != 0) {
      if (childrenCount == 1) {
        return 8.0;
      }
      if (childrenCount == 2) {
        return 10.0;
      }
      if (childrenCount >= 3) {
        return 12.0;
      }
    }
    return 0.0;
  }

  // method for validating originally readonly fields
  public String readOnlyProfileFieldValidator(ProfileDTO p, User u) {
    String name = p.getName();
    String username = p.getUsername();
    String email = p.getEmail();

    if (name == null || name.trim().isEmpty()) {
      return "PROFILE_NAME_NULL_OR_BLANK";
    }

    if (username == null || username.trim().isEmpty()) {
      return "PROFILE_USERNAME_NULL_OR_BLANK";
    }

    if (email == null || email.trim().isEmpty()) {
      return "PROFILE_EMAIL_NULL_OR_BLANK";
    }

    // if not null, check against logged in user's profile
    if (!Objects.equals(u.getName(), name) || !Objects.equals(u.getUsername(), username) || !Objects.equals(u.getEmail(), email)) {
      return "PROFILE_FIELDS_LOGGED_IN_USER_MISMATCH";
    }
    
    return "NO_ERROR";
  }


}

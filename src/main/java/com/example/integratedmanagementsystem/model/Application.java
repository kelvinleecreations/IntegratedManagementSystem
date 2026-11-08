package com.example.integratedmanagementsystem.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity

@Table(name="leaveapplications")
public class Application {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Integer applicationId;

  @Enumerated(EnumType.STRING)
  @NotNull(message="Leave Type is required")
  private LeaveType leaveType;

  @Enumerated(EnumType.STRING)
  @NotNull(message="Leave duration is required")
  private LeaveDuration leaveDuration;

  @NotNull(message="Start Date is required")
  @DateTimeFormat(pattern="yyyy-MM-dd")
  private LocalDate startDate;

  @NotNull(message="End Date is required")
  @DateTimeFormat(pattern="yyyy-MM-dd")
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private ApplicationStatus applicationStatus;

  @Column(nullable=false)
  @Size(max=1000, message="Reason provided should not exceed 1000 characters")
  @NotBlank(message="Please provide a reason for your leave application")
  private String userReason;

  private String managerReason;

  private String userCancellationReason;

  // Each application can only be linked to 1 user (Owner side)
  @ManyToOne(fetch=FetchType.LAZY)
  private User user;

  public Application() {
  }

  public Application(LeaveType leaveType, LeaveDuration leaveDuration, LocalDate startDate, LocalDate endDate, ApplicationStatus applicationStatus, String userReason, String managerReason, String userCancellationReason) {
    this.leaveType = leaveType;
    this.leaveDuration = leaveDuration;
    this.startDate = startDate;
    this.endDate = endDate;
    this.applicationStatus = applicationStatus;
    this.userReason = userReason;
    this.managerReason = managerReason;
    this.userCancellationReason = userCancellationReason;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public LeaveType getLeaveType() {
    return leaveType;
  }

  public void setLeaveType(LeaveType leaveType) {
    this.leaveType = leaveType;
  }

  public LeaveDuration getLeaveDuration() {
    return leaveDuration;
  }

  public void setLeaveDuration(LeaveDuration leaveDuration) {
    this.leaveDuration = leaveDuration;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public ApplicationStatus getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(ApplicationStatus applicationStatus) {
    this.applicationStatus = applicationStatus;
  }

  public String getUserReason() {
    return userReason;
  }

  public void setUserReason(String userReason) {
    this.userReason = userReason;
  }

  public String getManagerReason() {
    return managerReason;
  }

  public void setManagerReason(String managerReason) {
    this.managerReason = managerReason;
  }

  public String getUserCancellationReason() {
    return userCancellationReason;
  }

  public void setUserCancellationReason(String userCancellationReason) {
    this.userCancellationReason = userCancellationReason;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Application other = (Application) obj;
    if (applicationId == null) {
      if (other.applicationId != null)
        return false;
    } else if (!applicationId.equals(other.applicationId))
      return false;
    return true;
  }
}

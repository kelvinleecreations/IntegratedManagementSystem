package com.example.integratedmanagementsystem.model;

public class LeaveAppAnalysisDTO {
  private String name;
  private String startDate;
  private String endDate;
  private String leaveDuration;
  private String applicationStatus;

  public LeaveAppAnalysisDTO () {
  }

  public LeaveAppAnalysisDTO (String name, String startDate, String endDate, String leaveDuration, String applicationStatus) {
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.leaveDuration = leaveDuration;
    this.applicationStatus = applicationStatus;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getLeaveDuration() {
    return leaveDuration;
  }

  public void setLeaveDuration(String leaveDuration) {
    this.leaveDuration = leaveDuration;
  }

  public String getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(String applicationStatus) {
    this.applicationStatus = applicationStatus;
  }

  

}

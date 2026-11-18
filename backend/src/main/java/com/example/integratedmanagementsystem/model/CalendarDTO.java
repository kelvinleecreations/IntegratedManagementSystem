package com.example.integratedmanagementsystem.model;

public class CalendarDTO {
  private Integer day;
  private String name;
  private LeaveType leaveType;
  private LeaveDuration leaveDuration;
  private String durationStr;

  public CalendarDTO(Integer day, String name, LeaveType leaveType, LeaveDuration leaveDuration, String durationStr) {
    this.day = day;
    this.name = name;
    this.leaveType = leaveType;
    this.leaveDuration = leaveDuration;
    this.durationStr = durationStr;
  }

  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getDurationStr() {
    return durationStr;
  }

  public void setDurationStr(String durationStr) {
    this.durationStr = durationStr;
  }

  

}
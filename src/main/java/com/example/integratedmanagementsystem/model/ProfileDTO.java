package com.example.integratedmanagementsystem.model;

public class ProfileDTO {
  private Integer userId;
  private String name;
  private String username;
  private String email;
  private String currentPassword;
  private String newPassword;
  
  public Integer getUserId() {
    return userId;
  }
  public void setUserId(Integer userId) {
    this.userId = userId;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getCurrentPassword() {
    return currentPassword;
  }
  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }
  public String getNewPassword() {
    return newPassword;
  }
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }  
}

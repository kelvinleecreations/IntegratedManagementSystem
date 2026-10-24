package com.example.integratedmanagementsystem.model;

public enum Role {
  ROLE_ADMIN("Administrator"),
  ROLE_CEO("Chief Executive Officer"),
  ROLE_HR_DIRECTOR("HR Director"),
  ROLE_FINANCE_DIRECTOR("Finance Director"),
  ROLE_HR_MANAGER_R("HR Recruiter Manager"),
  ROLE_HR_MANAGER_I("HR Internal Manager"),
  ROLE_HR_RECRUITER("HR Recruiter"),
  ROLE_HR_INTERNAL("HR Internal"),
  ROLE_FINANCE_MANAGER("Finance Manager"),
  ROLE_FINANCE_EMPLOYEE("Finance Employee");

  private final String roleLabel;

  private Role(String roleLabel) {
    this.roleLabel = roleLabel;
  }

  public String getRoleLabel() {
    return roleLabel;
  }
}

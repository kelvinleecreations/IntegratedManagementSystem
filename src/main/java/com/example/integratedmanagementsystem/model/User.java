package com.example.integratedmanagementsystem.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity

@Table(name="users")
public class User {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Integer userId;

  @NotBlank(message="Name must not be empty")
  private String name;

  @Column(unique=true, nullable=false)
  @NotBlank(message="Username must not be empty")
  private String username;

  @NotBlank(message="Email must not be empty")
  private String email;

  @Column(nullable=false, length=100)
  private String password;

  @Enumerated(EnumType.STRING)
  @NotNull(message="Role must not be empty")
  private Role role;

  // One manager can have 1 to many subordinates (Inverse side)
  @OneToMany(mappedBy="manager", fetch=FetchType.LAZY)
  private List<User> subordinates = new ArrayList<>();

  // One subordinate can have 0 to 1 manager (Owner side)
  @ManyToOne
  private User manager;

  @PositiveOrZero(message="Days cannot be negative")
  private Double actualAnnualLeaveDays;
  @PositiveOrZero(message="Days cannot be negative")
  private Double budgetedAnnualLeaveDays;

  @PositiveOrZero(message="Days cannot be negative")
  private Double actualHospitalizationLeaveDays;
  @PositiveOrZero(message="Days cannot be negative")
  private Double budgetedHospitalizationLeaveDays;

  @Enumerated(EnumType.STRING)
  private MaritalStatus maritalStatus;

  @Column(nullable=true)
  private Integer childrenCount;

  @PositiveOrZero(message="Days cannot be negative")
  private Double actualChildcareLeaveDays;
  @PositiveOrZero(message="Days cannot be negative")
  private Double budgetedChildcareLeaveDays;

  @DateTimeFormat(pattern="yyyy-MM-dd")
  private LocalDate dateOfJoin;

  // One user can make 0 to many applications (Inverse side)
  @OneToMany(mappedBy="user", fetch=FetchType.LAZY)
  private List<Application> applications;

  public User() {
  }

  public User(String name, String username, String email, String password, Role role, User manager, Double actualAnnualLeaveDays, Double budgetedAnnualLeaveDays, Double actualHospitalizationLeaveDays, Double budgetedHospitalizationLeaveDays, MaritalStatus maritalStatus, Integer childrenCount, Double actualChildcareLeaveDays, Double budgetedChildcareLeaveDays, LocalDate dateOfJoin) {
    this.name = name;
    this.username = username;
    this.email = email;
    this.password = password;
    this.role = role;
    this.manager = manager;
    this.actualAnnualLeaveDays = actualAnnualLeaveDays;
    this.budgetedAnnualLeaveDays = budgetedAnnualLeaveDays;
    this.actualHospitalizationLeaveDays = actualHospitalizationLeaveDays;
    this.budgetedHospitalizationLeaveDays = budgetedHospitalizationLeaveDays;
    this.maritalStatus = maritalStatus;
    this.childrenCount = childrenCount;
    this.actualChildcareLeaveDays = actualChildcareLeaveDays;
    this.budgetedChildcareLeaveDays = budgetedChildcareLeaveDays;
    this.dateOfJoin = dateOfJoin;
  }

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public List<User> getSubordinates() {
    return subordinates;
  }

  public void setSubordinates(List<User> subordinates) {
    this.subordinates = subordinates;
  }

  public User getManager() {
    return manager;
  }

  public void setManager(User manager) {
    this.manager = manager;
  }

  public Double getActualAnnualLeaveDays() {
    return actualAnnualLeaveDays;
  }

  public void setActualAnnualLeaveDays(Double actualAnnualLeaveDays) {
    this.actualAnnualLeaveDays = actualAnnualLeaveDays;
  }

  public Double getBudgetedAnnualLeaveDays() {
    return budgetedAnnualLeaveDays;
  }

  public void setBudgetedAnnualLeaveDays(Double budgetedAnnualLeaveDays) {
    this.budgetedAnnualLeaveDays = budgetedAnnualLeaveDays;
  }

  public Double getActualHospitalizationLeaveDays() {
    return actualHospitalizationLeaveDays;
  }

  public void setActualHospitalizationLeaveDays(Double actualHospitalizationLeaveDays) {
    this.actualHospitalizationLeaveDays = actualHospitalizationLeaveDays;
  }

  public Double getBudgetedHospitalizationLeaveDays() {
    return budgetedHospitalizationLeaveDays;
  }

  public void setBudgetedHospitalizationLeaveDays(Double budgetedHospitalizationLeaveDays) {
    this.budgetedHospitalizationLeaveDays = budgetedHospitalizationLeaveDays;
  }

  public MaritalStatus getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(MaritalStatus maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public Integer getChildrenCount() {
    return childrenCount;
  }

  public void setChildrenCount(Integer childrenCount) {
    this.childrenCount = childrenCount;
  }

  public Double getActualChildcareLeaveDays() {
    return actualChildcareLeaveDays;
  }

  public void setActualChildcareLeaveDays(Double actualChildcareLeaveDays) {
    this.actualChildcareLeaveDays = actualChildcareLeaveDays;
  }

  public Double getBudgetedChildcareLeaveDays() {
    return budgetedChildcareLeaveDays;
  }

  public void setBudgetedChildcareLeaveDays(Double budgetedChildcareLeaveDays) {
    this.budgetedChildcareLeaveDays = budgetedChildcareLeaveDays;
  }

  public LocalDate getDateOfJoin() {
    return dateOfJoin;
  }

  public void setDateOfJoin(LocalDate dateOfJoin) {
    this.dateOfJoin = dateOfJoin;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
    User other = (User) obj;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    return true;
  }

  public List<Application> getApplications() {
    return applications;
  }

  public void setApplications(List<Application> applications) {
    this.applications = applications;
  }  
}

package com.example.integratedmanagementsystem.model;

import java.util.ArrayList;
import java.util.List;

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

  public User() {
  }

  public User(String name, String username, String email, String password, Role role, User manager) {
    this.name = name;
    this.username = username;
    this.email = email;
    this.password = password;
    this.role = role;
    this.manager = manager;
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
}

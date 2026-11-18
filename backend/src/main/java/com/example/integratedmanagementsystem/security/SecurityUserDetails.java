package com.example.integratedmanagementsystem.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.integratedmanagementsystem.model.User;

public class SecurityUserDetails implements UserDetails {
  private final User user;
  public SecurityUserDetails(User user) {
    this.user = user;
  }

  // Inherited Abstract Methods from UserDetails
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // converting roles into SimpleGrantedAuthority object
    return List.of(new SimpleGrantedAuthority(user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }
  
  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public User getUser() {
    return this.user;
  }
}

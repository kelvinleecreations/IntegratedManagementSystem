package com.example.integratedmanagementsystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.example.integratedmanagementsystem.model.Role;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
  @Bean
  @Order(1)
  public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
    return http
      // focus URL starting with /admin, and route it to this filter chain
      .securityMatcher("/admin/**")
      .authorizeHttpRequests(auth -> auth
        // allow access to admin page and styles
        .requestMatchers("/admin/login", "/style.css", "/error").permitAll()

        // only users with ROLE_ADMIN can access administrative features
        .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
      )
      .formLogin(form -> form
        .loginPage("/admin/login")
        .loginProcessingUrl("/admin/login/index")
        .successHandler((request, response, authentication) -> {
          SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
    
          // Only allow Admin
          if (userDetails.getUser().getRole() == Role.ROLE_ADMIN) {
            response.sendRedirect(request.getContextPath() + "/admin/index");
          }

          // Scenario when CEO, HR and Employee try to log in via Admin Page 
          else {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(request.getContextPath() + "/?staffDenied");
          }
        })
      )
      .logout(logout -> logout
        .logoutUrl("/admin/logout")
        .logoutSuccessUrl("/welcome?logout")
      )
      .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain staffFilterChain(HttpSecurity http) throws Exception {
    return http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/about", "/staff/login", "/style.css", "/error").permitAll()
        .requestMatchers("/staff/**").hasAnyRole("CEO", "HR_DIRECTOR", "FINANCE_DIRECTOR", "HR_MANAGER_R", "HR_MANAGER_I", "HR_RECRUITER", "HR_INTERNAL", "FINANCE_MANAGER", "FINANCE_EMPLOYEE")
        .anyRequest().authenticated() // as long as the user is logged in, whether it is get or post request, allow
      )
      .formLogin(form -> form
        .loginPage("/staff/login")
        .loginProcessingUrl("/staff/login/index")
        .successHandler((request, response, authentication) -> {
          SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();

          // Does not allow Admin to log in via Staff Page
          if (userDetails.getUser().getRole() == Role.ROLE_ADMIN) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(request.getContextPath() + "/?adminDenied");
            return;
          }
          // If the user is not an Admin,
          response.sendRedirect(request.getContextPath() + "/staff/index");
        })
        .failureUrl("/staff/login?error")
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/?logout")
      )
      .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

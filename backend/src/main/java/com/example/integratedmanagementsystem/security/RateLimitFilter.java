package com.example.integratedmanagementsystem.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.integratedmanagementsystem.service.RateLimitingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final RateLimitingService rateLimitingService;

  public RateLimitFilter (RateLimitingService rateLimitingService) {
    this.rateLimitingService = rateLimitingService;
  }

  // inherited abstract method from OncePerRequestFilter
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    // get url path without domain name and query param
    String urlPath = request.getRequestURI();

    // Limit Login attempts
    // before Spring Security checks, this if block will get the request first, and calls the service to count and track number of attempts
    // this if block will only target login POST request
    if ("POST".equalsIgnoreCase(request.getMethod()) && ("/admin/login/index".equals(urlPath) || "/staff/login/index".equals(urlPath))) {

      String ipAddress = request.getRemoteAddr();
      Integer maxRequests = 5;
      Long cdDuration = (Long) (long) 60;

      // if rate limit exceeded,
      if (!rateLimitingService.hasNotExceeded("[Login] " + ipAddress, maxRequests, cdDuration)) {

        // redirect to login page with message, will not be passed to Spring Security
        response.sendRedirect(request.getContextPath() + "/?rateLimitExceeded");

        return;
      }
    }

    // if request is within the rate limit,
    // pass the request to Spring Security (UsernamePasswordAuthenticationFilter) to check on login credentials
    // if credential is incorrect, Spring Security rejects it
    // if credential is correct, the user is authenticated
    filterChain.doFilter(request, response);
  }
}

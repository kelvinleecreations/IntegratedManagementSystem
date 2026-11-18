package com.example.integratedmanagementsystem.exception;

import java.util.Map;

public class SvcValidationException extends RuntimeException {
  private final Map<String, String> errors;
  private final String errorCode;

  public SvcValidationException(Map<String, String> errors, String errorCode, String message) {
    super(message);
    this.errors = errors;
    this.errorCode = errorCode;
  }

  public Map<String, String> getErrors() {
    return errors;
  }

  public String getErrorCode() {
    return errorCode;
  }

}

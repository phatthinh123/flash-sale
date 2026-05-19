package com.flashsale.auth.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
        String email,
    @Pattern(regexp = "^$|^\\d{10,15}$", message = "Phone number must be 10-15 digits")
        String phone,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password) {
  @AssertTrue(message = "Either email or phone must be provided")
  public boolean isEitherEmailOrPhoneProvided() {
    return (email != null && !email.isBlank()) || (phone != null && !phone.isBlank());
  }
}
